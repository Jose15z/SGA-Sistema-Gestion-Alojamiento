package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del valor congelado del alojamiento.
 *
 * <p>Verifican igualdad por valor, protección del desglose,
 * validaciones, cálculo monetario y correspondencia con
 * las noches de una estancia.</p>
 *
 * <p>No requieren Spring, repositorios ni reloj del sistema.</p>
 */
class ValorCongeladoTest {

    private static final LocalDate ENTRADA =
            LocalDate.of(2026, 12, 10);

    @Test
    void debeCompararPorValorAunqueCambienElOrdenYLaEscalaDelPorcentaje() {
        CargoNoche primera = cargo(ENTRADA, 100_000);
        CargoNoche segunda = cargo(ENTRADA.plusDays(1), 150_000);

        ValorCongelado primero = new ValorCongelado(
                List.of(primera, segunda),
                new BigDecimal("10")
        );

        ValorCongelado segundo = new ValorCongelado(
                List.of(segunda, primera),
                new BigDecimal("10.00")
        );

        assertEquals(primero, segundo);
        assertEquals(primero.hashCode(), segundo.hashCode());
    }

    @Test
    void debeSumarNochesConTarifasYTemporadasDistintas() {
        CargoNoche normal = new CargoNoche(
                ENTRADA, "Normal", Dinero.de(100_000), 2
        );

        CargoNoche especial = new CargoNoche(
                ENTRADA.plusDays(1),
                "Especial",
                Dinero.de(150_000),
                2
        );

        ValorCongelado valor = new ValorCongelado(
                List.of(normal, especial)
        );

        assertEquals(Dinero.de(500_000), valor.subtotal());
        assertEquals(Dinero.de(500_000), valor.total());
        assertEquals(Dinero.CERO, valor.descuento());
    }

    @Test
    void debeAplicarElPorcentajeSobreTodoElAlojamiento() {
        ValorCongelado valor = new ValorCongelado(
                List.of(
                        cargo(ENTRADA, 100_000),
                        cargo(ENTRADA.plusDays(1), 150_000)
                ),
                new BigDecimal("10")
        );

        assertEquals(Dinero.de(250_000), valor.subtotal());
        assertEquals(Dinero.de(225_000), valor.total());
        assertEquals(Dinero.de(25_000), valor.descuento());
    }

    @Test
    void debeRedondearUnaSolaVezDespuesDeSumarTodasLasNoches() {
        ValorCongelado valor = new ValorCongelado(
                List.of(
                        cargo(ENTRADA, 5),
                        cargo(ENTRADA.plusDays(1), 5)
                ),
                new BigDecimal("10")
        );

        // Dos noches de 5 pesos suman 10.
        // Con descuento del 10 %, el total correcto es 9.
        // Redondear cada noche por separado produciría 10.
        assertEquals(Dinero.de(9), valor.total());
        assertEquals(Dinero.de(1), valor.descuento());
    }

    @Test
    void debeMantenerCoherenciaEntreSubtotalDescuentoYTotalRedondeado() {
        ValorCongelado valor = new ValorCongelado(
                List.of(cargo(ENTRADA, 15)),
                new BigDecimal("10")
        );

        // El total exacto es 13.5 y se redondea a 14.
        assertEquals(Dinero.de(14), valor.total());
        assertEquals(Dinero.de(1), valor.descuento());
        assertEquals(
                valor.total(),
                valor.subtotal().menos(valor.descuento())
        );
    }

    @Test
    void debeAceptarElLimiteDeCienPorCientoDeDescuento() {
        ValorCongelado valor = new ValorCongelado(
                List.of(cargo(ENTRADA, 100_000)),
                new BigDecimal("100")
        );

        assertEquals(Dinero.CERO, valor.total());
        assertEquals(valor.subtotal(), valor.descuento());
    }

    @Test
    void modificarLaListaOriginalNoDebeAlterarElValorCongelado() {
        CargoNoche original = cargo(ENTRADA, 100_000);
        List<CargoNoche> listaOriginal = new ArrayList<>();
        listaOriginal.add(original);

        ValorCongelado valor = new ValorCongelado(listaOriginal);

        listaOriginal.clear();
        listaOriginal.add(cargo(ENTRADA, 900_000));

        assertEquals(List.of(original), valor.detalle());
        assertEquals(Dinero.de(100_000), valor.total());
    }

