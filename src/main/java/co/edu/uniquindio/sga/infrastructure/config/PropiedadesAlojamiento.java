package co.edu.uniquindio.sga.infrastructure.config;

import co.edu.uniquindio.sga.domain.valueobject.CapacidadParqueadero;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.PlazoConfirmacion;
import co.edu.uniquindio.sga.domain.valueobject.TiempoPreparacion;
import co.edu.uniquindio.sga.domain.valueobject.TipoTemporada;
import co.edu.uniquindio.sga.domain.valueobject.UmbralEdadFacturable;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Map;

/**
 * Parámetros de la Ficha del Alojamiento leídos de {@code sga.alojamiento.*}.
 * Cambiarlos no requiere recompilar: basta editar {@code application.properties}
 * o sobrescribirlos con variables de entorno.
 */
@ConfigurationProperties(prefix = "sga.alojamiento")
public record PropiedadesAlojamiento(
        int umbralEdadFacturable,
        LocalTime horaEntrada,
        LocalTime horaSalida,
        int tiempoPreparacionHoras,
        int plazoConfirmacionHoras,
        LocalTime horaLimiteNoShow,
        BigDecimal porcentajeAnticipo,
        int nochesMinimasDescuento,
        BigDecimal porcentajeDescuentoEstanciaProlongada,
        Map<TipoTemporada, Integer> nochesMinimasPorTemporada,
        int mascotasMaximasPorReserva,
        int vehiculosMaximosPorReserva,
        int vehiculosIncluidosPorReserva,
        int cuposParqueadero,
        long tarifaVehiculoAdicionalNoche
) {

    public ParametrosAlojamiento aConfiguracion() {
        return new ParametrosAlojamiento(
                new UmbralEdadFacturable(umbralEdadFacturable),
                new TiempoPreparacion(tiempoPreparacionHoras, horaSalida, horaEntrada),
                new PlazoConfirmacion(plazoConfirmacionHoras),
                nochesMinimasDescuento,
                porcentajeDescuentoEstanciaProlongada,
                porcentajeAnticipo,
                horaLimiteNoShow,
                nochesMinimasPorTemporada == null ? Map.of() : nochesMinimasPorTemporada,
                mascotasMaximasPorReserva,
                vehiculosMaximosPorReserva,
                vehiculosIncluidosPorReserva,
                new CapacidadParqueadero(cuposParqueadero),
                Dinero.de(tarifaVehiculoAdicionalNoche));
    }
}
