package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.Pagina;
import co.edu.uniquindio.sga.domain.repository.PeticionPagina;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/** Listar el inventario de apartamentos, activos y retirados, paginado. */
@Service
public class ListarApartamentosUseCase {
    private final ApartamentoRepository apartamentos;

    public ListarApartamentosUseCase(ApartamentoRepository apartamentos) {
        this.apartamentos = Objects.requireNonNull(apartamentos);
    }

    @Transactional(readOnly = true)
    public Pagina<Apartamento> ejecutar(PeticionPagina pagina) {
        return apartamentos.listar(pagina);
    }
}
