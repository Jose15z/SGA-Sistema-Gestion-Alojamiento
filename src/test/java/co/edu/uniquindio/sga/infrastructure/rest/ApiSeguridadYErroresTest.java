package co.edu.uniquindio.sga.infrastructure.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Recorrido HTTP real (seguridad, controller, caso de uso, dominio y H2 con data.sql):
 * cada error llega al cliente con su código y el ErrorResponse único.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ApiSeguridadYErroresTest {

    @Autowired
    private MockMvc mockMvc;

    private String token(String correo, String contrasena) throws Exception {
        String cuerpo = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"" + correo + "\",\"contrasena\":\"" + contrasena + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonMapper.builder().build().readTree(cuerpo).get("token").asString();
    }

    @Test
    void sinTokenResponde401ConErrorResponse() throws Exception {
        mockMvc.perform(get("/api/reservas"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("NO_AUTENTICADO"));
    }

    @Test
    void credencialesIncorrectasResponden401() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"admin@sieteraices.co\",\"contrasena\":\"mala\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("CREDENCIALES_INVALIDAS"));
    }

    @Test
    void rolInsuficienteResponde403() throws Exception {
        mockMvc.perform(put("/api/reservas/RES-2025-00001/confirmar")
                        .header("Authorization", token("ana.ramirez@correo.com", "Huesped123*")))
                .andExpect(status().isForbidden());
    }

    @Test
    void otroHuespedResponde403PorPropietario() throws Exception {
        mockMvc.perform(get("/api/reservas/RES-2025-00001")
                        .header("Authorization", token("carlos.gomez@correo.com", "Huesped123*")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));
        mockMvc.perform(get("/api/reservas/RES-2025-00001")
                        .header("Authorization", token("ana.ramirez@correo.com", "Huesped123*")))
                .andExpect(status().isOk());
    }

    @Test
    void reglaDelDominioResponde409() throws Exception {
        // RN-08: una reserva PENDIENTE no puede declararse no-show
        mockMvc.perform(put("/api/reservas/RES-2025-00001/no-show")
                        .header("Authorization", token("recepcion@sieteraices.co", "Recepcion123*")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("REGLA_NEGOCIO_VIOLADA"));
    }

    @Test
    void formaInvalidaResponde400YRecursoInexistente404() throws Exception {
        String recepcion = token("recepcion@sieteraices.co", "Recepcion123*");
        mockMvc.perform(post("/api/reservas").header("Authorization", recepcion)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles").isNotEmpty());
        mockMvc.perform(get("/api/reservas/RES-2026-99999").header("Authorization", recepcion))
                .andExpect(status().isNotFound());
    }

    @Test
    void lasFechasGuardadasSeLeenIguales() throws Exception {
        // Regresión: con la zona de H2 distinta de la de la JVM las fechas se corrían un día
        String admin = token("admin@sieteraices.co", "Admin123*");
        mockMvc.perform(post("/api/apartamentos/APT-102/bloqueos").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fechaEntrada\":\"2031-02-10\",\"fechaSalida\":\"2031-02-12\",\"motivo\":\"Prueba\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/api/apartamentos/APT-102/bloqueos").header("Authorization", admin))
                .andExpect(jsonPath("$.contenido[?(@.motivo == 'Prueba')].fechaEntrada").value("2031-02-10"));
    }

    @Test
    void elPersonalDeServicioPreparaPeroNoGestionaReservas() throws Exception {
        String servicio = token("servicio@sieteraices.co", "Servicio123*");
        mockMvc.perform(get("/api/reservas").header("Authorization", servicio))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/apartamentos?estadoOperativo=FUERA_DE_SERVICIO").header("Authorization", servicio))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido[0].identificacion").value("APT-202"));
    }

    @Test
    void soloElAdministradorAutorizaCerrarUnFolioConSaldo() throws Exception {
        mockMvc.perform(put("/api/folios/RES-2025-00003/cerrar").header("Authorization",
                                token("recepcion@sieteraices.co", "Recepcion123*"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"motivo\":\"Saldo pendiente\"}"))
                .andExpect(status().isForbidden());
    }
}
