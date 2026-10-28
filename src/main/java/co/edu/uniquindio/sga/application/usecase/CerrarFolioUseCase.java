package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.FolioNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/** Cerrar folio (RN-17): con saldo distinto de cero exige autorización registrada. */
@Service
public class CerrarFolioUseCase {
    private final FolioRepository folios;

    public CerrarFolioUseCase(FolioRepository folios) {
        this.folios = Objects.requireNonNull(folios);
    }

    @Transactional
    public Folio ejecutar(CodigoReserva codigo, String autorizacion) {
        Folio folio = folios.obtenerPorReserva(codigo).orElseThrow(() -> new FolioNoEncontradoException(codigo));
        folio.cerrar(autorizacion);
        folios.guardar(folio);
        return folio;
    }
}
