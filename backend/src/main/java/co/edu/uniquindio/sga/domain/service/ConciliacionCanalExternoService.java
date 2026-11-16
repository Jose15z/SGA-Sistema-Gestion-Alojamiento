package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.IdentificadorExterno;

import java.util.Objects;

/**
 * RN-19: la combinación canal + identificador externo es única, de modo
 * que un mismo mensaje recibido dos veces no crea dos reservas.
 *
 * <p>RN-18 (reserva externa en conflicto) se resuelve con
 * {@link DisponibilidadApartamentoService}: la reserva externa se rechaza
 * y nunca sobrescribe la vigente. Una reserva individual no conoce a las
 * demás, por eso la verificación vive en un servicio.</p>
 */
public final class ConciliacionCanalExternoService {

    private final ReservaRepository reservas;

    public ConciliacionCanalExternoService(ReservaRepository reservas) {
        this.reservas = Objects.requireNonNull(reservas);
    }

    public void verificarNoDuplicada(CanalOrigen canal, IdentificadorExterno identificador) {
        if (canal == null) {
            throw new ReglaDominioException("El canal de origen es obligatorio");
        }
        if (identificador == null) {
            return;
        }
        if (reservas.existePorCanalEIdentificadorExterno(canal, identificador)) {
            throw new ReglaDominioException("Ya existe una reserva del canal " + canal
                    + " con el identificador externo " + identificador.valor());
        }
    }
}
