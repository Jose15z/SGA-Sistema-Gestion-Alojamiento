package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.UsuarioNoEncontradoException;
import co.edu.uniquindio.sga.application.port.UsuarioRepository;
import co.edu.uniquindio.sga.application.seguridad.Usuario;
import co.edu.uniquindio.sga.domain.service.TitularReservasService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

/**
 * Eliminación lógica de un usuario (7.2): no procede si es titular de reservas
 * PENDIENTE, CONFIRMADA o EN_CURSO; sus reservas históricas se conservan.
 */
@Service
public class EliminarUsuarioUseCase {
    private final UsuarioRepository usuarios;
    private final TitularReservasService titulares;

    public EliminarUsuarioUseCase(UsuarioRepository usuarios, TitularReservasService titulares) {
        this.usuarios = Objects.requireNonNull(usuarios);
        this.titulares = Objects.requireNonNull(titulares);
    }

    @Transactional
    public Usuario ejecutar(UUID identificacion) {
        Usuario usuario = usuarios.obtenerPorIdentificacion(identificacion)
                .orElseThrow(() -> new UsuarioNoEncontradoException(identificacion));
        titulares.verificarSinReservasActivas(usuario.getDocumento());
        usuario.desactivar();
        usuarios.guardar(usuario);
        return usuario;
    }
}
