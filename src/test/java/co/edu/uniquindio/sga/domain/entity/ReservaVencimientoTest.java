package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.CargoNoche;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.EventoReserva;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.PlazoConfirmacion;
import co.edu.uniquindio.sga.domain.valueobject.UmbralEdadFacturable;
import co.edu.uniquindio.sga.domain.valueobject.ValorCongelado;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pruebas del vencimiento de reservas pendientes.
 *
 * <p>El vencimiento exige superar estrictamente el límite.
 * Los rechazos deben conservar estado e historial.</p>
 *
 * <p>No requieren Spring, persistencia ni reloj del sistema.</p>
 */
class ReservaVencimientoTest {

    private static final LocalDateTime CREACION =
            LocalDateTime.of(2026, 12, 1, 10, 30);

    private static final LocalDateTime LIMITE =
            LocalDateTime.of(2026, 12, 2, 10, 30);

    private static final PlazoConfirmacion PLAZO =
            new PlazoConfirmacion(24);

    @Test
    void noDebeVencerJustoAntesDelLimite() {
        Reserva reserva = reservaPendiente();
        List<EventoReserva> historialOriginal = reserva.getHistorial();

        assertThrows(
                ReglaDominioException.class,
                () -> reserva.vencer(PLAZO, LIMITE.minusNanos(1))
        );

        verificarPendienteSinCambios(reserva, historialOriginal);
    }

    @Test
    void noDebeVencerEnElInstanteExactoDelLimite() {
        Reserva reserva = reservaPendiente();
        List<EventoReserva> historialOriginal = reserva.getHistorial();

        assertThrows(
                ReglaDominioException.class,
                () -> reserva.vencer(PLAZO, LIMITE)
        );

        verificarPendienteSinCambios(reserva, historialOriginal);
    }

    @Test
    void debeVencerJustoDespuesDelLimiteYRegistrarElEvento() {
        Reserva reserva = reservaPendiente();
        EventoReserva eventoCreacion = reserva.getHistorial().get(0);
        LocalDateTime momentoVencimiento = LIMITE.plusNanos(1);

        reserva.vencer(PLAZO, momentoVencimiento);

        assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());
        assertFalse(reserva.getEstado().retieneDisponibilidad());
        assertEquals(2, reserva.getHistorial().size());
        assertEquals(eventoCreacion, reserva.getHistorial().get(0));

