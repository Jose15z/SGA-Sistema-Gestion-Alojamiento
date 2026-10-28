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

/** Tarifa del apartamento para una temporada (tabla apartamento_tarifas). */
@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TarifaEmbeddable {

    @Enumerated(EnumType.STRING)
    @Column(name = "temporada", nullable = false, length = 20)
    private TipoTemporada temporada;

    @Column(name = "valor_por_ocupante_noche", nullable = false, precision = 14, scale = 0)
    private BigDecimal valorPorOcupanteNoche;
}
