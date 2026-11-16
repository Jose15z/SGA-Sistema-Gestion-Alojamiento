package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import co.edu.uniquindio.sga.domain.valueobject.TipoTemporada;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Una noche del valor congelado de la reserva (RN-05, RN-22). */
@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CargoNocheEmbeddable {

    @Column(name = "noche", nullable = false)
    private LocalDate noche;

    @Enumerated(EnumType.STRING)
    @Column(name = "temporada", nullable = false, length = 20)
    private TipoTemporada temporada;

    @Column(name = "tarifa_por_ocupante", nullable = false, precision = 14, scale = 0)
    private BigDecimal tarifaPorOcupante;

    @Column(name = "ocupantes_facturables", nullable = false)
    private int ocupantesFacturables;
}
