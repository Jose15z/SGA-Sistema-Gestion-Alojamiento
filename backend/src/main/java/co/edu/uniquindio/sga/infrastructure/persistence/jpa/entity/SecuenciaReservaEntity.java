package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Último consecutivo de código de reserva emitido por año (RES-YYYY-NNNNN). */
@Entity
@Table(name = "secuencias_reserva")
@Getter @Setter @NoArgsConstructor
public class SecuenciaReservaEntity {

    @Id
    @Column(name = "anio")
    private Integer anio;

    @Column(name = "ultimo", nullable = false)
    private int ultimo;
}
