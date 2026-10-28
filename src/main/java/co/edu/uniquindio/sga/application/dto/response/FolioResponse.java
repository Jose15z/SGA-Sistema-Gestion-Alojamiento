package co.edu.uniquindio.sga.application.dto.response;

import java.util.List;

/** Cuenta de la reserva: saldo = cargos - pagos (RN-15). */
public record FolioResponse(
        String codigoReserva,
        Long saldo,
        Long totalPagado,
        boolean cerrado,
        String autorizacionCierre,
        List<CargoResponse> cargos,
        List<PagoResponse> pagos
) {
}
