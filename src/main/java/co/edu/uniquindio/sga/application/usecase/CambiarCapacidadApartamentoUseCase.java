package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.ApartamentoNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.Capacidad;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/** Cambiar la capacidad (7.3): no afecta las reservas ya creadas. */
@Service
public class CambiarCapacidadApartamentoUseCase {
    private final ApartamentoRepository apartamentos;

    public CambiarCapacidadApartamentoUseCase(ApartamentoRepository apartamentos) {
        this.apartamentos = Objects.requireNonNull(apartamentos);
    }

    @Transactional
    public Apartamento ejecutar(IdentificacionApartamento identificacion, Capacidad capacidad) {
        Apartamento apartamento = apartamentos.obtenerPorIdentificacion(identificacion)
                .orElseThrow(() -> new ApartamentoNoEncontradoException(identificacion));
        apartamento.cambiarCapacidad(capacidad);
        apartamentos.guardar(apartamento);
        return apartamento;
    }
}
