package co.edu.uniquindio.sga.infrastructure.config.setup;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.UsuarioEntity;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.UsuarioJpaDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Idempotente: no sobrescribe contraseñas ni roles de cuentas existentes. */
@Component
@EnableConfigurationProperties(DefaultUserProperties.class)
public class DefaultUserInitializer implements ApplicationRunner {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultUserInitializer.class);
    private final DefaultUserProperties propiedades;
    private final UsuarioJpaDataRepository usuarios;
    private final PasswordEncoder encoder;

    public DefaultUserInitializer(DefaultUserProperties propiedades, UsuarioJpaDataRepository usuarios,
                                  PasswordEncoder encoder) {
        this.propiedades = propiedades;
        this.usuarios = usuarios;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int cargados = 0;
        for (var dato : propiedades.usuarios()) {
            String correo = dato.correo().trim().toLowerCase(java.util.Locale.ROOT);
            if (usuarios.existsByCorreo(correo)) { continue; }
            UsuarioEntity usuario = new UsuarioEntity();
            usuario.setIdentificacion(dato.identificacion());
            usuario.setCorreo(correo);
            usuario.setDocumento(dato.documento());
            usuario.setFechaNacimiento(dato.fechaNacimiento());
            usuario.setNombreCompleto(dato.nombreCompleto());
            usuario.setTelefono(dato.telefono());
            usuario.setRol(dato.rol());
            usuario.setContrasenaCifrada(encoder.encode(dato.contrasena()));
            usuario.setActivo(true);
            usuarios.save(usuario);
            cargados++;
        }
        LOG.info("Usuarios iniciales cargados en H2: {}", cargados);
    }
}
