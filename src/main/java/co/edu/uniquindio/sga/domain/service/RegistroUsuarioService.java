package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.UsuarioRepository;
import co.edu.uniquindio.sga.domain.valueobject.CorreoElectronico;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;

import java.util.Objects;

/**
 * 7.2: el correo y el documento de un usuario son únicos. Un usuario
 * no puede comprobarlo por sí solo porque requiere conocer a los demás.
 */
public final class RegistroUsuarioService {

    private final UsuarioRepository usuarios;

    public RegistroUsuarioService(UsuarioRepository usuarios) {
        this.usuarios = Objects.requireNonNull(usuarios);
    }

    public void verificarDisponible(CorreoElectronico correo, DocumentoIdentidad documento) {
        if (correo == null || documento == null) {
            throw new ReglaDominioException("El registro requiere correo y documento");
        }
        if (usuarios.existePorCorreo(correo)) {
            throw new ReglaDominioException("Ya existe un usuario registrado con el correo " + correo.valor());
        }
        if (usuarios.existePorDocumento(documento)) {
            throw new ReglaDominioException("Ya existe un usuario registrado con ese documento");
        }
    }
}
