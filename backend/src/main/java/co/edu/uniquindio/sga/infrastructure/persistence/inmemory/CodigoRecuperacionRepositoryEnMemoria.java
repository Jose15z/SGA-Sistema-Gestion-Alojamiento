package co.edu.uniquindio.sga.infrastructure.persistence.inmemory;

import co.edu.uniquindio.sga.application.port.CodigoRecuperacionRepository;
import co.edu.uniquindio.sga.application.seguridad.CodigoRecuperacion;
import co.edu.uniquindio.sga.application.seguridad.CorreoElectronico;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Adaptador en memoria para pruebas sin base de datos; no es un bean: la aplicación usa el adaptador JPA (Guía 10). */
public final class CodigoRecuperacionRepositoryEnMemoria implements CodigoRecuperacionRepository {
    private final Map<UUID, CodigoRecuperacion> datos = new LinkedHashMap<>();

    @Override public List<CodigoRecuperacion> buscarNoUsadosPorCorreo(CorreoElectronico correo) {
        return datos.values().stream().filter(c -> !c.estaUsado() && c.getCorreo().equals(correo)).toList();
    }
    @Override public void guardar(CodigoRecuperacion codigo) { datos.put(codigo.getIdentificacion(), codigo); }
}
