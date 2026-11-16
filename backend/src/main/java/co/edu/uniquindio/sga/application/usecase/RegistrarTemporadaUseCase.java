package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.repository.TemporadaRepository;
import co.edu.uniquindio.sga.domain.valueobject.Temporada;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/** Registrar temporada (7.4, L-08): el calendario rechaza solapamientos. */
@Service
public class RegistrarTemporadaUseCase {
    private final TemporadaRepository temporadas;

    public RegistrarTemporadaUseCase(TemporadaRepository temporadas) {
        this.temporadas = Objects.requireNonNull(temporadas);
    }

    @Transactional
    public Temporada ejecutar(Temporada temporada) {
        temporadas.obtenerCalendario().con(temporada);
        temporadas.guardar(temporada);
        return temporada;
    }
}
