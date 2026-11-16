package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.port.UsuarioRepository;
import co.edu.uniquindio.sga.application.seguridad.Usuario;
import co.edu.uniquindio.sga.domain.repository.Pagina;
import co.edu.uniquindio.sga.domain.repository.PeticionPagina;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/** Listar usuarios activos, paginados (7.1: gestionar usuarios y roles). */
@Service
public class ListarUsuariosUseCase {
    private final UsuarioRepository usuarios;

    public ListarUsuariosUseCase(UsuarioRepository usuarios) {
        this.usuarios = Objects.requireNonNull(usuarios);
    }

    @Transactional(readOnly = true)
    public Pagina<Usuario> ejecutar(PeticionPagina pagina) {
        return usuarios.listarActivos(pagina);
    }
}
