package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pruebas unitarias del umbral de edad facturable.
 *
 * <p>Verifican igualdad por valor y los límites admitidos
 * por el objeto de valor actual: de cero a treinta años.</p>
 *
 * <p>No modifican la configuración del alojamiento ni
 * sustituyen las pruebas de edad a la entrada de Ocupante.</p>
 */
class UmbralEdadFacturableTest {

    @Test
    void umbralesConLaMismaEdadDebenSerIguales() {
        UmbralEdadFacturable primero = new UmbralEdadFacturable(10);
        UmbralEdadFacturable segundo = new UmbralEdadFacturable(10);

        assertEquals(primero, segundo);
        assertEquals(primero.hashCode(), segundo.hashCode());
    }

    @Test
    void umbralesConEdadesDistintasNoDebenSerIguales() {
        UmbralEdadFacturable primero = new UmbralEdadFacturable(10);
        UmbralEdadFacturable segundo = new UmbralEdadFacturable(11);

        assertNotEquals(primero, segundo);
    }

    @Test
    void debeAceptarCeroComoLimiteInferior() {
        assertDoesNotThrow(() -> new UmbralEdadFacturable(0));
    }

    @Test
    void debeAceptarTreintaComoLimiteSuperior() {
        assertDoesNotThrow(() -> new UmbralEdadFacturable(30));
    }

    @Test
    void debeRechazarUnaEdadMenorQueCero() {
        assertThrows(
                ReglaDominioException.class,
                () -> new UmbralEdadFacturable(-1)
        );
    }

    @Test
    void debeRechazarUnaEdadMayorQueTreinta() {
        assertThrows(
                ReglaDominioException.class,
                () -> new UmbralEdadFacturable(31)
        );
    }
}