package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import co.edu.uniquindio.sga.domain.valueobject.GravedadNovedad;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Novedad de un apartamento (tabla apartamento_novedades). */
@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class NovedadApartamentoEmbeddable {

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "autor", nullable = false, length = 120)
    private String autor;

    @Column(name = "descripcion", nullable = false, length = 500)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "gravedad", nullable = false, length = 20)
    private GravedadNovedad gravedad;
}
