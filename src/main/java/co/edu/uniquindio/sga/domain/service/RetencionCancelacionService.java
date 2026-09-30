package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.*;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.PoliticaCancelacionRepository;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import java.time.LocalDateTime;
import java.util.Objects;

/** RN-13: siempre recupera la versión histórica conservada por la reserva. */
public final class RetencionCancelacionService {
    private final PoliticaCancelacionRepository politicas;
    public RetencionCancelacionService(PoliticaCancelacionRepository politicas) {
        this.politicas = Objects.requireNonNull(politicas);
    }
    public Dinero calcular(Reserva reserva, LocalDateTime ahora) {
        return politicaDe(reserva).retencionPorCancelacion(reserva.getValor().total(), reserva.getEstancia(), ahora);
    }
    public Dinero calcularNoShow(Reserva reserva) {
        return politicaDe(reserva).retencionPorNoShow(reserva.getValor().total());
    }
    private PoliticaCancelacion politicaDe(Reserva reserva) {
        if (reserva == null) throw new ReglaDominioException("La reserva es obligatoria");
        PoliticaCancelacion politica = politicas.obtenerPorVersion(reserva.getPolitica())
                .orElseThrow(() -> new ReglaDominioException("No existe la versión histórica de la política"));
        if (!politica.getVersion().equals(reserva.getPolitica())) {
            throw new ReglaDominioException("La política consultada no corresponde a la reserva");
        }
        return politica;
    }
}
