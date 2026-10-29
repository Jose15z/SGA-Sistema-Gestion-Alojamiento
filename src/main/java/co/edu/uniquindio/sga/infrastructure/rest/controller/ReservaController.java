package co.edu.uniquindio.sga.infrastructure.rest.controller;

import co.edu.uniquindio.sga.application.dto.request.CrearReservaRequest;
import co.edu.uniquindio.sga.application.dto.request.HoraLlegadaRequest;
import co.edu.uniquindio.sga.application.dto.request.ModificarReservaRequest;
import co.edu.uniquindio.sga.application.dto.request.MotivoRequest;
import co.edu.uniquindio.sga.application.dto.request.NovedadLlegadaRequest;
import co.edu.uniquindio.sga.application.dto.request.RegistrarVehiculoRequest;
import co.edu.uniquindio.sga.application.dto.response.MovimientosDelDiaResponse;
import co.edu.uniquindio.sga.application.dto.response.PaginaResponse;
import co.edu.uniquindio.sga.application.dto.response.ReservaDetalleResponse;
import co.edu.uniquindio.sga.application.dto.response.ReservaResumenResponse;
import co.edu.uniquindio.sga.application.seguridad.Solicitante;
import co.edu.uniquindio.sga.application.usecase.CancelarReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.ConfirmarReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarMovimientosDelDiaUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarReservasPorEstadoUseCase;
import co.edu.uniquindio.sga.application.usecase.CrearReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.DeclararNoShowUseCase;
import co.edu.uniquindio.sga.application.usecase.IndicarHoraLlegadaUseCase;
import co.edu.uniquindio.sga.application.usecase.InformarNovedadLlegadaUseCase;
import co.edu.uniquindio.sga.application.usecase.ModificarReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.RegistrarLlegadaUseCase;
import co.edu.uniquindio.sga.application.usecase.RegistrarSalidaUseCase;
import co.edu.uniquindio.sga.application.usecase.RegistrarVehiculoUseCase;
import co.edu.uniquindio.sga.application.usecase.ResolverNovedadLlegadaUseCase;
import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.FiltroReservas;
import co.edu.uniquindio.sga.domain.repository.PeticionPagina;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.Vehiculo;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.ReservaRestMapper;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.ValorRestMapper;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
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
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * Reservas (7.5) y su operación (7.6). Controller delgado: valida la forma con
 * {@code @Valid}, traduce con el mapper y delega; las reglas viven en el dominio.
 * No hay endpoint genérico de cambio de estado ni DELETE: una reserva nunca se borra.
 */
@RestController
@RequestMapping("/api/reservas")
@Tag(name = "Reservas")
public class ReservaController {

    private static final String CODIGO = "^RES-[0-9]{4}-[0-9]{5}$";

    private final CrearReservaUseCase crear;
    private final ConsultarReservaUseCase consultar;
    private final ConsultarReservasPorEstadoUseCase consultarReservas;
    private final ConfirmarReservaUseCase confirmar;
    private final CancelarReservaUseCase cancelar;
    private final IndicarHoraLlegadaUseCase indicarHoraLlegada;
    private final InformarNovedadLlegadaUseCase informarNovedad;
    private final ResolverNovedadLlegadaUseCase resolverNovedad;
    private final ModificarReservaUseCase modificar;
    private final RegistrarVehiculoUseCase registrarVehiculo;
    private final RegistrarLlegadaUseCase registrarLlegada;
    private final RegistrarSalidaUseCase registrarSalida;
    private final DeclararNoShowUseCase declararNoShow;
    private final ConsultarMovimientosDelDiaUseCase movimientosDelDia;
    private final ReservaRestMapper mapper;
    private final ValorRestMapper valorMapper;
    private final Clock reloj;

    public ReservaController(CrearReservaUseCase crear, ConsultarReservaUseCase consultar,
                             ConsultarReservasPorEstadoUseCase consultarReservas, ConfirmarReservaUseCase confirmar,
                             CancelarReservaUseCase cancelar, IndicarHoraLlegadaUseCase indicarHoraLlegada,
                             InformarNovedadLlegadaUseCase informarNovedad,
                             ResolverNovedadLlegadaUseCase resolverNovedad, ModificarReservaUseCase modificar,
                             RegistrarVehiculoUseCase registrarVehiculo, RegistrarLlegadaUseCase registrarLlegada,
                             RegistrarSalidaUseCase registrarSalida, DeclararNoShowUseCase declararNoShow,
                             ConsultarMovimientosDelDiaUseCase movimientosDelDia, ReservaRestMapper mapper, ValorRestMapper valorMapper, Clock reloj) {
        this.crear = crear;
        this.consultar = consultar;
        this.consultarReservas = consultarReservas;
        this.confirmar = confirmar;
        this.cancelar = cancelar;
        this.indicarHoraLlegada = indicarHoraLlegada;
        this.informarNovedad = informarNovedad;
        this.resolverNovedad = resolverNovedad;
        this.modificar = modificar;
        this.registrarVehiculo = registrarVehiculo;
        this.registrarLlegada = registrarLlegada;
        this.registrarSalida = registrarSalida;
        this.declararNoShow = declararNoShow;
        this.movimientosDelDia = movimientosDelDia;
        this.mapper = mapper;
        this.valorMapper = valorMapper;
        this.reloj = reloj;
    }

