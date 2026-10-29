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
 * Consultar reservas por estado (caso de uso de la Entrega 1). Conserva el filtro por
 * estado y acepta, opcionales, los demás filtros de 7.5 (apartamento, canal, titular y
 * fechas); paginado y de la más reciente a la más antigua.
 * Un huésped solo obtiene las reservas de las que es titular.
 */
@Service
public class ConsultarReservasPorEstadoUseCase {
    private final ReservaRepository reservas;

    public ConsultarReservasPorEstadoUseCase(ReservaRepository reservas) {
        this.reservas = Objects.requireNonNull(reservas);
    }

    @Transactional(readOnly = true)
    public Pagina<Reserva> ejecutar(FiltroReservas filtro, PeticionPagina pagina, Solicitante solicitante) {
        FiltroReservas aplicado = solicitante.esHuesped() ? filtro.soloDelTitular(solicitante.documento()) : filtro;
        return reservas.buscar(aplicado, pagina);
    }
}
