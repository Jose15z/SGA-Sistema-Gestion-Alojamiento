package co.edu.uniquindio.sga.infrastructure.rest.controller;

import co.edu.uniquindio.sga.application.dto.request.InicioSesionRequest;
import co.edu.uniquindio.sga.application.dto.request.RegistroHuespedRequest;
import co.edu.uniquindio.sga.application.dto.response.TokenResponse;
import co.edu.uniquindio.sga.application.dto.response.UsuarioResponse;
import co.edu.uniquindio.sga.application.usecase.IniciarSesionUseCase;
import co.edu.uniquindio.sga.application.usecase.RegistrarHuespedUseCase;
import co.edu.uniquindio.sga.domain.entity.Usuario;
import co.edu.uniquindio.sga.domain.valueobject.CorreoElectronico;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.UsuarioRestMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/** Usuarios y seguridad (7.2): registro público de huéspedes e inicio de sesión. */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación")
public class AutenticacionController {

    private final RegistrarHuespedUseCase registrarHuesped;
    private final IniciarSesionUseCase iniciarSesion;
    private final UsuarioRestMapper mapper;

    public AutenticacionController(RegistrarHuespedUseCase registrarHuesped, IniciarSesionUseCase iniciarSesion,
                                   UsuarioRestMapper mapper) {
        this.registrarHuesped = registrarHuesped;
        this.iniciarSesion = iniciarSesion;
        this.mapper = mapper;
    }

    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponse> registrarHuesped(@Valid @RequestBody RegistroHuespedRequest request) {
        Usuario usuario = registrarHuesped.ejecutar(new CorreoElectronico(request.correo()),
                new DocumentoIdentidad(request.documento()), request.nombreCompleto(), request.contrasena());
        return ResponseEntity.created(URI.create("/api/auth/usuarios/" + usuario.getIdentificacion()))
                .body(mapper.toResponse(usuario));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> iniciarSesion(@Valid @RequestBody InicioSesionRequest request) {
        return ResponseEntity.ok(mapper.toToken(
                iniciarSesion.ejecutar(new CorreoElectronico(request.correo()), request.contrasena())));
    }
}