    @PostMapping
    public ResponseEntity<ReservaDetalleResponse> crear(@Valid @RequestBody CrearReservaRequest request,
                                                        @Parameter(hidden = true) Solicitante solicitante) {
        List<Ocupante> grupo = mapper.toGrupo(request.titular(), request.acompanantes(), LocalDate.now(reloj));
        Reserva reserva = crear.ejecutar(
                new IdentificacionApartamento(request.identificacionApartamento()),
                new Estancia(request.fechaEntrada(), request.fechaSalida()),
                grupo.getFirst(), grupo, request.canalOrigen(),
                mapper.toIdentificadorExterno(request.identificadorExterno()),
                request.horaEstimadaLlegada(),
                request.mascotas() == null ? 0 : request.mascotas(),
                solicitante);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest().path("/{codigo}")
                .buildAndExpand(reserva.getCodigo().valor()).toUri();
        return ResponseEntity.created(ubicacion).body(mapper.toDetalle(reserva));
    }

    @GetMapping
    public ResponseEntity<PaginaResponse<ReservaResumenResponse>> listar(
            @RequestParam(required = false) EstadoReserva estado,
            @RequestParam(required = false) @Pattern(regexp = "^APT-[0-9]{3}$") String apartamento,
            @RequestParam(required = false) CanalOrigen canal,
            @RequestParam(required = false) @Size(max = 30) String titular,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate entradaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate entradaHasta,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(10) int size,
            @Parameter(hidden = true) Solicitante solicitante) {
        FiltroReservas filtro = new FiltroReservas(estado,
                apartamento == null ? null : new IdentificacionApartamento(apartamento), canal,
                titular == null || titular.isBlank() ? null : new DocumentoIdentidad(titular),
                entradaDesde, entradaHasta);
        return ResponseEntity.ok(valorMapper.toPagina(
                consultarReservas.ejecutar(filtro, new PeticionPagina(page, size), solicitante), mapper::toResumen));
    }

