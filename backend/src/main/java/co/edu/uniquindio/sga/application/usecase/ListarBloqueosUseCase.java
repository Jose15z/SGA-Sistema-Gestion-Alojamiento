package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.ApartamentoNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.BloqueoRepository;
import co.edu.uniquindio.sga.domain.repository.Pagina;
import co.edu.uniquindio.sga.domain.repository.PeticionPagina;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/** Listar los bloqueos de un apartamento, paginados. */
@Service
public class ListarBloqueosUseCase {
    private final ApartamentoRepository apartamentos;
    private final BloqueoRepository bloqueos;

    public ListarBloqueosUseCase(ApartamentoRepository apartamentos, BloqueoRepository bloqueos) {
        this.apartamentos = Objects.requireNonNull(apartamentos);
        this.bloqueos = Objects.requireNonNull(bloqueos);
    }

    @Transactional(readOnly = true)
    public Pagina<Bloqueo> ejecutar(IdentificacionApartamento apartamento, PeticionPagina pagina) {
        if (!apartamentos.existe(apartamento)) {
            throw new ApartamentoNoEncontradoException(apartamento);
        }
        return bloqueos.listarPorApartamento(apartamento, pagina);
    }
}
