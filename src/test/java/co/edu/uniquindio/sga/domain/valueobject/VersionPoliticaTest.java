package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pruebas unitarias de la referencia a una versión
 * de la política de cancelación.
 *
 * <p>Verifican igualdad por valor y la restricción
 * de que el número de versión sea positivo.</p>
 *
 * <p>No verifican la existencia de la política referenciada
 * ni calculan retenciones por cancelación o no-show.</p>
 */
class VersionPoliticaTest {

    @Test
    void versionesConElMismoNumeroDebenSerIguales() {
        VersionPolitica primera = new VersionPolitica(2);
        VersionPolitica segunda = new VersionPolitica(2);

        assertEquals(primera, segunda);
        assertEquals(primera.hashCode(), segunda.hashCode());
    }

    @Test
    void versionesConNumerosDistintosNoDebenSerIguales() {
        VersionPolitica primera = new VersionPolitica(1);
        VersionPolitica segunda = new VersionPolitica(2);

        assertNotEquals(primera, segunda);
    }

    @Test
    void debeAceptarLaVersionUnoComoLimiteMinimo() {
        assertDoesNotThrow(() -> new VersionPolitica(1));
    }

    @Test
    void debeRechazarLaVersionCero() {
        assertThrows(
                ReglaDominioException.class,
                () -> new VersionPolitica(0)
        );
    }

    @Test
    void debeRechazarUnaVersionNegativa() {
        assertThrows(
                ReglaDominioException.class,
                () -> new VersionPolitica(-1)
        );
    }
}