package co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper;

import co.edu.uniquindio.sga.application.seguridad.CorreoElectronico;
import co.edu.uniquindio.sga.application.seguridad.Usuario;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.UsuarioEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Mapper dominio ↔ entidad JPA del usuario. */
@Mapper(componentModel = "spring")
public interface UsuarioPersistenceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "correo", source = "correo.valor")
    @Mapping(target = "documento", source = "documento.numero")
    @Mapping(target = "activo", expression = "java(usuario.estaActivo())")
    UsuarioEntity toEntity(Usuario usuario);

    default Usuario toDomain(UsuarioEntity entity) {
        if (entity == null) {
            return null;
        }
        return Usuario.reconstruir(entity.getIdentificacion(), new CorreoElectronico(entity.getCorreo()),
                new DocumentoIdentidad(entity.getDocumento()), entity.getFechaNacimiento(), entity.getNombreCompleto(),
                entity.getTelefono(), entity.getRol(),
                entity.getContrasenaCifrada(), entity.isActivo());
    }
}
