package co.edu.uniquindio.sga.infrastructure.security;

import co.edu.uniquindio.sga.application.port.CifradorContrasenas;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Adaptador del puerto de cifrado con BCrypt (7.2). */
@Component
public class CifradorContrasenasBCrypt implements CifradorContrasenas {

    private final PasswordEncoder encoder;

    public CifradorContrasenasBCrypt(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public String cifrar(String contrasenaPlana) {
        return encoder.encode(contrasenaPlana);
    }

    @Override
    public boolean coincide(String contrasenaPlana, String contrasenaCifrada) {
        return encoder.matches(contrasenaPlana, contrasenaCifrada);
    }
}
