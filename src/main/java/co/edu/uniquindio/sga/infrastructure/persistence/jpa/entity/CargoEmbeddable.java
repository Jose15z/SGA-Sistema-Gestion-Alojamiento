package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import co.edu.uniquindio.sga.domain.valueobject.TipoCargo;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Cargo inmutable del folio (RN-16). */
@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CargoEmbeddable {

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private TipoCargo tipo;

    @Column(name = "concepto", nullable = false, length = 300)
    private String concepto;

    @Column(name = "valor", nullable = false, precision = 14, scale = 0)
    private BigDecimal valor;

    @Column(name = "momento", nullable = false)
    private LocalDateTime momento;

    @Column(name = "autor", nullable = false, length = 120)
    private String autor;
}
