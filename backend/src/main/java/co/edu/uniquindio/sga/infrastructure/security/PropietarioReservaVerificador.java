package co.edu.uniquindio.sga.infrastructure.security;

import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/** Regla de acceso por titular, sin modificar el dominio ni revelar códigos ajenos. */
@Component("propietarioReserva")
public class PropietarioReservaVerificador {
    private final ReservaRepository reservas;

    public PropietarioReservaVerificador(ReservaRepository reservas) { this.reservas = reservas; }

    public boolean esTitular(String codigo, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)
                || codigo == null || !codigo.matches("^RES-[0-9]{4}-[0-9]{5}$")) {
            return false;
        }
        String documento = jwt.getClaimAsString("documento");
        if (documento == null || documento.isBlank()) { return false; }
        return reservas.obtenerPorCodigo(new CodigoReserva(codigo))
                .map(reserva -> reserva.perteneceA(new DocumentoIdentidad(documento)))
                .orElse(false);
    }
}
