package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.valueobject.TipoTemporada;
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
                NOCHE, TipoTemporada.ESPECIAL, Dinero.de(100_000), 3
        );

        CargoNoche segundo = new CargoNoche(
                NOCHE, TipoTemporada.ESPECIAL, Dinero.de(100_000), 3
        );

        assertEquals(primero, segundo);
        assertEquals(primero.hashCode(), segundo.hashCode());
    }

    @Test
    void cargosDeNochesDistintasNoDebenSerIguales() {
        CargoNoche primero = new CargoNoche(
                NOCHE, TipoTemporada.ESPECIAL, Dinero.de(100_000), 3
        );

        CargoNoche segundo = new CargoNoche(
                NOCHE.plusDays(1), TipoTemporada.ESPECIAL, Dinero.de(100_000), 3
        );

        assertNotEquals(primero, segundo);
    }


    @Test
    void debeMultiplicarLaTarifaPorLosOcupantesFacturables() {
        CargoNoche cargo = new CargoNoche(
                NOCHE, TipoTemporada.ESPECIAL, Dinero.de(100_000), 3
        );

        Dinero subtotal = cargo.subtotal();

        assertEquals(Dinero.de(300_000), subtotal);
        assertEquals(Dinero.de(100_000), cargo.tarifaPorOcupante());
    }

    @Test
    void debePermitirCeroOcupantesFacturablesEnLaLinea() {
        CargoNoche cargo = new CargoNoche(
                NOCHE, TipoTemporada.ESPECIAL, Dinero.de(100_000), 0
        );

        Dinero subtotal = cargo.subtotal();

        assertEquals(Dinero.CERO, subtotal);
    }

    @Test
    void debePermitirUnaTarifaDeCero() {
        CargoNoche cargo = new CargoNoche(
                NOCHE, TipoTemporada.ESPECIAL, Dinero.CERO, 3
        );

        Dinero subtotal = cargo.subtotal();

        assertEquals(Dinero.CERO, subtotal);
    }

    @Test
    void debeRechazarUnaNocheNula() {
        assertThrows(
                ReglaDominioException.class,
                () -> new CargoNoche(
                        null, TipoTemporada.ESPECIAL, Dinero.de(100_000), 3
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
    void debeRechazarUnaTarifaNula() {
        assertThrows(
                ReglaDominioException.class,
                () -> new CargoNoche(
                        NOCHE, TipoTemporada.ESPECIAL, null, 3
                )
        );
    }

    @Test
    void debeRechazarUnaTarifaNegativa() {
        assertThrows(
                ReglaDominioException.class,
                () -> new CargoNoche(
                        NOCHE, TipoTemporada.ESPECIAL, Dinero.de(-1), 3
                )
        );
    }

    @Test
    void debeRechazarUnaCantidadNegativaDeOcupantesFacturables() {
        assertThrows(
                ReglaDominioException.class,
                () -> new CargoNoche(
                        NOCHE, TipoTemporada.ESPECIAL, Dinero.de(100_000), -1
                )
        );
    }
}