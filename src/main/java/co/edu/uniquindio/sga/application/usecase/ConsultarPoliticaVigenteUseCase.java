package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.entity.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.repository.PoliticaCancelacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/** Consultar la política de cancelación vigente (7.4). */
@Service
public class ConsultarPoliticaVigenteUseCase {
    private final PoliticaCancelacionRepository politicas;

    public ConsultarPoliticaVigenteUseCase(PoliticaCancelacionRepository politicas) {
        this.politicas = Objects.requireNonNull(politicas);
    }

    @Transactional(readOnly = true)
    public PoliticaCancelacion ejecutar() {
        return politicas.obtenerVigente();
    }
}
