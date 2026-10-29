package co.edu.uniquindio.sga.infrastructure.rest.exception;

import co.edu.uniquindio.sga.application.dto.response.ErrorResponse;
import co.edu.uniquindio.sga.application.exception.AccesoDenegadoException;
import co.edu.uniquindio.sga.application.exception.CredencialesInvalidasException;
import co.edu.uniquindio.sga.application.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.SolicitudUsuarioInvalidaException;
import co.edu.uniquindio.sga.application.exception.UsuarioYaRegistradoException;
import co.edu.uniquindio.sga.application.seguridad.Usuario;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.List;

/**
 * Traduce cada excepción a su código HTTP con un único {@link ErrorResponse}:
 * <ul>
 *     <li>400: forma del mensaje (Bean Validation, JSON mal formado, parámetros).</li>
 *     <li>401: credenciales inválidas o falta de autenticación.</li>
 *     <li>403: el usuario no es el propietario o su rol no lo permite.</li>
 *     <li>404: recurso inexistente (excepciones de aplicación).</li>
 *     <li>409: regla del negocio violada (excepción del dominio) o correo/documento ya registrados.</li>
 * </ul>
 * Nunca envía trazas técnicas al cliente.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ReglaDominioException.class)
    public ResponseEntity<ErrorResponse> reglaDeNegocio(ReglaDominioException excepcion) {
        return respuesta(HttpStatus.CONFLICT, "REGLA_NEGOCIO_VIOLADA", excepcion.getMessage(), List.of());
    }

    @ExceptionHandler({UsuarioYaRegistradoException.class, Usuario.UsuarioInactivoException.class})
    public ResponseEntity<ErrorResponse> conflictoUsuario(RuntimeException excepcion) {
        return respuesta(HttpStatus.CONFLICT, "CONFLICTO_USUARIO", excepcion.getMessage(), List.of());
    }

    @ExceptionHandler(SolicitudUsuarioInvalidaException.class)
    public ResponseEntity<ErrorResponse> solicitudUsuarioInvalida(SolicitudUsuarioInvalidaException excepcion) {
        return respuesta(HttpStatus.BAD_REQUEST, "SOLICITUD_INVALIDA", excepcion.getMessage(), List.of());
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(RecursoNoEncontradoException excepcion) {
        return respuesta(HttpStatus.NOT_FOUND, "RECURSO_NO_ENCONTRADO", excepcion.getMessage(), List.of());
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ErrorResponse> credencialesInvalidas(CredencialesInvalidasException excepcion) {
        return respuesta(HttpStatus.UNAUTHORIZED, "CREDENCIALES_INVALIDAS", excepcion.getMessage(), List.of());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> noAutenticado(AuthenticationException excepcion) {
        return respuesta(HttpStatus.UNAUTHORIZED, "NO_AUTENTICADO",
                "Se requiere un token de acceso válido y vigente", List.of());
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<ErrorResponse> accesoDenegado(AccesoDenegadoException excepcion) {
        return respuesta(HttpStatus.FORBIDDEN, "ACCESO_DENEGADO", excepcion.getMessage(), List.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> rolInsuficiente(AccessDeniedException excepcion) {
        return respuesta(HttpStatus.FORBIDDEN, "ACCESO_DENEGADO",
                "El rol del usuario no permite realizar esta operación", List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> cuerpoInvalido(MethodArgumentNotValidException excepcion) {
        List<String> detalles = excepcion.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .sorted()
                .toList();
        return respuesta(HttpStatus.BAD_REQUEST, "SOLICITUD_INVALIDA",
                "La solicitud no cumple el formato esperado", detalles);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> parametrosInvalidos(HandlerMethodValidationException excepcion) {
        List<String> detalles = excepcion.getParameterValidationResults().stream()
                .flatMap(resultado -> resultado.getResolvableErrors().stream()
                        .map(error -> resultado.getMethodParameter().getParameterName() + ": "
                                + error.getDefaultMessage()))
                .sorted()
                .toList();
        return respuesta(HttpStatus.BAD_REQUEST, "SOLICITUD_INVALIDA",
                "Los parámetros de la solicitud no son válidos", detalles);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> cuerpoIlegible(HttpMessageNotReadableException excepcion) {
        return respuesta(HttpStatus.BAD_REQUEST, "SOLICITUD_INVALIDA",
                "El cuerpo de la solicitud no es un JSON válido o tiene valores con formato incorrecto", List.of());
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class,
            IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> parametroIncorrecto(Exception excepcion) {
        String detalle = excepcion instanceof MethodArgumentTypeMismatchException tipo
                ? tipo.getName() + ": valor con formato incorrecto"
                : excepcion.getMessage();
        return respuesta(HttpStatus.BAD_REQUEST, "SOLICITUD_INVALIDA",
                "Los parámetros de la solicitud no son válidos", List.of(detalle));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> rutaInexistente(NoResourceFoundException excepcion) {
        return respuesta(HttpStatus.NOT_FOUND, "RECURSO_NO_ENCONTRADO", "La ruta solicitada no existe", List.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> metodoNoPermitido(HttpRequestMethodNotSupportedException excepcion) {
        return respuesta(HttpStatus.METHOD_NOT_ALLOWED, "METODO_NO_PERMITIDO",
                "El método " + excepcion.getMethod() + " no está permitido en esta ruta", List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> inesperado(Exception excepcion) {
        LOG.error("Error no controlado", excepcion);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO",
                "Ocurrió un error inesperado. Intente nuevamente o contacte al administrador", List.of());
    }

    private ResponseEntity<ErrorResponse> respuesta(HttpStatus estado, String codigo, String mensaje,
                                                    List<String> detalles) {
        return ResponseEntity.status(estado)
                .body(new ErrorResponse(estado.value(), codigo, mensaje, detalles, Instant.now()));
    }
}
