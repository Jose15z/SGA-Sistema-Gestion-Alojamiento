package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.entity.Usuario;
import co.edu.uniquindio.sga.domain.valueobject.CorreoElectronico;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;

import java.util.Optional;

/** Puerto del agregado Usuario (7.2). */
public interface UsuarioRepository {

    Optional<Usuario> obtenerPorCorreo(CorreoElectronico correo);

    boolean existePorCorreo(CorreoElectronico correo);

    boolean existePorDocumento(DocumentoIdentidad documento);

    void guardar(Usuario usuario);
}
