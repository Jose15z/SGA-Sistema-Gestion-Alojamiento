package co.edu.uniquindio.sga.application.seguridad;

import co.edu.uniquindio.sga.application.exception.SolicitudUsuarioInvalidaException;

import java.util.regex.Pattern;

/** Contraseña segura (7.2): mínimo 8 caracteres, con al menos una mayúscula y un número. */
public final class PoliticaContrasena {

    public static final String PATRON = "^(?=.*[A-Z])(?=.*\\d).{8,72}$";
    private static final Pattern REGLA = Pattern.compile(PATRON);

    private PoliticaContrasena() {
    }

    public static void verificar(String contrasena) {
        if (contrasena == null || !REGLA.matcher(contrasena).matches()) {
            throw new SolicitudUsuarioInvalidaException(
                    "La contraseña debe tener mínimo 8 caracteres, con mayúsculas y números");
        }
    }
}
