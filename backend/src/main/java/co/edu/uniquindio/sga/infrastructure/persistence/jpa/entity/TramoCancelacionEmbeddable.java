package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Tramo de la política: antelación mínima en horas y porcentaje retenido (L-10). */
@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TramoCancelacionEmbeddable {

    @Column(name = "antelacion_minima_horas", nullable = false)
    private long antelacionMinimaHoras;

    @Column(name = "porcentaje", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentaje;
}
