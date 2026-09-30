package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.*;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.ConfiguracionAlojamiento;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import java.math.BigDecimal;
import java.util.Objects;

/** Condición externa de confirmación: pagos netos del folio frente al porcentaje vigente. */
public final class AnticipoReservaService {
    private final ConfiguracionAlojamiento configuracion;
    public AnticipoReservaService(ConfiguracionAlojamiento configuracion) {
        this.configuracion = Objects.requireNonNull(configuracion);
    }
    public void verificar(Reserva reserva, Folio folio) {
        if (reserva == null || folio == null || !reserva.getCodigo().equals(folio.getReserva()) || folio.estaCerrado()) {
            throw new ReglaDominioException("La confirmación requiere el folio abierto de la reserva");
        }
        BigDecimal porcentaje = configuracion.porcentajeAnticipo();
        if (porcentaje == null || porcentaje.signum() < 0 || porcentaje.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new ReglaDominioException("El porcentaje de anticipo debe estar entre 0 y 100");
        }
        Dinero requerido = new Dinero(reserva.getValor().total().valor().multiply(porcentaje).movePointLeft(2));
        if (folio.totalPagado().valor().compareTo(requerido.valor()) < 0) {
            throw new ReglaDominioException("El anticipo registrado no alcanza el importe requerido: " + requerido.valor());
        }
    }
}
