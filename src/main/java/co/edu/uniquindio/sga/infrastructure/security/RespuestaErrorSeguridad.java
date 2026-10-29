package co.edu.uniquindio.sga.infrastructure.security;

import co.edu.uniquindio.sga.application.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

/**
 * Traduce los rechazos de Spring Security al mismo {@link ErrorResponse} de la API:
 * 401 sin token o con token inválido/expirado, 403 con rol insuficiente.
 */
@Component
public class RespuestaErrorSeguridad implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final JsonMapper json;

    public RespuestaErrorSeguridad(JsonMapper json) {
        this.json = json;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException excepcion) throws IOException {
        escribir(response, HttpStatus.UNAUTHORIZED, "NO_AUTENTICADO",
                "Se requiere un token de acceso válido y vigente");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException excepcion) throws IOException {
        escribir(response, HttpStatus.FORBIDDEN, "ACCESO_DENEGADO",
                "El rol del usuario no permite realizar esta operación");
    }

    private void escribir(HttpServletResponse response, HttpStatus estado, String codigo, String mensaje)
            throws IOException {
        response.setStatus(estado.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        json.writeValue(response.getOutputStream(),
                new ErrorResponse(estado.value(), codigo, mensaje, List.of(), Instant.now()));
    }
}
