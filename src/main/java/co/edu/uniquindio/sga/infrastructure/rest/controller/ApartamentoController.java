package co.edu.uniquindio.sga.infrastructure.rest.controller;

import co.edu.uniquindio.sga.application.dto.request.CambiarCapacidadRequest;
import co.edu.uniquindio.sga.application.dto.request.RegistrarApartamentoRequest;
import co.edu.uniquindio.sga.application.dto.request.TarifaRequest;
import co.edu.uniquindio.sga.application.dto.response.ApartamentoDisponibleResponse;
import co.edu.uniquindio.sga.application.dto.response.ApartamentoResponse;
import co.edu.uniquindio.sga.application.dto.response.PaginaResponse;
import co.edu.uniquindio.sga.application.usecase.ActualizarTarifaApartamentoUseCase;
import co.edu.uniquindio.sga.application.usecase.BuscarApartamentosDisponiblesUseCase;
import co.edu.uniquindio.sga.application.usecase.CambiarCapacidadApartamentoUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarApartamentoUseCase;
import co.edu.uniquindio.sga.application.usecase.DeclararFueraDeServicioUseCase;
import co.edu.uniquindio.sga.application.usecase.IniciarPreparacionApartamentoUseCase;
import co.edu.uniquindio.sga.application.usecase.ListarApartamentosUseCase;
import co.edu.uniquindio.sga.application.usecase.MarcarApartamentoPreparadoUseCase;
import co.edu.uniquindio.sga.application.usecase.ReactivarApartamentoUseCase;
import co.edu.uniquindio.sga.application.usecase.RegistrarApartamentoUseCase;
import co.edu.uniquindio.sga.application.usecase.ReincorporarApartamentoUseCase;
import co.edu.uniquindio.sga.application.usecase.RetirarApartamentoUseCase;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.repository.PeticionPagina;
import co.edu.uniquindio.sga.domain.valueobject.Capacidad;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.ApartamentoRestMapper;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.ValorRestMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;

/** Alojamiento y apartamentos (7.3), tarifas (7.4) y búsqueda de disponibilidad (7.5). */
@RestController
@RequestMapping("/api/apartamentos")
@Tag(name = "Apartamentos")
public class ApartamentoController {

    private static final String ID = "^APT-[0-9]{3}$";

    private final RegistrarApartamentoUseCase registrar;
    private final ConsultarApartamentoUseCase consultar;
    private final ListarApartamentosUseCase listar;
    private final CambiarCapacidadApartamentoUseCase cambiarCapacidad;
    private final ActualizarTarifaApartamentoUseCase actualizarTarifa;
    private final RetirarApartamentoUseCase retirar;
    private final ReactivarApartamentoUseCase reactivar;
    private final IniciarPreparacionApartamentoUseCase iniciarPreparacion;
    private final MarcarApartamentoPreparadoUseCase marcarPreparado;
    private final DeclararFueraDeServicioUseCase declararFueraDeServicio;
    private final ReincorporarApartamentoUseCase reincorporar;
    private final BuscarApartamentosDisponiblesUseCase buscarDisponibles;
    private final ApartamentoRestMapper mapper;
    private final ValorRestMapper valorMapper;

    public ApartamentoController(RegistrarApartamentoUseCase registrar, ConsultarApartamentoUseCase consultar,
                                 ListarApartamentosUseCase listar, CambiarCapacidadApartamentoUseCase cambiarCapacidad,
                                 ActualizarTarifaApartamentoUseCase actualizarTarifa, RetirarApartamentoUseCase retirar,
                                 ReactivarApartamentoUseCase reactivar,
                                 IniciarPreparacionApartamentoUseCase iniciarPreparacion,
                                 MarcarApartamentoPreparadoUseCase marcarPreparado,
                                 DeclararFueraDeServicioUseCase declararFueraDeServicio,
                                 ReincorporarApartamentoUseCase reincorporar,
                                 BuscarApartamentosDisponiblesUseCase buscarDisponibles,
                                 ApartamentoRestMapper mapper, ValorRestMapper valorMapper) {
        this.registrar = registrar;
        this.consultar = consultar;
        this.listar = listar;
        this.cambiarCapacidad = cambiarCapacidad;
        this.actualizarTarifa = actualizarTarifa;
        this.retirar = retirar;
        this.reactivar = reactivar;
        this.iniciarPreparacion = iniciarPreparacion;
        this.marcarPreparado = marcarPreparado;
        this.declararFueraDeServicio = declararFueraDeServicio;
        this.reincorporar = reincorporar;
        this.buscarDisponibles = buscarDisponibles;
        this.mapper = mapper;
        this.valorMapper = valorMapper;
    }

