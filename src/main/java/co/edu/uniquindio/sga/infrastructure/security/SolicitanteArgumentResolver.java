package co.edu.uniquindio.sga.infrastructure.security;

import co.edu.uniquindio.sga.application.seguridad.Solicitante;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.Rol;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Entrega a los controllers el {@link Solicitante} construido a partir del JWT.
 * El autor de cada operación sale del token, nunca del cuerpo de la petición.
 */
@Component
public class SolicitanteArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return Solicitante.class.equals(parameter.getParameterType());
    }

    @Override
    public Solicitante resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                       NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion == null || !(autenticacion.getPrincipal() instanceof Jwt jwt)) {
            throw new AuthenticationCredentialsNotFoundException("Se requiere un token de acceso");
        }
        return new Solicitante(jwt.getSubject(),
                Rol.valueOf(jwt.getClaimAsString(EmisorTokensJwt.CLAIM_ROL)),
                new DocumentoIdentidad(jwt.getClaimAsString(EmisorTokensJwt.CLAIM_DOCUMENTO)));
    }
}
