package co.edu.uniquindio.sga.application.seguridad;

import co.edu.uniquindio.sga.application.exception.SolicitudUsuarioInvalidaException;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Correo con el que un usuario inicia sesión (7.2). Único y no editable.
 *
 * @param valor dirección normalizada en minúsculas
 */
public record CorreoElectronico(String valor) {

    private static final Pattern FORMATO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public CorreoElectronico {
        if (valor == null || valor.isBlank()) {
            throw new SolicitudUsuarioInvalidaException("El correo electrónico es obligatorio");
        }
        valor = valor.trim().toLowerCase(Locale.ROOT);
        if (!FORMATO.matcher(valor).matches()) {
            throw new SolicitudUsuarioInvalidaException("El correo electrónico no tiene un formato válido");
        }
    }
}
