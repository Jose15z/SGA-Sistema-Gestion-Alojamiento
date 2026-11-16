package co.edu.uniquindio.sga.infrastructure.rest.controller;

import co.edu.uniquindio.sga.application.dto.request.RegistrarTemporadaRequest;
import co.edu.uniquindio.sga.application.dto.response.PaginaResponse;
import co.edu.uniquindio.sga.application.dto.response.TemporadaResponse;
import co.edu.uniquindio.sga.application.usecase.ListarTemporadasUseCase;
import co.edu.uniquindio.sga.application.usecase.RegistrarTemporadaUseCase;
import co.edu.uniquindio.sga.domain.repository.PeticionPagina;
import co.edu.uniquindio.sga.domain.valueobject.Temporada;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.CatalogoRestMapper;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.ValorRestMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/** Temporadas del calendario (7.4, L-08). */
@RestController
@RequestMapping("/api/temporadas")
@Tag(name = "Temporadas")
public class TemporadaController {

    private final RegistrarTemporadaUseCase registrar;
    private final ListarTemporadasUseCase listar;
    private final CatalogoRestMapper mapper;
    private final ValorRestMapper valorMapper;

    public TemporadaController(RegistrarTemporadaUseCase registrar, ListarTemporadasUseCase listar,
                               CatalogoRestMapper mapper, ValorRestMapper valorMapper) {
        this.registrar = registrar;
        this.listar = listar;
        this.mapper = mapper;
        this.valorMapper = valorMapper;
    }

    @PostMapping
    public ResponseEntity<TemporadaResponse> registrar(@Valid @RequestBody RegistrarTemporadaRequest request) {
        Temporada temporada = registrar.ejecutar(mapper.toTemporada(request));
        return ResponseEntity.created(URI.create("/api/temporadas")).body(mapper.toTemporadaResponse(temporada));
    }

    @GetMapping
    public ResponseEntity<PaginaResponse<TemporadaResponse>> listar(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(10) int size) {
        return ResponseEntity.ok(valorMapper.toPagina(listar.ejecutar(new PeticionPagina(page, size)),
                mapper::toTemporadaResponse));
    }
}
