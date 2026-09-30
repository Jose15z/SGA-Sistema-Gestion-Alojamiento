package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pruebas unitarias del desglose de alojamiento por noche.
 *
 * <p>Comprueban igualdad por valor, validaciones de construcción
 * y cálculo del subtotal relacionado con RN-05.</p>
 *
 * <p>Utilizan fechas fijas y no requieren Spring,
 * repositorios ni una base de datos.</p>
 */
class CargoNocheTest {

    private static final LocalDate NOCHE =
            LocalDate.of(2026, 12, 10);

    @Test
    void cargosConLosMismosValoresDebenSerIguales() {
        CargoNoche primero = new CargoNoche(
                NOCHE, "Especial", Dinero.de(100_000), 3
        );

        CargoNoche segundo = new CargoNoche(
                NOCHE, "Especial", Dinero.de(100_000), 3
        );

        assertEquals(primero, segundo);
        assertEquals(primero.hashCode(), segundo.hashCode());
    }

    @Test
    void cargosDeNochesDistintasNoDebenSerIguales() {
        CargoNoche primero = new CargoNoche(
                NOCHE, "Especial", Dinero.de(100_000), 3
        );

        CargoNoche segundo = new CargoNoche(
                NOCHE.plusDays(1), "Especial", Dinero.de(100_000), 3
        );

        assertNotEquals(primero, segundo);
    }

    @Test
    void debeNormalizarLosEspaciosDeLaTemporada() {
        CargoNoche conEspacios = new CargoNoche(
                NOCHE, "  Especial  ", Dinero.de(100_000), 3
        );

        CargoNoche sinEspacios = new CargoNoche(
                NOCHE, "Especial", Dinero.de(100_000), 3
        );

        assertEquals(sinEspacios, conEspacios);
    }

    @Test
    void debeMultiplicarLaTarifaPorLosOcupantesFacturables() {
        CargoNoche cargo = new CargoNoche(
                NOCHE, "Especial", Dinero.de(100_000), 3
        );

        Dinero subtotal = cargo.subtotal();

        assertEquals(Dinero.de(300_000), subtotal);
        assertEquals(Dinero.de(100_000), cargo.tarifaPorOcupante());
    }

    @Test
    void debePermitirCeroOcupantesFacturablesEnLaLinea() {
        CargoNoche cargo = new CargoNoche(
                NOCHE, "Especial", Dinero.de(100_000), 0
        );

        Dinero subtotal = cargo.subtotal();

        assertEquals(Dinero.CERO, subtotal);
    }

    @Test
    void debePermitirUnaTarifaDeCero() {
        CargoNoche cargo = new CargoNoche(
                NOCHE, "Especial", Dinero.CERO, 3
        );

        Dinero subtotal = cargo.subtotal();

        assertEquals(Dinero.CERO, subtotal);
    }

    @Test
    void debeRechazarUnaNocheNula() {
        assertThrows(
                ReglaDominioException.class,
                () -> new CargoNoche(
                        null, "Especial", Dinero.de(100_000), 3
                )
        );
    }

    @Test
    void debeRechazarUnaTemporadaNula() {
        assertThrows(
                ReglaDominioException.class,
                () -> new CargoNoche(
                        NOCHE, null, Dinero.de(100_000), 3
                )
        );
    }

    @Test
    void debeRechazarUnaTemporadaVacia() {
        assertThrows(
                ReglaDominioException.class,
                () -> new CargoNoche(
                        NOCHE, "", Dinero.de(100_000), 3
                )
        );
    }

    @Test
    void debeRechazarUnaTemporadaCompuestaSoloPorEspacios() {
        assertThrows(
                ReglaDominioException.class,
                () -> new CargoNoche(
                        NOCHE, "   ", Dinero.de(100_000), 3
                )
        );
    }

    @Test
    void debeRechazarUnaTarifaNula() {
        assertThrows(
                ReglaDominioException.class,
                () -> new CargoNoche(
                        NOCHE, "Especial", null, 3
                )
        );
    }

    @Test
    void debeRechazarUnaTarifaNegativa() {
        assertThrows(
                ReglaDominioException.class,
                () -> new CargoNoche(
                        NOCHE, "Especial", Dinero.de(-1), 3
                )
        );
    }

    @Test
    void debeRechazarUnaCantidadNegativaDeOcupantesFacturables() {
        assertThrows(
                ReglaDominioException.class,
                () -> new CargoNoche(
                        NOCHE, "Especial", Dinero.de(100_000), -1
                )
        );
    }
}