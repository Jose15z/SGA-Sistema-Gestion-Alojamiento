package co.edu.uniquindio.sga.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del requisito de identificación externa
 * según el canal de origen de una reserva.
 *
 * <p>No verifican conflictos, unicidad del identificador
 * externo ni idempotencia de la importación.</p>
 */
class CanalOrigenTest {

    @Test
    void canalExternoDebeExigirIdentificadorExterno() {
        assertTrue(CanalOrigen.EXTERNO.exigeIdentificadorExterno());
    }

    @Test
    void canalPortalNoDebeExigirIdentificadorExterno() {
        assertFalse(CanalOrigen.PORTAL.exigeIdentificadorExterno());
    }

    @Test
    void canalDirectoNoDebeExigirIdentificadorExterno() {
        assertFalse(CanalOrigen.DIRECTO.exigeIdentificadorExterno());
    }
}