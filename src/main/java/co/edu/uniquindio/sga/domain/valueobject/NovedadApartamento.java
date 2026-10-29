package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.LocalDateTime;

/**
 * Novedad sobre un apartamento (7.6): daño, faltante o cualquier hallazgo del
 * personal. Inmutable; el historial solo crece.
 *
 * @param fecha       momento del registro
 * @param autor       usuario que la registra
 * @param descripcion qué se encontró
 * @param gravedad    qué tan grave es
 */
public record NovedadApartamento(LocalDateTime fecha, String autor, String descripcion, GravedadNovedad gravedad) {

    public NovedadApartamento {
        if (fecha == null || gravedad == null) {
            throw new ReglaDominioException("La novedad requiere fecha y gravedad");
        }
        if (autor == null || autor.isBlank()) {
            throw new ReglaDominioException("La novedad debe registrar su autor");
        }
        if (descripcion == null || descripcion.isBlank()) {
            throw new ReglaDominioException("La novedad debe tener una descripción");
        }
        autor = autor.trim();
        descripcion = descripcion.trim();
    }
}
