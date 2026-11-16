package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;

import java.util.Objects;

/**
 * 7.3: un apartamento solo puede retirarse de la venta (eliminación lógica)
 * si no tiene reservas activas ni futuras.
 *
 * <p>El apartamento sabe si está activo, pero no conoce sus reservas.</p>
 */
public final class RetiroApartamentoService {

    private final ReservaRepository reservas;

    public RetiroApartamentoService(ReservaRepository reservas) {
        this.reservas = Objects.requireNonNull(reservas);
    }

    public void validarRetiro(Apartamento apartamento) {
        if (apartamento == null) {
            throw new ReglaDominioException("El apartamento es obligatorio");
        }
        if (!reservas.buscarActivasPorApartamento(apartamento.getIdentificacion()).isEmpty()) {
            throw new ReglaDominioException(
                    "No se puede retirar un apartamento con reservas activas o futuras");
        }
    }
}
