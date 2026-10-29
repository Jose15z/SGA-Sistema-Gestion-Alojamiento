package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Vehículo asociado a la reserva (RP-02). */
@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class VehiculoEmbeddable {

    @Column(name = "placa", nullable = false, length = 10)
    private String placa;
}
