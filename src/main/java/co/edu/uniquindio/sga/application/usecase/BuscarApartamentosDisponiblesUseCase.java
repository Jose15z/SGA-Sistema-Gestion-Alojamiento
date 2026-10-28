package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.Pagina;
import co.edu.uniquindio.sga.domain.repository.PeticionPagina;
import co.edu.uniquindio.sga.domain.service.DisponibilidadApartamentoService;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Buscar apartamentos disponibles por fechas y composición del grupo (7.5).
 *
 * <p>El repositorio solo prefiltra los activos con capacidad suficiente; la
 * decisión de disponibilidad (RN-01, RN-02, RN-07, RN-20) la toma el servicio
 * de dominio para cada candidato.</p>
 */
@Service
public class BuscarApartamentosDisponiblesUseCase {
    private final ApartamentoRepository apartamentos;
    private final DisponibilidadApartamentoService disponibilidad;

    public BuscarApartamentosDisponiblesUseCase(ApartamentoRepository apartamentos,
                                                DisponibilidadApartamentoService disponibilidad) {
        this.apartamentos = Objects.requireNonNull(apartamentos);
        this.disponibilidad = Objects.requireNonNull(disponibilidad);
    }

    /**
     * @param dormitoriosMinimos filtro opcional por número de dormitorios (null no filtra)
     */
    @Transactional(readOnly = true)
    public Pagina<Apartamento> ejecutar(Estancia estancia, int totalOcupantes, Integer dormitoriosMinimos,
                                        PeticionPagina pagina) {
        List<Apartamento> disponibles = apartamentos.buscarActivosConCapacidadPara(totalOcupantes).stream()
                .filter(apartamento -> dormitoriosMinimos == null
                        || apartamento.getDormitorios() >= dormitoriosMinimos)
                .filter(apartamento -> disponibilidad.estaDisponible(apartamento, estancia, totalOcupantes))
                .sorted(Comparator.comparing(apartamento -> apartamento.getIdentificacion().valor()))
                .toList();
        return Pagina.de(disponibles, pagina);
    }
}
