package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.ApartamentoNoEncontradoException;
import co.edu.uniquindio.sga.application.seguridad.Solicitante;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.valueobject.GravedadNovedad;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.NovedadApartamento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

/** Registrar una novedad sobre un apartamento (7.6); el autor sale del token. */
@Service
public class RegistrarNovedadApartamentoUseCase {
    private final ApartamentoRepository apartamentos;
    private final Clock reloj;

    public RegistrarNovedadApartamentoUseCase(ApartamentoRepository apartamentos, Clock reloj) {
        this.apartamentos = Objects.requireNonNull(apartamentos);
        this.reloj = Objects.requireNonNull(reloj);
    }

    @Transactional
    public NovedadApartamento ejecutar(IdentificacionApartamento identificacion, String descripcion,
                                      GravedadNovedad gravedad, Solicitante solicitante) {
        Apartamento apartamento = apartamentos.obtenerPorIdentificacion(identificacion)
                .orElseThrow(() -> new ApartamentoNoEncontradoException(identificacion));
        NovedadApartamento novedad = new NovedadApartamento(LocalDateTime.now(reloj), solicitante.usuario(),
                descripcion, gravedad);
        apartamento.registrarNovedad(novedad);
        apartamentos.guardar(apartamento);
        return novedad;
    }
}
