package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.ReservasDePrueba;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class ReservaLlegadaTest {
    @Test
    void debeRegistrarLlegadaYConservarLaCotizacion() {
        Reserva reserva = ReservasDePrueba.confirmada();
        var valor = reserva.getValor();
        reserva.registrarLlegada("recepcion-1", ReservasDePrueba.LLEGADA);
        assertEquals(EstadoReserva.EN_CURSO, reserva.getEstado());
        assertEquals(valor, reserva.getValor());
        var evento = reserva.getHistorial().get(3);
        assertEquals("REGISTRO", evento.accion());
        assertEquals(EstadoReserva.CONFIRMADA, evento.estadoAnterior());
        assertEquals(EstadoReserva.EN_CURSO, evento.estadoNuevo());
    }

    @Test
    void debeRechazarElDiaAnteriorALaEntrada() {
        rechazar(ReservasDePrueba.confirmada(), "recepcion-1",
                ReservasDePrueba.LLEGADA.toLocalDate().atStartOfDay().minusNanos(1));
    }

    @Test
    void debeRechazarReservaPendiente() {
        rechazar(ReservasDePrueba.pendiente(), "recepcion-1", ReservasDePrueba.LLEGADA);
    }

    @Test
    void debeRechazarReservaCancelada() {
        Reserva reserva = ReservasDePrueba.confirmada();
        reserva.cancelar("Cambio", "recepcion-1", ReservasDePrueba.CREACION);
        rechazar(reserva, "recepcion-1", ReservasDePrueba.LLEGADA);
    }

    @Test
    void debeRechazarSegundaLlegada() {
        Reserva reserva = ReservasDePrueba.confirmada();
        reserva.registrarLlegada("recepcion-1", ReservasDePrueba.LLEGADA);
        rechazar(reserva, "recepcion-1", ReservasDePrueba.LLEGADA.plusMinutes(1));
    }

    @Test
    void debeRechazarAutorInvalidoSinCambiarElEstado() {
        rechazar(ReservasDePrueba.confirmada(), "  ", ReservasDePrueba.LLEGADA);
    }

    @Test
    void debeRechazarMomentoNulo() {
        rechazar(ReservasDePrueba.confirmada(), "recepcion-1", null);
    }

    private void rechazar(Reserva reserva, String autor, LocalDateTime momento) {
        var estado = reserva.getEstado();
        var historial = reserva.getHistorial();
        assertThrows(ReglaDominioException.class, () -> reserva.registrarLlegada(autor, momento));
        assertEquals(estado, reserva.getEstado());
        assertEquals(historial, reserva.getHistorial());
    }
}
