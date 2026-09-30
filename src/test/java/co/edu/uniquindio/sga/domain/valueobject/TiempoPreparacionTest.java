package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del tiempo de preparación entre grupos.
 *
 * <p>Verifican las validaciones del objeto de valor
 * y los límites temporales relacionados con RN-20.</p>
 *
 * <p>Los horarios son datos de prueba explícitos.
 * No se utiliza el reloj del sistema ni Spring.</p>
 */
class TiempoPreparacionTest {

    @Test
    void configuracionesConLosMismosValoresDebenSerIguales() {
        TiempoPreparacion primera = new TiempoPreparacion(
                3,
                LocalTime.of(11, 0),
                LocalTime.of(15, 0)
        );

        TiempoPreparacion segunda = new TiempoPreparacion(
                3,
                LocalTime.of(11, 0),
                LocalTime.of(15, 0)
        );

        assertEquals(primera, segunda);
        assertEquals(primera.hashCode(), segunda.hashCode());
    }

    @Test
    void debePermitirLaEntradaConLosHorariosDeSieteRaices() {
        TiempoPreparacion preparacion = new TiempoPreparacion(
                3,
                LocalTime.of(11, 0),
                LocalTime.of(15, 0)
        );

        boolean permiteEntrada = preparacion.permiteEntradaElMismoDia();

        assertTrue(permiteEntrada);
    }

    @Test
    void debePermitirLaEntradaCuandoLaVentanaEsExactamenteLaRequerida() {
        TiempoPreparacion preparacion = new TiempoPreparacion(
                3,
                LocalTime.of(11, 0),
                LocalTime.of(14, 0)
        );

        boolean permiteEntrada = preparacion.permiteEntradaElMismoDia();

        assertTrue(permiteEntrada);
    }

    @Test
    void noDebePermitirLaEntradaCuandoFaltaUnMinutoDePreparacion() {
        TiempoPreparacion preparacion = new TiempoPreparacion(
                3,
                LocalTime.of(11, 0),
                LocalTime.of(13, 59)
        );

        boolean permiteEntrada = preparacion.permiteEntradaElMismoDia();

        assertFalse(permiteEntrada);
    }

    @Test
    void debePermitirLaEntradaCuandoSobraUnMinutoDePreparacion() {
        TiempoPreparacion preparacion = new TiempoPreparacion(
                3,
                LocalTime.of(11, 0),
                LocalTime.of(14, 1)
        );

        boolean permiteEntrada = preparacion.permiteEntradaElMismoDia();

        assertTrue(permiteEntrada);
    }

    @Test
    void noDebeInterpretarUnaEntradaAnteriorComoSiFueraDelDiaSiguiente() {
        TiempoPreparacion preparacion = new TiempoPreparacion(
                3,
                LocalTime.of(23, 0),
                LocalTime.of(2, 0)
        );

        boolean permiteEntrada = preparacion.permiteEntradaElMismoDia();

        assertFalse(permiteEntrada);
    }

    @Test
    void noDebePermitirHorariosIgualesSiSeRequierePreparacion() {
        TiempoPreparacion preparacion = new TiempoPreparacion(
                1,
                LocalTime.of(11, 0),
                LocalTime.of(11, 0)
        );

        boolean permiteEntrada = preparacion.permiteEntradaElMismoDia();

        assertFalse(permiteEntrada);
    }

    @Test
    void debePermitirHorariosIgualesCuandoLaPreparacionConfiguradaEsCero() {
        TiempoPreparacion preparacion = new TiempoPreparacion(
                0,
                LocalTime.of(11, 0),
                LocalTime.of(11, 0)
        );

        boolean permiteEntrada = preparacion.permiteEntradaElMismoDia();

        assertTrue(permiteEntrada);
    }

    @Test
    void noDebePermitirEntradaAnteriorAunqueLaPreparacionSeaCero() {
        TiempoPreparacion preparacion = new TiempoPreparacion(
                0,
                LocalTime.of(11, 0),
                LocalTime.of(10, 59)
        );

        boolean permiteEntrada = preparacion.permiteEntradaElMismoDia();

        assertFalse(permiteEntrada);
    }

    @Test
    void noDebePermitirEntradaElMismoDiaSiLaPreparacionRequiereUnDiaCompleto() {
        TiempoPreparacion preparacion = new TiempoPreparacion(
                24,
                LocalTime.MIDNIGHT,
                LocalTime.of(23, 59)
        );

        boolean permiteEntrada = preparacion.permiteEntradaElMismoDia();

        assertFalse(permiteEntrada);
    }

    @Test
    void debeRechazarUnaDuracionNegativa() {
        assertThrows(
                ReglaDominioException.class,
                () -> new TiempoPreparacion(
                        -1,
                        LocalTime.of(11, 0),
                        LocalTime.of(15, 0)
                )
        );
    }

    @Test
    void debeRechazarUnaHoraDeSalidaNula() {
        assertThrows(
                ReglaDominioException.class,
                () -> new TiempoPreparacion(
                        3,
                        null,
                        LocalTime.of(15, 0)
                )
        );
    }

    @Test
    void debeRechazarUnaHoraDeEntradaNula() {
        assertThrows(
                ReglaDominioException.class,
                () -> new TiempoPreparacion(
                        3,
                        LocalTime.of(11, 0),
                        null
                )
        );
    }
}