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
class RegistrarSalidaUseCaseTest {
    @Mock ReservaRepository reservas;
    @Mock ApartamentoRepository apartamentos;
    @Mock FolioRepository folios;
    RegistrarSalidaUseCase caso;
    Reserva reserva;
    Apartamento apartamento;

    @BeforeEach void preparar() {
        reserva = ReservasDePrueba.confirmada();
        reserva.registrarLlegada("recepcion", ReservasDePrueba.LLEGADA);
        apartamento = EscenarioReservas.apartamento("APT-301", "Orquídea", 2, 4);
        apartamento.marcarOcupado();
        caso = new RegistrarSalidaUseCase(reservas, apartamentos, folios, new SalidaApartamentoService(),
                reloj(ReservasDePrueba.LLEGADA.plusDays(2)));
    }
    private void cargar(Folio cuenta) {
        when(reservas.obtenerPorCodigo(reserva.getCodigo())).thenReturn(Optional.of(reserva));
        when(folios.obtenerPorReserva(reserva.getCodigo())).thenReturn(Optional.of(cuenta));
        when(apartamentos.obtenerPorIdentificacion(reserva.getApartamento())).thenReturn(Optional.of(apartamento));
    }
    @Test void finalizaReservaYDejaApartamentoPendienteDePreparacion() {
        Folio cuenta = folio(reserva);
        cuenta.registrarPago(new Pago(UUID.randomUUID(), reserva.getValor().total(), MedioPago.EFECTIVO,
                ReservasDePrueba.CREACION, "Pago total", "recepcion"));
        cuenta.cerrar();
        cargar(cuenta);
        caso.ejecutar(reserva.getCodigo(), EscenarioReservas.RECEPCION);
        assertEquals(EstadoReserva.FINALIZADA, reserva.getEstado());
        assertEquals(EstadoOperativo.PENDIENTE_PREPARACION, apartamento.getEstadoOperativo());
        verify(reservas).guardar(reserva);
        verify(apartamentos).guardar(apartamento);
        verify(folios, never()).guardar(any());
    }
    @Test void folioAbiertoImpideAmbosCambios() {
        cargar(folio(reserva));
        assertThrows(ReglaDominioException.class, () -> caso.ejecutar(reserva.getCodigo(), EscenarioReservas.RECEPCION));
        assertEquals(EstadoReserva.EN_CURSO, reserva.getEstado());
        assertEquals(EstadoOperativo.OCUPADO, apartamento.getEstadoOperativo());
        verify(reservas, never()).guardar(any());
        verify(apartamentos, never()).guardar(any());
    }
    @Test void reservaInexistenteNoGuardaNada() {
        assertThrows(ReservaNoEncontradaException.class,
                () -> caso.ejecutar(reserva.getCodigo(), EscenarioReservas.RECEPCION));
        verifyNoInteractions(apartamentos, folios);
        verify(reservas, never()).guardar(any());
    }
    @Test void folioInexistenteNoFinalizaLaReserva() {
        when(reservas.obtenerPorCodigo(reserva.getCodigo())).thenReturn(Optional.of(reserva));
        assertThrows(FolioNoEncontradoException.class,
                () -> caso.ejecutar(reserva.getCodigo(), EscenarioReservas.RECEPCION));
        assertEquals(EstadoReserva.EN_CURSO, reserva.getEstado());
        verify(reservas, never()).guardar(any());
        verifyNoInteractions(apartamentos);
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
