package co.edu.uniquindio.sga.domain.entity;
import co.edu.uniquindio.sga.ReservasDePrueba;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.*;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;

class ReservaOperacionesAdicionalesTest {
    ValorCongelado cotizar(Estancia estancia,long tarifa) {
        return new ValorCongelado(estancia.nochesOcupadas().stream()
                .map(n -> new CargoNoche(n,"Base",Dinero.de(tarifa),1)).toList());
    }
    @Test void modificacionConservaPoliticaYCalculaAjusteNegativo() {
        Reserva r=ReservasDePrueba.pendiente();Estancia nueva=new Estancia(LocalDate.of(2026,12,11),LocalDate.of(2026,12,12));
        Dinero ajuste=r.modificarEstancia(nueva,cotizar(nueva,80_000),new UmbralEdadFacturable(10),"recepcion",ReservasDePrueba.CREACION);
        assertEquals(Dinero.de(-120_000),ajuste);
        assertEquals(new VersionPolitica(1),r.getPolitica());assertEquals(nueva,r.getEstancia());
        assertEquals("MODIFICACION",r.getHistorial().getLast().accion());
    }
    @Test void modificacionConAutorInvalidoNoMutaEstanciaNiValor() {
        Reserva r=ReservasDePrueba.pendiente();var original=r.getEstancia();var valor=r.getValor();
        Estancia nueva=new Estancia(LocalDate.of(2026,12,11),LocalDate.of(2026,12,12));
        assertThrows(ReglaDominioException.class,()->r.modificarEstancia(nueva,cotizar(nueva,80_000),
                new UmbralEdadFacturable(10)," ",ReservasDePrueba.CREACION));
        assertEquals(original,r.getEstancia());assertSame(valor,r.getValor());
    }
    @Test void modificacionRechazaDesgloseDeOtrasFechasAunqueTengaIgualNumeroDeNoches() {
        Reserva r=ReservasDePrueba.pendiente();Estancia nueva=new Estancia(LocalDate.of(2026,12,11),LocalDate.of(2026,12,13));
        assertThrows(ReglaDominioException.class,()->r.modificarEstancia(nueva,r.getValor(),new UmbralEdadFacturable(10),
                "recepcion",ReservasDePrueba.CREACION));
    }
    @Test void modificacionRechazaReservaYaIniciada() {
        Reserva r=ReservasDePrueba.confirmada();r.registrarLlegada("recepcion",ReservasDePrueba.LLEGADA);
        assertThrows(ReglaDominioException.class,()->r.modificarEstancia(r.getEstancia(),r.getValor(),new UmbralEdadFacturable(10),
                "recepcion",ReservasDePrueba.LLEGADA));
    }
    @Test void noShowNoAdmitePendienteNiRetrocesoCronologico() {
        Reserva r=ReservasDePrueba.pendiente();
        assertThrows(ReglaDominioException.class,()->r.declararNoShow(LocalTime.of(20,0),"recepcion",ReservasDePrueba.LLEGADA.plusDays(1)));
        Reserva confirmada=ReservasDePrueba.confirmada();
        confirmada.indicarHoraEstimadaLlegada(LocalTime.of(21,0),"recepcion",ReservasDePrueba.LLEGADA.withHour(21));
        assertThrows(ReglaDominioException.class,()->confirmada.declararNoShow(LocalTime.of(20,0),"recepcion",ReservasDePrueba.LLEGADA.withHour(20)));
        assertEquals(EstadoReserva.CONFIRMADA,confirmada.getEstado());
    }
    @Test void salidaConAutorInvalidoNoFinaliza() {
        Reserva r=ReservasDePrueba.confirmada();r.registrarLlegada("recepcion",ReservasDePrueba.LLEGADA);
        assertThrows(ReglaDominioException.class,()->r.registrarSalida(" ",ReservasDePrueba.LLEGADA));
        assertEquals(EstadoReserva.EN_CURSO,r.getEstado());
    }
    @Test void confirmacionExactaAlLimiteEsValidaPeroUnNanosegundoDespuesNo() {
        Reserva a=ReservasDePrueba.pendiente();a.indicarHoraEstimadaLlegada(LocalTime.NOON,"recepcion",ReservasDePrueba.CREACION);
        assertDoesNotThrow(()->a.confirmar("recepcion",ReservasDePrueba.CREACION.plusHours(24),new PlazoConfirmacion(24)));
        Reserva b=ReservasDePrueba.pendiente();b.indicarHoraEstimadaLlegada(LocalTime.NOON,"recepcion",ReservasDePrueba.CREACION);
        assertThrows(ReglaDominioException.class,()->b.confirmar("recepcion",ReservasDePrueba.CREACION.plusHours(24).plusNanos(1),new PlazoConfirmacion(24)));
        assertEquals(EstadoReserva.PENDIENTE,b.getEstado());
    }
}
