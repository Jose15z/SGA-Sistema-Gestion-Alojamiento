package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.EscenarioReservas;
import co.edu.uniquindio.sga.ParametrosDePrueba;
import co.edu.uniquindio.sga.ReservasDePrueba;
import co.edu.uniquindio.sga.application.exception.ReservaNoEncontradaException;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Pago;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.service.AnticipoReservaService;
import co.edu.uniquindio.sga.domain.valueobject.Cargo;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.MedioPago;
import co.edu.uniquindio.sga.domain.valueobject.TipoCargo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Guía 09, Parte 5: la orquestación, sin Spring ni base de datos. */
@ExtendWith(MockitoExtension.class)
class ConfirmarReservaUseCaseTest {

    @Mock private ReservaRepository reservas;
    @Mock private FolioRepository folios;

    private ConfirmarReservaUseCase useCase;

    @BeforeEach
    void preparar() {
        var config = ParametrosDePrueba.sieteRaices();
        var reloj = java.time.Clock.fixed(ReservasDePrueba.CREACION.atZone(ZoneId.of("America/Bogota")).toInstant(),
                ZoneId.of("America/Bogota"));
        useCase = new ConfirmarReservaUseCase(reservas, folios, new AnticipoReservaService(config), config, reloj);
    }

    private Folio folioConAnticipo(Reserva reserva, long pagado) {
        Folio folio = new Folio(reserva.getCodigo(), new Cargo(TipoCargo.ALOJAMIENTO, "Alojamiento",
                reserva.getValor().total(), ReservasDePrueba.CREACION, "recepcion"));
        if (pagado > 0) {
            folio.registrarPago(new Pago(UUID.randomUUID(), Dinero.de(pagado), MedioPago.EFECTIVO,
                    ReservasDePrueba.CREACION, "Anticipo", "recepcion"));
        }
        return folio;
    }

    @Test
    void deberiaConfirmarYGuardarLaReserva() {
        Reserva reserva = ReservasDePrueba.pendiente();
        reserva.indicarHoraEstimadaLlegada(java.time.LocalTime.of(15, 0), "recepcion", ReservasDePrueba.CREACION);
        when(reservas.obtenerPorCodigo(reserva.getCodigo())).thenReturn(Optional.of(reserva));
        when(folios.obtenerPorReserva(reserva.getCodigo())).thenReturn(Optional.of(folioConAnticipo(reserva, 60_000)));

        Reserva resultado = useCase.ejecutar(reserva.getCodigo(), EscenarioReservas.RECEPCION);

        assertEquals(EstadoReserva.CONFIRMADA, resultado.getEstado());
        verify(reservas).guardar(reserva);
    }

    @Test
    void deberiaLanzarExcepcionCuandoLaReservaNoExiste() {
        CodigoReserva codigo = new CodigoReserva("RES-2026-99999");
        when(reservas.obtenerPorCodigo(codigo)).thenReturn(Optional.empty());

        assertThrows(ReservaNoEncontradaException.class, () -> useCase.ejecutar(codigo, EscenarioReservas.RECEPCION));
        verify(reservas, never()).guardar(any());
    }

    @Test
    void noDeberiaGuardarCuandoElDominioRechazaLaOperacion() {
        Reserva reserva = ReservasDePrueba.pendiente();
        when(reservas.obtenerPorCodigo(reserva.getCodigo())).thenReturn(Optional.of(reserva));
        when(folios.obtenerPorReserva(reserva.getCodigo())).thenReturn(Optional.of(folioConAnticipo(reserva, 0)));

        assertThrows(ReglaDominioException.class, () -> useCase.ejecutar(reserva.getCodigo(), EscenarioReservas.RECEPCION));
        verify(reservas, never()).guardar(any());
    }
}
