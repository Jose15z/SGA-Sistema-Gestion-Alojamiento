package co.edu.uniquindio.sga.infrastructure.persistence.inmemory;
import co.edu.uniquindio.sga.domain.entity.*;
import co.edu.uniquindio.sga.domain.repository.*;
import co.edu.uniquindio.sga.domain.valueobject.*;
import java.time.LocalDate;
import java.util.*;

/** Adaptador en memoria para pruebas sin base de datos; no es un bean: la aplicación usa el adaptador JPA (Guía 10). */
public final class ReservaRepositoryEnMemoria implements ReservaRepository {
    private final Map<CodigoReserva, Reserva> datos = new LinkedHashMap<>();
    @Override public Optional<Reserva> obtenerPorCodigo(CodigoReserva identidad) {
        return Optional.ofNullable(datos.get(Objects.requireNonNull(identidad)));
    }
    @Override public void guardar(Reserva entidad) {
        Objects.requireNonNull(entidad);
        datos.put(entidad.getCodigo(), entidad);
    }

    @Override public List<Reserva> buscarPorEstado(EstadoReserva estado) {
        Objects.requireNonNull(estado);
        return datos.values().stream().filter(r -> r.getEstado() == estado).toList();
    }
    @Override public List<Reserva> buscarActivasPorApartamento(IdentificacionApartamento apartamento) {
        Objects.requireNonNull(apartamento);
        return datos.values().stream().filter(r -> r.getApartamento().equals(apartamento)
                && r.getEstado().retieneDisponibilidad()).toList();
    }
    @Override public Pagina<Reserva> buscar(FiltroReservas filtro, PeticionPagina pagina) {
        List<Reserva> filtradas = datos.values().stream()
                .filter(r -> filtro.estado() == null || r.getEstado() == filtro.estado())
                .filter(r -> filtro.apartamento() == null || r.getApartamento().equals(filtro.apartamento()))
                .filter(r -> filtro.canal() == null || r.getCanalOrigen() == filtro.canal())
                .filter(r -> filtro.titular() == null || r.perteneceA(filtro.titular()))
                .filter(r -> filtro.entradaDesde() == null || !r.getEstancia().fechaEntrada().isBefore(filtro.entradaDesde()))
                .filter(r -> filtro.entradaHasta() == null || !r.getEstancia().fechaEntrada().isAfter(filtro.entradaHasta()))
                .sorted(Comparator.comparing((Reserva r) -> r.getFechaCreacion().momento()).reversed())
                .toList();
        return Pagina.de(filtradas, pagina);
    }
    @Override public boolean existePorCanalEIdentificadorExterno(CanalOrigen canal, IdentificadorExterno identificador) {
        return datos.values().stream().anyMatch(r -> r.getCanalOrigen() == canal
                && identificador.equals(r.getIdentificadorExterno()));
    }
    @Override public List<Reserva> buscarLlegadasPrevistas(LocalDate dia) {
        return datos.values().stream().filter(r -> r.getEstancia().fechaEntrada().equals(dia)
                && (r.getEstado() == EstadoReserva.PENDIENTE || r.getEstado() == EstadoReserva.CONFIRMADA)).toList();
    }
    @Override public List<Reserva> buscarSalidasPrevistas(LocalDate dia) {
        return datos.values().stream().filter(r -> r.getEstancia().fechaSalida().equals(dia)
                && r.getEstado() == EstadoReserva.EN_CURSO).toList();
    }
    @Override public boolean existenActivasDelTitular(DocumentoIdentidad titular) {
        return datos.values().stream().anyMatch(r -> r.getEstado().esActiva() && r.perteneceA(titular));
    }
    @Override public List<Reserva> buscarActivasEnPeriodo(Estancia periodo) {
        return datos.values().stream().filter(r -> r.getEstado().esActiva()
                && r.getEstancia().seSolapaCon(periodo)).toList();
    }
}
