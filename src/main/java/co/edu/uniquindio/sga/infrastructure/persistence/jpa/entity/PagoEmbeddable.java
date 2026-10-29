package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import co.edu.uniquindio.sga.domain.valueobject.MedioPago;
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
import java.util.UUID;

/**
 * Pago del folio. Es entidad interna del agregado Folio: conserva su identidad
 * (UUID) pero no existe fuera de su folio, por eso se guarda con él.
 */
@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PagoEmbeddable {

    @Column(name = "identificacion", nullable = false)
    private UUID identificacion;

    @Column(name = "valor", nullable = false, precision = 14, scale = 0)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(name = "medio", nullable = false, length = 30)
    private MedioPago medio;

    @Column(name = "momento", nullable = false)
    private LocalDateTime momento;

    @Column(name = "concepto", nullable = false, length = 300)
    private String concepto;

    @Column(name = "autor", nullable = false, length = 120)
    private String autor;
}
