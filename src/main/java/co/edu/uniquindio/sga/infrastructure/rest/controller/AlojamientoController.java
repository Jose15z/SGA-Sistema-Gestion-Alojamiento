package co.edu.uniquindio.sga.infrastructure.rest.controller;

import co.edu.uniquindio.sga.application.dto.request.ActualizarAlojamientoRequest;
import co.edu.uniquindio.sga.application.dto.response.AlojamientoResponse;
import co.edu.uniquindio.sga.application.usecase.ActualizarAlojamientoUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarAlojamientoUseCase;
import co.edu.uniquindio.sga.domain.repository.ConfiguracionAlojamiento;
import co.edu.uniquindio.sga.domain.valueobject.Ubicacion;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.AlojamientoRestMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Datos del alojamiento, normas y servicios adicionales (7.3). La consulta es pública. */
@RestController
@RequestMapping("/api/alojamiento")
@Tag(name = "Alojamiento")
public class AlojamientoController {

    private final ConsultarAlojamientoUseCase consultar;
    private final ActualizarAlojamientoUseCase actualizar;
    private final ConfiguracionAlojamiento configuracion;
    private final AlojamientoRestMapper mapper;

    public AlojamientoController(ConsultarAlojamientoUseCase consultar, ActualizarAlojamientoUseCase actualizar,
                                 ConfiguracionAlojamiento configuracion, AlojamientoRestMapper mapper) {
        this.consultar = consultar;
        this.actualizar = actualizar;
        this.configuracion = configuracion;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<AlojamientoResponse> consultar() {
        return ResponseEntity.ok(mapper.toResponse(consultar.ejecutar(), configuracion.tiempoPreparacion()));
    }

    @PutMapping
    public ResponseEntity<AlojamientoResponse> actualizar(@Valid @RequestBody ActualizarAlojamientoRequest request) {
        return ResponseEntity.ok(mapper.toResponse(actualizar.ejecutar(request.nombre(), request.descripcion(),
                request.ciudad(), request.direccion(), new Ubicacion(request.latitud(), request.longitud()),
                request.normas(), mapper.toServicios(request.servicios())), configuracion.tiempoPreparacion()));
    }
}
