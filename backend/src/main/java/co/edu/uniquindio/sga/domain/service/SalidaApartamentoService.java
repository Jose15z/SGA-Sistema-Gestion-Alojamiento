package co.edu.uniquindio.sga.domain.service;
import co.edu.uniquindio.sga.domain.entity.*;
import co.edu.uniquindio.sga.domain.valueobject.*;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
/** Precondiciones entre agregados para registrar la salida (RN-08/17). */
public final class SalidaApartamentoService {
    public void verificar(Reserva reserva, Apartamento apartamento, Folio folio) {
        if (reserva == null || apartamento == null || folio == null
                || !reserva.getCodigo().equals(folio.getReserva())
                || !reserva.getApartamento().equals(apartamento.getIdentificacion())) {
            throw new ReglaDominioException("La salida requiere reserva, apartamento y folio correspondientes");
        }
        if (reserva.getEstado() != EstadoReserva.EN_CURSO
                || apartamento.getEstadoOperativo() != EstadoOperativo.OCUPADO || !folio.estaCerrado()) {
            throw new ReglaDominioException("La salida requiere reserva EN_CURSO, apartamento OCUPADO y folio cerrado");
        }
    }
}
