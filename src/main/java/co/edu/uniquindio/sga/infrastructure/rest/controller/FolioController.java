package co.edu.uniquindio.sga.infrastructure.rest.controller;

import co.edu.uniquindio.sga.application.dto.request.CerrarFolioRequest;
import co.edu.uniquindio.sga.application.dto.request.RegistrarCargoRequest;
import co.edu.uniquindio.sga.application.dto.request.RegistrarPagoRequest;
import co.edu.uniquindio.sga.application.dto.request.RegistrarServicioFolioRequest;
import co.edu.uniquindio.sga.application.dto.response.CargoResponse;
import co.edu.uniquindio.sga.application.dto.response.FolioResponse;
import co.edu.uniquindio.sga.application.dto.response.PagoResponse;
import co.edu.uniquindio.sga.application.seguridad.Solicitante;
import co.edu.uniquindio.sga.application.usecase.CerrarFolioUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarFolioUseCase;
import co.edu.uniquindio.sga.application.usecase.RegistrarCargoUseCase;
import co.edu.uniquindio.sga.application.usecase.RegistrarPagoUseCase;
import co.edu.uniquindio.sga.application.usecase.RegistrarServicioAdicionalUseCase;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.FolioRestMapper;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/** Folio de la reserva (7.7): cargos, pagos, saldo y cierre. Los movimientos nunca se borran (RN-16). */
@RestController
@RequestMapping("/api/folios")
@Tag(name = "Folios")
public class FolioController {

    private static final String CODIGO = "^RES-[0-9]{4}-[0-9]{5}$";

    private final ConsultarFolioUseCase consultar;
    private final RegistrarPagoUseCase registrarPago;
    private final RegistrarCargoUseCase registrarCargo;
    private final RegistrarServicioAdicionalUseCase registrarServicio;
    private final CerrarFolioUseCase cerrar;
    private final FolioRestMapper mapper;

    public FolioController(ConsultarFolioUseCase consultar, RegistrarPagoUseCase registrarPago,
                           RegistrarCargoUseCase registrarCargo, RegistrarServicioAdicionalUseCase registrarServicio,
                           CerrarFolioUseCase cerrar, FolioRestMapper mapper) {
        this.consultar = consultar;
        this.registrarPago = registrarPago;
        this.registrarCargo = registrarCargo;
        this.registrarServicio = registrarServicio;
        this.cerrar = cerrar;
        this.mapper = mapper;
    }

    @GetMapping("/{codigoReserva}")
    public ResponseEntity<FolioResponse> consultar(@PathVariable @Pattern(regexp = CODIGO) String codigoReserva,
                                                   @Parameter(hidden = true) Solicitante solicitante) {
        return ResponseEntity.ok(mapper.toResponse(consultar.ejecutar(new CodigoReserva(codigoReserva), solicitante)));
    }

    @PostMapping("/{codigoReserva}/pagos")
    public ResponseEntity<PagoResponse> registrarPago(@PathVariable @Pattern(regexp = CODIGO) String codigoReserva,
                                                      @Valid @RequestBody RegistrarPagoRequest request,
                                                      @Parameter(hidden = true) Solicitante solicitante) {
        PagoResponse pago = mapper.toPagoResponse(registrarPago.ejecutar(new CodigoReserva(codigoReserva),
                Dinero.de(request.valor()), request.medio(), request.concepto(), solicitante));
        return ResponseEntity.created(URI.create("/api/folios/" + codigoReserva)).body(pago);
    }

    @PostMapping("/{codigoReserva}/cargos")
    public ResponseEntity<CargoResponse> registrarCargo(@PathVariable @Pattern(regexp = CODIGO) String codigoReserva,
                                                        @Valid @RequestBody RegistrarCargoRequest request,
                                                        @Parameter(hidden = true) Solicitante solicitante) {
        CargoResponse cargo = mapper.toCargoResponse(registrarCargo.ejecutar(new CodigoReserva(codigoReserva),
                request.concepto(), Dinero.de(request.valor()), solicitante));
        return ResponseEntity.created(URI.create("/api/folios/" + codigoReserva)).body(cargo);
    }

    /** L-17: el valor lo calcula el sistema con el catálogo del alojamiento y la cantidad. */
    @PostMapping("/{codigoReserva}/servicios")
    public ResponseEntity<CargoResponse> registrarServicio(@PathVariable @Pattern(regexp = CODIGO) String codigoReserva,
                                                           @Valid @RequestBody RegistrarServicioFolioRequest request,
                                                           @Parameter(hidden = true) Solicitante solicitante) {
        CargoResponse cargo = mapper.toCargoResponse(registrarServicio.ejecutar(new CodigoReserva(codigoReserva),
                request.codigoServicio(), request.cantidad(), solicitante));
        return ResponseEntity.created(URI.create("/api/folios/" + codigoReserva)).body(cargo);
    }

    @PutMapping("/{codigoReserva}/cerrar")
    public ResponseEntity<FolioResponse> cerrar(@PathVariable @Pattern(regexp = CODIGO) String codigoReserva,
                                                @Valid @RequestBody CerrarFolioRequest request,
                                                @Parameter(hidden = true) Solicitante solicitante) {
        return ResponseEntity.ok(mapper.toResponse(cerrar.ejecutar(new CodigoReserva(codigoReserva),
                request.motivo(), solicitante)));
    }
}
