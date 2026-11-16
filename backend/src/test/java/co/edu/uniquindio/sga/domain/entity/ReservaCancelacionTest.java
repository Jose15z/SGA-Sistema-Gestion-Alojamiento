package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.ReservasDePrueba;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class ReservaCancelacionTest {
    @Test
    void debeCancelarPendienteYConservarValorPoliticaEIdentidad() {
        Reserva reserva = ReservasDePrueba.pendiente();
        var valor = reserva.getValor();
        var politica = reserva.getPolitica();
        var codigo = reserva.getCodigo();
        reserva.cancelar("Cambio de planes", "recepcion-1", ReservasDePrueba.CREACION.plusHours(1));
        assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());
        assertFalse(reserva.getEstado().retieneDisponibilidad());
        assertEquals(valor, reserva.getValor());
        assertEquals(politica, reserva.getPolitica());
        assertEquals(codigo, reserva.getCodigo());
        var evento = reserva.getHistorial().get(1);
        assertEquals("CANCELACION", evento.accion());
        assertEquals(EstadoReserva.PENDIENTE, evento.estadoAnterior());
        assertEquals(EstadoReserva.CANCELADA, evento.estadoNuevo());
        assertTrue(evento.observacion().contains("Cambio de planes"));
    }

    @Test
    void debeCancelarConfirmada() {
        Reserva reserva = ReservasDePrueba.confirmada();
        reserva.cancelar("Cambio de planes", "recepcion-1", ReservasDePrueba.CREACION.plusHours(1));
        assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getHistorial().get(3).estadoAnterior());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void debeRechazarMotivoInvalidoSinCambios(String motivo) {
        rechazar(ReservasDePrueba.confirmada(), motivo, "recepcion-1", ReservasDePrueba.CREACION);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void debeRechazarAutorInvalidoSinCambios(String autor) {
        rechazar(ReservasDePrueba.confirmada(), "Cambio de planes", autor, ReservasDePrueba.CREACION);
    }

    @Test
    void debeRechazarMomentoNulo() {
        rechazar(ReservasDePrueba.confirmada(), "Cambio", "recepcion-1", null);
    }

    @Test
    void debeRechazarMomentoAnteriorAlHistorial() {
        rechazar(ReservasDePrueba.confirmada(), "Cambio", "recepcion-1",
                ReservasDePrueba.CREACION.minusSeconds(1));
    }

    @Test
    void noDebeCancelarDosVeces() {
        Reserva reserva = ReservasDePrueba.confirmada();
        reserva.cancelar("Cambio", "recepcion-1", ReservasDePrueba.CREACION);
        rechazar(reserva, "Otra vez", "recepcion-1", ReservasDePrueba.CREACION.plusHours(1));
    }

    @Test
    void noDebeCancelarUnaEstanciaEnCurso() {
        Reserva reserva = ReservasDePrueba.confirmada();
        reserva.registrarLlegada("recepcion-1", ReservasDePrueba.LLEGADA);
        rechazar(reserva, "Salida anticipada", "recepcion-1", ReservasDePrueba.LLEGADA.plusHours(1));
    }

    private void rechazar(Reserva reserva, String motivo, String autor, LocalDateTime momento) {
        var estado = reserva.getEstado();
        var historial = reserva.getHistorial();
        var valor = reserva.getValor();
        var politica = reserva.getPolitica();
        assertThrows(ReglaDominioException.class, () -> reserva.cancelar(motivo, autor, momento));
        assertEquals(estado, reserva.getEstado());
        assertEquals(historial, reserva.getHistorial());
        assertEquals(valor, reserva.getValor());
        assertEquals(politica, reserva.getPolitica());
    }
}
