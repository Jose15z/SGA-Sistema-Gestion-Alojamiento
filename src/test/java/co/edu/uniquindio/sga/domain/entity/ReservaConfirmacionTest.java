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
import org.junit.jupiter.api.function.Executable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pruebas de hora estimada de llegada y confirmación.
 *
 * <p>Verifican RN-08 y RN-09, además de la conservación
 * del estado y del historial cuando una operación falla.</p>
 *
 * <p>No verifican el anticipo, que requiere consultar el folio.</p>
 */
class ReservaConfirmacionTest {

    private static final LocalDateTime CREACION =
            LocalDateTime.of(2026, 12, 1, 10, 30);

    private static final LocalDateTime REGISTRO_HORA =
            CREACION.plusMinutes(1);

    private static final LocalDateTime CONFIRMACION =
            CREACION.plusMinutes(2);

    private static final LocalTime HORA_LLEGADA =
            LocalTime.of(17, 0);

    @Test
    void debeRegistrarLaHoraSinCambiarElEstadoPendiente() {
        Reserva reserva = reservaPendiente();

        reserva.indicarHoraEstimadaLlegada(
                HORA_LLEGADA, "recepcion-1", REGISTRO_HORA
        );

        assertEquals(HORA_LLEGADA, reserva.getHoraEstimadaLlegada());
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
        assertEquals(2, reserva.getHistorial().size());

        EventoReserva evento = reserva.getHistorial().get(1);
        assertEquals("HORA_LLEGADA", evento.accion());
        assertEquals("recepcion-1", evento.autor());
        assertEquals(REGISTRO_HORA, evento.momento());
        assertEquals(EstadoReserva.PENDIENTE, evento.estadoAnterior());
        assertEquals(EstadoReserva.PENDIENTE, evento.estadoNuevo());
    }

    @Test
    void debeConfirmarUnaReservaPendienteConHoraEstimada() {
        Reserva reserva = pendienteConHora();
        ValorCongelado valorOriginal = reserva.getValor();
        VersionPolitica politicaOriginal = reserva.getPolitica();

        reserva.confirmar("recepcion-2", CONFIRMACION);

        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
        assertEquals(HORA_LLEGADA, reserva.getHoraEstimadaLlegada());
        assertEquals(valorOriginal, reserva.getValor());
        assertEquals(politicaOriginal, reserva.getPolitica());
        assertEquals(3, reserva.getHistorial().size());

        EventoReserva evento = reserva.getHistorial().get(2);
        assertEquals("CONFIRMACION", evento.accion());
        assertEquals("recepcion-2", evento.autor());
        assertEquals(CONFIRMACION, evento.momento());
        assertEquals(EstadoReserva.PENDIENTE, evento.estadoAnterior());
        assertEquals(EstadoReserva.CONFIRMADA, evento.estadoNuevo());
    }

    @Test
    void noDebeConfirmarSinHoraEstimadaDeLlegada() {
        Reserva reserva = reservaPendiente();

        verificarRechazoSinCambios(
                reserva,
                () -> reserva.confirmar("recepcion-1", CONFIRMACION)
        );
    }

    @Test
    void noDebeConfirmarDosVeces() {
        Reserva reserva = pendienteConHora();
        reserva.confirmar("recepcion-1", CONFIRMACION);

        verificarRechazoSinCambios(
                reserva,
                () -> reserva.confirmar(
                        "recepcion-1", CONFIRMACION.plusMinutes(1)
                )
        );
    }

    @Test
    void noDebeConfirmarUnaReservaCanceladaPorVencimiento() {
        Reserva reserva = pendienteConHora();
        LocalDateTime vencimiento = CREACION.plusHours(25);
        reserva.vencer(new PlazoConfirmacion(24), vencimiento);

        verificarRechazoSinCambios(
                reserva,
                () -> reserva.confirmar(
                        "recepcion-1", vencimiento.plusMinutes(1)
                )
        );
    }

    @Test
    void noDebeVencerUnaReservaConfirmada() {
        Reserva reserva = pendienteConHora();
        reserva.confirmar("recepcion-1", CONFIRMACION);

        verificarRechazoSinCambios(
                reserva,
                () -> reserva.vencer(
                        new PlazoConfirmacion(24),
                        CREACION.plusHours(25)
                )
        );
    }

    @Test
    void debeRechazarConfirmacionSinAutor() {
        Reserva reserva = pendienteConHora();

        verificarRechazoSinCambios(
                reserva,
                () -> reserva.confirmar(null, CONFIRMACION)
        );
    }

    @Test
    void debeRechazarConfirmacionConAutorEnBlanco() {
        Reserva reserva = pendienteConHora();

        verificarRechazoSinCambios(
                reserva,
                () -> reserva.confirmar("   ", CONFIRMACION)
        );
    }

