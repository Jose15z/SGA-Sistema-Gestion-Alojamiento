package co.edu.uniquindio.sga.infrastructure.rest.controller;

import co.edu.uniquindio.sga.application.dto.request.ActualizarApartamentoRequest;
import co.edu.uniquindio.sga.application.dto.request.RegistrarNovedadApartamentoRequest;
import co.edu.uniquindio.sga.application.dto.response.CambioCapacidadResponse;
import co.edu.uniquindio.sga.application.dto.response.NocheCalendarioResponse;
import co.edu.uniquindio.sga.application.dto.response.NovedadApartamentoResponse;
import co.edu.uniquindio.sga.application.dto.response.RegistroTarifaResponse;
import co.edu.uniquindio.sga.application.seguridad.Solicitante;
import co.edu.uniquindio.sga.application.usecase.ActualizarDatosApartamentoUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarCalendarioOcupacionUseCase;
import co.edu.uniquindio.sga.application.usecase.RegistrarNovedadApartamentoUseCase;
import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.ReservaRestMapper;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.Authentication;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
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

/** Apartamentos (7.3), tarifas (7.4), búsqueda y calendario (7.5) y novedades (7.6). */
@RestController
@RequestMapping("/api/apartamentos")
@Tag(name = "Apartamentos")
public class ApartamentoController {

