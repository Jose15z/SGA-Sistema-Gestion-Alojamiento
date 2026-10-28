package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.FolioNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.ReservaNoEncontradaException;
import co.edu.uniquindio.sga.application.seguridad.Solicitante;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.ConfiguracionAlojamiento;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.service.AnticipoReservaService;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

/** Confirmar reserva: anticipo (L-11), hora estimada (RN-09) y plazo vigente (RN-21). */
@Service
public class ConfirmarReservaUseCase {
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

    @Transactional
    public Reserva ejecutar(CodigoReserva codigo, Solicitante solicitante) {
        Objects.requireNonNull(codigo, "El código es obligatorio");
        Reserva reserva = reservas.obtenerPorCodigo(codigo).orElseThrow(() -> new ReservaNoEncontradaException(codigo));
        solicitante.verificarAcceso(reserva);
        Folio folio = folios.obtenerPorReserva(codigo).orElseThrow(() -> new FolioNoEncontradoException(codigo));
        anticipo.verificar(reserva, folio);
        reserva.confirmar(solicitante.usuario(), LocalDateTime.now(reloj), configuracion.plazoConfirmacion());
        reservas.guardar(reserva);
        return reserva;
    }
}
