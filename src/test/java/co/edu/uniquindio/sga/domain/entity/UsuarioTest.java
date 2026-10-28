package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.CorreoElectronico;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.Rol;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioTest {

    private Usuario huesped() {
        return Usuario.registrarHuesped(UUID.randomUUID(), new CorreoElectronico("ana@correo.com"),
                new DocumentoIdentidad("1094"), "Ana", "$2a$10$hash");
    }

    @Test
    void elRegistroPublicoSiempreCreaUnHuespedActivo() {
        Usuario usuario = huesped();

        assertEquals(Rol.HUESPED, usuario.getRol());
        assertTrue(usuario.puedeIniciarSesion());
    }

    @Test
    void unUsuarioDesactivadoNoIniciaSesion() {
        Usuario usuario = huesped();
        usuario.desactivar();

        assertFalse(usuario.puedeIniciarSesion());
        assertThrows(ReglaDominioException.class, usuario::desactivar);
    }

    @Test
    void requiereContrasenaCifrada() {
        assertThrows(ReglaDominioException.class, () -> Usuario.registrarHuesped(UUID.randomUUID(),
                new CorreoElectronico("a@b.co"), new DocumentoIdentidad("1"), "A", " "));
    }

    @Test
    void laIgualdadEsPorIdentidad() {
        UUID id = UUID.randomUUID();
        Usuario a = Usuario.reconstruir(id, new CorreoElectronico("a@b.co"), new DocumentoIdentidad("1"), "A",
                Rol.ADMINISTRADOR, "h", true);
        Usuario b = Usuario.reconstruir(id, new CorreoElectronico("c@d.co"), new DocumentoIdentidad("2"), "B",
                Rol.HUESPED, "h", false);
        assertEquals(a, b);
    }
}
