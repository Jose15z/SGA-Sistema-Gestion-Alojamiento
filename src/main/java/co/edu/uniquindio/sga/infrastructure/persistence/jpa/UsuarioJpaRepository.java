package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import co.edu.uniquindio.sga.domain.entity.Usuario;
import co.edu.uniquindio.sga.domain.repository.UsuarioRepository;
import co.edu.uniquindio.sga.domain.valueobject.CorreoElectronico;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.UsuarioEntity;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.UsuarioPersistenceMapper;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.UsuarioJpaDataRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/** Adaptador JPA del puerto UsuarioRepository. */
@Repository
@Transactional
public class UsuarioJpaRepository implements UsuarioRepository {

    private final UsuarioJpaDataRepository dataRepository;
    private final UsuarioPersistenceMapper mapper;

    public UsuarioJpaRepository(UsuarioJpaDataRepository dataRepository, UsuarioPersistenceMapper mapper) {
        this.dataRepository = dataRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> obtenerPorCorreo(CorreoElectronico correo) {
        return dataRepository.findByCorreo(correo.valor()).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorCorreo(CorreoElectronico correo) {
        return dataRepository.existsByCorreo(correo.valor());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorDocumento(DocumentoIdentidad documento) {
        return dataRepository.existsByDocumento(documento.numero());
    }

    @Override
    public void guardar(Usuario usuario) {
        UsuarioEntity entity = mapper.toEntity(usuario);
        dataRepository.findByIdentificacion(entity.getIdentificacion())
                .ifPresent(existente -> entity.setId(existente.getId()));
        dataRepository.save(entity);
    }
}
