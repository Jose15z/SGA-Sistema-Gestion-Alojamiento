package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.FolioNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.ReservaNoEncontradaException;
import co.edu.uniquindio.sga.application.seguridad.Solicitante;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.ConfiguracionAlojamiento;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.service.DisponibilidadParqueaderoService;
import co.edu.uniquindio.sga.domain.valueobject.Cargo;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.TipoCargo;
import co.edu.uniquindio.sga.domain.valueobject.Vehiculo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Solicitar vehículo (RP-02): el primero tiene parqueadero incluido; el segundo
 * depende de cupos y genera cargo por noche en el folio (misma transacción).
 */
@Service
public class RegistrarVehiculoUseCase {
    private final ReservaRepository reservas;
    private final FolioRepository folios;
    private final DisponibilidadParqueaderoService parqueadero;
    private final ConfiguracionAlojamiento configuracion;
    private final Clock reloj;

    public RegistrarVehiculoUseCase(ReservaRepository reservas, FolioRepository folios,
                                    DisponibilidadParqueaderoService parqueadero,
                                    ConfiguracionAlojamiento configuracion, Clock reloj) {
        this.reservas = Objects.requireNonNull(reservas);
        this.folios = Objects.requireNonNull(folios);
        this.parqueadero = Objects.requireNonNull(parqueadero);
        this.configuracion = Objects.requireNonNull(configuracion);
        this.reloj = Objects.requireNonNull(reloj);
    }

    @Transactional
    public Reserva ejecutar(CodigoReserva codigo, Vehiculo vehiculo, Solicitante solicitante) {
        Reserva reserva = reservas.obtenerPorCodigo(codigo).orElseThrow(() -> new ReservaNoEncontradaException(codigo));
        solicitante.verificarAcceso(reserva);
        Folio folio = folios.obtenerPorReserva(codigo).orElseThrow(() -> new FolioNoEncontradoException(codigo));
        String autor = solicitante.usuario();
        LocalDateTime ahora = LocalDateTime.now(reloj);

        Dinero cargoAdicional = null;
        if (reserva.siguienteVehiculoEsAdicional(configuracion.vehiculosIncluidosPorReserva())) {
            cargoAdicional = parqueadero.verificarVehiculoAdicional(reserva);
        }
        reserva.registrarVehiculo(vehiculo, configuracion.vehiculosMaximosPorReserva(), autor, ahora);
        if (cargoAdicional != null) {
            folio.registrarCargo(new Cargo(TipoCargo.SERVICIO_ADICIONAL,
                    "Parqueadero vehículo adicional " + vehiculo.placa(), cargoAdicional, ahora, autor));
        }
        reservas.guardar(reserva);
        folios.guardar(folio);
        return reserva;
    }
}
