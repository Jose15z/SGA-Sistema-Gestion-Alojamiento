package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.ApartamentoNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.service.DisponibilidadApartamentoService;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.NocheCalendario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/** Calendario de ocupación de un apartamento en un rango de fechas (7.1, 7.5). */
@Service
public class ConsultarCalendarioOcupacionUseCase {

    /** Un calendario abarca a lo sumo un año, para que la consulta siga siendo acotada. */
    static final int MAXIMO_NOCHES = 366;

    private final ApartamentoRepository apartamentos;
    private final DisponibilidadApartamentoService disponibilidad;

    public ConsultarCalendarioOcupacionUseCase(ApartamentoRepository apartamentos,
                                               DisponibilidadApartamentoService disponibilidad) {
        this.apartamentos = Objects.requireNonNull(apartamentos);
        this.disponibilidad = Objects.requireNonNull(disponibilidad);
    }

    @Transactional(readOnly = true)
    public List<NocheCalendario> ejecutar(IdentificacionApartamento identificacion, Estancia rango) {
        if (rango.noches() > MAXIMO_NOCHES) {
            throw new IllegalArgumentException("El rango del calendario no puede superar " + MAXIMO_NOCHES + " noches");
        }
        Apartamento apartamento = apartamentos.obtenerPorIdentificacion(identificacion)
                .orElseThrow(() -> new ApartamentoNoEncontradoException(identificacion));
        return disponibilidad.calendario(apartamento, rango);
    }
}
