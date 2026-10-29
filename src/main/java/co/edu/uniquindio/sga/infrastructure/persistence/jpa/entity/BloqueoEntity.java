package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Embedded;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/** Modelo de persistencia del agregado Bloqueo; levantarlo es eliminación lógica (vigente = false). */
@Entity
@Table(name = "bloqueos")
@Getter @Setter @NoArgsConstructor
public class BloqueoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "identificacion", nullable = false, unique = true)
    private UUID identificacion;

    @Column(name = "apartamento_id", nullable = false, length = 20)
    private String apartamentoId;

    @Embedded
    private EstanciaEmbeddable periodo;

    @Column(name = "motivo", nullable = false, length = 300)
    private String motivo;

    @Column(name = "vigente", nullable = false)
    private boolean vigente;

    @Column(name = "motivo_levantamiento", length = 300)
    private String motivoLevantamiento;
}
