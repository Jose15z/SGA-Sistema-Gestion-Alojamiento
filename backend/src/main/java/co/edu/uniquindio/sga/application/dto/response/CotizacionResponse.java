package co.edu.uniquindio.sga.application.dto.response;

import java.math.BigDecimal;
import java.util.List;

/** Cotización de una estancia o valor congelado de una reserva. */
public record CotizacionResponse(
        Long subtotal,
        BigDecimal porcentajeDescuento,
        Long valorDescuento,
        Long valorTotal,
        int totalNoches,
        List<CargoNocheResponse> detalleNoches
) {
}
