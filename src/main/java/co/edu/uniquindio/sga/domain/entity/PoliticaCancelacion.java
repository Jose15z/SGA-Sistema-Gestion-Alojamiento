package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

/** Versión inmutable de política; congela también la hora de referencia de sus plazos. */
public final class PoliticaCancelacion {
    private final VersionPolitica version;
    private final List<TramoCancelacion> tramos;
    private final BigDecimal porcentajeNoShow;
    private final LocalTime horaEntrada;

    public PoliticaCancelacion(VersionPolitica version, List<TramoCancelacion> tramos,
                               BigDecimal porcentajeNoShow, LocalTime horaEntrada) {
        if (version == null || tramos == null || tramos.isEmpty() || tramos.stream().anyMatch(Objects::isNull)
                || porcentajeNoShow == null || porcentajeNoShow.signum() < 0
                || porcentajeNoShow.compareTo(BigDecimal.valueOf(100)) > 0 || horaEntrada == null) {
            throw new ReglaDominioException("La política requiere versión, tramos, porcentaje no-show válido y hora de entrada");
        }
        if (tramos.stream().noneMatch(t -> t.antelacionMinima().isZero())
                || tramos.stream().map(TramoCancelacion::antelacionMinima).distinct().count() != tramos.size()) {
            throw new ReglaDominioException("Los tramos deben cubrir desde cero sin repetir umbrales");
        }
        this.version = version;
        this.tramos = tramos.stream().sorted(Comparator.comparing(TramoCancelacion::antelacionMinima).reversed()).toList();
        this.porcentajeNoShow = porcentajeNoShow;
        this.horaEntrada = horaEntrada;
    }

    /** Plazos exactos respecto de la entrada prevista; 48 y 168 horas pertenecen al tramo superior. */
    public Dinero retencionPorCancelacion(Dinero total, Estancia estancia, LocalDateTime ahora) {
        if (estancia == null || ahora == null) throw new ReglaDominioException("Se requieren estancia y momento");
        Duration antelacion = Duration.between(ahora, estancia.fechaEntrada().atTime(horaEntrada));
        if (antelacion.isNegative()) antelacion = Duration.ZERO;
        for (TramoCancelacion tramo : tramos) {
            if (antelacion.compareTo(tramo.antelacionMinima()) >= 0) return porcentajeDe(total, tramo.porcentaje());
        }
        throw new IllegalStateException("La política no cubre la antelación");
    }
    public Dinero retencionPorNoShow(Dinero total) { return porcentajeDe(total, porcentajeNoShow); }
    private Dinero porcentajeDe(Dinero total, BigDecimal porcentaje) {
        if (total == null || total.esNegativo()) throw new ReglaDominioException("El total debe ser no negativo");
        return new Dinero(total.valor().multiply(porcentaje).movePointLeft(2));
    }
    public VersionPolitica getVersion() { return version; }
    public List<TramoCancelacion> getTramos() { return tramos; }
    @Override public boolean equals(Object objeto) {
        return objeto instanceof PoliticaCancelacion otra && version.equals(otra.version);
    }
    @Override public int hashCode() { return version.hashCode(); }
}
