package co.edu.uniquindio.sga.application.dto.response;

/** Apartamento disponible para las fechas y el grupo consultados. */
public record ApartamentoDisponibleResponse(
        String identificacion,
        String nombre,
        int dormitorios,
        int capacidadMaxima
) {
}
