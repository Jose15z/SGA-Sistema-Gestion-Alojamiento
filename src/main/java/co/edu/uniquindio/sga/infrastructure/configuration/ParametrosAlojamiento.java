package co.edu.uniquindio.sga.infrastructure.configuration;

import co.edu.uniquindio.sga.domain.entity.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.repository.ConfiguracionAlojamiento;
import co.edu.uniquindio.sga.domain.valueobject.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

/** Adaptador de configuración para la entrega en memoria. Los valores del alojamiento viven fuera del dominio. */
public record ParametrosAlojamiento(UmbralEdadFacturable umbralEdadFacturable,
        TiempoPreparacion tiempoPreparacion, PlazoConfirmacion plazoConfirmacion,
        int nochesMinimasParaDescuento, BigDecimal porcentajeDescuentoEstanciaProlongada,
        BigDecimal porcentajeAnticipo, LocalTime horaLimiteNoShow, Map<String, Integer> minimosPorTemporada)
        implements ConfiguracionAlojamiento {
    public ParametrosAlojamiento {
        Objects.requireNonNull(umbralEdadFacturable);
        Objects.requireNonNull(tiempoPreparacion);
        Objects.requireNonNull(plazoConfirmacion);
        Objects.requireNonNull(horaLimiteNoShow);
        validarPorcentaje(porcentajeAnticipo);
        validarPorcentaje(porcentajeDescuentoEstanciaProlongada);
        if (nochesMinimasParaDescuento < 1) throw new IllegalArgumentException("El mínimo de noches debe ser positivo");
        Map<String, Integer> normalizados = new HashMap<>();
        Objects.requireNonNull(minimosPorTemporada).forEach((nombre, minimo) -> {
            if (nombre == null || nombre.isBlank() || minimo == null || minimo < 1) {
                throw new IllegalArgumentException("La temporada y su mínimo deben ser válidos");
            }
            normalizados.put(nombre.trim().toLowerCase(Locale.ROOT), minimo);
        });
        minimosPorTemporada = Map.copyOf(normalizados);
    }
    private static void validarPorcentaje(BigDecimal valor) {
        if (valor == null || valor.signum() < 0 || valor.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("El porcentaje debe estar entre 0 y 100");
        }
    }
    @Override public int nochesMinimasEnTemporada(String temporada) {
        return minimosPorTemporada.getOrDefault(Objects.requireNonNull(temporada).trim().toLowerCase(Locale.ROOT), 1);
    }
    /** Ficha A.4/A.6; hora de no-show acordada por el equipo: 20:00. */
    public static ParametrosAlojamiento sieteRaices() {
        return new ParametrosAlojamiento(new UmbralEdadFacturable(10),
                new TiempoPreparacion(3, LocalTime.of(11, 0), LocalTime.of(15, 0)),
                new PlazoConfirmacion(24), 7, new BigDecimal("10"), new BigDecimal("30"),
                LocalTime.of(20, 0), Map.of("Temporada Especial", 2, "Especial", 2));
    }
    /** Versión inicial: 0 % desde 7 días, 30 % desde 48 h, 50 % por debajo y por no-show. */
    public static PoliticaCancelacion politicaInicialSieteRaices() {
        return new PoliticaCancelacion(new VersionPolitica(1), List.of(
                new TramoCancelacion(Duration.ofDays(7), BigDecimal.ZERO),
                new TramoCancelacion(Duration.ofHours(48), new BigDecimal("30")),
                new TramoCancelacion(Duration.ZERO, new BigDecimal("50"))),
                new BigDecimal("50"), LocalTime.of(15, 0));
    }
}
