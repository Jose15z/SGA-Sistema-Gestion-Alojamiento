package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.seguridad.PoliticaContrasena;
import co.edu.uniquindio.sga.application.port.CifradorContrasenas;
import co.edu.uniquindio.sga.application.port.UsuarioRepository;
import co.edu.uniquindio.sga.application.seguridad.CorreoElectronico;
import co.edu.uniquindio.sga.application.seguridad.Rol;
import co.edu.uniquindio.sga.application.seguridad.Usuario;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/** El administrador crea usuarios con roles internos (7.1, 7.2). */
@Service
public class CrearUsuarioUseCase {
    private final UsuarioRepository usuarios;
    private final CifradorContrasenas cifrador;

    public CrearUsuarioUseCase(UsuarioRepository usuarios, CifradorContrasenas cifrador) {
        this.usuarios = Objects.requireNonNull(usuarios);
        this.cifrador = Objects.requireNonNull(cifrador);
    }

    @Transactional
    public Usuario ejecutar(CorreoElectronico correo, DocumentoIdentidad documento, LocalDate fechaNacimiento,
                            String nombreCompleto, String telefono, Rol rol, String contrasenaInicial) {
        PoliticaContrasena.verificar(contrasenaInicial);
        RegistrarHuespedUseCase.verificarDisponible(usuarios, correo, documento);
        Usuario usuario = Usuario.crearConRol(UUID.randomUUID(), correo, documento, fechaNacimiento,
                nombreCompleto, telefono, rol, cifrador.cifrar(contrasenaInicial));
        usuarios.guardar(usuario);
        return usuario;
    }
}
