package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.Pagina;
import co.edu.uniquindio.sga.domain.repository.PeticionPagina;
import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Listar apartamentos (7.3), paginado. Ignora los retirados salvo que el administrador
 * los pida; el personal de servicio filtra por estado operativo (7.1).
 */
@Service
public class ListarApartamentosUseCase {
    private final ApartamentoRepository apartamentos;

    public ListarApartamentosUseCase(ApartamentoRepository apartamentos) {
        this.apartamentos = Objects.requireNonNull(apartamentos);
    }

    @Transactional(readOnly = true)
    public Pagina<Apartamento> ejecutar(EstadoOperativo estado, boolean incluirRetirados, PeticionPagina pagina) {
        return apartamentos.listar(estado, incluirRetirados, pagina);
    }
}
