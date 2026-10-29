package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Modelo de persistencia del agregado Alojamiento (un único registro, F-13). */
@Entity
@Table(name = "alojamiento")
@Getter @Setter @NoArgsConstructor
public class AlojamientoEntity {

    @Id
    @Column(name = "identificacion", nullable = false)
    private UUID identificacion;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Column(name = "descripcion", nullable = false, length = 1000)
    private String descripcion;

    @Column(name = "ciudad", nullable = false, length = 80)
    private String ciudad;

    @Column(name = "direccion", nullable = false, length = 150)
    private String direccion;

    @Column(name = "latitud", nullable = false, precision = 9, scale = 6)
    private BigDecimal latitud;

    @Column(name = "longitud", nullable = false, precision = 9, scale = 6)
    private BigDecimal longitud;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "alojamiento_normas", joinColumns = @JoinColumn(name = "alojamiento_id"))
    @OrderColumn(name = "posicion")
    @Column(name = "norma", nullable = false, length = 300)
    private List<String> normas = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "alojamiento_servicios", joinColumns = @JoinColumn(name = "alojamiento_id"))
    @OrderColumn(name = "posicion")
    private List<ServicioAdicionalEmbeddable> servicios = new ArrayList<>();
}
