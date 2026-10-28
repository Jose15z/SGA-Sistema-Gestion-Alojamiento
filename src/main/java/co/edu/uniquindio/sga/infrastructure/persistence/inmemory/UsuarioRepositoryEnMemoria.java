package co.edu.uniquindio.sga.infrastructure.persistence.inmemory;

import co.edu.uniquindio.sga.domain.entity.Usuario;
import co.edu.uniquindio.sga.domain.repository.UsuarioRepository;
import co.edu.uniquindio.sga.domain.valueobject.CorreoElectronico;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Adaptador en memoria para pruebas sin base de datos; no es un bean: la aplicación usa el adaptador JPA (Guía 10). */
public final class UsuarioRepositoryEnMemoria implements UsuarioRepository {
    private final Map<UUID, Usuario> datos = new LinkedHashMap<>();

    @Override public Optional<Usuario> obtenerPorCorreo(CorreoElectronico correo) {
        return datos.values().stream().filter(u -> u.getCorreo().equals(correo)).findFirst();
    }
    @Override public boolean existePorCorreo(CorreoElectronico correo) { return obtenerPorCorreo(correo).isPresent(); }
    @Override public boolean existePorDocumento(DocumentoIdentidad documento) {
        return datos.values().stream().anyMatch(u -> u.getDocumento().equals(documento));
    }
    @Override public void guardar(Usuario usuario) { datos.put(usuario.getIdentificacion(), usuario); }
}
