package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/**
 * Identificador asignado a una reserva por un canal externo (RN-19).
 *
 * <p>Junto con el canal de origen permite detectar mensajes repetidos.</p>
 *
 * @param valor identificador recibido del canal externo
 */
public record IdentificadorExterno(String valor) {

    public IdentificadorExterno {
        if (valor == null || valor.isBlank()) {
            throw new ReglaDominioException("El identificador externo no puede estar vacío");
        }
        valor = valor.trim();
        if (valor.length() > 60) {
            throw new ReglaDominioException("El identificador externo no puede superar 60 caracteres");
        }
    }
}
