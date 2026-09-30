package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del objeto de valor FechaCreacion con fecha y hora.
 *
 * <p>Verifican igualdad, conservación de precisión temporal
 * y comparaciones estrictas entre momentos.</p>
 *
 * <p>No utilizan el reloj del sistema ni implementan
 * la decisión de vencimiento de una reserva.</p>
 */
class FechaCreacionTest {

    private static final LocalDateTime MOMENTO =
            LocalDateTime.of(2026, 12, 31, 15, 30, 45);

    @Test
    void creacionesConElMismoMomentoDebenSerIguales() {
        FechaCreacion primera = new FechaCreacion(MOMENTO);
        FechaCreacion segunda = new FechaCreacion(
                LocalDateTime.of(2026, 12, 31, 15, 30, 45)
        );

        assertEquals(primera, segunda);
        assertEquals(primera.hashCode(), segunda.hashCode());
    }

    @Test
    void creacionesDelMismoDiaConDistintaHoraNoDebenSerIguales() {
        FechaCreacion primera = new FechaCreacion(MOMENTO);
        FechaCreacion segunda = new FechaCreacion(
                MOMENTO.plusHours(1)
        );

        assertNotEquals(primera, segunda);
    }

    @Test
    void creacionesConLaMismaHoraEnDistintosDiasNoDebenSerIguales() {
        FechaCreacion primera = new FechaCreacion(MOMENTO);
        FechaCreacion segunda = new FechaCreacion(
                MOMENTO.plusDays(1)
        );

        assertNotEquals(primera, segunda);
    }

    @Test
    void obtenerLaFechaCalendarioNoDebeEliminarLaHoraConservada() {
        FechaCreacion creacion = new FechaCreacion(MOMENTO);

        LocalDate fechaCalendario = creacion.fecha();

        assertEquals(LocalDate.of(2026, 12, 31), fechaCalendario);
        assertEquals(MOMENTO, creacion.momento());
    }

    @Test
    void debeReconocerUnMomentoPosteriorDentroDelMismoDia() {
        FechaCreacion creacion = new FechaCreacion(MOMENTO);
        LocalDateTime posterior = MOMENTO.plusSeconds(1);

        assertTrue(creacion.esAnteriorA(posterior));
        assertFalse(creacion.esPosteriorA(posterior));
    }

    @Test
    void debeReconocerUnMomentoAnteriorDentroDelMismoDia() {
        FechaCreacion creacion = new FechaCreacion(MOMENTO);
        LocalDateTime anterior = MOMENTO.minusSeconds(1);

        assertTrue(creacion.esPosteriorA(anterior));
        assertFalse(creacion.esAnteriorA(anterior));
    }

    @Test
    void debeCompararCorrectamenteAlCruzarLaMedianocheYElAnio() {
        FechaCreacion creacion = new FechaCreacion(
                LocalDateTime.of(2026, 12, 31, 23, 59, 59)
        );
        LocalDateTime siguienteAnio =
                LocalDateTime.of(2027, 1, 1, 0, 0);

        assertTrue(creacion.esAnteriorA(siguienteAnio));
        assertFalse(creacion.esPosteriorA(siguienteAnio));
        assertEquals(2026, creacion.anio());
    }

    @Test
    void unMomentoIgualNoDebeSerAnteriorNiPosterior() {
        FechaCreacion creacion = new FechaCreacion(MOMENTO);

        assertFalse(creacion.esAnteriorA(MOMENTO));
        assertFalse(creacion.esPosteriorA(MOMENTO));
    }

    @Test
    void debeRechazarUnMomentoDeCreacionNulo() {
        assertThrows(
                ReglaDominioException.class,
                () -> new FechaCreacion(null)
        );
    }

    @Test
    void debeRechazarComparacionAnteriorConNullSinAlterarElMomento() {
        FechaCreacion creacion = new FechaCreacion(MOMENTO);

        assertThrows(
                ReglaDominioException.class,
                () -> creacion.esAnteriorA(null)
        );

        assertEquals(MOMENTO, creacion.momento());
    }

    @Test
    void debeRechazarComparacionPosteriorConNullSinAlterarElMomento() {
        FechaCreacion creacion = new FechaCreacion(MOMENTO);

        assertThrows(
                ReglaDominioException.class,
                () -> creacion.esPosteriorA(null)
        );

        assertEquals(MOMENTO, creacion.momento());
    }
}