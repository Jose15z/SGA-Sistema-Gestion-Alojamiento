package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias del intervalo de una estancia.
 *
 * <p>Verifican RN-03 y el cálculo de solapamientos utilizado
 * como soporte para RN-01.</p>
 *
 * <p>La entrada está incluida y la salida está excluida.
 * Todas las fechas son fijas.</p>
 */
class EstanciaTest {

    private static final LocalDate ENTRADA =
            LocalDate.of(2026, 12, 10);

    @Test
    void estanciasConLasMismasFechasDebenSerIguales() {
        Estancia primera = new Estancia(
                ENTRADA, ENTRADA.plusDays(2)
        );
        Estancia segunda = new Estancia(
                ENTRADA, ENTRADA.plusDays(2)
        );

        assertEquals(primera, segunda);
        assertEquals(primera.hashCode(), segunda.hashCode());
    }

    @Test
    void estanciasConFechasDistintasNoDebenSerIguales() {
        Estancia primera = new Estancia(
                ENTRADA, ENTRADA.plusDays(2)
        );
        Estancia segunda = new Estancia(
                ENTRADA, ENTRADA.plusDays(3)
        );

        assertNotEquals(primera, segunda);
    }

    @Test
    void debePermitirUnaEstanciaDeUnaNoche() {
        Estancia estancia = new Estancia(
                ENTRADA, ENTRADA.plusDays(1)
        );

        assertEquals(1, estancia.noches());
        assertEquals(List.of(ENTRADA), estancia.nochesOcupadas());
    }

    @Test
    void debeContarLasNochesAlCruzarUnCambioDeAnio() {
        Estancia estancia = new Estancia(
                LocalDate.of(2026, 12, 30),
                LocalDate.of(2027, 1, 2)
        );

        assertEquals(3, estancia.noches());
    }

    @Test
    void debeContarElVeintinueveDeFebreroEnUnAnioBisiesto() {
        Estancia estancia = new Estancia(
                LocalDate.of(2028, 2, 28),
                LocalDate.of(2028, 3, 1)
        );

        assertEquals(2, estancia.noches());
        assertEquals(
                List.of(
                        LocalDate.of(2028, 2, 28),
                        LocalDate.of(2028, 2, 29)
                ),
                estancia.nochesOcupadas()
        );
    }

    @Test
    void debeIncluirLaEntradaYExcluirLaSalida() {
        Estancia estancia = new Estancia(
                ENTRADA, ENTRADA.plusDays(2)
        );

        assertTrue(estancia.incluye(ENTRADA));
        assertTrue(estancia.incluye(ENTRADA.plusDays(1)));
        assertFalse(estancia.incluye(ENTRADA.plusDays(2)));
    }

    @Test
    void noDebeIncluirFechasAnterioresOPosterioresAlIntervalo() {
        Estancia estancia = new Estancia(
                ENTRADA, ENTRADA.plusDays(2)
        );

        assertFalse(estancia.incluye(ENTRADA.minusDays(1)));
        assertFalse(estancia.incluye(ENTRADA.plusDays(3)));
    }

    @Test
    void debeEnumerarTodasLasNochesEnOrdenSinIncluirLaSalida() {
        Estancia estancia = new Estancia(
                ENTRADA, ENTRADA.plusDays(3)
        );

        assertEquals(
                List.of(
                        ENTRADA,
                        ENTRADA.plusDays(1),
                        ENTRADA.plusDays(2)
                ),
                estancia.nochesOcupadas()
        );
    }

    @Test
    void debeDetectarSolapamientoCuandoSeComparteUnaSolaNoche() {
        Estancia primera = new Estancia(
                ENTRADA, ENTRADA.plusDays(2)
        );
        Estancia segunda = new Estancia(
                ENTRADA.plusDays(1), ENTRADA.plusDays(3)
        );

        assertTrue(primera.seSolapaCon(segunda));
        assertTrue(segunda.seSolapaCon(primera));
    }

    @Test
    void debeDetectarSolapamientoCuandoUnaEstanciaContieneALaOtra() {
        Estancia exterior = new Estancia(
                ENTRADA, ENTRADA.plusDays(5)
        );
        Estancia interior = new Estancia(
                ENTRADA.plusDays(1), ENTRADA.plusDays(3)
        );

        assertTrue(exterior.seSolapaCon(interior));
        assertTrue(interior.seSolapaCon(exterior));
    }

    @Test
    void debeDetectarSolapamientoEntreEstanciasConLasMismasFechas() {
        Estancia primera = new Estancia(
                ENTRADA, ENTRADA.plusDays(2)
        );
        Estancia segunda = new Estancia(
                ENTRADA, ENTRADA.plusDays(2)
        );

        assertTrue(primera.seSolapaCon(segunda));
    }

    @Test
    void noDebeDetectarSolapamientoEntreEstanciasConsecutivas() {
        Estancia primera = new Estancia(
                ENTRADA, ENTRADA.plusDays(2)
        );
        Estancia segunda = new Estancia(
                ENTRADA.plusDays(2), ENTRADA.plusDays(4)
        );

        assertFalse(primera.seSolapaCon(segunda));
        assertFalse(segunda.seSolapaCon(primera));
    }

    @Test
    void noDebeDetectarSolapamientoEntreEstanciasSeparadas() {
        Estancia primera = new Estancia(
                ENTRADA, ENTRADA.plusDays(2)
        );
        Estancia segunda = new Estancia(
                ENTRADA.plusDays(3), ENTRADA.plusDays(5)
        );

        assertFalse(primera.seSolapaCon(segunda));
        assertFalse(segunda.seSolapaCon(primera));
    }

    @Test
    void debeRechazarUnaEntradaNula() {
        assertThrows(
                ReglaDominioException.class,
                () -> new Estancia(null, ENTRADA.plusDays(1))
        );
    }

    @Test
    void debeRechazarUnaSalidaNula() {
        assertThrows(
                ReglaDominioException.class,
                () -> new Estancia(ENTRADA, null)
        );
    }

    @Test
    void debeRechazarEntradaYSalidaEnLaMismaFecha() {
        assertThrows(
                ReglaDominioException.class,
                () -> new Estancia(ENTRADA, ENTRADA)
        );
    }

    @Test
    void debeRechazarUnaSalidaAnteriorALaEntrada() {
        assertThrows(
                ReglaDominioException.class,
                () -> new Estancia(ENTRADA, ENTRADA.minusDays(1))
        );
    }
}