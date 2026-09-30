package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.*;
import co.edu.uniquindio.sga.application.exception.*;
import co.edu.uniquindio.sga.domain.entity.*;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.service.*;
import co.edu.uniquindio.sga.domain.valueobject.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class ReservasUseCasesTest {
    EscenarioReservas e;
    @BeforeEach void preparar() { e = new EscenarioReservas(); }
    @Test void creaPendienteConFolioCotizacionYPolitica() {
        Reserva r = e.crear();
        assertEquals(EstadoReserva.PENDIENTE, r.getEstado());
        assertEquals(Dinero.de(200_000), r.getValor().total());
        assertEquals(r.getValor().total(), e.folio(r).saldo());
        assertEquals(new VersionPolitica(1), r.getPolitica());
        assertEquals(ReservasDePrueba.CREACION, r.getFechaCreacion().momento());
        assertEquals(r, e.reservas.obtenerPorCodigo(r.getCodigo()).orElseThrow());
    }
    @Test void aplicaDescuentoYLoCongela() {
        Estancia estancia = new Estancia(LocalDate.of(2026,12,10), LocalDate.of(2026,12,17));
        Reserva r = e.crear(estancia, "Base");
        assertEquals(Dinero.de(630_000), r.getValor().total());
        assertEquals(Dinero.de(630_000), e.folio(r).saldo());
    }
    @Test void rechazaUnaNocheEspecialSinGuardarNada() {
        Estancia estancia = new Estancia(LocalDate.of(2026,12,10), LocalDate.of(2026,12,11));
        assertThrows(ReglaDominioException.class, () -> e.crear(estancia, "Temporada Especial"));
        assertTrue(e.reservas.buscarPorEstado(EstadoReserva.PENDIENTE).isEmpty());
    }
    @Test void admiteDosNochesEspeciales() { assertNotNull(e.crear(ReservasDePrueba.pendiente().getEstancia(), "Especial")); }
    @Test void rechazaReservaSolapada() {
        e.crear();
        assertThrows(ReglaDominioException.class, () -> e.crear());
        assertEquals(1, e.reservas.buscarPorEstado(EstadoReserva.PENDIENTE).size());
    }
    @Test void rechazaApartamentoInexistente() {
        var titular = ReservasDePrueba.pendiente().getTitular();
        assertThrows(ApartamentoNoEncontradoException.class, () -> e.creador(ReservasDePrueba.CREACION,"Base",100)
                .ejecutar(new IdentificacionApartamento("APT-999"), ReservasDePrueba.pendiente().getEstancia(), titular,
                        List.of(titular), CanalOrigen.DIRECTO, null, null, "recepcion"));
    }
    @Test void rechazaCanalExternoSinReferenciaSinGuardar() {
        var titular = ReservasDePrueba.pendiente().getTitular();
        assertThrows(ReglaDominioException.class, () -> e.creador(ReservasDePrueba.CREACION,"Base",100)
                .ejecutar(e.apartamento.getIdentificacion(), ReservasDePrueba.pendiente().getEstancia(), titular,
                        List.of(titular), CanalOrigen.EXTERNO, null, null, "recepcion"));
        assertTrue(e.reservas.buscarPorEstado(EstadoReserva.PENDIENTE).isEmpty());
    }
    @Test void confirmaConAnticipoExacto() {
        Reserva r = e.crear(); e.pagar(r,60_000); e.confirmar(r);
        assertEquals(EstadoReserva.CONFIRMADA, r.getEstado());
    }
    @ParameterizedTest @ValueSource(longs={0,59_999}) void rechazaAnticipoInsuficienteSinCambios(long pago) {
        Reserva r = e.crear(); if(pago>0) e.pagar(r,pago);
        var historial = r.getHistorial();
        assertThrows(ReglaDominioException.class, () -> e.confirmar(r));
        assertEquals(EstadoReserva.PENDIENTE, r.getEstado());
        assertEquals(historial, r.getHistorial());
    }
    @Test void confirmarSinHoraNoModificaReserva() {
        var titular = ReservasDePrueba.pendiente().getTitular();
        Reserva r = e.creador(ReservasDePrueba.CREACION,"Base",100_000).ejecutar(e.apartamento.getIdentificacion(),
                ReservasDePrueba.pendiente().getEstancia(),titular,List.of(titular),CanalOrigen.DIRECTO,null,null,"recepcion");
        e.pagar(r,60_000);
        assertThrows(ReglaDominioException.class, () -> e.confirmar(r));
        assertEquals(EstadoReserva.PENDIENTE,r.getEstado());
    }
    @Test void confirmarSinFolioFallaExplicitamente() {
        Reserva r = ReservasDePrueba.pendiente(); e.reservas.guardar(r);
        assertThrows(FolioNoEncontradoException.class, () -> e.confirmar(r));
    }
    @Test void confirmarReservaInexistenteFallaExplicitamente() {
        assertThrows(ReservaNoEncontradaException.class, () -> e.confirmar(ReservasDePrueba.pendiente()));
    }
    @Test void cancelaUsandoPoliticaHistoricaYRevierteCargoOriginal() {
        Reserva r = e.crear(); e.pagar(r,80_000);
        e.politicas.publicar(new PoliticaCancelacion(new VersionPolitica(2),
                List.of(new TramoCancelacion(Duration.ZERO,BigDecimal.valueOf(100))),BigDecimal.valueOf(100),LocalTime.NOON));
        cancelar(r, LocalDateTime.of(2026,12,7,15,0), "Cambio de planes", "recepcion");
        assertEquals(EstadoReserva.CANCELADA,r.getEstado());
        assertEquals(new VersionPolitica(1),r.getPolitica());
        assertEquals(Dinero.de(-20_000),e.folio(r).saldo());
        assertEquals(Dinero.de(200_000),e.folio(r).obtenerCargos().getFirst().valor());
        assertEquals(3,e.folio(r).obtenerCargos().size());
        assertTrue(e.reservas.buscarActivasPorApartamento(r.getApartamento()).isEmpty());
    }
    @Test void cancelarDosVecesNoDuplicaMovimientos() {
        Reserva r=e.crear(); cancelar(r,ReservasDePrueba.CREACION,"Cambio","recepcion");
        assertThrows(ReglaDominioException.class,()->cancelar(r,ReservasDePrueba.CREACION,"Cambio","recepcion"));
        assertEquals(3,e.folio(r).obtenerCargos().size());
    }
    @Test void cancelarConMotivoInvalidoNoMutaFolio() {
        Reserva r=e.crear();
        assertThrows(ReglaDominioException.class,()->cancelar(r,ReservasDePrueba.CREACION," ","recepcion"));
        assertEquals(1,e.folio(r).obtenerCargos().size());
        assertEquals(EstadoReserva.PENDIENTE,r.getEstado());
    }
    @Test void cancelarConFolioCerradoNoMutaReserva() {
        Reserva r=e.crear();e.folio(r).cerrar("Administrador autoriza saldo");
        assertThrows(ReglaDominioException.class,()->cancelar(r,ReservasDePrueba.CREACION,"Cambio","recepcion"));
        assertEquals(EstadoReserva.PENDIENTE,r.getEstado());
    }
    @Test void llegadaYSalidaCoordinanApartamentoYReserva() {
        Reserva r=e.crear();e.pagar(r,200_000);e.confirmar(r);
        llegada(r,"recepcion");
        assertEquals(EstadoReserva.EN_CURSO,r.getEstado());
        assertEquals(EstadoOperativo.OCUPADO,e.apartamento.getEstadoOperativo());
        e.folio(r).cerrar(null);
        salida(r);
        assertEquals(EstadoReserva.FINALIZADA,r.getEstado());
        assertEquals(EstadoOperativo.PENDIENTE_PREPARACION,e.apartamento.getEstadoOperativo());
    }
    @Test void llegadaSinApartamentoPreparadoNoCambiaReserva() {
        Reserva r=e.crear();e.pagar(r,60_000);e.confirmar(r);e.apartamento.declararFueraDeServicio();
        assertThrows(ReglaDominioException.class,()->llegada(r,"recepcion"));
        assertEquals(EstadoReserva.CONFIRMADA,r.getEstado());
    }
    @Test void llegadaConAutorInvalidoNoOcupaApartamento() {
        Reserva r=e.crear();e.pagar(r,60_000);e.confirmar(r);
        assertThrows(ReglaDominioException.class,()->llegada(r," "));
        assertEquals(EstadoOperativo.PREPARADO,e.apartamento.getEstadoOperativo());
    }
    @Test void salidaConFolioAbiertoNoLiberaApartamento() {
        Reserva r=e.crear();e.pagar(r,60_000);e.confirmar(r);llegada(r,"recepcion");
        assertThrows(ReglaDominioException.class,()->salida(r));
        assertEquals(EstadoReserva.EN_CURSO,r.getEstado());
        assertEquals(EstadoOperativo.OCUPADO,e.apartamento.getEstadoOperativo());
    }
    @Test void noShowExactamenteALasVeinteAplicaCincuentaPorCiento() {
        Reserva r=e.crear();e.pagar(r,60_000);e.confirmar(r);
        noShow(r,LocalTime.of(20,0));
        assertEquals(EstadoReserva.NO_SHOW,r.getEstado());
        assertEquals(Dinero.de(40_000),e.folio(r).saldo());
        assertTrue(e.reservas.buscarActivasPorApartamento(r.getApartamento()).isEmpty());
    }
    @Test void noShowAntesDeLasVeinteNoMutaNada() {
        Reserva r=e.crear();e.pagar(r,60_000);e.confirmar(r);
        assertThrows(ReglaDominioException.class,()->noShow(r,LocalTime.of(19,59,59)));
        assertEquals(EstadoReserva.CONFIRMADA,r.getEstado());
        assertEquals(1,e.folio(r).obtenerCargos().size());
    }
    @Test void noShowConNovedadDeLlegadaDebeEsperarResolucion() {
        Reserva r=e.crear();e.pagar(r,60_000);e.confirmar(r);
        r.informarNovedadLlegada("Retraso de vuelo","recepcion",ReservasDePrueba.LLEGADA);
        assertThrows(ReglaDominioException.class,()->noShow(r,LocalTime.of(20,0)));
        assertEquals(1,e.folio(r).obtenerCargos().size());
        r.resolverNovedadLlegada("No llegará","recepcion",ReservasDePrueba.LLEGADA);
        noShow(r,LocalTime.of(20,0));
        assertEquals(EstadoReserva.NO_SHOW,r.getEstado());
    }
    @Test void venceSoloDespuesDelLimiteYEsIdempotente() {
        Reserva r=e.crear();
        assertEquals(0,vencer(ReservasDePrueba.CREACION.plusHours(24)));
        assertEquals(1,vencer(ReservasDePrueba.CREACION.plusHours(24).plusNanos(1)));
        assertEquals(0,vencer(ReservasDePrueba.CREACION.plusHours(25)));
        assertEquals(EstadoReserva.CANCELADA,r.getEstado());
        assertEquals(Dinero.CERO,e.folio(r).saldo());
        assertEquals(3,e.folio(r).obtenerCargos().size());
    }
    @Test void vencimientoNoAfectaConfirmadas() {
        Reserva r=e.crear();e.pagar(r,60_000);e.confirmar(r);
        assertEquals(0,vencer(ReservasDePrueba.CREACION.plusDays(2)));
        assertEquals(EstadoReserva.CONFIRMADA,r.getEstado());
    }
    @Test void consultaEstadosSinMezclarlos() {
        Reserva r=e.crear(); var consulta=new ConsultarReservasPorEstadoUseCase(e.reservas);
        assertEquals(List.of(r),consulta.ejecutar(EstadoReserva.PENDIENTE));
        assertTrue(consulta.ejecutar(EstadoReserva.CANCELADA).isEmpty());
        assertThrows(UnsupportedOperationException.class,()->consulta.ejecutar(EstadoReserva.PENDIENTE).clear());
    }
    void cancelar(Reserva r,LocalDateTime momento,String motivo,String autor) {
        new CancelarReservaUseCase(e.reservas,e.folios,new RetencionCancelacionService(e.politicas),e.reloj(momento))
                .ejecutar(r.getCodigo(),motivo,autor);
    }
    void llegada(Reserva r,String autor) {
        new RegistrarLlegadaUseCase(e.reservas,e.apartamentos,new EntregaApartamentoService(e.apartamentos),
                e.reloj(ReservasDePrueba.LLEGADA)).ejecutar(r.getCodigo(),autor);
    }
    void salida(Reserva r) {
        new RegistrarSalidaUseCase(e.reservas,e.apartamentos,e.folios,new SalidaApartamentoService(),
                e.reloj(ReservasDePrueba.LLEGADA.plusDays(2))).ejecutar(r.getCodigo(),"recepcion");
    }
    void noShow(Reserva r,LocalTime hora) {
        new DeclararNoShowUseCase(e.reservas,e.folios,new RetencionCancelacionService(e.politicas),e.config,
                e.reloj(ReservasDePrueba.LLEGADA.toLocalDate().atTime(hora))).ejecutar(r.getCodigo(),"recepcion");
    }
    int vencer(LocalDateTime momento) {
        return new VencerReservasPendientesUseCase(e.reservas,e.folios,e.config,e.reloj(momento)).ejecutar();
    }
}