        EventoReserva vencimiento = reserva.getHistorial().get(1);
        assertEquals("VENCIMIENTO", vencimiento.accion());
        assertEquals("SISTEMA", vencimiento.autor());
        assertEquals(momentoVencimiento, vencimiento.momento());
        assertEquals(EstadoReserva.PENDIENTE, vencimiento.estadoAnterior());
        assertEquals(EstadoReserva.CANCELADA, vencimiento.estadoNuevo());
    }

    @Test
    void noDebeVencerDosVecesNiDuplicarElEvento() {
        Reserva reserva = reservaPendiente();
        reserva.vencer(PLAZO, LIMITE.plusSeconds(1));
        List<EventoReserva> historialOriginal = reserva.getHistorial();

        assertThrows(
                ReglaDominioException.class,
                () -> reserva.vencer(PLAZO, LIMITE.plusHours(1))
        );

        assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());
        assertEquals(historialOriginal, reserva.getHistorial());
    }

    @Test
    void debeConservarIdentidadValorPoliticaYEstanciaAlVencer() {
        Reserva reserva = reservaPendiente();
        CodigoReserva codigoOriginal = reserva.getCodigo();
        int hashOriginal = reserva.hashCode();
        ValorCongelado valorOriginal = reserva.getValor();
        VersionPolitica politicaOriginal = reserva.getPolitica();
        Estancia estanciaOriginal = reserva.getEstancia();

        reserva.vencer(PLAZO, LIMITE.plusSeconds(1));

        assertEquals(codigoOriginal, reserva.getCodigo());
        assertEquals(hashOriginal, reserva.hashCode());
        assertEquals(valorOriginal, reserva.getValor());
        assertEquals(politicaOriginal, reserva.getPolitica());
        assertEquals(estanciaOriginal, reserva.getEstancia());
        assertEquals(CREACION, reserva.getFechaCreacion().momento());
    }

    @Test
    void debeRespetarUnPlazoRecibidoDistintoDeVeinticuatroHoras() {
        Reserva reserva = reservaPendiente();
        PlazoConfirmacion plazoLargo = new PlazoConfirmacion(48);
        List<EventoReserva> historialOriginal = reserva.getHistorial();

        assertThrows(
                ReglaDominioException.class,
                () -> reserva.vencer(
                        plazoLargo, CREACION.plusHours(25)
                )
        );

        verificarPendienteSinCambios(reserva, historialOriginal);
    }

    @Test
    void debePermitirVencerConUnPlazoConfiguradoMenor() {
        Reserva reserva = reservaPendiente();
        PlazoConfirmacion plazoCorto = new PlazoConfirmacion(2);

        reserva.vencer(plazoCorto, CREACION.plusHours(3));

        assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());
        assertEquals(2, reserva.getHistorial().size());
    }

    @Test
    void debeRechazarUnPlazoNuloSinAlterarLaReserva() {
        Reserva reserva = reservaPendiente();
        List<EventoReserva> historialOriginal = reserva.getHistorial();

        assertThrows(
                ReglaDominioException.class,
                () -> reserva.vencer(null, LIMITE.plusHours(1))
        );

        verificarPendienteSinCambios(reserva, historialOriginal);
    }

    @Test
    void debeRechazarUnMomentoNuloSinAlterarLaReserva() {
        Reserva reserva = reservaPendiente();
        List<EventoReserva> historialOriginal = reserva.getHistorial();

        assertThrows(
                ReglaDominioException.class,
                () -> reserva.vencer(PLAZO, null)
        );

        verificarPendienteSinCambios(reserva, historialOriginal);
    }

    @Test
    void debeRechazarUnMomentoAnteriorALaCreacion() {
        Reserva reserva = reservaPendiente();
        List<EventoReserva> historialOriginal = reserva.getHistorial();

        assertThrows(
                ReglaDominioException.class,
                () -> reserva.vencer(PLAZO, CREACION.minusSeconds(1))
        );

        verificarPendienteSinCambios(reserva, historialOriginal);
    }

    @Test
    void elHistorialConsultadoAntesDeVencerDebeConservarSuContenido() {
        Reserva reserva = reservaPendiente();
        List<EventoReserva> historialAnterior = reserva.getHistorial();
        EventoReserva creacion = historialAnterior.get(0);

        reserva.vencer(PLAZO, LIMITE.plusSeconds(1));

        assertEquals(List.of(creacion), historialAnterior);
        assertEquals(2, reserva.getHistorial().size());
    }

    private void verificarPendienteSinCambios(
            Reserva reserva,
            List<EventoReserva> historialOriginal
    ) {
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
        assertEquals(historialOriginal, reserva.getHistorial());
    }

    private Reserva reservaPendiente() {
        Ocupante titular = new Ocupante(
                new DocumentoIdentidad("CC-1"),
                "Ana",
                LocalDate.of(1990, 1, 1),
                CREACION.toLocalDate()
        );

        Estancia estancia = new Estancia(
                LocalDate.of(2026, 12, 10),
                LocalDate.of(2026, 12, 12)
        );

        List<CargoNoche> detalle = estancia.nochesOcupadas().stream()
                .map(noche -> new CargoNoche(
                        noche,
                        "Normal",
                        Dinero.de(100_000),
                        1
                ))
                .toList();

        return Reserva.crear(
                new CodigoReserva("RES-2026-00001"),
                new IdentificacionApartamento("APT-301"),
                estancia,
                titular,
                List.of(titular),
                CanalOrigen.DIRECTO,
                null,
                new UmbralEdadFacturable(10),
                new ValorCongelado(detalle),
                new VersionPolitica(1),
                "recepcion-1",
                CREACION
        );
    }
}