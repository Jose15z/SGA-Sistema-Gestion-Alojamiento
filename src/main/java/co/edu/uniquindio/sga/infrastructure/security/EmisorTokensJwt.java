package co.edu.uniquindio.sga.infrastructure.security;

import co.edu.uniquindio.sga.application.port.EmisorTokens;
import co.edu.uniquindio.sga.application.seguridad.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/** Emite el JWT (HS256) con el rol y el documento del usuario y una expiración. */
@Component
public class EmisorTokensJwt implements EmisorTokens {

    static final String CLAIM_ROL = "rol";
    static final String CLAIM_DOCUMENTO = "documento";

    private final JwtEncoder encoder;
    private final Clock reloj;
    private final Duration vigencia;
    private final String emisor;

    public EmisorTokensJwt(JwtEncoder encoder, Clock reloj,
                           @Value("${sga.seguridad.jwt.expiracion-minutos}") long minutos,
                           @Value("${sga.seguridad.jwt.emisor}") String emisor) {
        this.encoder = encoder;
        this.reloj = reloj;
        this.vigencia = Duration.ofMinutes(minutos);
        this.emisor = emisor;
    }

    @Override
    public TokenSesion emitir(Usuario usuario) {
        Instant ahora = reloj.instant();
        Instant expiraEn = ahora.plus(vigencia);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(emisor)
                .subject(usuario.getCorreo().valor())
                .issuedAt(ahora)
                .expiresAt(expiraEn)
                .claim(CLAIM_ROL, usuario.getRol().name())
                .claim(CLAIM_DOCUMENTO, usuario.getDocumento().numero())
                .build();
        JwsHeader cabecera = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(cabecera, claims)).getTokenValue();
        return new TokenSesion(token, expiraEn);
    }
}
