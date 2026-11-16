package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.TarifarioRepository;
import co.edu.uniquindio.sga.domain.repository.TemporadaRepository;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.Tarifa;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Adaptador del puerto TarifarioRepository sobre los datos persistidos de
 * temporadas y apartamentos. No decide nada: qué temporada aplica lo resuelve
 * {@code CalendarioTemporadas.tipoDe} (L-08) y qué tarifa corresponde lo resuelve
 * {@code Apartamento.tarifaPara} (7.4), ambos en el dominio.
 */
@Repository
@Transactional(readOnly = true)
public class TarifarioRepositoryAdapter implements TarifarioRepository {

    private final ApartamentoRepository apartamentos;
    private final TemporadaRepository temporadas;

    public TarifarioRepositoryAdapter(ApartamentoRepository apartamentos, TemporadaRepository temporadas) {
        this.apartamentos = apartamentos;
        this.temporadas = temporadas;
    }

    @Override
    public Tarifa tarifaDe(IdentificacionApartamento identificacion, LocalDate noche) {
        Apartamento apartamento = apartamentos.obtenerPorIdentificacion(identificacion)
                .orElseThrow(() -> new ReglaDominioException(
                        "El apartamento " + identificacion.valor() + " no existe en el tarifario"));
        return apartamento.tarifaPara(temporadas.obtenerCalendario().tipoDe(noche));
    }
}
