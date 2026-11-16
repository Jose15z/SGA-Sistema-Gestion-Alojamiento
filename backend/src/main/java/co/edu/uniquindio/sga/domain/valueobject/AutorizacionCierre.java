package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.LocalDateTime;

/**
 * Autorización explícita para cerrar un folio con saldo distinto de cero (7.7, RN-17):
 * queda registrada con su autor, su motivo y el momento en que se dio.
 *
 * @param autor   administrador que autoriza
 * @param motivo  por qué se cierra con saldo pendiente o a favor
 * @param momento cuándo se autorizó
 */
public record AutorizacionCierre(String autor, String motivo, LocalDateTime momento) {

    public AutorizacionCierre {
        if (autor == null || autor.isBlank()) {
            throw new ReglaDominioException("La autorización de cierre debe registrar su autor");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new ReglaDominioException("La autorización de cierre debe registrar su motivo");
        }
        if (momento == null) {
            throw new ReglaDominioException("La autorización de cierre debe registrar su momento");
        }
        autor = autor.trim();
        motivo = motivo.trim();
    }
}
