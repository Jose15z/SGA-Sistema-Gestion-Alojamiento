package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.seguridad.PoliticaContrasena;
import co.edu.uniquindio.sga.application.exception.CredencialesInvalidasException;
import co.edu.uniquindio.sga.application.exception.UsuarioNoEncontradoException;
import co.edu.uniquindio.sga.application.port.CifradorContrasenas;
import co.edu.uniquindio.sga.application.port.UsuarioRepository;
import co.edu.uniquindio.sga.application.seguridad.CorreoElectronico;
import co.edu.uniquindio.sga.application.seguridad.Solicitante;
import co.edu.uniquindio.sga.application.seguridad.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/** Cambio voluntario de contraseña con la actual y la nueva (7.2). */
@Service
public class CambiarContrasenaUseCase {
    private final UsuarioRepository usuarios;
    private final CifradorContrasenas cifrador;

    public CambiarContrasenaUseCase(UsuarioRepository usuarios, CifradorContrasenas cifrador) {
        this.usuarios = Objects.requireNonNull(usuarios);
        this.cifrador = Objects.requireNonNull(cifrador);
    }

    @Transactional
    public void ejecutar(String contrasenaActual, String contrasenaNueva, Solicitante solicitante) {
        PoliticaContrasena.verificar(contrasenaNueva);
        Usuario usuario = usuarios.obtenerPorCorreo(new CorreoElectronico(solicitante.usuario()))
                .orElseThrow(() -> new UsuarioNoEncontradoException(solicitante.usuario()));
        if (!cifrador.coincide(contrasenaActual, usuario.getContrasenaCifrada())) {
            throw new CredencialesInvalidasException();
        }
        usuario.cambiarContrasena(cifrador.cifrar(contrasenaNueva));
        usuarios.guardar(usuario);
    }
}
