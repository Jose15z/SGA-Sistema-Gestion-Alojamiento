package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.ApartamentoNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.service.RetiroApartamentoService;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/** Retirar apartamento de la venta (7.3, eliminación lógica 12.4): sin reservas activas ni futuras. */
@Service
public class RetirarApartamentoUseCase {
    private final ApartamentoRepository apartamentos;
    private final RetiroApartamentoService retiro;

    public RetirarApartamentoUseCase(ApartamentoRepository apartamentos, RetiroApartamentoService retiro) {
        this.apartamentos = Objects.requireNonNull(apartamentos);
        this.retiro = Objects.requireNonNull(retiro);
    }

    @Transactional
    public Apartamento ejecutar(IdentificacionApartamento identificacion) {
        Apartamento apartamento = apartamentos.obtenerPorIdentificacion(identificacion)
                .orElseThrow(() -> new ApartamentoNoEncontradoException(identificacion));
        retiro.validarRetiro(apartamento);
        apartamento.desactivar();
        apartamentos.guardar(apartamento);
        return apartamento;
    }
}
