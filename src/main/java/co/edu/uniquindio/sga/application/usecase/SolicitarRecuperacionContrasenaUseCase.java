package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.port.CifradorContrasenas;
import co.edu.uniquindio.sga.application.port.CodigoRecuperacionRepository;
import co.edu.uniquindio.sga.application.port.ConfiguracionSeguridad;
import co.edu.uniquindio.sga.application.port.NotificadorCorreo;
import co.edu.uniquindio.sga.application.port.UsuarioRepository;
import co.edu.uniquindio.sga.application.seguridad.CodigoRecuperacion;
import co.edu.uniquindio.sga.application.seguridad.CorreoElectronico;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Recuperación de contraseña (7.2): genera un código de un solo uso con vigencia
 * configurable (15 minutos) y lo envía al correo registrado.
 *
 * <p>Responde igual exista o no el correo, para no revelar qué cuentas existen.
 * Si el envío del correo falla, el código queda registrado y el fallo en el log (I-05).</p>
 */
@Service
public class SolicitarRecuperacionContrasenaUseCase {
    private static final Logger LOG = LoggerFactory.getLogger(SolicitarRecuperacionContrasenaUseCase.class);
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final UsuarioRepository usuarios;
    private final CodigoRecuperacionRepository codigos;
    private final CifradorContrasenas cifrador;
    private final NotificadorCorreo notificador;
    private final Clock reloj;
    private final ConfiguracionSeguridad configuracion;

    public SolicitarRecuperacionContrasenaUseCase(UsuarioRepository usuarios, CodigoRecuperacionRepository codigos,
                                                  CifradorContrasenas cifrador, NotificadorCorreo notificador,
                                                  Clock reloj, ConfiguracionSeguridad configuracion) {
        this.usuarios = Objects.requireNonNull(usuarios);
        this.codigos = Objects.requireNonNull(codigos);
        this.cifrador = Objects.requireNonNull(cifrador);
        this.notificador = Objects.requireNonNull(notificador);
        this.reloj = Objects.requireNonNull(reloj);
        this.configuracion = Objects.requireNonNull(configuracion);
    }

    @Transactional
    public void ejecutar(CorreoElectronico correo) {
        boolean existe = usuarios.obtenerPorCorreo(correo).filter(u -> u.puedeIniciarSesion()).isPresent();
        if (!existe) {
            return;
        }
        int vigenciaMinutos = configuracion.vigenciaCodigoRecuperacionMinutos();
        String codigo = String.format("%06d", ALEATORIO.nextInt(1_000_000));
        codigos.guardar(new CodigoRecuperacion(UUID.randomUUID(), correo, cifrador.cifrar(codigo),
                LocalDateTime.now(reloj).plusMinutes(vigenciaMinutos), false));
        try {
            notificador.enviarCodigoRecuperacion(correo, codigo, vigenciaMinutos);
        } catch (RuntimeException fallo) {
            LOG.warn("No se pudo enviar el código de recuperación a {}: {}", correo.valor(), fallo.getMessage());
        }
    }
}
