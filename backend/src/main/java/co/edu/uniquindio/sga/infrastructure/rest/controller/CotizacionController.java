package co.edu.uniquindio.sga.infrastructure.rest.controller;

import co.edu.uniquindio.sga.application.dto.request.CotizacionRequest;
import co.edu.uniquindio.sga.application.dto.response.CotizacionResponse;
import co.edu.uniquindio.sga.application.usecase.CotizarEstanciaUseCase;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.ReservaRestMapper;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.ValorRestMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.LocalDate;

/** Cotización de una estancia (RN-05, RN-06, RP-01, RP-03): calcula sin guardar. */
@RestController
@RequestMapping("/api/cotizaciones")
@Tag(name = "Disponibilidad")
public class CotizacionController {

    private final CotizarEstanciaUseCase cotizar;
    private final ValorRestMapper mapper;
    private final ReservaRestMapper reservaMapper;
    private final Clock reloj;

    public CotizacionController(CotizarEstanciaUseCase cotizar, ValorRestMapper mapper,
                                ReservaRestMapper reservaMapper, Clock reloj) {
        this.cotizar = cotizar;
        this.mapper = mapper;
        this.reservaMapper = reservaMapper;
        this.reloj = reloj;
    }

    @PostMapping
    public ResponseEntity<CotizacionResponse> cotizar(@Valid @RequestBody CotizacionRequest request) {
        return ResponseEntity.ok(mapper.toCotizacion(cotizar.ejecutar(
                new IdentificacionApartamento(request.identificacionApartamento()),
                new Estancia(request.fechaEntrada(), request.fechaSalida()),
                reservaMapper.toOcupantesCotizacion(request.ocupantes(), LocalDate.now(reloj)))));
    }
}
