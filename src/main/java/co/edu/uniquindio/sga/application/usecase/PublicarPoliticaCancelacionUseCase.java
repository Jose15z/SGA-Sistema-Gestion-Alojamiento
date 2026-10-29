package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.entity.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.repository.PoliticaCancelacionRepository;
import co.edu.uniquindio.sga.domain.valueobject.TramoCancelacion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

/**
 * Publicar una nueva versión de la política (7.4, L-10). La anterior se conserva:
 * las reservas existentes siguen usando la versión congelada (RN-13, RN-22).
 */
@Service
public class PublicarPoliticaCancelacionUseCase {
    private final PoliticaCancelacionRepository politicas;

    public PublicarPoliticaCancelacionUseCase(PoliticaCancelacionRepository politicas) {
        this.politicas = Objects.requireNonNull(politicas);
    }

    @Transactional
    public PoliticaCancelacion ejecutar(List<TramoCancelacion> tramos, BigDecimal porcentajeNoShow, LocalTime horaEntrada) {
        PoliticaCancelacion nueva = politicas.obtenerVigente().siguienteVersion(tramos, porcentajeNoShow, horaEntrada);
        politicas.publicar(nueva);
        return nueva;
    }
}
