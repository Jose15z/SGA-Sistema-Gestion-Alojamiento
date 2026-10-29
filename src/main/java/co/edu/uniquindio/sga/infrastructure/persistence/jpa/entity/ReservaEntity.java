package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Embedded;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.UniqueConstraint;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de persistencia del agregado Reserva (Guía 10). Separado de la entidad
 * del dominio: aquí viven el id técnico, el constructor vacío y los setters.
 * La reserva nunca se borra: termina CANCELADA, FINALIZADA o NO_SHOW.
 */
@Entity
@Table(name = "reservas",
        indexes = {
                @Index(name = "idx_reserva_estado", columnList = "estado"),
                @Index(name = "idx_reserva_apartamento", columnList = "apartamento_id"),
                @Index(name = "idx_reserva_titular", columnList = "titular_documento")
        },
        uniqueConstraints = @UniqueConstraint(name = "uk_reserva_canal_externo",
                columnNames = {"canal_origen", "identificador_externo"}))
@Getter @Setter @NoArgsConstructor
public class ReservaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 20)
    private String codigo;

    // Referencia a otro agregado: solo su identificación (no hay @ManyToOne)
    @Column(name = "apartamento_id", nullable = false, length = 20)
    private String apartamentoId;

    @Embedded
    private EstanciaEmbeddable estancia;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal_origen", nullable = false, length = 20)
    private CanalOrigen canalOrigen;

    @Column(name = "identificador_externo", length = 60)
    private String identificadorExterno;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "titular_documento", nullable = false, length = 30)
    private String titularDocumento;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "reserva_ocupantes", joinColumns = @JoinColumn(name = "reserva_id"))
    @OrderColumn(name = "posicion")
    private List<OcupanteEmbeddable> ocupantes = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "reserva_cargos_noche", joinColumns = @JoinColumn(name = "reserva_id"))
    @OrderColumn(name = "posicion")
    private List<CargoNocheEmbeddable> cargosNoche = new ArrayList<>();

    @Column(name = "porcentaje_descuento", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeDescuento;

    @Column(name = "politica_version", nullable = false)
    private int politicaVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoReserva estado;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "reserva_historial", joinColumns = @JoinColumn(name = "reserva_id"))
    @OrderColumn(name = "posicion")
    private List<EventoReservaEmbeddable> historial = new ArrayList<>();

    @Column(name = "hora_estimada_llegada")
    private LocalTime horaEstimadaLlegada;

    @Column(name = "novedad_llegada", length = 300)
    private String novedadLlegada;

    @Column(name = "mascotas", nullable = false)
    private int mascotas;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "reserva_vehiculos", joinColumns = @JoinColumn(name = "reserva_id"))
    @OrderColumn(name = "posicion")
    private List<VehiculoEmbeddable> vehiculos = new ArrayList<>();
}
