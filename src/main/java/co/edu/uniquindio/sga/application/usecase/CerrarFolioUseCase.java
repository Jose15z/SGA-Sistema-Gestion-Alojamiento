package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.AccesoDenegadoException;
import co.edu.uniquindio.sga.application.exception.FolioNoEncontradoException;
import co.edu.uniquindio.sga.application.seguridad.Solicitante;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.valueobject.AutorizacionCierre;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Cerrar folio (7.7, RN-17). Con saldo cero lo cierra la recepción o la administración.
 * Con saldo distinto de cero solo lo cierra un administrador, que indica el motivo:
 * la autorización queda registrada con su autor (tomado del token) y su momento.
 */
@Service
public class CerrarFolioUseCase {
    private final FolioRepository folios;
    private final Clock reloj;

    public CerrarFolioUseCase(FolioRepository folios, Clock reloj) {
        this.folios = Objects.requireNonNull(folios);
        this.reloj = Objects.requireNonNull(reloj);
    }

    /** @param motivo motivo de la autorización; solo se usa si el saldo no es cero */
    @Transactional
    public Folio ejecutar(CodigoReserva codigo, String motivo, Solicitante solicitante) {
        Folio folio = folios.obtenerPorReserva(codigo).orElseThrow(() -> new FolioNoEncontradoException(codigo));
        if (folio.requiereAutorizacionParaCerrar()) {
            if (!solicitante.esAdministrador()) {
                throw new AccesoDenegadoException("Solo un administrador autoriza cerrar un folio con saldo distinto de cero");
            }
            folio.cerrar(motivo == null || motivo.isBlank() ? null
                    : new AutorizacionCierre(solicitante.usuario(), motivo, LocalDateTime.now(reloj)));
        } else {
            folio.cerrar();
        }
        folios.guardar(folio);
        return folio;
    }
}