    @Test
    void debeRechazarConfirmacionSinMomento() {
        Reserva reserva = pendienteConHora();

        verificarRechazoSinCambios(
                reserva,
                () -> reserva.confirmar("recepcion-1", null)
        );
    }

    @Test
    void noDebeConfirmarConUnMomentoAnteriorAlUltimoEvento() {
        Reserva reserva = pendienteConHora();

        verificarRechazoSinCambios(
                reserva,
                () -> reserva.confirmar(
                        "recepcion-1", REGISTRO_HORA.minusSeconds(1)
                )
        );
    }

    @Test
    void debePermitirConfirmarEnElMismoMomentoDelUltimoEvento() {
        Reserva reserva = pendienteConHora();

        reserva.confirmar("recepcion-1", REGISTRO_HORA);

        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
        assertEquals(3, reserva.getHistorial().size());
        assertEquals(
                REGISTRO_HORA,
                reserva.getHistorial().get(2).momento()
        );
    }

    @Test
    void debeRechazarUnaHoraNulaSinBorrarLaAnterior() {
        Reserva reserva = pendienteConHora();

        verificarRechazoSinCambios(
                reserva,
                () -> reserva.indicarHoraEstimadaLlegada(
                        null, "recepcion-1", CONFIRMACION
                )
        );
    }

    @Test
    void debeRechazarCambioDeHoraConAutorInvalido() {
        Reserva reserva = pendienteConHora();

        verificarRechazoSinCambios(
                reserva,
                () -> reserva.indicarHoraEstimadaLlegada(
                        LocalTime.of(18, 0), "   ", CONFIRMACION
                )
        );
    }

    @Test
    void debeRechazarCambioDeHoraSinMomento() {
        Reserva reserva = pendienteConHora();

        verificarRechazoSinCambios(
                reserva,
                () -> reserva.indicarHoraEstimadaLlegada(
                        LocalTime.of(18, 0), "recepcion-1", null
                )
        );
    }

    @Test
    void debeRechazarCambioDeHoraAnteriorAlUltimoEvento() {
        Reserva reserva = pendienteConHora();

        verificarRechazoSinCambios(
                reserva,
                () -> reserva.indicarHoraEstimadaLlegada(
                        LocalTime.of(18, 0),
                        "recepcion-1",
                        REGISTRO_HORA.minusSeconds(1)
                )
        );
    }

    @Test
    void noDebeCambiarLaHoraDeUnaReservaCancelada() {
        Reserva reserva = pendienteConHora();
        LocalDateTime vencimiento = CREACION.plusHours(25);
        reserva.vencer(new PlazoConfirmacion(24), vencimiento);

        verificarRechazoSinCambios(
                reserva,
                () -> reserva.indicarHoraEstimadaLlegada(
                        LocalTime.of(18, 0),
                        "recepcion-1",
                        vencimiento.plusMinutes(1)
                )
        );
    }

    @Test
    void debePermitirActualizarLaHoraDeUnaReservaConfirmada() {
        Reserva reserva = pendienteConHora();
        reserva.confirmar("recepcion-1", CONFIRMACION);
        LocalTime nuevaHora = LocalTime.of(18, 0);

        reserva.indicarHoraEstimadaLlegada(
                nuevaHora,
                "recepcion-2",
                CONFIRMACION.plusMinutes(1)
        );

        assertEquals(nuevaHora, reserva.getHoraEstimadaLlegada());
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
        assertEquals(4, reserva.getHistorial().size());

        EventoReserva evento = reserva.getHistorial().get(3);
        assertEquals("HORA_LLEGADA", evento.accion());
        assertEquals(EstadoReserva.CONFIRMADA, evento.estadoAnterior());
        assertEquals(EstadoReserva.CONFIRMADA, evento.estadoNuevo());
    }

    /**
     * Comprueba la excepción y los datos que las operaciones
     * de esta clase podrían modificar.
     */
    private void verificarRechazoSinCambios(
            Reserva reserva,
            Executable operacion
    ) {
        EstadoReserva estadoOriginal = reserva.getEstado();
        LocalTime horaOriginal = reserva.getHoraEstimadaLlegada();
        List<EventoReserva> historialOriginal = reserva.getHistorial();
        ValorCongelado valorOriginal = reserva.getValor();
        VersionPolitica politicaOriginal = reserva.getPolitica();

        assertThrows(ReglaDominioException.class, operacion);

        assertEquals(estadoOriginal, reserva.getEstado());
        assertEquals(horaOriginal, reserva.getHoraEstimadaLlegada());
        assertEquals(historialOriginal, reserva.getHistorial());
        assertEquals(valorOriginal, reserva.getValor());
        assertEquals(politicaOriginal, reserva.getPolitica());
    }

    private Reserva pendienteConHora() {
        Reserva reserva = reservaPendiente();
        reserva.indicarHoraEstimadaLlegada(
                HORA_LLEGADA, "recepcion-1", REGISTRO_HORA
        );
        return reserva;
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