package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.EscenarioReservas;
import co.edu.uniquindio.sga.ReservasDePrueba;
import co.edu.uniquindio.sga.application.exception.ReservaNoEncontradaException;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.service.EntregaApartamentoService;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Guía 09, Ejercicio 3: la llegada cambia dos agregados o ninguno. */
@ExtendWith(MockitoExtension.class)
class RegistrarLlegadaUseCaseTest {

    @Mock private ReservaRepository reservas;
    @Mock private ApartamentoRepository apartamentos;

    private RegistrarLlegadaUseCase useCase;

    @BeforeEach
    void preparar() {
        ZoneId zona = ZoneId.of("America/Bogota");
        Clock reloj = Clock.fixed(ReservasDePrueba.LLEGADA.atZone(zona).toInstant(), zona);
        useCase = new RegistrarLlegadaUseCase(reservas, apartamentos, new EntregaApartamentoService(apartamentos), reloj);
    }

    @Test
    void registraLaLlegadaYOcupaElApartamento() {
        Reserva reserva = ReservasDePrueba.confirmada();
        Apartamento apartamento = EscenarioReservas.apartamento("APT-301", "Orquídea", 2, 4);
        when(reservas.obtenerPorCodigo(reserva.getCodigo())).thenReturn(Optional.of(reserva));
        when(apartamentos.obtenerPorIdentificacion(reserva.getApartamento())).thenReturn(Optional.of(apartamento));

        useCase.ejecutar(reserva.getCodigo(), EscenarioReservas.RECEPCION);

        assertEquals(EstadoReserva.EN_CURSO, reserva.getEstado());
        assertEquals(EstadoOperativo.OCUPADO, apartamento.getEstadoOperativo());
        verify(reservas).guardar(reserva);
        verify(apartamentos).guardar(apartamento);
    }

    @Test
    void noGuardaNadaSiLaReservaNoExiste() {
        CodigoReserva codigo = new CodigoReserva("RES-2026-99999");
        when(reservas.obtenerPorCodigo(codigo)).thenReturn(Optional.empty());

        assertThrows(ReservaNoEncontradaException.class, () -> useCase.ejecutar(codigo, EscenarioReservas.RECEPCION));
        verify(reservas, never()).guardar(any());
        verify(apartamentos, never()).guardar(any());
    }

    @Test
    void noGuardaNingunAgregadoSiElApartamentoNoEstaPreparado() {
        Reserva reserva = ReservasDePrueba.confirmada();
        Apartamento apartamento = EscenarioReservas.apartamento("APT-301", "Orquídea", 2, 4);
        apartamento.declararFueraDeServicio();
        when(reservas.obtenerPorCodigo(reserva.getCodigo())).thenReturn(Optional.of(reserva));
        when(apartamentos.obtenerPorIdentificacion(reserva.getApartamento())).thenReturn(Optional.of(apartamento));

        assertThrows(ReglaDominioException.class, () -> useCase.ejecutar(reserva.getCodigo(), EscenarioReservas.RECEPCION));
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
        verify(reservas, never()).guardar(any());
        verify(apartamentos, never()).guardar(any());
    }
}
