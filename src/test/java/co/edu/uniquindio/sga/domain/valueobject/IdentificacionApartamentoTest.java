package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pruebas unitarias de la identificación de un apartamento.
 *
 * <p>Verifican normalización, igualdad por valor
 * y rechazo de identificaciones nulas o vacías.</p>
 *
 * <p>No verifican la unicidad entre apartamentos persistidos.</p>
 */
class IdentificacionApartamentoTest {

    @Test
    void debeNormalizarLaIdentificacionAMayusculas() {
        IdentificacionApartamento identificacion =
                new IdentificacionApartamento("apt-301");

        assertEquals("APT-301", identificacion.valor());
    }

    @Test
    void debeEliminarLosEspaciosExteriores() {
        IdentificacionApartamento identificacion =
                new IdentificacionApartamento("  APT-301  ");

        assertEquals("APT-301", identificacion.valor());
    }

    @Test
    void identificacionesConElMismoValorNormalizadoDebenSerIguales() {
        IdentificacionApartamento primera =
                new IdentificacionApartamento("  apt-301  ");

        IdentificacionApartamento segunda =
                new IdentificacionApartamento("APT-301");

        assertEquals(primera, segunda);
        assertEquals(primera.hashCode(), segunda.hashCode());
    }

    @Test
    void identificacionesConValoresDistintosNoDebenSerIguales() {
        IdentificacionApartamento primera =
                new IdentificacionApartamento("APT-301");

        IdentificacionApartamento segunda =
                new IdentificacionApartamento("APT-302");

        assertNotEquals(primera, segunda);
    }

    @Test
    void debeRechazarUnaIdentificacionNula() {
        assertThrows(
                ReglaDominioException.class,
                () -> new IdentificacionApartamento(null)
        );
    }

    @Test
    void debeRechazarUnaIdentificacionVacia() {
        assertThrows(
                ReglaDominioException.class,
                () -> new IdentificacionApartamento("")
        );
    }

    @Test
    void debeRechazarUnaIdentificacionCompuestaSoloPorEspacios() {
        assertThrows(
                ReglaDominioException.class,
                () -> new IdentificacionApartamento("   ")
        );
    }
}