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
class CrearReservaUseCaseTest {
    @Mock ReservaRepository reservas;
    @Mock ApartamentoRepository apartamentos;
    @Mock FolioRepository folios;
    @Mock GeneradorCodigoReserva codigos;
    @Mock PoliticaCancelacionRepository politicas;
    @Mock DisponibilidadApartamentoService disponibilidad;
    @Mock CotizadorEstanciaService cotizador;
    @Mock EstanciaMinimaTemporadaService estanciaMinima;
    @Mock ConciliacionCanalExternoService conciliacion;
    CrearReservaUseCase caso;
    Reserva datos;
    Apartamento apartamento;

    @BeforeEach void preparar() {
        datos = ReservasDePrueba.pendiente();
        apartamento = EscenarioReservas.apartamento("APT-301", "Orquídea", 2, 4);
        caso = new CrearReservaUseCase(reservas, apartamentos, folios, codigos, politicas,
                ParametrosDePrueba.sieteRaices(), disponibilidad, cotizador, estanciaMinima,
                conciliacion, reloj(ReservasDePrueba.CREACION));
    }
    private Reserva crear(Solicitante solicitante) {
        return caso.ejecutar(datos.getApartamento(), datos.getEstancia(), datos.getTitular(), datos.getOcupantes(),
                CanalOrigen.DIRECTO, null, LocalTime.of(15, 0), 1, solicitante);
    }
    @Test void creaReservaYFolioConElValorYLaPoliticaConsultados() {
        when(apartamentos.obtenerPorIdentificacion(datos.getApartamento())).thenReturn(Optional.of(apartamento));
        when(cotizador.cotizar(datos.getApartamento(), datos.getEstancia(), datos.getOcupantes())).thenReturn(datos.getValor());
        when(politicas.obtenerVigente()).thenReturn(ParametrosDePrueba.politicaInicialSieteRaices());
        when(codigos.generar(new FechaCreacion(ReservasDePrueba.CREACION))).thenReturn(datos.getCodigo());

        Reserva resultado = crear(EscenarioReservas.RECEPCION);

        assertEquals(EstadoReserva.PENDIENTE, resultado.getEstado());
        assertEquals(datos.getValor(), resultado.getValor());
        assertEquals(datos.getPolitica(), resultado.getPolitica());
        assertEquals(LocalTime.of(15, 0), resultado.getHoraEstimadaLlegada());
        assertEquals(1, resultado.getMascotas());
        var cuenta = ArgumentCaptor.forClass(Folio.class);
        verify(folios).guardar(cuenta.capture());
        assertEquals(resultado.getCodigo(), cuenta.getValue().getReserva());
        assertEquals(resultado.getValor().total(), cuenta.getValue().saldo());
        verify(reservas).guardar(resultado);
        verify(conciliacion).verificarNoDuplicada(CanalOrigen.DIRECTO, null);
        verify(disponibilidad).verificarDisponible(apartamento, datos.getEstancia(), 1, null,
                ReservasDePrueba.CREACION.toLocalDate());
        verify(estanciaMinima).verificar(datos.getValor());
    }
    @Test void apartamentoInexistenteNoCreaReservaNiFolio() {
        when(apartamentos.obtenerPorIdentificacion(datos.getApartamento())).thenReturn(Optional.empty());
        assertThrows(ApartamentoNoEncontradoException.class, () -> crear(EscenarioReservas.RECEPCION));
        verifyNoInteractions(reservas, folios, codigos, cotizador);
    }
    @Test void huespedNoPuedeCrearPorCanalDirecto() {
        var huesped = new Solicitante("ana@correo.com", Rol.HUESPED, datos.getTitular().getDocumento());
        assertThrows(AccesoDenegadoException.class, () -> crear(huesped));
        verifyNoInteractions(apartamentos, reservas, folios, codigos);
    }
    @Test void rechazoDeDisponibilidadNoGuardaNiCotiza() {
        when(apartamentos.obtenerPorIdentificacion(datos.getApartamento())).thenReturn(Optional.of(apartamento));
        doThrow(new ReglaDominioException("Solapamiento")).when(disponibilidad)
                .verificarDisponible(apartamento, datos.getEstancia(), 1, null, ReservasDePrueba.CREACION.toLocalDate());
        assertThrows(ReglaDominioException.class, () -> crear(EscenarioReservas.RECEPCION));
        verify(reservas, never()).guardar(any());
        verifyNoInteractions(folios, codigos, cotizador);
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
