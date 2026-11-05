package co.edu.uniquindio.sga.infrastructure.rest;

import co.edu.uniquindio.sga.infrastructure.config.setup.DefaultUserInitializer;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.UsuarioJpaDataRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;
import java.time.Instant;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Peticiones con JWT reales: no se simula la autenticación ni el decodificador. */
@SpringBootTest
@AutoConfigureMockMvc
class Guia12SeguridadTest {
    @Autowired MockMvc mvc;
    @Autowired JwtEncoder encoder;
    @Autowired JwtDecoder decoder;
    @Autowired UsuarioJpaDataRepository usuarios;
    @Autowired co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.ApartamentoJpaDataRepository apartamentos;
    @Autowired PasswordEncoder passwords;
    @Autowired DefaultUserInitializer inicializador;
    @Value("${sga.jwt.emisor}") String emisor;

    String token(String correo, String clave) throws Exception {
        var respuesta = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"correo\":\"" + correo + "\",\"contrasena\":\"" + clave + "\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.roles").isArray()).andReturn().getResponse().getContentAsString();
        return JsonMapper.builder().build().readTree(respuesta).get("token").asString();
    }
    String recepcion() throws Exception { return "Bearer " + token("recepcion@sieteraices.co", "Recepcion123*"); }
    String huesped() throws Exception { return "Bearer " + token("ana.ramirez@correo.com", "Huesped123*"); }

    @Test void usuariosInicialesUsanBcryptYElInicializadorEsIdempotente() {
        var antes = usuarios.findAll();
        assertEquals(5, antes.size());
        assertEquals(4, antes.stream().map(u -> u.getRol()).distinct().count());
        for (var usuario : antes) {
            assertTrue(usuario.getContrasenaCifrada().startsWith("{bcrypt}"));
        }
        String hash = usuarios.findByCorreo("admin@sieteraices.co").orElseThrow().getContrasenaCifrada();
        assertTrue(passwords.matches("Admin123*", hash));
        inicializador.run(null);
        assertEquals(5, usuarios.count());
        assertEquals(hash, usuarios.findByCorreo("admin@sieteraices.co").orElseThrow().getContrasenaCifrada());
    }

    @Test void loginEmiteClaimsSinDatosSensiblesYVigenciaDeTreintaMinutos() throws Exception {
        Jwt jwt = decoder.decode(token("recepcion@sieteraices.co", "Recepcion123*"));
        assertEquals(emisor, jwt.getClaimAsString("iss"));
        assertEquals("recepcion@sieteraices.co", jwt.getSubject());
        assertEquals(List.of("RECEPCIONISTA"), jwt.getClaimAsStringList("roles"));
        assertEquals("1000000002", jwt.getClaimAsString("documento"));
        assertEquals(1800, jwt.getExpiresAt().getEpochSecond() - jwt.getIssuedAt().getEpochSecond());
        assertFalse(jwt.hasClaim("contrasena"));
        assertFalse(jwt.hasClaim("fechaNacimiento"));
    }

    @Test void loginNoRevelaSiLaCuentaExiste() throws Exception {
        for (String correo : List.of("recepcion@sieteraices.co", "desconocido@sieteraices.co")) {
            mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"correo\":\"" + correo + "\",\"contrasena\":\"Incorrecta123\"}"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.mensaje").value("Credenciales inválidas"));
        }
    }

    @Test @Transactional void usuarioInactivoNoPuedeIniciarSesion() throws Exception {
        var usuario = usuarios.findByCorreo("recepcion@sieteraices.co").orElseThrow();
        usuario.setActivo(false); usuarios.saveAndFlush(usuario);
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"correo\":\"recepcion@sieteraices.co\",\"contrasena\":\"Recepcion123*\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test void correoInvalidoResponde400() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"correo\":\"sin-formato\",\"contrasena\":\"UnaClave123\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.detalles[0]").value(
                        org.hamcrest.Matchers.containsString("correo")));
    }

    @Test void listadoSoloEsDeRecepcionYAdministracion() throws Exception {
        mvc.perform(get("/api/reservas?estado=PENDIENTE")).andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Bearer")).andExpect(jsonPath("$.estado").value(401));
        mvc.perform(get("/api/reservas?estado=PENDIENTE").header("Authorization", huesped()))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/reservas?estado=PENDIENTE").header("Authorization", recepcion()))
                .andExpect(status().isOk());
    }

    @Test void propietarioYRecursoInexistenteNoSeFiltranAlHuesped() throws Exception {
        mvc.perform(get("/api/reservas/RES-2025-00001").header("Authorization", huesped())).andExpect(status().isOk());
        mvc.perform(get("/api/reservas/RES-2025-00001").header("Authorization", "Bearer " + token(
                "carlos.gomez@correo.com", "Huesped123*"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/reservas/RES-2026-99999").header("Authorization", huesped())).andExpect(status().isForbidden());
        mvc.perform(get("/api/reservas/RES-2026-99999").header("Authorization", recepcion())).andExpect(status().isNotFound());
        mvc.perform(get("/api/folios/RES-2025-00001").header("Authorization", "Bearer " + token(
                "carlos.gomez@correo.com", "Huesped123*"))).andExpect(status().isForbidden());
    }

    String firmado(String issuer, Instant expiracion, String rol) {
        var claims = JwtClaimsSet.builder().issuer(issuer).subject("recepcion@sieteraices.co")
                .issuedAt(Instant.now().minusSeconds(7200)).claim("rol", rol)
                .claim("documento", "1000000002").claim("roles", List.of(rol));
        if (expiracion != null) claims.expiresAt(expiracion);
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims.build())).getTokenValue();
    }

    @Test void rechazaTokensAlteradosVencidosSinExpiracionConEmisorAjenoORolDesconocido() throws Exception {
        String valido = token("recepcion@sieteraices.co", "Recepcion123*");
        String alterado = valido.substring(0, valido.lastIndexOf('.') + 1) + "firma-alterada";
        String none = "eyJhbGciOiJub25lIn0.eyJzdWIiOiJhZG1pbiJ9.";
        for (String invalido : List.of(alterado, none,
                firmado(emisor, Instant.now().minusSeconds(120), "RECEPCIONISTA"),
                firmado("emisor-ajeno", Instant.now().plusSeconds(300), "RECEPCIONISTA"),
                firmado(emisor, null, "RECEPCIONISTA"),
                firmado(emisor, Instant.now().plusSeconds(300), "SUPERUSUARIO"))) {
            mvc.perform(get("/api/reservas").header("Authorization", "Bearer " + invalido))
                    .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.codigo").value("NO_AUTENTICADO"));
        }
    }

    @Test @Transactional void registroPublicoSiempreEsHuespedYCorreoDuplicadoEs409() throws Exception {
        String cuerpo = """
                {"nombreCompleto":"Registro Guía 12","correo":"registro.guia12@example.com",
                 "contrasena":"Segura1234","telefono":"3001234567","documento":"GUIA12-REG",
                 "fechaNacimiento":"1995-01-02","rol":"ADMINISTRADOR"}
                """;
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.rol").value("HUESPED"))
                .andExpect(jsonPath("$.contrasena").doesNotExist());
        assertTrue(usuarios.findByCorreo("registro.guia12@example.com").orElseThrow()
                .getContrasenaCifrada().startsWith("{bcrypt}"));
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo.replace("Segura1234", "debil"))).andExpect(status().isBadRequest());
    }

    @ParameterizedTest @CsvSource({"PORTAL,1094555666", "DIRECTO,1094123456", "EXTERNO,1094123456"})
    void huespedSoloCreaPortalASuNombre(String canal, String documento) throws Exception {
        mvc.perform(post("/api/reservas").header("Authorization", huesped())
                .contentType(MediaType.APPLICATION_JSON).content(reserva(canal, documento)))
                .andExpect(status().isForbidden());
    }

    @Test @Transactional void huespedPuedeCrearYCancelarSuReservaPortal() throws Exception {
        var respuesta = mvc.perform(post("/api/reservas").header("Authorization", huesped())
                .contentType(MediaType.APPLICATION_JSON).content(reserva("PORTAL", "1094123456")))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String codigo = JsonMapper.builder().build().readTree(respuesta).get("codigo").asString();
        mvc.perform(put("/api/reservas/" + codigo + "/cancelar").header("Authorization", huesped())
                .contentType(MediaType.APPLICATION_JSON).content("{\"motivo\":\"Prueba Guía 12\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("CANCELADA"));
    }

    @Test void recepcionNoCreaCanalExterno() throws Exception {
        mvc.perform(post("/api/reservas").header("Authorization", recepcion())
                .contentType(MediaType.APPLICATION_JSON).content(reserva("EXTERNO", "1094123456")))
                .andExpect(status().isForbidden());
    }

    static String reserva(String canal, String documento) {
        return """
                {"identificacionApartamento":"APT-101","fechaEntrada":"2031-06-10","fechaSalida":"2031-06-12",
                 "canalOrigen":"%s","titular":{"documento":"%s","nombreCompleto":"Ana Ramírez",
                 "fechaNacimiento":"1990-05-12"},"acompanantes":[],"mascotas":0}
                """.formatted(canal, documento);
    }

    @ParameterizedTest @CsvSource({
            "POST,/api/temporadas", "PUT,/api/apartamentos/APT-101/tarifas",
            "POST,/api/apartamentos/APT-101/bloqueos", "PUT,/api/alojamiento",
            "PUT,/api/reservas/RES-2025-00001/confirmar", "GET,/api/usuarios"})
    void personalDeServicioNoGestionaReservasNiAdministracion(String metodo, String ruta) throws Exception {
        mvc.perform(request(org.springframework.http.HttpMethod.valueOf(metodo), ruta)
                .header("Authorization", "Bearer " + token("servicio@sieteraices.co", "Servicio123*"))
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test void calendarioOcultaCodigosDeReservaSalvoAlPersonalAutorizado() throws Exception {
        var fecha = java.time.LocalDate.now(java.time.ZoneId.of("America/Bogota"));
        String ruta = "/api/apartamentos/APT-303/calendario?desde=" + fecha.minusDays(1)
                + "&hasta=" + fecha.plusDays(1);
        mvc.perform(get(ruta).header("Authorization", recepcion()))
                .andExpect(status().isOk()).andExpect(jsonPath("$[*].reserva",
                        org.hamcrest.Matchers.hasItem("RES-2025-00006")));
        mvc.perform(get(ruta).header("Authorization", huesped()))
                .andExpect(status().isOk()).andExpect(jsonPath("$[*].reserva",
                        org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.nullValue())));
        mvc.perform(get(ruta)).andExpect(status().isOk()).andExpect(jsonPath("$[*].reserva",
                        org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.nullValue())));
    }

    @Test @Transactional void servicioPuedePrepararPeroHuespedNo() throws Exception {
        var apartamento = apartamentos.findByIdentificacion("APT-101").orElseThrow();
        apartamento.setEstadoOperativo(co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo.PENDIENTE_PREPARACION);
        apartamentos.saveAndFlush(apartamento);
        String ruta = "/api/apartamentos/APT-101/iniciar-preparacion";
        mvc.perform(put(ruta).header("Authorization", huesped())).andExpect(status().isForbidden());
        mvc.perform(put(ruta).header("Authorization", "Bearer " + token("servicio@sieteraices.co", "Servicio123*")))
                .andExpect(status().isOk());
    }
}
