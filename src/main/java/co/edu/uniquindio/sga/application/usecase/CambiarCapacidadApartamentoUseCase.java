package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.ApartamentoNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.Capacidad;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Cambiar la capacidad (7.3): no afecta las reservas ya creadas, aunque queden
 * por encima de la nueva capacidad. Esas reservas se devuelven como advertencia.
 */
@Service
public class CambiarCapacidadApartamentoUseCase {
    private final ApartamentoRepository apartamentos;
    private final ReservaRepository reservas;

    public CambiarCapacidadApartamentoUseCase(ApartamentoRepository apartamentos, ReservaRepository reservas) {
        this.apartamentos = Objects.requireNonNull(apartamentos);
        this.reservas = Objects.requireNonNull(reservas);
    }

    @Transactional
    public Resultado ejecutar(IdentificacionApartamento identificacion, Capacidad capacidad) {
        Apartamento apartamento = apartamentos.obtenerPorIdentificacion(identificacion)
                .orElseThrow(() -> new ApartamentoNoEncontradoException(identificacion));
        apartamento.cambiarCapacidad(capacidad);
        apartamentos.guardar(apartamento);
        List<Reserva> excedidas = reservas.buscarActivasPorApartamento(identificacion).stream()
                .filter(reserva -> !apartamento.admite(reserva.totalOcupantes()))
                .toList();
        return new Resultado(apartamento, excedidas);
    }

    /**
     * @param apartamento        apartamento con la nueva capacidad
     * @param reservasExcedidas  reservas activas que superan la nueva capacidad y se conservan
     */
    public record Resultado(Apartamento apartamento, List<Reserva> reservasExcedidas) {
    }
}
