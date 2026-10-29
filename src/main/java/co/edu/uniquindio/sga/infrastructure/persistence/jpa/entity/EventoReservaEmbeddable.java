package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Evento inmutable del historial de la reserva. */
@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class EventoReservaEmbeddable {

    @Column(name = "momento", nullable = false)
    private LocalDateTime momento;

    @Column(name = "accion", nullable = false, length = 40)
    private String accion;

    @Column(name = "autor", nullable = false, length = 120)
    private String autor;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", length = 20)
    private EstadoReserva estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", nullable = false, length = 20)
    private EstadoReserva estadoNuevo;

    @Column(name = "observacion", length = 500)
    private String observacion;
}
