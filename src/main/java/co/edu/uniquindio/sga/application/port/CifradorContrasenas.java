package co.edu.uniquindio.sga.application.port;

/**
 * Puerto técnico para cifrar y comparar contraseñas.
 * La implementación con BCrypt vive en infraestructura.
 */
public interface CifradorContrasenas {

    String cifrar(String contrasenaPlana);

    boolean coincide(String contrasenaPlana, String contrasenaCifrada);
}
