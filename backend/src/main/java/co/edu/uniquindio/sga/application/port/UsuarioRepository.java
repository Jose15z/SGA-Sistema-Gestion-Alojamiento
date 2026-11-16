package co.edu.uniquindio.sga.application.port;

import co.edu.uniquindio.sga.application.seguridad.CorreoElectronico;
import co.edu.uniquindio.sga.application.seguridad.Usuario;
import co.edu.uniquindio.sga.domain.repository.Pagina;
import co.edu.uniquindio.sga.domain.repository.PeticionPagina;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;

import java.util.Optional;
import java.util.UUID;

/** Puerto de los usuarios. Vive en aplicación porque el dominio no conoce el usuario (3.6). */
public interface UsuarioRepository {

    Optional<Usuario> obtenerPorCorreo(CorreoElectronico correo);

    Optional<Usuario> obtenerPorIdentificacion(UUID identificacion);

    boolean existePorCorreo(CorreoElectronico correo);

    boolean existePorDocumento(DocumentoIdentidad documento);

    /** Usuarios activos, paginados (las eliminaciones son lógicas). */
    Pagina<Usuario> listarActivos(PeticionPagina pagina);

    void guardar(Usuario usuario);
}
