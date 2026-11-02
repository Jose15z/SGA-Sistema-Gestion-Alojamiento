package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.EscenarioReservas;
import co.edu.uniquindio.sga.ParametrosDePrueba;
import co.edu.uniquindio.sga.ReservasDePrueba;
import co.edu.uniquindio.sga.application.exception.*;
import co.edu.uniquindio.sga.application.seguridad.*;
import co.edu.uniquindio.sga.domain.entity.*;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.*;
import co.edu.uniquindio.sga.domain.service.*;
import co.edu.uniquindio.sga.domain.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Orquestación aislada, con agregados reales y puertos simulados; sin Spring ni H2. */
@ExtendWith(MockitoExtension.class)
class VencerReservasPendientesUseCaseTest {
    @Mock ReservaRepository reservas;
    @Mock FolioRepository folios;
    Reserva reserva;

    @BeforeEach void preparar() { reserva = ReservasDePrueba.pendiente(); }
    private VencerReservasPendientesUseCase caso(LocalDateTime ahora) {
        return new VencerReservasPendientesUseCase(reservas, folios, ParametrosDePrueba.sieteRaices(), reloj(ahora));
    }
    @Test void venceDespuesDelPlazoYLiquidaElFolioSinPenalidad() {
        Folio cuenta = folio(reserva);
        when(reservas.buscarPorEstado(EstadoReserva.PENDIENTE)).thenReturn(List.of(reserva));
        when(folios.obtenerPorReserva(reserva.getCodigo())).thenReturn(Optional.of(cuenta));
        assertEquals(1, caso(ReservasDePrueba.CREACION.plusHours(24).plusNanos(1)).ejecutar());
        assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());
        assertEquals(Dinero.CERO, cuenta.saldo());
        verify(reservas).guardar(reserva);
        verify(folios).guardar(cuenta);
    }
    @Test void exactamenteAlLimiteTodaviaNoVence() {
        when(reservas.buscarPorEstado(EstadoReserva.PENDIENTE)).thenReturn(List.of(reserva));
        assertEquals(0, caso(ReservasDePrueba.CREACION.plusHours(24)).ejecutar());
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
        verifyNoInteractions(folios);
        verify(reservas, never()).guardar(any());
    }
    @Test void segundaEjecucionNoDuplicaLaLiquidacion() {
        Folio cuenta = folio(reserva);
        when(reservas.buscarPorEstado(EstadoReserva.PENDIENTE)).thenAnswer(invocacion ->
                reserva.getEstado() == EstadoReserva.PENDIENTE ? List.of(reserva) : List.of());
        when(folios.obtenerPorReserva(reserva.getCodigo())).thenReturn(Optional.of(cuenta));
        var caso = caso(ReservasDePrueba.CREACION.plusHours(25));
        assertEquals(1, caso.ejecutar());
        var cargos = cuenta.obtenerCargos();
        assertEquals(0, caso.ejecutar());
        assertEquals(cargos, cuenta.obtenerCargos());
        verify(reservas, times(1)).guardar(reserva);
        verify(folios, times(1)).guardar(cuenta);
    }
    @Test void folioFaltanteEnElLoteNoDejaLaPrimeraReservaModificada() {
        Reserva segunda = Reserva.crear(new CodigoReserva("RES-2026-00002"), reserva.getApartamento(),
                reserva.getEstancia(), reserva.getTitular(), reserva.getOcupantes(), CanalOrigen.DIRECTO, null,
                ParametrosDePrueba.sieteRaices().umbralEdadFacturable(), reserva.getValor(), reserva.getPolitica(),
                "recepcion", ReservasDePrueba.CREACION);
        Folio cuenta = folio(reserva);
        when(reservas.buscarPorEstado(EstadoReserva.PENDIENTE)).thenReturn(List.of(reserva, segunda));
        when(folios.obtenerPorReserva(reserva.getCodigo())).thenReturn(Optional.of(cuenta));
        assertThrows(FolioNoEncontradoException.class,
                () -> caso(ReservasDePrueba.CREACION.plusHours(25)).ejecutar());
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
        assertEquals(EstadoReserva.PENDIENTE, segunda.getEstado());
        assertEquals(1, cuenta.obtenerCargos().size());
        verify(reservas, never()).guardar(any());
        verify(folios, never()).guardar(any());
    }
    @Test void sinPendientesNoGuardaNiConsultaFolios() {
        when(reservas.buscarPorEstado(EstadoReserva.PENDIENTE)).thenReturn(List.of());
        assertEquals(0, caso(ReservasDePrueba.CREACION.plusHours(25)).ejecutar());
        verifyNoInteractions(folios);
        verify(reservas, never()).guardar(any());
    }

    private Clock reloj(LocalDateTime momento) {
        ZoneId zona = ZoneId.of("America/Bogota");
        return Clock.fixed(momento.atZone(zona).toInstant(), zona);
    }

    private Folio folio(Reserva reserva) {
        return new Folio(reserva.getCodigo(), new Cargo(TipoCargo.ALOJAMIENTO, "Alojamiento",
                reserva.getValor().total(), ReservasDePrueba.CREACION, "recepcion"));
    }
}
