package co.edu.uniquindio.sga.infrastructure.persistence.inmemory;
import co.edu.uniquindio.sga.domain.entity.*;
import co.edu.uniquindio.sga.domain.repository.*;
import co.edu.uniquindio.sga.domain.valueobject.*;
import java.util.*;

/** Adaptador para ejercicios de Guía 06, de un solo hilo y sin durabilidad ni transacciones. */
/** Adaptador en memoria para pruebas sin base de datos; no es un bean: la aplicación usa el adaptador JPA (Guía 10). */
public final class ApartamentoRepositoryEnMemoria implements ApartamentoRepository {
    private final Map<IdentificacionApartamento, Apartamento> datos = new LinkedHashMap<>();
    @Override public Optional<Apartamento> obtenerPorIdentificacion(IdentificacionApartamento identidad) {
        return Optional.ofNullable(datos.get(Objects.requireNonNull(identidad)));
    }
    @Override public void guardar(Apartamento entidad) {
        Objects.requireNonNull(entidad);
        datos.put(entidad.getIdentificacion(), entidad);
    }
    @Override public Pagina<Apartamento> listar(PeticionPagina pagina) {
        return Pagina.de(List.copyOf(datos.values()), pagina);
    }
    @Override public List<Apartamento> buscarActivosConCapacidadPara(int totalOcupantes) {
        return datos.values().stream().filter(a -> a.estaActivo() && a.admite(totalOcupantes)).toList();
    }
    @Override public boolean existe(IdentificacionApartamento identificacion) {
        return datos.containsKey(identificacion);
    }
}
