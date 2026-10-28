package co.edu.uniquindio.sga.application.port;

import co.edu.uniquindio.sga.domain.entity.Usuario;

import java.time.Instant;

/** Puerto técnico que emite el token de sesión (JWT) de un usuario autenticado. */
public interface EmisorTokens {

    TokenSesion emitir(Usuario usuario);

    /**
     * @param token      token firmado
     * @param expiraEn   instante de expiración
     */
    record TokenSesion(String token, Instant expiraEn) {
    }
}
