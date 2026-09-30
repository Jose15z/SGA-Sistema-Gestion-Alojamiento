package co.edu.uniquindio.sga;

import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.valueobject.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/** Datos reproducibles compartidos por pruebas de cancelación y entrega. */
public final class ReservasDePrueba {
    public static final LocalDateTime CREACION = LocalDateTime.of(2026, 12, 1, 10, 0);
    public static final LocalDateTime LLEGADA = LocalDateTime.of(2026, 12, 10, 15, 0);

    private ReservasDePrueba() { }

    public static Reserva pendiente() {
        Ocupante titular = new Ocupante(new DocumentoIdentidad("CC-1"), "Ana",
                LocalDate.of(1990, 1, 1), CREACION.toLocalDate());
        Estancia estancia = new Estancia(LLEGADA.toLocalDate(), LLEGADA.toLocalDate().plusDays(2));
        ValorCongelado valor = new ValorCongelado(estancia.nochesOcupadas().stream()
                .map(noche -> new CargoNoche(noche, "Normal", Dinero.de(100_000), 1)).toList());
        return Reserva.crear(new CodigoReserva("RES-2026-00001"),
                new IdentificacionApartamento("APT-301"), estancia, titular, List.of(titular),
                CanalOrigen.DIRECTO, null, new UmbralEdadFacturable(10), valor,
                new VersionPolitica(1), "recepcion-1", CREACION);
    }

    public static Reserva confirmada() {
        Reserva reserva = pendiente();
        reserva.indicarHoraEstimadaLlegada(LocalTime.of(15, 0), "recepcion-1", CREACION);
        reserva.confirmar("recepcion-1", CREACION);
        return reserva;
    }
}
