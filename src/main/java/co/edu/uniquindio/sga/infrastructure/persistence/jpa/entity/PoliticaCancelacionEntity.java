package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/** Versión de la política de cancelación; las anteriores se conservan (RN-13). */
@Entity
@Table(name = "politicas_cancelacion")
@Getter @Setter @NoArgsConstructor
public class PoliticaCancelacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "version", nullable = false, unique = true)
    private int version;

    @Column(name = "porcentaje_no_show", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeNoShow;

    @Column(name = "hora_entrada", nullable = false)
    private LocalTime horaEntrada;

    @Column(name = "vigente", nullable = false)
    private boolean vigente;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "politica_tramos", joinColumns = @JoinColumn(name = "politica_id"))
    @OrderColumn(name = "posicion")
    private List<TramoCancelacionEmbeddable> tramos = new ArrayList<>();
}
