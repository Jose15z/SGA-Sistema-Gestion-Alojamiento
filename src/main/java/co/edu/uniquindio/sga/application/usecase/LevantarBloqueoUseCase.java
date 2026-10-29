package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.BloqueoNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.repository.BloqueoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

/** Levantar bloqueo: eliminación lógica, el bloqueo se conserva con su motivo. */
@Service
public class LevantarBloqueoUseCase {
    private final BloqueoRepository bloqueos;

    public LevantarBloqueoUseCase(BloqueoRepository bloqueos) {
        this.bloqueos = Objects.requireNonNull(bloqueos);
    }

    @Transactional
    public Bloqueo ejecutar(UUID identificacion, String motivo) {
        Bloqueo bloqueo = bloqueos.obtenerPorIdentificacion(identificacion)
                .orElseThrow(() -> new BloqueoNoEncontradoException(identificacion));
        bloqueo.levantar(motivo);
        bloqueos.guardar(bloqueo);
        return bloqueo;
    }
}
