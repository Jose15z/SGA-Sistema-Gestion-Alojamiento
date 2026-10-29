package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.FolioNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.ReservaNoEncontradaException;
import co.edu.uniquindio.sga.application.seguridad.Solicitante;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/** Consultar el folio de una reserva (RN-15); el huésped solo ve el de sus reservas. */
@Service
public class ConsultarFolioUseCase {
    private final ReservaRepository reservas;
    private final FolioRepository folios;

    public ConsultarFolioUseCase(ReservaRepository reservas, FolioRepository folios) {
        this.reservas = Objects.requireNonNull(reservas);
        this.folios = Objects.requireNonNull(folios);
    }

    @Transactional(readOnly = true)
    public Folio ejecutar(CodigoReserva codigo, Solicitante solicitante) {
        Reserva reserva = reservas.obtenerPorCodigo(codigo).orElseThrow(() -> new ReservaNoEncontradaException(codigo));
        solicitante.verificarAcceso(reserva);
        return folios.obtenerPorReserva(codigo).orElseThrow(() -> new FolioNoEncontradoException(codigo));
    }
}
