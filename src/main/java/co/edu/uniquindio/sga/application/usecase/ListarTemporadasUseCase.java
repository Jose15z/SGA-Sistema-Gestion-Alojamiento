package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.repository.Pagina;
import co.edu.uniquindio.sga.domain.repository.PeticionPagina;
import co.edu.uniquindio.sga.domain.repository.TemporadaRepository;
import co.edu.uniquindio.sga.domain.valueobject.Temporada;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/** Listar las temporadas configuradas, paginadas. */
@Service
public class ListarTemporadasUseCase {
    private final TemporadaRepository temporadas;

    public ListarTemporadasUseCase(TemporadaRepository temporadas) {
        this.temporadas = Objects.requireNonNull(temporadas);
    }

    @Transactional(readOnly = true)
    public Pagina<Temporada> ejecutar(PeticionPagina pagina) {
        return temporadas.listar(pagina);
    }
}
