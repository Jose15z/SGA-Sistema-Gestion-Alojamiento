package co.edu.uniquindio.sga.infrastructure.config;

import co.edu.uniquindio.sga.domain.repository.ConfiguracionAlojamiento;
import co.edu.uniquindio.sga.domain.valueobject.CapacidadParqueadero;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.PlazoConfirmacion;
import co.edu.uniquindio.sga.domain.valueobject.TiempoPreparacion;
import co.edu.uniquindio.sga.domain.valueobject.TipoTemporada;
import co.edu.uniquindio.sga.domain.valueobject.UmbralEdadFacturable;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Adaptador del puerto {@link ConfiguracionAlojamiento}. Sus valores vienen de
 * {@code application.properties} (Ficha del Alojamiento, sección 6) a través de
 * {@link PropiedadesAlojamiento}: ninguno está quemado en el código.
 */
public record ParametrosAlojamiento(
        UmbralEdadFacturable umbralEdadFacturable,
        TiempoPreparacion tiempoPreparacion,
        PlazoConfirmacion plazoConfirmacion,
        int nochesMinimasParaDescuento,
        BigDecimal porcentajeDescuentoEstanciaProlongada,
        BigDecimal porcentajeAnticipo,
        LocalTime horaLimiteNoShow,
        Map<TipoTemporada, Integer> minimosPorTemporada,
        int mascotasMaximasPorReserva,
        int vehiculosMaximosPorReserva,
        int vehiculosIncluidosPorReserva,
        CapacidadParqueadero capacidadParqueadero,
        Dinero tarifaVehiculoAdicionalPorNoche
) implements ConfiguracionAlojamiento {

    public ParametrosAlojamiento {
        Objects.requireNonNull(umbralEdadFacturable);
        Objects.requireNonNull(tiempoPreparacion);
        Objects.requireNonNull(plazoConfirmacion);
        Objects.requireNonNull(horaLimiteNoShow);
        Objects.requireNonNull(capacidadParqueadero);
        Objects.requireNonNull(tarifaVehiculoAdicionalPorNoche);
        validarPorcentaje(porcentajeAnticipo);
        validarPorcentaje(porcentajeDescuentoEstanciaProlongada);
        if (nochesMinimasParaDescuento < 1) {
            throw new IllegalArgumentException("El mínimo de noches debe ser positivo");
        }
        if (mascotasMaximasPorReserva < 0 || vehiculosMaximosPorReserva < 0
                || vehiculosIncluidosPorReserva < 0 || vehiculosIncluidosPorReserva > vehiculosMaximosPorReserva) {
            throw new IllegalArgumentException("Los máximos de mascotas y vehículos deben ser coherentes");
        }
        Map<TipoTemporada, Integer> minimos = new EnumMap<>(TipoTemporada.class);
        Objects.requireNonNull(minimosPorTemporada).forEach((temporada, minimo) -> {
            if (temporada == null || minimo == null || minimo < 1) {
                throw new IllegalArgumentException("La temporada y su mínimo de noches deben ser válidos");
            }
            minimos.put(temporada, minimo);
        });
        minimosPorTemporada = Map.copyOf(minimos);
    }

    private static void validarPorcentaje(BigDecimal valor) {
        if (valor == null || valor.signum() < 0 || valor.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("El porcentaje debe estar entre 0 y 100");
        }
    }

    @Override
    public int nochesMinimasEnTemporada(TipoTemporada temporada) {
        return minimosPorTemporada.getOrDefault(Objects.requireNonNull(temporada), 1);
    }
}
