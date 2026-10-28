package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.ApartamentoNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/** Operación: FUERA_DE_SERVICIO a PENDIENTE_PREPARACION. */
@Service
public class ReincorporarApartamentoUseCase {
    private final ApartamentoRepository apartamentos;

    public ReincorporarApartamentoUseCase(ApartamentoRepository apartamentos) {
        this.apartamentos = Objects.requireNonNull(apartamentos);
    }

    @Transactional
    public Apartamento ejecutar(IdentificacionApartamento identificacion) {
        Apartamento apartamento = apartamentos.obtenerPorIdentificacion(identificacion)
                .orElseThrow(() -> new ApartamentoNoEncontradoException(identificacion));
        apartamento.reincorporarAPreparacion();
        apartamentos.guardar(apartamento);
        return apartamento;
    }
}
