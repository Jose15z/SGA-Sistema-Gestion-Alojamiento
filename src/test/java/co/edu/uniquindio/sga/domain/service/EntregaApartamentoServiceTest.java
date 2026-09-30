package co.edu.uniquindio.sga.domain.service;
import co.edu.uniquindio.sga.*;
import co.edu.uniquindio.sga.domain.entity.*;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class EntregaApartamentoServiceTest {
    @Test void entregaNoModificaAgregados() {
        EscenarioReservas e=new EscenarioReservas();Reserva r=ReservasDePrueba.confirmada();
        Apartamento autorizado=new EntregaApartamentoService(e.apartamentos).autorizarEntrega(r,ReservasDePrueba.LLEGADA.toLocalDate());
        assertSame(e.apartamento,autorizado);
        assertEquals(EstadoOperativo.PREPARADO,autorizado.getEstadoOperativo());
        assertEquals(EstadoReserva.CONFIRMADA,r.getEstado());
    }
    @Test void rechazaAntesDeLaEntrada() {
        EscenarioReservas e=new EscenarioReservas();
        assertThrows(ReglaDominioException.class,()->new EntregaApartamentoService(e.apartamentos)
                .autorizarEntrega(ReservasDePrueba.confirmada(),ReservasDePrueba.LLEGADA.toLocalDate().minusDays(1)));
    }
    @Test void rechazaReservaPendiente() {
        EscenarioReservas e=new EscenarioReservas();
        assertThrows(ReglaDominioException.class,()->new EntregaApartamentoService(e.apartamentos)
                .autorizarEntrega(ReservasDePrueba.pendiente(),ReservasDePrueba.LLEGADA.toLocalDate()));
    }
    @Test void rechazaApartamentoInactivoAunqueEstePreparado() {
        EscenarioReservas e=new EscenarioReservas();e.apartamento.desactivar();
        assertThrows(ReglaDominioException.class,()->new EntregaApartamentoService(e.apartamentos)
                .autorizarEntrega(ReservasDePrueba.confirmada(),ReservasDePrueba.LLEGADA.toLocalDate()));
    }
}
