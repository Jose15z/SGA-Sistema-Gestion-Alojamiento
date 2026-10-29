package co.edu.uniquindio.sga.infrastructure.rest.controller;

import co.edu.uniquindio.sga.application.dto.request.ActualizarPerfilRequest;
import co.edu.uniquindio.sga.application.dto.request.AsignarRolRequest;
import co.edu.uniquindio.sga.application.dto.request.CambiarContrasenaRequest;
import co.edu.uniquindio.sga.application.dto.request.CrearUsuarioRequest;
import co.edu.uniquindio.sga.application.dto.response.PaginaResponse;
import co.edu.uniquindio.sga.application.dto.response.UsuarioResponse;
import co.edu.uniquindio.sga.application.seguridad.CorreoElectronico;
import co.edu.uniquindio.sga.application.seguridad.Solicitante;
import co.edu.uniquindio.sga.application.seguridad.Usuario;
import co.edu.uniquindio.sga.application.usecase.ActualizarPerfilUseCase;
import co.edu.uniquindio.sga.application.usecase.AsignarRolUseCase;
import co.edu.uniquindio.sga.application.usecase.CambiarContrasenaUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarPerfilUseCase;
import co.edu.uniquindio.sga.application.usecase.CrearUsuarioUseCase;
import co.edu.uniquindio.sga.application.usecase.EliminarUsuarioUseCase;
import co.edu.uniquindio.sga.application.usecase.ListarUsuariosUseCase;
import co.edu.uniquindio.sga.domain.repository.PeticionPagina;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.UsuarioRestMapper;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.ValorRestMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

/** Perfil del usuario autenticado y gestión de usuarios y roles por el administrador (7.2). */
@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios")
public class UsuarioController {

    private final ConsultarPerfilUseCase consultarPerfil;
    private final ActualizarPerfilUseCase actualizarPerfil;
    private final CambiarContrasenaUseCase cambiarContrasena;
    private final ListarUsuariosUseCase listar;
    private final CrearUsuarioUseCase crear;
    private final AsignarRolUseCase asignarRol;
    private final EliminarUsuarioUseCase eliminar;
    private final UsuarioRestMapper mapper;
    private final ValorRestMapper valorMapper;

    public UsuarioController(ConsultarPerfilUseCase consultarPerfil, ActualizarPerfilUseCase actualizarPerfil,
                             CambiarContrasenaUseCase cambiarContrasena, ListarUsuariosUseCase listar,
                             CrearUsuarioUseCase crear, AsignarRolUseCase asignarRol, EliminarUsuarioUseCase eliminar,
                             UsuarioRestMapper mapper, ValorRestMapper valorMapper) {
        this.consultarPerfil = consultarPerfil;
        this.actualizarPerfil = actualizarPerfil;
        this.cambiarContrasena = cambiarContrasena;
        this.listar = listar;
        this.crear = crear;
        this.asignarRol = asignarRol;
        this.eliminar = eliminar;
        this.mapper = mapper;
        this.valorMapper = valorMapper;
    }

    @GetMapping("/yo")
    public ResponseEntity<UsuarioResponse> consultarPerfil(Solicitante solicitante) {
        return ResponseEntity.ok(mapper.toResponse(consultarPerfil.ejecutar(solicitante)));
    }

    @PutMapping("/yo")
    public ResponseEntity<UsuarioResponse> actualizarPerfil(@Valid @RequestBody ActualizarPerfilRequest request,
                                                            Solicitante solicitante) {
        return ResponseEntity.ok(mapper.toResponse(
                actualizarPerfil.ejecutar(request.nombreCompleto(), request.telefono(), solicitante)));
    }

    @PutMapping("/yo/contrasena")
    public ResponseEntity<Void> cambiarContrasena(@Valid @RequestBody CambiarContrasenaRequest request,
                                                  Solicitante solicitante) {
        cambiarContrasena.ejecutar(request.contrasenaActual(), request.contrasenaNueva(), solicitante);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<PaginaResponse<UsuarioResponse>> listar(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(10) int size) {
        return ResponseEntity.ok(valorMapper.toPagina(listar.ejecutar(new PeticionPagina(page, size)),
                mapper::toResponse));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody CrearUsuarioRequest request) {
        Usuario usuario = crear.ejecutar(new CorreoElectronico(request.correo()),
                new DocumentoIdentidad(request.documento()), request.fechaNacimiento(), request.nombreCompleto(),
                request.telefono(), request.rol(), request.contrasena());
        return ResponseEntity.created(URI.create("/api/usuarios/" + usuario.getIdentificacion()))
                .body(mapper.toResponse(usuario));
    }

    @PutMapping("/{identificacion}/rol")
    public ResponseEntity<UsuarioResponse> asignarRol(@PathVariable UUID identificacion,
                                                      @Valid @RequestBody AsignarRolRequest request) {
        return ResponseEntity.ok(mapper.toResponse(asignarRol.ejecutar(identificacion, request.rol())));
    }

    /** Eliminación lógica: el usuario queda inactivo y sus reservas históricas se conservan. */
    @DeleteMapping("/{identificacion}")
    public ResponseEntity<UsuarioResponse> eliminar(@PathVariable UUID identificacion) {
        return ResponseEntity.ok(mapper.toResponse(eliminar.ejecutar(identificacion)));
    }
}
