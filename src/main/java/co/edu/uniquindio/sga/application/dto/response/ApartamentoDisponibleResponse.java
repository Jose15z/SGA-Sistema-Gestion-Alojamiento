package co.edu.uniquindio.sga.application.dto.response;

import java.util.List;

/** Tarjeta de un apartamento disponible para las fechas y el grupo consultados (7.5). */
public record ApartamentoDisponibleResponse(
        String identificacion,
        String nombre,
        String descripcion,
        int dormitorios,
        int capacidadMaxima,
        List<String> caracteristicas,
        int noches,
        Long valorEstimado
) {
}
