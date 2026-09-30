package co.edu.uniquindio.sga.domain.exception;

import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias del valor monetario en pesos colombianos.
 *
 * No requieren Spring, bases de datos ni acceso al reloj.
 */
class DineroTest {

    @Test
    void importesEquivalentesConDistintaEscalaSonIguales() {
        Dinero primero = new Dinero(new BigDecimal("100"));
        Dinero segundo = new Dinero(new BigDecimal("100.00"));

        assertEquals(primero, segundo);
        assertEquals(primero.hashCode(), segundo.hashCode());
    }

    @Test
    void debeRechazarUnValorMonetarioNulo() {
        assertThrows(
                ReglaDominioException.class,
                () -> new Dinero(null)
        );
    }

    @Test
    void debeSumarSinModificarLosImportesOriginales() {
        Dinero primero = Dinero.de(70_000);
        Dinero segundo = Dinero.de(20_000);

        Dinero resultado = primero.mas(segundo);

        assertEquals(Dinero.de(90_000), resultado);
        assertEquals(Dinero.de(70_000), primero);
        assertEquals(Dinero.de(20_000), segundo);
    }

    @Test
    void debePermitirUnSaldoNegativoAlRestar() {
        Dinero cargos = Dinero.de(100_000);
        Dinero pagos = Dinero.de(120_000);

        Dinero saldo = cargos.menos(pagos);

        assertEquals(Dinero.de(-20_000), saldo);
        assertTrue(saldo.esNegativo());
        assertEquals(Dinero.de(100_000), cargos);
        assertEquals(Dinero.de(120_000), pagos);
    }

    @Test
    void debeObtenerSaldoCeroCuandoLosImportesCoinciden() {
        Dinero cargos = Dinero.de(100_000);
        Dinero pagos = Dinero.de(100_000);

        Dinero saldo = cargos.menos(pagos);

        assertEquals(Dinero.CERO, saldo);
        assertTrue(saldo.esCero());
        assertFalse(saldo.esNegativo());
    }

    @Test
    void debeMultiplicarLaTarifaPorLaCantidadDeOcupantes() {
        Dinero tarifa = Dinero.de(72_000);

        Dinero subtotal = tarifa.por(3);

        assertEquals(Dinero.de(216_000), subtotal);
        assertEquals(Dinero.de(72_000), tarifa);
    }

    @Test
    void debeObtenerCeroAlMultiplicarPorCero() {
        Dinero tarifa = Dinero.de(72_000);

        Dinero resultado = tarifa.por(0);

        assertEquals(Dinero.CERO, resultado);
    }

    @Test
    void debeConservarPrecisionEnImportesGrandes() {
        Dinero importe = new Dinero(
                new BigDecimal("9007199254740992")
        );

        Dinero resultado = importe.mas(Dinero.de(1));

        assertEquals(
                new BigDecimal("9007199254740993"),
                resultado.valor()
        );
    }

    @Test
    void debeRedondearHaciaAbajoUnaFraccionMenorAMedioPeso() {
        Dinero resultado = new Dinero(new BigDecimal("100.49"));

        assertEquals(Dinero.de(100), resultado);
    }

    @Test
    void debeRedondearMedioPesoHaciaArribaEnUnImportePositivo() {
        Dinero resultado = new Dinero(new BigDecimal("100.50"));

        assertEquals(Dinero.de(101), resultado);
    }

    @Test
    void debeRedondearUnImporteNegativoConHalfUp() {
        Dinero resultado = new Dinero(new BigDecimal("-100.50"));

        assertEquals(Dinero.de(-101), resultado);
    }

    @Test
    void debeRechazarUnaSumaNulaSinAlterarElImporte() {
        Dinero importe = Dinero.de(70_000);

        assertThrows(
                ReglaDominioException.class,
                () -> importe.mas(null)
        );

        assertEquals(Dinero.de(70_000), importe);
    }

    @Test
    void debeRechazarUnaRestaNulaSinAlterarElImporte() {
        Dinero importe = Dinero.de(70_000);

        assertThrows(
                ReglaDominioException.class,
                () -> importe.menos(null)
        );

        assertEquals(Dinero.de(70_000), importe);
    }
}