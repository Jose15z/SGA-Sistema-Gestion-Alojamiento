package co.edu.uniquindio.sga.infrastructure.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 * Clave HMAC con la que se firman y verifican los JWT.
 *
 * <p>Nunca se versiona: llega por la variable de entorno {@code SGA_JWT_SECRETO}.
 * Si no está definida, se genera una clave aleatoria al arrancar (los tokens
 * dejan de servir al reiniciar, lo que es aceptable en desarrollo).</p>
 */
@Component
public class ClaveJwt {

    private static final Logger LOG = LoggerFactory.getLogger(ClaveJwt.class);
    private static final int LONGITUD_MINIMA = 32;

    private final SecretKey clave;

    public ClaveJwt(@Value("${sga.seguridad.jwt.secreto:}") String secreto) {
        byte[] bytes;
        if (secreto == null || secreto.isBlank()) {
            bytes = new byte[LONGITUD_MINIMA];
            new SecureRandom().nextBytes(bytes);
            LOG.warn("SGA_JWT_SECRETO no está definida: se generó una clave JWT aleatoria para esta ejecución");
        } else {
            bytes = secreto.getBytes(StandardCharsets.UTF_8);
            if (bytes.length < LONGITUD_MINIMA) {
                throw new IllegalStateException("SGA_JWT_SECRETO debe tener al menos 32 caracteres");
            }
        }
        this.clave = new SecretKeySpec(bytes, "HmacSHA256");
    }

    public SecretKey clave() {
        return clave;
    }
}
