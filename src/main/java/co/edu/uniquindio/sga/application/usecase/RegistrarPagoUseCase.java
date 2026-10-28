package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.FolioNoEncontradoException;
import co.edu.uniquindio.sga.application.seguridad.Solicitante;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Pago;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.MedioPago;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/** Registrar pago o devolución (RN-15, RN-16, L-18). El SGA registra; no ejecuta transferencias. */
@Service
public class RegistrarPagoUseCase {
    private final FolioRepository folios;
    private final Clock reloj;

    public RegistrarPagoUseCase(FolioRepository folios, Clock reloj) {
        this.folios = Objects.requireNonNull(folios);
        this.reloj = Objects.requireNonNull(reloj);
    }

    @Transactional
    public Pago ejecutar(CodigoReserva codigo, Dinero valor, MedioPago medio, String concepto, Solicitante solicitante) {
        Folio folio = folios.obtenerPorReserva(codigo).orElseThrow(() -> new FolioNoEncontradoException(codigo));
        Pago pago = new Pago(UUID.randomUUID(), valor, medio, LocalDateTime.now(reloj), concepto, solicitante.usuario());
        folio.registrarPago(pago);
        folios.guardar(folio);
        return pago;
    }
}
