package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.*;
import co.edu.uniquindio.sga.domain.repository.*;
import co.edu.uniquindio.sga.domain.service.AnticipoReservaService;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import java.time.*;
import java.util.Objects;

/** Verifica el anticipo en el folio y delega estado, hora y trazabilidad a Reserva. */
public final class ConfirmarReservaUseCase {
    private final ReservaRepository reservas;
    private final FolioRepository folios;
    private final AnticipoReservaService anticipo;
    private final Clock reloj;
    private final ConfiguracionAlojamiento configuracion;
    public ConfirmarReservaUseCase(ReservaRepository reservas, FolioRepository folios,
                                   AnticipoReservaService anticipo, ConfiguracionAlojamiento configuracion, Clock reloj) {
        this.reservas = Objects.requireNonNull(reservas);
        this.folios = Objects.requireNonNull(folios);
        this.anticipo = Objects.requireNonNull(anticipo);
        this.reloj = Objects.requireNonNull(reloj);
        this.configuracion = Objects.requireNonNull(configuracion);
    }
    public void ejecutar(CodigoReserva codigo, String autor) {
        Objects.requireNonNull(codigo, "El código es obligatorio");
        var reserva = reservas.obtenerPorCodigo(codigo).orElseThrow(() -> new ReservaNoEncontradaException(codigo));
        var folio = folios.obtenerPorReserva(codigo).orElseThrow(() -> new FolioNoEncontradoException(codigo));
        anticipo.verificar(reserva, folio);
        reserva.confirmar(autor, LocalDateTime.now(reloj), configuracion.plazoConfirmacion());
        reservas.guardar(reserva);
    }
}
