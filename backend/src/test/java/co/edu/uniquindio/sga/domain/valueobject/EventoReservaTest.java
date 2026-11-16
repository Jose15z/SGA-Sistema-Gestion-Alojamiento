package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pruebas unitarias del registro de una acción sobre una reserva.
 *
 * <p>Verifican igualdad por valor, normalización de textos
 * y validación de datos obligatorios.</p>
 *
 * <p>La legalidad de las transiciones y la protección del
 * historial pertenecen al agregado Reserva.</p>
 */
class EventoReservaTest {

    private static final LocalDateTime MOMENTO =
            LocalDateTime.of(2026, 12, 1, 10, 30);

    @Test
    void eventosConLosMismosDatosDebenSerIguales() {
        EventoReserva primero = confirmacion(MOMENTO);
        EventoReserva segundo = confirmacion(MOMENTO);

        assertEquals(primero, segundo);
        assertEquals(primero.hashCode(), segundo.hashCode());
    }

    @Test
    void eventosConMomentosDistintosNoDebenSerIguales() {
        EventoReserva primero = confirmacion(MOMENTO);
        EventoReserva segundo = confirmacion(MOMENTO.plusMinutes(1));

        assertNotEquals(primero, segundo);
    }

    @Test
    void debeNormalizarLosEspaciosDeLosTextos() {
        EventoReserva conEspacios = new EventoReserva(
                MOMENTO,
                "  confirmar  ",
                "  recepcion-1  ",
                EstadoReserva.PENDIENTE,
                EstadoReserva.CONFIRMADA,
                "  Anticipo verificado  "
        );

        EventoReserva sinEspacios = confirmacion(MOMENTO);

        assertEquals(sinEspacios, conEspacios);
    }

    @Test
    void debePermitirUnEventoDeCreacionSinEstadoAnterior() {
        assertDoesNotThrow(() -> new EventoReserva(
                MOMENTO,
                "crear",
                "recepcion-1",
                null,
                EstadoReserva.PENDIENTE,
                null
        ));
    }

    @Test
    void debePermitirUnaAccionQueNoCambieElEstado() {
        assertDoesNotThrow(() -> new EventoReserva(
                MOMENTO,
                "indicarHoraEstimadaLlegada",
                "recepcion-1",
                EstadoReserva.CONFIRMADA,
                EstadoReserva.CONFIRMADA,
                "Llegada estimada a las 18:00"
        ));
    }

    @Test
    void debePermitirUnaObservacionNula() {
        assertDoesNotThrow(() -> new EventoReserva(
                MOMENTO,
                "confirmar",
                "recepcion-1",
                EstadoReserva.PENDIENTE,
                EstadoReserva.CONFIRMADA,
                null
        ));
    }

    @Test
    void debeRechazarUnMomentoNulo() {
        assertThrows(
                ReglaDominioException.class,
                () -> confirmacion(null)
        );
    }

    @Test
    void debeRechazarUnaAccionNula() {
        assertThrows(
                ReglaDominioException.class,
                () -> new EventoReserva(
                        MOMENTO,
                        null,
                        "recepcion-1",
                        EstadoReserva.PENDIENTE,
                        EstadoReserva.CONFIRMADA,
                        null
                )
        );
    }

    @Test
    void debeRechazarUnaAccionVacia() {
        assertThrows(
                ReglaDominioException.class,
                () -> new EventoReserva(
                        MOMENTO,
                        "",
                        "recepcion-1",
                        EstadoReserva.PENDIENTE,
                        EstadoReserva.CONFIRMADA,
                        null
                )
        );
    }

    @Test
    void debeRechazarUnaAccionCompuestaSoloPorEspacios() {
        assertThrows(
                ReglaDominioException.class,
                () -> new EventoReserva(
                        MOMENTO,
                        "   ",
                        "recepcion-1",
                        EstadoReserva.PENDIENTE,
                        EstadoReserva.CONFIRMADA,
                        null
                )
        );
    }

    @Test
    void debeRechazarUnAutorNulo() {
        assertThrows(
                ReglaDominioException.class,
                () -> new EventoReserva(
                        MOMENTO,
                        "confirmar",
                        null,
                        EstadoReserva.PENDIENTE,
                        EstadoReserva.CONFIRMADA,
                        null
                )
        );
    }

    @Test
    void debeRechazarUnAutorVacio() {
        assertThrows(
                ReglaDominioException.class,
                () -> new EventoReserva(
                        MOMENTO,
                        "confirmar",
                        "",
                        EstadoReserva.PENDIENTE,
                        EstadoReserva.CONFIRMADA,
                        null
                )
        );
    }

    @Test
    void debeRechazarUnAutorCompuestoSoloPorEspacios() {
        assertThrows(
                ReglaDominioException.class,
                () -> new EventoReserva(
                        MOMENTO,
                        "confirmar",
                        "   ",
                        EstadoReserva.PENDIENTE,
                        EstadoReserva.CONFIRMADA,
                        null
                )
        );
    }

    @Test
    void debeRechazarUnEstadoResultanteNulo() {
        assertThrows(
                ReglaDominioException.class,
                () -> new EventoReserva(
                        MOMENTO,
                        "confirmar",
                        "recepcion-1",
                        EstadoReserva.PENDIENTE,
                        null,
                        null
                )
        );
    }

    private EventoReserva confirmacion(LocalDateTime momento) {
        return new EventoReserva(
                momento,
                "confirmar",
                "recepcion-1",
                EstadoReserva.PENDIENTE,
                EstadoReserva.CONFIRMADA,
                "Anticipo verificado"
        );
    }
}