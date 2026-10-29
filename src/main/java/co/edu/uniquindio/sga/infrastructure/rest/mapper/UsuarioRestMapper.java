package co.edu.uniquindio.sga.infrastructure.rest.mapper;

import co.edu.uniquindio.sga.application.dto.response.TokenResponse;
import co.edu.uniquindio.sga.application.dto.response.UsuarioResponse;
import co.edu.uniquindio.sga.application.seguridad.Usuario;
import co.edu.uniquindio.sga.application.usecase.IniciarSesionUseCase;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Mapper usuario → DTO de usuarios y sesiones; nunca expone la contraseña. */
@Mapper(componentModel = "spring")
public interface UsuarioRestMapper {

    @Mapping(target = "correo", source = "correo.valor")
    @Mapping(target = "documento", source = "documento.numero")
    @Mapping(target = "activo", expression = "java(usuario.estaActivo())")
    UsuarioResponse toResponse(Usuario usuario);

    default TokenResponse toToken(IniciarSesionUseCase.Resultado resultado) {
        return new TokenResponse(resultado.token().token(), "Bearer", resultado.token().expiraEn(),
                resultado.usuario().getCorreo().valor(), resultado.usuario().getRol());
    }
}
