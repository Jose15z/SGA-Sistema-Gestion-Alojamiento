package co.edu.uniquindio.sga.application.seguridad;

import co.edu.uniquindio.sga.application.exception.SolicitudUsuarioInvalidaException;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioTest {

    private static final LocalDate NACIMIENTO = LocalDate.of(1995, 4, 12);

    private Usuario huesped() {
        return Usuario.registrarHuesped(UUID.randomUUID(), new CorreoElectronico("ana@correo.com"),
                new DocumentoIdentidad("1094"), NACIMIENTO, "Ana", "3001234567", "$2a$10$hash");
    }

    @Test
    void elRegistroPublicoSiempreCreaUnHuespedActivo() {
        Usuario usuario = huesped();

        assertEquals(Rol.HUESPED, usuario.getRol());
        assertTrue(usuario.puedeIniciarSesion());
        assertEquals("3001234567", usuario.getTelefono());
        assertEquals(NACIMIENTO, usuario.getFechaNacimiento());
    }

    @Test
    void unUsuarioDesactivadoNoIniciaSesionYNoSeDesactivaDosVeces() {
        Usuario usuario = huesped();
        usuario.desactivar();

        assertFalse(usuario.puedeIniciarSesion());
        assertThrows(Usuario.UsuarioInactivoException.class, usuario::desactivar);
    }

    @Test
    void requiereContrasenaCifradaTelefonoYFechaDeNacimiento() {
        CorreoElectronico correo = new CorreoElectronico("a@b.co");
        DocumentoIdentidad documento = new DocumentoIdentidad("1");
        assertThrows(SolicitudUsuarioInvalidaException.class, () -> Usuario.registrarHuesped(UUID.randomUUID(),
                correo, documento, NACIMIENTO, "A", "300", " "));
        assertThrows(SolicitudUsuarioInvalidaException.class, () -> Usuario.registrarHuesped(UUID.randomUUID(),
                correo, documento, NACIMIENTO, "A", " ", "hash"));
        assertThrows(SolicitudUsuarioInvalidaException.class, () -> Usuario.registrarHuesped(UUID.randomUUID(),
                correo, documento, null, "A", "300", "hash"));
    }

    @Test
    void correoSeNormalizaYValidaSuFormato() {
        assertEquals(new CorreoElectronico("ana@correo.com"), new CorreoElectronico(" Ana@Correo.COM "));
        assertThrows(SolicitudUsuarioInvalidaException.class, () -> new CorreoElectronico("sin-arroba"));
    }

    @Test
    void elPerfilCambiaNombreYTelefonoPeroNoElCorreo() {
        Usuario usuario = huesped();
        usuario.actualizarPerfil("Ana María", "3109998877");

        assertEquals("Ana María", usuario.getNombreCompleto());
        assertEquals("3109998877", usuario.getTelefono());
        assertEquals(new CorreoElectronico("ana@correo.com"), usuario.getCorreo());
    }

    @Test
    void elAdministradorAsignaRolesInternos() {
        Usuario usuario = huesped();
        usuario.asignarRol(Rol.PERSONAL_SERVICIO);

        assertEquals(Rol.PERSONAL_SERVICIO, usuario.getRol());
        assertFalse(usuario.getRol().gestionaReservas());
    }

    @Test
    void laIgualdadEsPorIdentidad() {
        UUID id = UUID.randomUUID();
        Usuario a = Usuario.reconstruir(id, new CorreoElectronico("a@b.co"), new DocumentoIdentidad("1"), NACIMIENTO,
                "A", "300", Rol.ADMINISTRADOR, "h", true);
        Usuario b = Usuario.reconstruir(id, new CorreoElectronico("c@d.co"), new DocumentoIdentidad("2"), NACIMIENTO,
                "B", "301", Rol.HUESPED, "h", false);
        assertEquals(a, b);
    }

    @ParameterizedTest
    @ValueSource(strings = {"corta1A", "sinnumerosAA", "sinmayusculas1", ""})
    void laContrasenaDebeSerSegura(String contrasena) {
        assertThrows(SolicitudUsuarioInvalidaException.class, () -> PoliticaContrasena.verificar(contrasena));
    }

    @Test
    void aceptaUnaContrasenaSegura() {
        assertDoesNotThrow(() -> PoliticaContrasena.verificar("Huesped123*"));
    }

    @Test
    void elCodigoDeRecuperacionVenceYSeUsaUnaSolaVez() {
        LocalDateTime emision = LocalDateTime.of(2026, 5, 1, 10, 0);
        CodigoRecuperacion codigo = new CodigoRecuperacion(UUID.randomUUID(), new CorreoElectronico("a@b.co"),
                "cifrado", emision.plusMinutes(15), false);

        assertTrue(codigo.estaVigente(emision.plusMinutes(15)));
        assertFalse(codigo.estaVigente(emision.plusMinutes(16)));
        codigo.marcarUsado();
        assertFalse(codigo.estaVigente(emision));
    }
}
