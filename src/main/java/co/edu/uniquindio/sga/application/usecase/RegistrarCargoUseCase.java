package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.FolioNoEncontradoException;
import co.edu.uniquindio.sga.application.seguridad.Solicitante;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.valueobject.Cargo;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.TipoCargo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

/** Registrar un cargo por servicio adicional en un folio abierto (RN-16, L-17). */
@Service
public class RegistrarCargoUseCase {
    private final FolioRepository folios;
    private final Clock reloj;

    public RegistrarCargoUseCase(FolioRepository folios, Clock reloj) {
        this.folios = Objects.requireNonNull(folios);
        this.reloj = Objects.requireNonNull(reloj);
    }

    @Transactional
    public Cargo ejecutar(CodigoReserva codigo, String concepto, Dinero valor, Solicitante solicitante) {
        Folio folio = folios.obtenerPorReserva(codigo).orElseThrow(() -> new FolioNoEncontradoException(codigo));
        Cargo cargo = new Cargo(TipoCargo.SERVICIO_ADICIONAL, concepto, valor, LocalDateTime.now(reloj),
                solicitante.usuario());
        folio.registrarCargo(cargo);
        folios.guardar(folio);
        return cargo;
    }
}
