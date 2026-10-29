package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.repository.AlojamientoRepository;
import co.edu.uniquindio.sga.domain.valueobject.ServicioAdicional;
import co.edu.uniquindio.sga.domain.valueobject.Ubicacion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Gestionar los datos del alojamiento, sus normas y sus servicios adicionales (7.3).
 * Si aún no existe, lo registra; si existe, conserva su identidad y lo actualiza.
 */
@Service
public class ActualizarAlojamientoUseCase {
    private final AlojamientoRepository alojamientos;

    public ActualizarAlojamientoUseCase(AlojamientoRepository alojamientos) {
        this.alojamientos = Objects.requireNonNull(alojamientos);
    }

    @Transactional
    public Alojamiento ejecutar(String nombre, String descripcion, String ciudad, String direccion, Ubicacion ubicacion,
                                List<String> normas, List<ServicioAdicional> servicios) {
        Alojamiento alojamiento = alojamientos.obtener()
                .map(existente -> {
                    existente.actualizarDatos(nombre, descripcion, ciudad, direccion, ubicacion, normas);
                    existente.definirServicios(servicios);
                    return existente;
                })
                .orElseGet(() -> new Alojamiento(UUID.randomUUID(), nombre, descripcion, ciudad, direccion,
                        ubicacion, normas, servicios));
        alojamientos.guardar(alojamiento);
        return alojamiento;
    }
}
