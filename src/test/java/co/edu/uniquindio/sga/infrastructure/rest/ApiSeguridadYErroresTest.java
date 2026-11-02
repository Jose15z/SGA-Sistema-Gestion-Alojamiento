package co.edu.uniquindio.sga.infrastructure.rest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;
import java.util.stream.Stream;
import org.springframework.http.HttpMethod;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;

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

    @Test
    void servicioSinIndicadorDeCargoResponde400EnLugarDeAsumirFalse() throws Exception {
        mockMvc.perform(put("/api/alojamiento").header("Authorization", token("admin@sieteraices.co", "Admin123*"))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"nombre":"Siete Raíces","descripcion":"Prueba de forma",
                                 "ciudad":"Armenia","direccion":"Dirección de prueba",
                                 "latitud":4.53,"longitud":-75.68,"normas":[],
                                 "servicios":[{"codigo":"WIFI","nombre":"Wifi","valor":0,"unidad":"POR_SOLICITUD"}]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"))
                .andExpect(jsonPath("$.detalles", hasItem(containsString("generaCargo"))));
    }

    @Test
    void tramoSinAntelacionResponde400EnLugarDeAsumirCero() throws Exception {
        mockMvc.perform(post("/api/politicas-cancelacion")
                        .header("Authorization", token("admin@sieteraices.co", "Admin123*"))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"tramos":[{"porcentaje":50}],"porcentajeNoShow":50,"horaEntrada":"15:00"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"))
                .andExpect(jsonPath("$.detalles", hasItem(containsString("antelacionMinimaHoras"))));
    }

    @ParameterizedTest(name = "{0} {1}: {3} no admite elementos null")
    @MethodSource("solicitudesConElementoNulo")
    void unElementoNuloEnUnaListaResponde400(String metodo, String ruta, String cuerpo, String campo) throws Exception {
        mockMvc.perform(request(HttpMethod.valueOf(metodo), ruta)
                        .header("Authorization", token("admin@sieteraices.co", "Admin123*"))
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"))
                .andExpect(jsonPath("$.detalles").isNotEmpty());
    }

    private static Stream<Arguments> solicitudesConElementoNulo() {
        return Stream.of(
                Arguments.of("POST", "/api/cotizaciones", """
                        {"identificacionApartamento":"APT-101","fechaEntrada":"2031-02-10",
                         "fechaSalida":"2031-02-12","ocupantes":[null]}
                        """, "ocupantes"),
                Arguments.of("POST", "/api/reservas", """
                        {"identificacionApartamento":"APT-101","fechaEntrada":"2031-02-10",
                         "fechaSalida":"2031-02-12","canalOrigen":"DIRECTO",
                         "titular":{"documento":"CC-TEST","nombreCompleto":"Ana","fechaNacimiento":"1990-01-01"},
                         "acompanantes":[null]}
                        """, "acompanantes"),
                Arguments.of("PUT", "/api/reservas/RES-2025-00001/modificacion",
                        "{\"acompanantes\":[null]}", "acompanantes"),
                Arguments.of("POST", "/api/apartamentos", """
                        {"identificacion":"APT-999","nombre":"Prueba","descripcion":"Prueba",
                         "dormitorios":1,"capacidad":2,"caracteristicas":[],"tarifas":[null]}
                        """, "tarifas"),
                Arguments.of("POST", "/api/politicas-cancelacion", """
                        {"tramos":[null],"porcentajeNoShow":50,"horaEntrada":"15:00"}
                        """, "tramos"),
                Arguments.of("PUT", "/api/alojamiento", """
                        {"nombre":"Siete Raíces","descripcion":"Prueba","ciudad":"Armenia",
                         "direccion":"Dirección de prueba","latitud":4.53,"longitud":-75.68,
                         "normas":[],"servicios":[null]}
                        """, "servicios"));
    }

    @Test
    void disponibilidadConPaginaExtremaRespondeVaciaEnLugarDe500() throws Exception {
        mockMvc.perform(get("/api/apartamentos/disponibles")
                        .param("fechaEntrada", "2031-02-10").param("fechaSalida", "2031-02-12")
                        .param("totalOcupantes", "1").param("page", String.valueOf(Integer.MAX_VALUE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido").isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"tarifas/historial", "novedades"})
    void losHistorialesSonPaginadosYRechazanTamaniosMayoresADiez(String historial) throws Exception {
        String admin = token("admin@sieteraices.co", "Admin123*");
        String ruta = "/api/apartamentos/APT-101/" + historial;
        mockMvc.perform(get(ruta).header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido").isArray())
                .andExpect(jsonPath("$.pagina").value(0))
                .andExpect(jsonPath("$.tamanio").value(10));
        mockMvc.perform(get(ruta).header("Authorization", admin).param("size", "11"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get(ruta).header("Authorization", admin).param("page", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    void historialDeTarifasSeDivideEnPaginasDeDiezSinPerderElOrden() throws Exception {
        String admin = token("admin@sieteraices.co", "Admin123*");
        for (int i = 0; i < 11; i++) {
            mockMvc.perform(put("/api/apartamentos/APT-101/tarifas").header("Authorization", admin)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"temporada\":\"BASE\",\"valorPorOcupanteNoche\":" + (100_000 + i) + "}"))
                    .andExpect(status().isOk());
        }
        mockMvc.perform(get("/api/apartamentos/APT-101/tarifas/historial").header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido.length()").value(10))
                .andExpect(jsonPath("$.totalElementos").value(14))
                .andExpect(jsonPath("$.totalPaginas").value(2))
                .andExpect(jsonPath("$.contenido[0].valorPorOcupanteNoche").value(100_010));
        mockMvc.perform(get("/api/apartamentos/APT-101/tarifas/historial").header("Authorization", admin)
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido.length()").value(4))
                .andExpect(jsonPath("$.pagina").value(1));
    }
}
