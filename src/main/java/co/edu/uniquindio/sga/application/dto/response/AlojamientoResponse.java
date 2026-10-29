package co.edu.uniquindio.sga.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

/** Datos del alojamiento (7.3); los horarios vienen de la configuración externa. */
public record AlojamientoResponse(
        String nombre,
        String descripcion,
        String ciudad,
        String direccion,
        BigDecimal latitud,
        BigDecimal longitud,
        LocalTime horaEntrada,
        LocalTime horaSalida,
        List<String> normas,
        List<ServicioAdicionalResponse> servicios
) {
}
