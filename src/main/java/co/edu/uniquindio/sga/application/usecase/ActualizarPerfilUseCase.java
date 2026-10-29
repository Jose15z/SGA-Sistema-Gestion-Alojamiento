package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.UsuarioNoEncontradoException;
import co.edu.uniquindio.sga.application.port.UsuarioRepository;
import co.edu.uniquindio.sga.application.seguridad.CorreoElectronico;
import co.edu.uniquindio.sga.application.seguridad.Solicitante;
import co.edu.uniquindio.sga.application.seguridad.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/** Actualizar nombre y teléfono del perfil propio; el correo no es editable (7.2). */
@Service
public class ActualizarPerfilUseCase {
    private final UsuarioRepository usuarios;

    public ActualizarPerfilUseCase(UsuarioRepository usuarios) {
        this.usuarios = Objects.requireNonNull(usuarios);
    }

    @Transactional
    public Usuario ejecutar(String nombreCompleto, String telefono, Solicitante solicitante) {
        Usuario usuario = usuarios.obtenerPorCorreo(new CorreoElectronico(solicitante.usuario()))
                .orElseThrow(() -> new UsuarioNoEncontradoException(solicitante.usuario()));
        usuario.actualizarPerfil(nombreCompleto, telefono);
        usuarios.guardar(usuario);
        return usuario;
    }
}
