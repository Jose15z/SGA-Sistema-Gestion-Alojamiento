package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** Ocupante: entidad interna de la reserva, vive y muere con ella (tabla reserva_ocupantes). */
@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class OcupanteEmbeddable {

    @Column(name = "documento", nullable = false, length = 30)
    private String documento;

    @Column(name = "nombre_completo", nullable = false, length = 150)
    private String nombre;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;
}
