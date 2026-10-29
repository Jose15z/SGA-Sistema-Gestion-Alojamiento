package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.ApartamentoNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.TipoTemporada;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

/** Actualizar la tarifa de una temporada (7.4): queda en el histórico y no altera reservas existentes (RN-22). */
@Service
public class ActualizarTarifaApartamentoUseCase {
    private final ApartamentoRepository apartamentos;

    private final Clock reloj;

    public ActualizarTarifaApartamentoUseCase(ApartamentoRepository apartamentos, Clock reloj) {
        this.apartamentos = Objects.requireNonNull(apartamentos);
        this.reloj = Objects.requireNonNull(reloj);
    }

    @Transactional
    public Apartamento ejecutar(IdentificacionApartamento identificacion, TipoTemporada temporada, Dinero valor) {
        Apartamento apartamento = apartamentos.obtenerPorIdentificacion(identificacion)
                .orElseThrow(() -> new ApartamentoNoEncontradoException(identificacion));
        apartamento.actualizarTarifa(temporada, valor, LocalDateTime.now(reloj));
        apartamentos.guardar(apartamento);
        return apartamento;
    }
}
