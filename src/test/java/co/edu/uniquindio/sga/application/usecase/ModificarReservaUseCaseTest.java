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
class ModificarReservaUseCaseTest {
    @Mock ReservaRepository reservas;
    @Mock ApartamentoRepository apartamentos;
    @Mock FolioRepository folios;
    @Mock ModificacionReservaService modificacion;
    ModificarReservaUseCase caso;
    Reserva reserva;
    Apartamento apartamento;

    @BeforeEach void preparar() {
        reserva = ReservasDePrueba.pendiente();
        apartamento = EscenarioReservas.apartamento("APT-301", "Orquídea", 2, 4);
        caso = new ModificarReservaUseCase(reservas, apartamentos, folios, modificacion,
                ParametrosDePrueba.sieteRaices(), reloj(ReservasDePrueba.CREACION));
    }
    private Folio cargar() {
        Folio cuenta = folio(reserva);
        when(reservas.obtenerPorCodigo(reserva.getCodigo())).thenReturn(Optional.of(reserva));
        when(apartamentos.obtenerPorIdentificacion(reserva.getApartamento())).thenReturn(Optional.of(apartamento));
        when(folios.obtenerPorReserva(reserva.getCodigo())).thenReturn(Optional.of(cuenta));
        return cuenta;
    }
    @Test void modificaFechasYRegistraSoloLaDiferenciaConservandoLaPolitica() {
        Folio cuenta = cargar();
        Estancia nueva = new Estancia(reserva.getEstancia().fechaEntrada(), reserva.getEstancia().fechaSalida().plusDays(1));
        ValorCongelado valor = new ValorCongelado(nueva.nochesOcupadas().stream()
                .map(n -> new CargoNoche(n, TipoTemporada.BASE, Dinero.de(100_000), 1)).toList());
        when(modificacion.validarModificacion(reserva, apartamento, nueva, reserva.getOcupantes(),
                ReservasDePrueba.CREACION.toLocalDate())).thenReturn(valor);

        caso.ejecutar(reserva.getCodigo(), nueva, EscenarioReservas.RECEPCION);

        assertEquals(nueva, reserva.getEstancia());
        assertEquals(new VersionPolitica(1), reserva.getPolitica());
        assertEquals(Dinero.de(300_000), cuenta.saldo());
        assertEquals(2, cuenta.obtenerCargos().size());
        assertEquals(Dinero.de(100_000), cuenta.obtenerCargos().getLast().valor());
        verify(reservas).guardar(reserva);
        verify(folios).guardar(cuenta);
    }
    @Test void valorSinDiferenciaNoAgregaCargo() {
        Folio cuenta = cargar();
        when(modificacion.validarModificacion(reserva, apartamento, reserva.getEstancia(), reserva.getOcupantes(),
                ReservasDePrueba.CREACION.toLocalDate())).thenReturn(reserva.getValor());
        caso.ejecutar(reserva.getCodigo(), null, EscenarioReservas.RECEPCION);
        assertEquals(1, cuenta.obtenerCargos().size());
        verify(reservas).guardar(reserva);
        verify(folios).guardar(cuenta);
    }
    @Test void rechazoDelServicioNoMutaNiGuardaAgregados() {
        Folio cuenta = cargar();
        var historial = reserva.getHistorial();
        when(modificacion.validarModificacion(reserva, apartamento, reserva.getEstancia(), reserva.getOcupantes(),
                ReservasDePrueba.CREACION.toLocalDate())).thenThrow(new ReglaDominioException("Sin disponibilidad"));
        assertThrows(ReglaDominioException.class,
                () -> caso.ejecutar(reserva.getCodigo(), null, EscenarioReservas.RECEPCION));
        assertEquals(historial, reserva.getHistorial());
        assertEquals(1, cuenta.obtenerCargos().size());
        verify(reservas, never()).guardar(any());
        verify(folios, never()).guardar(any());
    }
    @Test void reservaInexistenteNoConsultaOtrosAgregados() {
        assertThrows(ReservaNoEncontradaException.class,
                () -> caso.ejecutar(reserva.getCodigo(), null, EscenarioReservas.RECEPCION));
        verifyNoInteractions(apartamentos, folios, modificacion);
        verify(reservas, never()).guardar(any());
    }
    @Test void otroHuespedNoPuedeModificarLaReserva() {
        when(reservas.obtenerPorCodigo(reserva.getCodigo())).thenReturn(Optional.of(reserva));
        var otro = new Solicitante("otro@correo.com", Rol.HUESPED, new DocumentoIdentidad("CC-999"));
        assertThrows(AccesoDenegadoException.class, () -> caso.ejecutar(reserva.getCodigo(), null, otro));
        verifyNoInteractions(apartamentos, folios, modificacion);
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
