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
class DeclararNoShowUseCaseTest {
    @Mock ReservaRepository reservas;
    @Mock FolioRepository folios;
    @Mock PoliticaCancelacionRepository politicas;
    Reserva reserva;

    @BeforeEach void preparar() { reserva = ReservasDePrueba.confirmada(); }
    private DeclararNoShowUseCase caso(LocalTime hora) {
        return new DeclararNoShowUseCase(reservas, folios, new RetencionCancelacionService(politicas),
                ParametrosDePrueba.sieteRaices(), reloj(ReservasDePrueba.LLEGADA.toLocalDate().atTime(hora)));
    }
    private Folio cargar() {
        Folio cuenta = folio(reserva);
        when(reservas.obtenerPorCodigo(reserva.getCodigo())).thenReturn(Optional.of(reserva));
        when(folios.obtenerPorReserva(reserva.getCodigo())).thenReturn(Optional.of(cuenta));
        when(politicas.obtenerPorVersion(reserva.getPolitica()))
                .thenReturn(Optional.of(ParametrosDePrueba.politicaInicialSieteRaices()));
        return cuenta;
    }
    @Test void desdeLasVeinteLiquidaElCincuentaPorCientoYGuardaAmbosAgregados() {
        Folio cuenta = cargar();
        caso(LocalTime.of(20, 0)).ejecutar(reserva.getCodigo(), EscenarioReservas.RECEPCION);
        assertEquals(EstadoReserva.NO_SHOW, reserva.getEstado());
        assertEquals(Dinero.de(100_000), cuenta.saldo());
        verify(reservas).guardar(reserva);
        verify(folios).guardar(cuenta);
        verify(politicas, never()).obtenerVigente();
    }
    @Test void antesDeLasVeinteNoMutaNiGuarda() {
        Folio cuenta = cargar();
        assertThrows(ReglaDominioException.class,
                () -> caso(LocalTime.of(19, 59, 59)).ejecutar(reserva.getCodigo(), EscenarioReservas.RECEPCION));
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
        assertEquals(1, cuenta.obtenerCargos().size());
        verify(reservas, never()).guardar(any());
        verify(folios, never()).guardar(any());
    }
    @Test void novedadPendienteImpideNoShowSinAlterarElFolio() {
        Folio cuenta = cargar();
        reserva.informarNovedadLlegada("Vuelo retrasado", "recepcion", ReservasDePrueba.LLEGADA);
        assertThrows(ReglaDominioException.class,
                () -> caso(LocalTime.of(20, 0)).ejecutar(reserva.getCodigo(), EscenarioReservas.RECEPCION));
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
        assertEquals(1, cuenta.obtenerCargos().size());
        verify(reservas, never()).guardar(any());
        verify(folios, never()).guardar(any());
    }
    @Test void reservaInexistenteNoGuardaNada() {
        assertThrows(ReservaNoEncontradaException.class,
                () -> caso(LocalTime.of(20, 0)).ejecutar(reserva.getCodigo(), EscenarioReservas.RECEPCION));
        verifyNoInteractions(folios, politicas);
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
