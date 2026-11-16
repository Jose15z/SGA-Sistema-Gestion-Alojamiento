package co.edu.uniquindio.sga.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/** Clave externa obligatoria para HS256; nunca genera secretos implícitos. */
@Component
public class ClaveJwt {
    private final SecretKey clave;

    public ClaveJwt(@Value("${SGA_JWT_SECRET:${sga.jwt.secret:}}") String secreto) {
        if (secreto == null || secreto.isBlank()
                || secreto.startsWith("REEMPLAZAR_") || secreto.startsWith("reemplace-")) {
            throw new IllegalStateException("Configure SGA_JWT_SECRET o sga.jwt.secret fuera del repositorio");
        }
        byte[] bytes = secreto.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("sga.jwt.secret debe tener al menos 32 bytes para HS256");
        }
        this.clave = new SecretKeySpec(bytes, "HmacSHA256");
    }

    public SecretKey clave() { return clave; }
}
