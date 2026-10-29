package co.edu.uniquindio.sga.application.seguridad;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Código de recuperación de contraseña (7.2): se guarda cifrado, vence a los
 * 15 minutos (configurable) y se usa una sola vez.
 */
public final class CodigoRecuperacion {

    private final UUID identificacion;
    private final CorreoElectronico correo;
    private final String codigoCifrado;
    private final LocalDateTime expiraEn;
    private boolean usado;

    public CodigoRecuperacion(UUID identificacion, CorreoElectronico correo, String codigoCifrado,
                              LocalDateTime expiraEn, boolean usado) {
        this.identificacion = Objects.requireNonNull(identificacion);
        this.correo = Objects.requireNonNull(correo);
        this.codigoCifrado = Objects.requireNonNull(codigoCifrado);
        this.expiraEn = Objects.requireNonNull(expiraEn);
        this.usado = usado;
    }

    public boolean estaVigente(LocalDateTime ahora) {
        return !usado && !ahora.isAfter(expiraEn);
    }

    public void marcarUsado() {
        usado = true;
    }

    public UUID getIdentificacion() { return identificacion; }
    public CorreoElectronico getCorreo() { return correo; }
    public String getCodigoCifrado() { return codigoCifrado; }
    public LocalDateTime getExpiraEn() { return expiraEn; }
    public boolean estaUsado() { return usado; }
}
