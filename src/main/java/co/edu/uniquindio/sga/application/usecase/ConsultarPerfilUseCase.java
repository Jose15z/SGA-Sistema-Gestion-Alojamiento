package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.UsuarioNoEncontradoException;
import co.edu.uniquindio.sga.application.port.UsuarioRepository;
import co.edu.uniquindio.sga.application.seguridad.CorreoElectronico;
import co.edu.uniquindio.sga.application.seguridad.Solicitante;
import co.edu.uniquindio.sga.application.seguridad.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/** Consultar el perfil del usuario autenticado (7.2). */
@Service
public class ConsultarPerfilUseCase {
    private final UsuarioRepository usuarios;

    public ConsultarPerfilUseCase(UsuarioRepository usuarios) {
        this.usuarios = Objects.requireNonNull(usuarios);
    }

    @Transactional(readOnly = true)
    public Usuario ejecutar(Solicitante solicitante) {
        return usuarios.obtenerPorCorreo(new CorreoElectronico(solicitante.usuario()))
                .orElseThrow(() -> new UsuarioNoEncontradoException(solicitante.usuario()));
    }
}
