package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import co.edu.uniquindio.sga.domain.valueobject.UnidadCobro;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Servicio adicional del alojamiento (tabla alojamiento_servicios). */
@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ServicioAdicionalEmbeddable {

    @Column(name = "codigo", nullable = false, length = 30)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 80)
    private String nombre;

    @Column(name = "genera_cargo", nullable = false)
    private boolean generaCargo;

    @Column(name = "valor", nullable = false, precision = 14, scale = 0)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidad", nullable = false, length = 20)
    private UnidadCobro unidad;
}
