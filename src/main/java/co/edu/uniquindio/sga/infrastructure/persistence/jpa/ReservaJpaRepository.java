package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.FiltroReservas;
import co.edu.uniquindio.sga.domain.repository.Pagina;
import co.edu.uniquindio.sga.domain.repository.PeticionPagina;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.IdentificadorExterno;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.ReservaEntity;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.ReservaPersistenceMapper;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.ReservaJpaDataRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/** Adaptador JPA del puerto ReservaRepository (Guía 10, Parte 5). */
@Repository
@Transactional
public class ReservaJpaRepository implements ReservaRepository {

    private static final List<EstadoReserva> ACTIVAS = Arrays.stream(EstadoReserva.values())
            .filter(EstadoReserva::esActiva)
            .toList();

    private final ReservaJpaDataRepository dataRepository;
    private final ReservaPersistenceMapper mapper;

    public ReservaJpaRepository(ReservaJpaDataRepository dataRepository, ReservaPersistenceMapper mapper) {
        this.dataRepository = dataRepository;
        this.mapper = mapper;
    }

    @Override
    public void guardar(Reserva reserva) {
        ReservaEntity entity = mapper.toEntity(reserva);
        // El dominio no conoce el id técnico: si la reserva existe, se reutiliza para actualizar
        dataRepository.findByCodigo(entity.getCodigo()).ifPresent(existente -> entity.setId(existente.getId()));
        dataRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Reserva> obtenerPorCodigo(CodigoReserva codigo) {
        return dataRepository.findByCodigo(codigo.valor()).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reserva> buscarPorEstado(EstadoReserva estado) {
        return dataRepository.findByEstado(estado).stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reserva> buscarActivasPorApartamento(IdentificacionApartamento apartamento) {
        return dataRepository.findByApartamentoIdAndEstadoIn(apartamento.valor(), ACTIVAS).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Pagina<Reserva> buscar(FiltroReservas filtro, PeticionPagina pagina) {
        Sort masRecientePrimero = Sort.by(Sort.Order.desc("fechaCreacion"), Sort.Order.desc("id"));
        return PaginacionJpa.aPagina(dataRepository.buscar(
                filtro.estado(),
                filtro.apartamento() == null ? null : filtro.apartamento().valor(),
                filtro.titular() == null ? null : filtro.titular().numero(),
                filtro.entradaDesde(),
                filtro.entradaHasta(),
                PaginacionJpa.aPageable(pagina, masRecientePrimero)), mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorCanalEIdentificadorExterno(CanalOrigen canal, IdentificadorExterno identificador) {
        return dataRepository.existsByCanalOrigenAndIdentificadorExterno(canal, identificador.valor());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reserva> buscarActivasEnPeriodo(Estancia periodo) {
        return dataRepository.buscarEnPeriodo(ACTIVAS, periodo.fechaEntrada(), periodo.fechaSalida()).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
