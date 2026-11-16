package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.ApartamentoNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.BloqueoRepository;
import co.edu.uniquindio.sga.domain.service.BloqueoApartamentoService;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

/** Registrar bloqueo (7.3, RN-07): no sobre noches con reservas activas. */
@Service
public class RegistrarBloqueoUseCase {
    private final ApartamentoRepository apartamentos;
    private final BloqueoRepository bloqueos;
    private final BloqueoApartamentoService validacion;

    public RegistrarBloqueoUseCase(ApartamentoRepository apartamentos, BloqueoRepository bloqueos,
                                   BloqueoApartamentoService validacion) {
        this.apartamentos = Objects.requireNonNull(apartamentos);
        this.bloqueos = Objects.requireNonNull(bloqueos);
        this.validacion = Objects.requireNonNull(validacion);
    }

    @Transactional
    public Bloqueo ejecutar(IdentificacionApartamento apartamento, Estancia periodo, String motivo) {
        if (!apartamentos.existe(apartamento)) {
            throw new ApartamentoNoEncontradoException(apartamento);
        }
        validacion.validarBloqueo(apartamento, periodo);
        Bloqueo bloqueo = new Bloqueo(UUID.randomUUID(), apartamento, periodo, motivo);
        bloqueos.guardar(bloqueo);
        return bloqueo;
    }
}