    @GetMapping("/disponibles")
    public ResponseEntity<PaginaResponse<ApartamentoDisponibleResponse>> consultarDisponibles(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaEntrada,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaSalida,
            @RequestParam @Min(1) int totalOcupantes,
            @RequestParam(required = false) @Min(1) Integer dormitoriosMinimos,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(10) int size) {
        var pagina = buscarDisponibles.ejecutar(new Estancia(fechaEntrada, fechaSalida), totalOcupantes,
                dormitoriosMinimos, new PeticionPagina(page, size));
        return ResponseEntity.ok(valorMapper.toPagina(pagina, mapper::toDisponible));
    }

    @PostMapping
    public ResponseEntity<ApartamentoResponse> registrar(@Valid @RequestBody RegistrarApartamentoRequest request) {
        IdentificacionApartamento id = new IdentificacionApartamento(request.identificacion());
        Apartamento apartamento = registrar.ejecutar(id, request.nombre(), request.dormitorios(),
                new Capacidad(request.capacidad()), mapper.toTarifas(id, request.tarifas()));
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(id.valor()).toUri();
        return ResponseEntity.created(ubicacion).body(mapper.toResponse(apartamento));
    }

    @GetMapping
    public ResponseEntity<PaginaResponse<ApartamentoResponse>> listar(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(10) int size) {
        return ResponseEntity.ok(valorMapper.toPagina(listar.ejecutar(new PeticionPagina(page, size)),
                mapper::toResponse));
    }

    @GetMapping("/{identificacion}")
    public ResponseEntity<ApartamentoResponse> consultar(@PathVariable @Pattern(regexp = ID) String identificacion) {
        return ResponseEntity.ok(mapper.toResponse(consultar.ejecutar(new IdentificacionApartamento(identificacion))));
    }

    @PutMapping("/{identificacion}/capacidad")
    public ResponseEntity<ApartamentoResponse> cambiarCapacidad(@PathVariable @Pattern(regexp = ID) String identificacion,
                                                                @Valid @RequestBody CambiarCapacidadRequest request) {
        return ResponseEntity.ok(mapper.toResponse(cambiarCapacidad.ejecutar(
                new IdentificacionApartamento(identificacion), new Capacidad(request.capacidad()))));
    }

    @PutMapping("/{identificacion}/tarifas")
    public ResponseEntity<ApartamentoResponse> actualizarTarifa(@PathVariable @Pattern(regexp = ID) String identificacion,
                                                                @Valid @RequestBody TarifaRequest request) {
        return ResponseEntity.ok(mapper.toResponse(actualizarTarifa.ejecutar(new IdentificacionApartamento(identificacion),
                request.temporada(), Dinero.de(request.valorPorOcupanteNoche()))));
    }

    @PutMapping("/{identificacion}/retirar")
    public ResponseEntity<ApartamentoResponse> retirar(@PathVariable @Pattern(regexp = ID) String identificacion) {
        return ResponseEntity.ok(mapper.toResponse(retirar.ejecutar(new IdentificacionApartamento(identificacion))));
    }

    @PutMapping("/{identificacion}/reactivar")
    public ResponseEntity<ApartamentoResponse> reactivar(@PathVariable @Pattern(regexp = ID) String identificacion) {
        return ResponseEntity.ok(mapper.toResponse(reactivar.ejecutar(new IdentificacionApartamento(identificacion))));
    }

    @PutMapping("/{identificacion}/iniciar-preparacion")
    public ResponseEntity<ApartamentoResponse> iniciarPreparacion(@PathVariable @Pattern(regexp = ID) String identificacion) {
        return ResponseEntity.ok(mapper.toResponse(iniciarPreparacion.ejecutar(new IdentificacionApartamento(identificacion))));
    }

    @PutMapping("/{identificacion}/marcar-preparado")
    public ResponseEntity<ApartamentoResponse> marcarPreparado(@PathVariable @Pattern(regexp = ID) String identificacion) {
        return ResponseEntity.ok(mapper.toResponse(marcarPreparado.ejecutar(new IdentificacionApartamento(identificacion))));
    }

    @PutMapping("/{identificacion}/fuera-de-servicio")
    public ResponseEntity<ApartamentoResponse> declararFueraDeServicio(
            @PathVariable @Pattern(regexp = ID) String identificacion) {
        return ResponseEntity.ok(mapper.toResponse(
                declararFueraDeServicio.ejecutar(new IdentificacionApartamento(identificacion))));
    }

    @PutMapping("/{identificacion}/reincorporar")
    public ResponseEntity<ApartamentoResponse> reincorporar(@PathVariable @Pattern(regexp = ID) String identificacion) {
        return ResponseEntity.ok(mapper.toResponse(reincorporar.ejecutar(new IdentificacionApartamento(identificacion))));
    }
}
