package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.port.CifradorContrasenas;
import co.edu.uniquindio.sga.domain.entity.Usuario;
import co.edu.uniquindio.sga.domain.repository.UsuarioRepository;
import co.edu.uniquindio.sga.domain.service.RegistroUsuarioService;
import co.edu.uniquindio.sga.domain.valueobject.CorreoElectronico;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

/** Registro público de huéspedes (7.2): la contraseña se guarda cifrada con BCrypt. */
@Service
public class RegistrarHuespedUseCase {
    private final UsuarioRepository usuarios;
    private final RegistroUsuarioService registro;
    private final CifradorContrasenas cifrador;

    public RegistrarHuespedUseCase(UsuarioRepository usuarios, RegistroUsuarioService registro,
                                   CifradorContrasenas cifrador) {
        this.usuarios = Objects.requireNonNull(usuarios);
        this.registro = Objects.requireNonNull(registro);
        this.cifrador = Objects.requireNonNull(cifrador);
    }

    @Transactional
    public Usuario ejecutar(CorreoElectronico correo, DocumentoIdentidad documento, String nombreCompleto,
                            String contrasena) {
        registro.verificarDisponible(correo, documento);
        Usuario usuario = Usuario.registrarHuesped(UUID.randomUUID(), correo, documento, nombreCompleto,
                cifrador.cifrar(contrasena));
        usuarios.guardar(usuario);
        return usuario;
    }
}
