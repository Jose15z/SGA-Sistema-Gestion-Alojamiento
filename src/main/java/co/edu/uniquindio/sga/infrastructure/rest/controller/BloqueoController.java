package co.edu.uniquindio.sga.infrastructure.rest.controller;

import co.edu.uniquindio.sga.application.dto.request.MotivoRequest;
import co.edu.uniquindio.sga.application.dto.request.RegistrarBloqueoRequest;
import co.edu.uniquindio.sga.application.dto.response.BloqueoResponse;
import co.edu.uniquindio.sga.application.dto.response.PaginaResponse;
import co.edu.uniquindio.sga.application.usecase.LevantarBloqueoUseCase;
import co.edu.uniquindio.sga.application.usecase.ListarBloqueosUseCase;
import co.edu.uniquindio.sga.application.usecase.RegistrarBloqueoUseCase;
import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.repository.PeticionPagina;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.CatalogoRestMapper;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.ValorRestMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
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

/** Bloqueos administrativos de apartamentos (7.3, RN-07). */
@RestController
@RequestMapping("/api")
@Tag(name = "Bloqueos")
public class BloqueoController {

    private final RegistrarBloqueoUseCase registrar;
    private final LevantarBloqueoUseCase levantar;
    private final ListarBloqueosUseCase listar;
    private final CatalogoRestMapper mapper;
    private final ValorRestMapper valorMapper;

    public BloqueoController(RegistrarBloqueoUseCase registrar, LevantarBloqueoUseCase levantar,
                             ListarBloqueosUseCase listar, CatalogoRestMapper mapper, ValorRestMapper valorMapper) {
        this.registrar = registrar;
        this.levantar = levantar;
        this.listar = listar;
        this.mapper = mapper;
        this.valorMapper = valorMapper;
    }

    @PostMapping("/apartamentos/{identificacion}/bloqueos")
    public ResponseEntity<BloqueoResponse> registrar(
            @PathVariable @Pattern(regexp = "^APT-[0-9]{3}$") String identificacion,
            @Valid @RequestBody RegistrarBloqueoRequest request) {
        Bloqueo bloqueo = registrar.ejecutar(new IdentificacionApartamento(identificacion),
                new Estancia(request.fechaEntrada(), request.fechaSalida()), request.motivo());
        return ResponseEntity.created(URI.create("/api/bloqueos/" + bloqueo.getIdentificacion()))
                .body(mapper.toBloqueoResponse(bloqueo));
    }

    @GetMapping("/apartamentos/{identificacion}/bloqueos")
    public ResponseEntity<PaginaResponse<BloqueoResponse>> listar(
            @PathVariable @Pattern(regexp = "^APT-[0-9]{3}$") String identificacion,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(10) int size) {
        return ResponseEntity.ok(valorMapper.toPagina(listar.ejecutar(new IdentificacionApartamento(identificacion),
                new PeticionPagina(page, size)), mapper::toBloqueoResponse));
    }

    @PutMapping("/bloqueos/{identificacion}/levantar")
    public ResponseEntity<BloqueoResponse> levantar(@PathVariable UUID identificacion,
                                                    @Valid @RequestBody MotivoRequest request) {
        return ResponseEntity.ok(mapper.toBloqueoResponse(levantar.ejecutar(identificacion, request.motivo())));
    }
}
