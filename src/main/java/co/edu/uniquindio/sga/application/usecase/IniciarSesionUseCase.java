package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.CredencialesInvalidasException;
import co.edu.uniquindio.sga.application.port.CifradorContrasenas;
import co.edu.uniquindio.sga.application.port.EmisorTokens;
import co.edu.uniquindio.sga.application.port.UsuarioRepository;
import co.edu.uniquindio.sga.application.seguridad.CorreoElectronico;
import co.edu.uniquindio.sga.application.seguridad.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/** Iniciar sesión (7.2): emite un JWT con expiración para un usuario activo. */
@Service
public class IniciarSesionUseCase {
    private final UsuarioRepository usuarios;
    private final CifradorContrasenas cifrador;
    private final EmisorTokens emisor;

    public IniciarSesionUseCase(UsuarioRepository usuarios, CifradorContrasenas cifrador, EmisorTokens emisor) {
        this.usuarios = Objects.requireNonNull(usuarios);
        this.cifrador = Objects.requireNonNull(cifrador);
        this.emisor = Objects.requireNonNull(emisor);
    }

    @Transactional(readOnly = true)
    public Resultado ejecutar(CorreoElectronico correo, String contrasena) {
        Usuario usuario = usuarios.obtenerPorCorreo(correo)
                .filter(Usuario::puedeIniciarSesion)
                .filter(encontrado -> cifrador.coincide(contrasena, encontrado.getContrasenaCifrada()))
                .orElseThrow(CredencialesInvalidasException::new);
        return new Resultado(usuario, emisor.emitir(usuario));
    }

    /** Usuario autenticado y su token de sesión. */
    public record Resultado(Usuario usuario, EmisorTokens.TokenSesion token) {
    }
}
