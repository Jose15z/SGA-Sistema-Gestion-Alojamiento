package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.ApartamentoNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/** Actualizar nombre, descripción, dormitorios y características de un apartamento (7.3). */
@Service
public class ActualizarDatosApartamentoUseCase {
    private final ApartamentoRepository apartamentos;

    public ActualizarDatosApartamentoUseCase(ApartamentoRepository apartamentos) {
        this.apartamentos = Objects.requireNonNull(apartamentos);
    }

    @Transactional
    public Apartamento ejecutar(IdentificacionApartamento identificacion, String nombre, String descripcion,
                                int dormitorios, List<String> caracteristicas) {
        Apartamento apartamento = apartamentos.obtenerPorIdentificacion(identificacion)
                .orElseThrow(() -> new ApartamentoNoEncontradoException(identificacion));
        apartamento.actualizarDatos(nombre, descripcion, dormitorios, caracteristicas);
        apartamentos.guardar(apartamento);
        return apartamento;
    }
}
