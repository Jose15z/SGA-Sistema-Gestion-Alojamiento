package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.seguridad.PoliticaContrasena;
import co.edu.uniquindio.sga.application.exception.SolicitudUsuarioInvalidaException;
import co.edu.uniquindio.sga.application.port.CifradorContrasenas;
import co.edu.uniquindio.sga.application.port.CodigoRecuperacionRepository;
import co.edu.uniquindio.sga.application.port.UsuarioRepository;
import co.edu.uniquindio.sga.application.seguridad.CodigoRecuperacion;
import co.edu.uniquindio.sga.application.seguridad.CorreoElectronico;
import co.edu.uniquindio.sga.application.seguridad.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

/** Restablece la contraseña con el código vigente y de un solo uso (7.2). */
@Service
public class RestablecerContrasenaUseCase {
    private final UsuarioRepository usuarios;
    private final CodigoRecuperacionRepository codigos;
    private final CifradorContrasenas cifrador;
    private final Clock reloj;

    public RestablecerContrasenaUseCase(UsuarioRepository usuarios, CodigoRecuperacionRepository codigos,
                                        CifradorContrasenas cifrador, Clock reloj) {
        this.usuarios = Objects.requireNonNull(usuarios);
        this.codigos = Objects.requireNonNull(codigos);
        this.cifrador = Objects.requireNonNull(cifrador);
        this.reloj = Objects.requireNonNull(reloj);
    }

    @Transactional
    public void ejecutar(CorreoElectronico correo, String codigo, String contrasenaNueva) {
        PoliticaContrasena.verificar(contrasenaNueva);
        LocalDateTime ahora = LocalDateTime.now(reloj);
        CodigoRecuperacion vigente = codigos.buscarNoUsadosPorCorreo(correo).stream()
                .filter(candidato -> candidato.estaVigente(ahora))
                .filter(candidato -> cifrador.coincide(codigo, candidato.getCodigoCifrado()))
                .findFirst()
                .orElseThrow(() -> new SolicitudUsuarioInvalidaException(
                        "El código de recuperación no es válido, ya se usó o venció"));
        Usuario usuario = usuarios.obtenerPorCorreo(correo)
                .orElseThrow(() -> new SolicitudUsuarioInvalidaException("El código de recuperación no es válido"));
        usuario.cambiarContrasena(cifrador.cifrar(contrasenaNueva));
        vigente.marcarUsado();
        usuarios.guardar(usuario);
        codigos.guardar(vigente);
    }
}
