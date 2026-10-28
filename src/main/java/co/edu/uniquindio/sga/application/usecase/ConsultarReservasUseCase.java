package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.seguridad.Solicitante;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.FiltroReservas;
import co.edu.uniquindio.sga.domain.repository.Pagina;
import co.edu.uniquindio.sga.domain.repository.PeticionPagina;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Consultar reservas (Entrega 1: "Consultar reservas por estado"), ampliado con
 * filtros por apartamento y fechas, paginado y de la más reciente a la más antigua.
 * Un huésped solo obtiene las reservas de las que es titular.
 */
@Service
public class ConsultarReservasUseCase {
    private final ReservaRepository reservas;

    public ConsultarReservasUseCase(ReservaRepository reservas) {
        this.reservas = Objects.requireNonNull(reservas);
    }

    @Transactional(readOnly = true)
    public Pagina<Reserva> ejecutar(FiltroReservas filtro, PeticionPagina pagina, Solicitante solicitante) {
        FiltroReservas aplicado = solicitante.esHuesped() ? filtro.soloDelTitular(solicitante.documento()) : filtro;
        return reservas.buscar(aplicado, pagina);
    }
}
