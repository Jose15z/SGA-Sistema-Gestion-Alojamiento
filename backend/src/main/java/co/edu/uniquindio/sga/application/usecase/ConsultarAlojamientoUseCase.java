package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.AlojamientoNoConfiguradoException;
import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.repository.AlojamientoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/** Consultar los datos públicos del alojamiento: ubicación, normas y servicios (7.1, 7.3). */
@Service
public class ConsultarAlojamientoUseCase {
    private final AlojamientoRepository alojamientos;

    public ConsultarAlojamientoUseCase(AlojamientoRepository alojamientos) {
        this.alojamientos = Objects.requireNonNull(alojamientos);
    }

    @Transactional(readOnly = true)
    public Alojamiento ejecutar() {
        return alojamientos.obtener().orElseThrow(AlojamientoNoConfiguradoException::new);
    }
}
