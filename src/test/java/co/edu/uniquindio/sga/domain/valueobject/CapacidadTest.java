package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias del límite de ocupantes de un apartamento.
 *
 * <p>Verifican igualdad por valor, capacidad positiva
 * y límites de admisión relacionados con RN-02.</p>
 *
 * <p>La capacidad considera a todos los ocupantes,
 * independientemente de su condición de facturables.</p>
 */
class CapacidadTest {

    @Test
    void capacidadesConElMismoValorDebenSerIguales() {
        Capacidad primera = new Capacidad(4);
        Capacidad segunda = new Capacidad(4);

        assertEquals(primera, segunda);
        assertEquals(primera.hashCode(), segunda.hashCode());
    }

    @Test
    void capacidadesConValoresDistintosNoDebenSerIguales() {
        Capacidad primera = new Capacidad(4);
        Capacidad segunda = new Capacidad(5);

        assertNotEquals(primera, segunda);
    }

    @Test
    void debePermitirUnaCapacidadMinimaDeUno() {
        Capacidad capacidad = new Capacidad(1);

        assertTrue(capacidad.admite(1));
        assertFalse(capacidad.admite(2));
    }

    @Test
    void debeAdmitirUnGrupoMenorQueLaCapacidad() {
        Capacidad capacidad = new Capacidad(4);

        assertTrue(capacidad.admite(3));
    }

    @Test
    void debeAdmitirUnGrupoQueAlcanzaLaCapacidadExacta() {
        Capacidad capacidad = new Capacidad(4);

        assertTrue(capacidad.admite(4));
    }

    @Test
    void noDebeAdmitirUnGrupoQueSuperaLaCapacidadEnUnaPersona() {
        Capacidad capacidad = new Capacidad(4);

        assertFalse(capacidad.admite(5));
    }

    @Test
    void noDebeAdmitirUnGrupoVacio() {
        Capacidad capacidad = new Capacidad(4);

        assertFalse(capacidad.admite(0));
    }

    @Test
    void noDebeAdmitirUnaCantidadNegativaDeOcupantes() {
        Capacidad capacidad = new Capacidad(4);

        assertFalse(capacidad.admite(-1));
    }

    @Test
    void debeRechazarUnaCapacidadDeCero() {
        assertThrows(
                ReglaDominioException.class,
                () -> new Capacidad(0)
        );
    }

    @Test
    void debeRechazarUnaCapacidadNegativa() {
        assertThrows(
                ReglaDominioException.class,
                () -> new Capacidad(-1)
        );
    }
}