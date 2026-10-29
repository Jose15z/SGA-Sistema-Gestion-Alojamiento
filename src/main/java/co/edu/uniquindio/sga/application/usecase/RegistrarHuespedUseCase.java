package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.seguridad.PoliticaContrasena;
import co.edu.uniquindio.sga.application.exception.UsuarioYaRegistradoException;
import co.edu.uniquindio.sga.application.port.CifradorContrasenas;
import co.edu.uniquindio.sga.application.port.UsuarioRepository;
import co.edu.uniquindio.sga.application.seguridad.CorreoElectronico;
import co.edu.uniquindio.sga.application.seguridad.Usuario;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/** Registro público de huéspedes (7.2): correo único y contraseña cifrada con BCrypt. */
@Service
public class RegistrarHuespedUseCase {
    private final UsuarioRepository usuarios;
    private final CifradorContrasenas cifrador;

    public RegistrarHuespedUseCase(UsuarioRepository usuarios, CifradorContrasenas cifrador) {
        this.usuarios = Objects.requireNonNull(usuarios);
        this.cifrador = Objects.requireNonNull(cifrador);
    }

    @Transactional
    public Usuario ejecutar(CorreoElectronico correo, DocumentoIdentidad documento, LocalDate fechaNacimiento,
                            String nombreCompleto, String telefono, String contrasena) {
        PoliticaContrasena.verificar(contrasena);
        verificarDisponible(usuarios, correo, documento);
        Usuario usuario = Usuario.registrarHuesped(UUID.randomUUID(), correo, documento, fechaNacimiento,
                nombreCompleto, telefono, cifrador.cifrar(contrasena));
        usuarios.guardar(usuario);
        return usuario;
    }

    /** El correo y el documento de un usuario son únicos (7.2). */
    static void verificarDisponible(UsuarioRepository usuarios, CorreoElectronico correo, DocumentoIdentidad documento) {
        if (usuarios.existePorCorreo(correo)) {
            throw new UsuarioYaRegistradoException("Ya existe un usuario registrado con el correo " + correo.valor());
        }
        if (usuarios.existePorDocumento(documento)) {
            throw new UsuarioYaRegistradoException("Ya existe un usuario registrado con ese documento");
        }
    }
}
