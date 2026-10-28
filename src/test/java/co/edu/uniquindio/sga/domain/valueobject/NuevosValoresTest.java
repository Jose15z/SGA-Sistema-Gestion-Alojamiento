package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Objetos de valor incorporados en la Entrega 2. */
class NuevosValoresTest {

    @Test
    void vehiculoNormalizaLaPlaca() {
        assertEquals(new Vehiculo("ABC123"), new Vehiculo(" abc-123 "));
        assertThrows(ReglaDominioException.class, () -> new Vehiculo("A1"));
        assertThrows(ReglaDominioException.class, () -> new Vehiculo(null));
    }

    @Test
    void correoSeNormalizaYValidaSuFormato() {
        assertEquals(new CorreoElectronico("ana@correo.com"), new CorreoElectronico(" Ana@Correo.COM "));
        assertThrows(ReglaDominioException.class, () -> new CorreoElectronico("sin-arroba"));
    }

    @Test
    void identificadorExternoNoPuedeEstarVacio() {
        assertEquals("BK-1", new IdentificadorExterno(" BK-1 ").valor());
        assertThrows(ReglaDominioException.class, () -> new IdentificadorExterno("   "));
    }

    @Test
    void capacidadParqueaderoAdmiteHastaSusCupos() {
        CapacidadParqueadero capacidad = new CapacidadParqueadero(2);
        assertTrue(capacidad.admite(2));
        assertFalse(capacidad.admite(3));
        assertThrows(ReglaDominioException.class, () -> new CapacidadParqueadero(-1));
    }

    @Test
    void canalSegunElRolQueRegistra() {
        assertTrue(CanalOrigen.PORTAL.loRegistra(Rol.HUESPED));
        assertFalse(CanalOrigen.PORTAL.loRegistra(Rol.RECEPCIONISTA));
        assertTrue(CanalOrigen.DIRECTO.loRegistra(Rol.ADMINISTRADOR));
        assertFalse(CanalOrigen.EXTERNO.loRegistra(Rol.HUESPED));
    }

    @Test
    void versionSiguiente() {
        assertEquals(new VersionPolitica(2), new VersionPolitica(1).siguiente());
    }
}
