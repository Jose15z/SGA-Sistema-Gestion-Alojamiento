package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.FolioNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.ReservaNoEncontradaException;
import co.edu.uniquindio.sga.application.seguridad.Solicitante;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.ConfiguracionAlojamiento;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.service.RetencionCancelacionService;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Declarar no-show (L-15): la hora límite (20:00) es configuración del alojamiento.
 *
 * <p>Excepción documentada: reserva y folio cambian juntos (retención RN-13).</p>
 */
@Service
public class DeclararNoShowUseCase {
    private final ReservaRepository reservas;
    private final FolioRepository folios;
    private final RetencionCancelacionService retencion;
    private final ConfiguracionAlojamiento configuracion;
    private final Clock reloj;

    public DeclararNoShowUseCase(ReservaRepository reservas, FolioRepository folios,
            RetencionCancelacionService retencion, ConfiguracionAlojamiento configuracion, Clock reloj) {
        this.reservas = Objects.requireNonNull(reservas);
        this.folios = Objects.requireNonNull(folios);
        this.retencion = Objects.requireNonNull(retencion);
        this.configuracion = Objects.requireNonNull(configuracion);
        this.reloj = Objects.requireNonNull(reloj);
    }

    @Transactional
    public Reserva ejecutar(CodigoReserva codigo, Solicitante solicitante) {
        Objects.requireNonNull(codigo, "El código es obligatorio");
        Reserva reserva = reservas.obtenerPorCodigo(codigo).orElseThrow(() -> new ReservaNoEncontradaException(codigo));
        Folio folio = folios.obtenerPorReserva(codigo).orElseThrow(() -> new FolioNoEncontradoException(codigo));
        String autor = solicitante.usuario();
        LocalDateTime ahora = LocalDateTime.now(reloj);
        Dinero penalidad = retencion.calcularNoShow(reserva);
        String concepto = "No-show | política " + reserva.getPolitica().numero();
        folio.verificarLiquidacion(penalidad, concepto, autor, ahora);
        reserva.declararNoShow(configuracion.horaLimiteNoShow(), autor, ahora);
        folio.liquidarAlojamiento(penalidad, concepto, autor, ahora);
        reservas.guardar(reserva);
        folios.guardar(folio);
        return reserva;
    }
}
