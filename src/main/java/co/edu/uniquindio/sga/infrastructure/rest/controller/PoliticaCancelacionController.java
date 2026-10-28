package co.edu.uniquindio.sga.infrastructure.rest.controller;

import co.edu.uniquindio.sga.application.dto.request.PublicarPoliticaRequest;
import co.edu.uniquindio.sga.application.dto.response.PoliticaCancelacionResponse;
import co.edu.uniquindio.sga.application.usecase.ConsultarPoliticaVigenteUseCase;
import co.edu.uniquindio.sga.application.usecase.PublicarPoliticaCancelacionUseCase;
import co.edu.uniquindio.sga.domain.entity.PoliticaCancelacion;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.CatalogoRestMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/** Política de cancelación versionada (7.4, L-10, RN-13). */
@RestController
@RequestMapping("/api/politicas-cancelacion")
@Tag(name = "Política de cancelación")
public class PoliticaCancelacionController {

    private final ConsultarPoliticaVigenteUseCase consultarVigente;
    private final PublicarPoliticaCancelacionUseCase publicar;
    private final CatalogoRestMapper mapper;

    public PoliticaCancelacionController(ConsultarPoliticaVigenteUseCase consultarVigente,
                                         PublicarPoliticaCancelacionUseCase publicar, CatalogoRestMapper mapper) {
        this.consultarVigente = consultarVigente;
        this.publicar = publicar;
        this.mapper = mapper;
    }

    @GetMapping("/vigente")
    public ResponseEntity<PoliticaCancelacionResponse> consultarVigente() {
        return ResponseEntity.ok(mapper.toPoliticaResponse(consultarVigente.ejecutar()));
    }

    @PostMapping
    public ResponseEntity<PoliticaCancelacionResponse> publicar(@Valid @RequestBody PublicarPoliticaRequest request) {
        PoliticaCancelacion politica = publicar.ejecutar(
                request.tramos().stream().map(mapper::toTramo).toList(),
                request.porcentajeNoShow(), request.horaEntrada());
        return ResponseEntity.created(URI.create("/api/politicas-cancelacion/vigente"))
                .body(mapper.toPoliticaResponse(politica));
    }
}