    @Test
    void noDebePermitirModificarElDesgloseExpuesto() {
        CargoNoche original = cargo(ENTRADA, 100_000);
        ValorCongelado valor = new ValorCongelado(
                List.of(original)
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> valor.detalle().add(
                        cargo(ENTRADA.plusDays(1), 200_000)
                )
        );

        assertEquals(List.of(original), valor.detalle());
        assertEquals(Dinero.de(100_000), valor.total());
    }

    @Test
    void debeReconocerLasNochesExactasSinIncluirLaFechaDeSalida() {
        ValorCongelado valor = new ValorCongelado(
                List.of(
                        cargo(ENTRADA.plusDays(1), 100_000),
                        cargo(ENTRADA, 100_000)
                )
        );

        Estancia estancia = new Estancia(
                ENTRADA, ENTRADA.plusDays(2)
        );

        assertTrue(valor.correspondeA(estancia));
        assertEquals(2, valor.noches());
    }

    @Test
    void noDebeAceptarFechasDistintasAunqueCoincidaLaCantidadDeNoches() {
        ValorCongelado valor = new ValorCongelado(
                List.of(
                        cargo(ENTRADA, 100_000),
                        cargo(ENTRADA.plusDays(2), 100_000)
                )
        );

        Estancia estancia = new Estancia(
                ENTRADA, ENTRADA.plusDays(2)
        );

        assertFalse(valor.correspondeA(estancia));
    }

    @Test
    void noDebeCorresponderAUnaEstanciaSiFaltaUnaNoche() {
        ValorCongelado valor = new ValorCongelado(
                List.of(cargo(ENTRADA, 100_000))
        );

        Estancia estancia = new Estancia(
                ENTRADA, ENTRADA.plusDays(2)
        );

        assertFalse(valor.correspondeA(estancia));
    }

    @Test
    void debeRechazarUnaEstanciaNulaSinAlterarElValor() {
        CargoNoche original = cargo(ENTRADA, 100_000);
        ValorCongelado valor = new ValorCongelado(
                List.of(original)
        );

        assertThrows(
                ReglaDominioException.class,
                () -> valor.correspondeA(null)
        );

        assertEquals(List.of(original), valor.detalle());
        assertEquals(Dinero.de(100_000), valor.total());
    }

    @Test
    void debeRechazarUnDesgloseNulo() {
        assertThrows(
                ReglaDominioException.class,
                () -> new ValorCongelado(null)
        );
    }

    @Test
    void debeRechazarUnDesgloseVacio() {
        assertThrows(
                ReglaDominioException.class,
                () -> new ValorCongelado(List.of())
        );
    }

    @Test
    void debeRechazarUnCargoNuloEnElDesglose() {
        List<CargoNoche> detalle = new ArrayList<>();
        detalle.add(cargo(ENTRADA, 100_000));
        detalle.add(null);

        assertThrows(
                ReglaDominioException.class,
                () -> new ValorCongelado(detalle)
        );
    }

    @Test
    void debeRechazarUnaNocheRepetidaAunqueTengaOtraTarifa() {
        List<CargoNoche> detalle = List.of(
                cargo(ENTRADA, 100_000),
                cargo(ENTRADA, 150_000)
        );

        assertThrows(
                ReglaDominioException.class,
                () -> new ValorCongelado(detalle)
        );
    }

    @Test
    void debeRechazarUnPorcentajeNulo() {
        List<CargoNoche> detalle = List.of(
                cargo(ENTRADA, 100_000)
        );

        assertThrows(
                ReglaDominioException.class,
                () -> new ValorCongelado(detalle, null)
        );
    }

    @Test
    void debeRechazarUnPorcentajeMenorQueCero() {
        List<CargoNoche> detalle = List.of(
                cargo(ENTRADA, 100_000)
        );

        assertThrows(
                ReglaDominioException.class,
                () -> new ValorCongelado(
                        detalle, new BigDecimal("-0.01")
                )
        );
    }

    @Test
    void debeRechazarUnPorcentajeMayorQueCien() {
        List<CargoNoche> detalle = List.of(
                cargo(ENTRADA, 100_000)
        );

        assertThrows(
                ReglaDominioException.class,
                () -> new ValorCongelado(
                        detalle, new BigDecimal("100.01")
                )
        );
    }

    private CargoNoche cargo(LocalDate noche, long tarifa) {
        return new CargoNoche(
                noche, "Normal", Dinero.de(tarifa), 1
        );
    }
}