    private static final String ID = "^APT-[0-9]{3}$";
    private static final Set<String> PERSONAL = Set.of("ROLE_ADMINISTRADOR", "ROLE_RECEPCIONISTA");

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
    private final ActualizarDatosApartamentoUseCase actualizarDatos;
    private final ConsultarCalendarioOcupacionUseCase calendario;
    private final RegistrarNovedadApartamentoUseCase registrarNovedad;
    private final ApartamentoRestMapper mapper;
    private final ReservaRestMapper reservaMapper;
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
                                 ActualizarDatosApartamentoUseCase actualizarDatos,
                                 ConsultarCalendarioOcupacionUseCase calendario,
                                 RegistrarNovedadApartamentoUseCase registrarNovedad,
                                 ApartamentoRestMapper mapper, ReservaRestMapper reservaMapper,
                                 ValorRestMapper valorMapper) {
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
        this.actualizarDatos = actualizarDatos;
        this.calendario = calendario;
        this.registrarNovedad = registrarNovedad;
        this.mapper = mapper;
        this.reservaMapper = reservaMapper;
        this.valorMapper = valorMapper;
    }

    @GetMapping("/disponibles")
    public ResponseEntity<PaginaResponse<ApartamentoDisponibleResponse>> consultarDisponibles(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaEntrada,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaSalida,
            @RequestParam @Min(1) @Max(50) int totalOcupantes,
            @RequestParam(required = false) @Size(max = 50) List<@Past LocalDate> fechasNacimiento,
            @RequestParam(required = false) @Min(1) Integer capacidadMinima,
            @RequestParam(required = false) @Min(1) Integer dormitoriosMinimos,
            @RequestParam(required = false) @PositiveOrZero BigDecimal precioMinimo,
            @RequestParam(required = false) @PositiveOrZero BigDecimal precioMaximo,
            @RequestParam(required = false) @Size(max = 10) List<@NotBlank String> caracteristicas,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(10) int size) {
        var filtros = new BuscarApartamentosDisponiblesUseCase.Filtros(capacidadMinima, dormitoriosMinimos,
                precioMinimo, precioMaximo, caracteristicas);
        var pagina = buscarDisponibles.ejecutar(new Estancia(fechaEntrada, fechaSalida), totalOcupantes,
                fechasNacimiento, filtros, new PeticionPagina(page, size));
        return ResponseEntity.ok(valorMapper.toPagina(pagina, mapper::toDisponible));
    }

    @PostMapping
    public ResponseEntity<ApartamentoResponse> registrar(@Valid @RequestBody RegistrarApartamentoRequest request) {
        IdentificacionApartamento id = new IdentificacionApartamento(request.identificacion());
        Apartamento apartamento = registrar.ejecutar(id, request.nombre(), request.descripcion(),
                request.dormitorios(), new Capacidad(request.capacidad()), request.caracteristicas(),
                mapper.toTarifas(id, request.tarifas()));
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(id.valor()).toUri();
        return ResponseEntity.created(ubicacion).body(mapper.toResponse(apartamento));
    }

    /** Ignora los retirados salvo que se pidan; el personal de servicio filtra por estado operativo (7.1). */
    @GetMapping
    public ResponseEntity<PaginaResponse<ApartamentoResponse>> listar(
            @RequestParam(required = false) EstadoOperativo estadoOperativo,
            @RequestParam(defaultValue = "false") boolean incluirRetirados,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(10) int size) {
        return ResponseEntity.ok(valorMapper.toPagina(
                listar.ejecutar(estadoOperativo, incluirRetirados, new PeticionPagina(page, size)),
                mapper::toResponse));
    }

    @GetMapping("/{identificacion}")
    public ResponseEntity<ApartamentoResponse> consultar(@PathVariable @Pattern(regexp = ID) String identificacion) {
        return ResponseEntity.ok(mapper.toResponse(consultar.ejecutar(new IdentificacionApartamento(identificacion))));
    }

    @PutMapping("/{identificacion}")
    public ResponseEntity<ApartamentoResponse> actualizarDatos(@PathVariable @Pattern(regexp = ID) String identificacion,
                                                               @Valid @RequestBody ActualizarApartamentoRequest request) {
        return ResponseEntity.ok(mapper.toResponse(actualizarDatos.ejecutar(new IdentificacionApartamento(identificacion),
                request.nombre(), request.descripcion(), request.dormitorios(), request.caracteristicas())));
    }

    /** Calendario de disponibilidad (público) y de ocupación (con la reserva, para el personal). */
    @GetMapping("/{identificacion}/calendario")
    public ResponseEntity<List<NocheCalendarioResponse>> calendario(
            @PathVariable @Pattern(regexp = ID) String identificacion,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Authentication autenticacion) {
        boolean personal = autenticacion != null && autenticacion.getAuthorities().stream()
                .anyMatch(autoridad -> PERSONAL.contains(autoridad.getAuthority()));
        return ResponseEntity.ok(calendario.ejecutar(new IdentificacionApartamento(identificacion),
                        new Estancia(desde, hasta)).stream()
                .map(noche -> mapper.toNoche(noche, personal))
                .toList());
    }

    @GetMapping("/{identificacion}/tarifas/historial")
    public ResponseEntity<List<RegistroTarifaResponse>> historialTarifas(
            @PathVariable @Pattern(regexp = ID) String identificacion) {
        return ResponseEntity.ok(consultar.ejecutar(new IdentificacionApartamento(identificacion))
                .getHistorialTarifas().stream().map(mapper::toRegistroTarifa).toList());
    }

    @PostMapping("/{identificacion}/novedades")
    public ResponseEntity<NovedadApartamentoResponse> registrarNovedad(
            @PathVariable @Pattern(regexp = ID) String identificacion,
            @Valid @RequestBody RegistrarNovedadApartamentoRequest request,
            @Parameter(hidden = true) Solicitante solicitante) {
        var novedad = registrarNovedad.ejecutar(new IdentificacionApartamento(identificacion), request.descripcion(),
                request.gravedad(), solicitante);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
        return ResponseEntity.created(ubicacion).body(mapper.toNovedad(novedad));
    }

    @GetMapping("/{identificacion}/novedades")
    public ResponseEntity<List<NovedadApartamentoResponse>> novedades(
            @PathVariable @Pattern(regexp = ID) String identificacion) {
        return ResponseEntity.ok(consultar.ejecutar(new IdentificacionApartamento(identificacion))
                .getNovedades().stream().map(mapper::toNovedad).toList());
    }

    /** 7.3: las reservas ya creadas se conservan; si alguna supera la nueva capacidad, se advierte y se lista. */
    @PutMapping("/{identificacion}/capacidad")
    public ResponseEntity<CambioCapacidadResponse> cambiarCapacidad(
            @PathVariable @Pattern(regexp = ID) String identificacion,
            @Valid @RequestBody CambiarCapacidadRequest request) {
        var resultado = cambiarCapacidad.ejecutar(new IdentificacionApartamento(identificacion),
                new Capacidad(request.capacidad()));
        String advertencia = resultado.reservasExcedidas().isEmpty() ? null
                : resultado.reservasExcedidas().size() + " reserva(s) activa(s) superan la nueva capacidad y se conservan";
        return ResponseEntity.ok(new CambioCapacidadResponse(mapper.toResponse(resultado.apartamento()), advertencia,
                resultado.reservasExcedidas().stream().map(reservaMapper::toResumen).toList()));
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
