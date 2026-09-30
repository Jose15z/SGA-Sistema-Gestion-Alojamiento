package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pruebas unitarias del objeto de valor Tarifa.
 *
 * <p>Verifican igualdad por sus componentes, normalización
 * de la temporada y rechazo de datos inválidos.</p>
 *
 * <p>No requieren Spring, repositorios ni base de datos.</p>
 */
class TarifaTest {

    private static final IdentificacionApartamento APARTAMENTO =
            new IdentificacionApartamento("APT-301");

    @Test
    void tarifasConLosMismosValoresDebenSerIguales() {
        Tarifa primera = new Tarifa(
                APARTAMENTO,
                "Normal",
                Dinero.de(100_000)
        );

        Tarifa segunda = new Tarifa(
                new IdentificacionApartamento("APT-301"),
                "Normal",
                Dinero.de(100_000)
        );

        assertEquals(primera, segunda);
        assertEquals(primera.hashCode(), segunda.hashCode());
    }

    @Test
    void tarifasDeApartamentosDistintosNoDebenSerIguales() {
        Tarifa primera = new Tarifa(
                APARTAMENTO,
                "Normal",
                Dinero.de(100_000)
        );

        Tarifa segunda = new Tarifa(
                new IdentificacionApartamento("APT-302"),
                "Normal",
                Dinero.de(100_000)
        );

        assertNotEquals(primera, segunda);
    }

    @Test
    void tarifasDeTemporadasDistintasNoDebenSerIguales() {
        Tarifa normal = new Tarifa(
                APARTAMENTO,
                "Normal",
                Dinero.de(100_000)
        );

        Tarifa especial = new Tarifa(
                APARTAMENTO,
                "Especial",
                Dinero.de(100_000)
        );

        assertNotEquals(normal, especial);
    }

    @Test
    void tarifasConImportesDistintosNoDebenSerIguales() {
        Tarifa primera = new Tarifa(
                APARTAMENTO,
                "Normal",
                Dinero.de(100_000)
        );

        Tarifa segunda = new Tarifa(
                APARTAMENTO,
                "Normal",
                Dinero.de(150_000)
        );

        assertNotEquals(primera, segunda);
    }

    @Test
    void debeNormalizarLosEspaciosDeLaTemporada() {
        Tarifa conEspacios = new Tarifa(
                APARTAMENTO,
                "  Especial  ",
                Dinero.de(100_000)
        );

        Tarifa sinEspacios = new Tarifa(
                APARTAMENTO,
                "Especial",
                Dinero.de(100_000)
        );

        assertEquals(sinEspacios, conEspacios);
    }

    @Test
    void debeAceptarUnImporteDeCero() {
        assertDoesNotThrow(
                () -> new Tarifa(
                        APARTAMENTO,
                        "Normal",
                        Dinero.CERO
                )
        );
    }

    @Test
    void debeRechazarUnApartamentoNulo() {
        assertThrows(
                ReglaDominioException.class,
                () -> new Tarifa(
                        null,
                        "Normal",
                        Dinero.de(100_000)
                )
        );
    }

    @Test
    void debeRechazarUnaTemporadaNula() {
        assertThrows(
                ReglaDominioException.class,
                () -> new Tarifa(
                        APARTAMENTO,
                        null,
                        Dinero.de(100_000)
                )
        );
    }

    @Test
    void debeRechazarUnaTemporadaVacia() {
        assertThrows(
                ReglaDominioException.class,
                () -> new Tarifa(
                        APARTAMENTO,
                        "",
                        Dinero.de(100_000)
                )
        );
    }

    @Test
    void debeRechazarUnaTemporadaCompuestaSoloPorEspacios() {
        assertThrows(
                ReglaDominioException.class,
                () -> new Tarifa(
                        APARTAMENTO,
                        "   ",
                        Dinero.de(100_000)
                )
        );
    }

    @Test
    void debeRechazarUnImporteNulo() {
        assertThrows(
                ReglaDominioException.class,
                () -> new Tarifa(
                        APARTAMENTO,
                        "Normal",
                        null
                )
        );
    }

    @Test
    void debeRechazarUnImporteNegativo() {
        assertThrows(
                ReglaDominioException.class,
                () -> new Tarifa(
                        APARTAMENTO,
                        "Normal",
                        Dinero.de(-1)
                )
        );
    }
}