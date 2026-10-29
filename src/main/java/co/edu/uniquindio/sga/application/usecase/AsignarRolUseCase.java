package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.UsuarioNoEncontradoException;
import co.edu.uniquindio.sga.application.port.UsuarioRepository;
import co.edu.uniquindio.sga.application.seguridad.Rol;
import co.edu.uniquindio.sga.application.seguridad.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

/** Los roles internos solo los asigna un administrador (7.2). */
@Service
public class AsignarRolUseCase {
    private final UsuarioRepository usuarios;

    public AsignarRolUseCase(UsuarioRepository usuarios) {
        this.usuarios = Objects.requireNonNull(usuarios);
    }

    @Transactional
    public Usuario ejecutar(UUID identificacion, Rol rol) {
        Usuario usuario = usuarios.obtenerPorIdentificacion(identificacion)
                .orElseThrow(() -> new UsuarioNoEncontradoException(identificacion));
        usuario.asignarRol(rol);
        usuarios.guardar(usuario);
        return usuario;
    }
}