    /** 7.6: llegadas y salidas previstas del día (por defecto, hoy). */
    @GetMapping("/movimientos-del-dia")
    public ResponseEntity<MovimientosDelDiaResponse> movimientosDelDia(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        var resultado = movimientosDelDia.ejecutar(fecha == null ? LocalDate.now(reloj) : fecha);
        return ResponseEntity.ok(new MovimientosDelDiaResponse(resultado.dia(),
                resultado.llegadas().stream().map(mapper::toResumen).toList(),
                resultado.salidas().stream().map(mapper::toResumen).toList()));
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<ReservaDetalleResponse> consultar(@PathVariable @Pattern(regexp = CODIGO) String codigo,
                                                            @Parameter(hidden = true) Solicitante solicitante) {
        return ResponseEntity.ok(mapper.toDetalle(consultar.ejecutar(new CodigoReserva(codigo), solicitante)));
    }

    @PutMapping("/{codigo}/confirmar")
    public ResponseEntity<ReservaDetalleResponse> confirmar(@PathVariable @Pattern(regexp = CODIGO) String codigo,
                                                            @Parameter(hidden = true) Solicitante solicitante) {
        return ResponseEntity.ok(mapper.toDetalle(confirmar.ejecutar(new CodigoReserva(codigo), solicitante)));
    }

    @PutMapping("/{codigo}/cancelar")
    public ResponseEntity<ReservaDetalleResponse> cancelar(@PathVariable @Pattern(regexp = CODIGO) String codigo,
                                                           @Valid @RequestBody MotivoRequest request,
                                                           @Parameter(hidden = true) Solicitante solicitante) {
        return ResponseEntity.ok(mapper.toDetalle(
                cancelar.ejecutar(new CodigoReserva(codigo), request.motivo(), solicitante)));
    }

    @PutMapping("/{codigo}/hora-llegada")
    public ResponseEntity<ReservaDetalleResponse> indicarHoraLlegada(
            @PathVariable @Pattern(regexp = CODIGO) String codigo, @Valid @RequestBody HoraLlegadaRequest request,
            @Parameter(hidden = true) Solicitante solicitante) {
        return ResponseEntity.ok(mapper.toDetalle(indicarHoraLlegada.ejecutar(new CodigoReserva(codigo),
                request.horaEstimadaLlegada(), solicitante)));
    }

    @PutMapping("/{codigo}/novedad-llegada")
    public ResponseEntity<ReservaDetalleResponse> informarNovedad(
            @PathVariable @Pattern(regexp = CODIGO) String codigo, @Valid @RequestBody NovedadLlegadaRequest request,
            @Parameter(hidden = true) Solicitante solicitante) {
        return ResponseEntity.ok(mapper.toDetalle(
                informarNovedad.ejecutar(new CodigoReserva(codigo), request.novedad(), solicitante)));
    }

    @PutMapping("/{codigo}/resolver-novedad")
    public ResponseEntity<ReservaDetalleResponse> resolverNovedad(
            @PathVariable @Pattern(regexp = CODIGO) String codigo, @Valid @RequestBody MotivoRequest request,
            @Parameter(hidden = true) Solicitante solicitante) {
        return ResponseEntity.ok(mapper.toDetalle(
                resolverNovedad.ejecutar(new CodigoReserva(codigo), request.motivo(), solicitante)));
    }

    /** 7.5: fechas, acompañantes o apartamento; un campo ausente conserva su valor. */
    @PutMapping("/{codigo}/modificacion")
    public ResponseEntity<ReservaDetalleResponse> modificar(
            @PathVariable @Pattern(regexp = CODIGO) String codigo, @Valid @RequestBody ModificarReservaRequest request,
            @Parameter(hidden = true) Solicitante solicitante) {
        if ((request.fechaEntrada() == null) != (request.fechaSalida() == null)) {
            throw new IllegalArgumentException("Para cambiar las fechas indique la fecha de entrada y la de salida");
        }
        LocalDate hoy = LocalDate.now(reloj);
        return ResponseEntity.ok(mapper.toDetalle(modificar.ejecutar(new CodigoReserva(codigo),
                request.identificacionApartamento() == null ? null
                        : new IdentificacionApartamento(request.identificacionApartamento()),
                request.fechaEntrada() == null ? null : new Estancia(request.fechaEntrada(), request.fechaSalida()),
                request.acompanantes() == null ? null
                        : request.acompanantes().stream().map(a -> mapper.toOcupante(a, hoy)).toList(),
                solicitante)));
    }

    @PostMapping("/{codigo}/vehiculos")
    public ResponseEntity<ReservaDetalleResponse> registrarVehiculo(
            @PathVariable @Pattern(regexp = CODIGO) String codigo, @Valid @RequestBody RegistrarVehiculoRequest request,
            @Parameter(hidden = true) Solicitante solicitante) {
        Reserva reserva = registrarVehiculo.ejecutar(new CodigoReserva(codigo), new Vehiculo(request.placa()),
                solicitante);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/reservas/{codigo}")
                .buildAndExpand(codigo).toUri();
        return ResponseEntity.created(ubicacion).body(mapper.toDetalle(reserva));
    }

    @PutMapping("/{codigo}/registrar-llegada")
    public ResponseEntity<ReservaDetalleResponse> registrarLlegada(
            @PathVariable @Pattern(regexp = CODIGO) String codigo, @Parameter(hidden = true) Solicitante solicitante) {
        return ResponseEntity.ok(mapper.toDetalle(registrarLlegada.ejecutar(new CodigoReserva(codigo), solicitante)));
    }

    @PutMapping("/{codigo}/registrar-salida")
    public ResponseEntity<ReservaDetalleResponse> registrarSalida(
            @PathVariable @Pattern(regexp = CODIGO) String codigo, @Parameter(hidden = true) Solicitante solicitante) {
        return ResponseEntity.ok(mapper.toDetalle(registrarSalida.ejecutar(new CodigoReserva(codigo), solicitante)));
    }

    @PutMapping("/{codigo}/no-show")
    public ResponseEntity<ReservaDetalleResponse> declararNoShow(
            @PathVariable @Pattern(regexp = CODIGO) String codigo, @Parameter(hidden = true) Solicitante solicitante) {
        return ResponseEntity.ok(mapper.toDetalle(declararNoShow.ejecutar(new CodigoReserva(codigo), solicitante)));
    }
}
