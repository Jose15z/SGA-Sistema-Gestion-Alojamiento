package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.AlojamientoNoConfiguradoException;
import co.edu.uniquindio.sga.application.exception.FolioNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.ReservaNoEncontradaException;
import co.edu.uniquindio.sga.application.seguridad.Solicitante;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.AlojamientoRepository;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.Cargo;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.ServicioAdicional;
import co.edu.uniquindio.sga.domain.valueobject.TipoCargo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * L-17: cargar al folio un servicio adicional del catálogo del alojamiento.
 * El valor no lo escribe la recepción: sale del catálogo y de la cantidad.
 */
@Service
public class RegistrarServicioAdicionalUseCase {
    private final AlojamientoRepository alojamientos;
    private final ReservaRepository reservas;
    private final FolioRepository folios;
    private final Clock reloj;

    public RegistrarServicioAdicionalUseCase(AlojamientoRepository alojamientos, ReservaRepository reservas,
                                             FolioRepository folios, Clock reloj) {
        this.alojamientos = Objects.requireNonNull(alojamientos);
        this.reservas = Objects.requireNonNull(reservas);
        this.folios = Objects.requireNonNull(folios);
        this.reloj = Objects.requireNonNull(reloj);
    }

    /** @param cantidad personas, noches o solicitudes, según la unidad de cobro del servicio */
    @Transactional
    public Cargo ejecutar(CodigoReserva codigo, String codigoServicio, int cantidad, Solicitante solicitante) {
        Reserva reserva = reservas.obtenerPorCodigo(codigo).orElseThrow(() -> new ReservaNoEncontradaException(codigo));
        solicitante.verificarAcceso(reserva);
        ServicioAdicional servicio = alojamientos.obtener().orElseThrow(AlojamientoNoConfiguradoException::new)
                .servicio(codigoServicio);
        Folio folio = folios.obtenerPorReserva(codigo).orElseThrow(() -> new FolioNoEncontradoException(codigo));
        Cargo cargo = new Cargo(TipoCargo.SERVICIO_ADICIONAL,
                servicio.nombre() + " x" + cantidad + " (" + servicio.unidad() + ")",
                servicio.cargoPor(cantidad), LocalDateTime.now(reloj), solicitante.usuario());
        folio.registrarCargo(cargo);
        folios.guardar(folio);
        return cargo;
    }
}
