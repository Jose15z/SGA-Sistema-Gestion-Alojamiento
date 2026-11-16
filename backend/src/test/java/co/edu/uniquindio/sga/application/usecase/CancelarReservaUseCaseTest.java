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
class CancelarReservaUseCaseTest {
    @Mock ReservaRepository reservas;
    @Mock FolioRepository folios;
    @Mock PoliticaCancelacionRepository politicas;
    CancelarReservaUseCase caso;
    Reserva reserva;

    @BeforeEach void preparar() {
        reserva = ReservasDePrueba.pendiente();
        caso = new CancelarReservaUseCase(reservas, folios, new RetencionCancelacionService(politicas),
                reloj(ReservasDePrueba.CREACION));
    }
    private void cargar(Folio cuenta) {
        when(reservas.obtenerPorCodigo(reserva.getCodigo())).thenReturn(Optional.of(reserva));
        when(folios.obtenerPorReserva(reserva.getCodigo())).thenReturn(Optional.of(cuenta));
        when(politicas.obtenerPorVersion(reserva.getPolitica()))
                .thenReturn(Optional.of(ParametrosDePrueba.politicaInicialSieteRaices()));
    }
    @Test void cancelaYLiquidaSegunLaPoliticaCongelada() {
        Folio cuenta = folio(reserva);
        cargar(cuenta);
        assertSame(reserva, caso.ejecutar(reserva.getCodigo(), "Cambio de planes", EscenarioReservas.RECEPCION));
        assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());
        assertEquals(Dinero.CERO, cuenta.saldo());
        verify(politicas).obtenerPorVersion(new VersionPolitica(1));
        verify(politicas, never()).obtenerVigente();
        verify(reservas).guardar(reserva);
        verify(folios).guardar(cuenta);
    }
    @Test void reservaInexistenteNoGuardaNada() {
        assertThrows(ReservaNoEncontradaException.class,
                () -> caso.ejecutar(reserva.getCodigo(), "Cambio", EscenarioReservas.RECEPCION));
        verify(reservas, never()).guardar(any());
        verifyNoInteractions(folios, politicas);
    }
    @Test void otroHuespedNoPuedeCancelarLaReserva() {
        when(reservas.obtenerPorCodigo(reserva.getCodigo())).thenReturn(Optional.of(reserva));
        var otro = new Solicitante("otro@correo.com", Rol.HUESPED, new DocumentoIdentidad("CC-999"));
        assertThrows(AccesoDenegadoException.class, () -> caso.ejecutar(reserva.getCodigo(), "Cambio", otro));
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
        verifyNoInteractions(folios, politicas);
        verify(reservas, never()).guardar(any());
    }
    @Test void motivoInvalidoNoAlteraElFolioNiGuarda() {
        Folio cuenta = folio(reserva);
        cargar(cuenta);
        var cargos = cuenta.obtenerCargos();
        assertThrows(ReglaDominioException.class,
                () -> caso.ejecutar(reserva.getCodigo(), " ", EscenarioReservas.RECEPCION));
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
        assertEquals(cargos, cuenta.obtenerCargos());
        verify(reservas, never()).guardar(any());
        verify(folios, never()).guardar(any());
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
