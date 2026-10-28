package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.CorreoElectronico;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.Rol;

import java.util.UUID;

/**
 * Persona que ingresa al SGA con un rol de la matriz 7.1 (7.2).
 *
 * <p>Invariantes:</p>
 * <ul>
 *     <li>Identidad inmutable, correo, documento, nombre y rol obligatorios.</li>
 *     <li>Nunca guarda la contraseña en claro: solo su forma cifrada.</li>
 *     <li>Un usuario inactivo no puede iniciar sesión (eliminación lógica, 12.4).</li>
 * </ul>
 *
 * <p>La unicidad del correo y del documento requiere consultar a los demás
 * usuarios: la coordina {@code RegistroUsuarioService}.</p>
 */
public final class Usuario {

    private final UUID identificacion;
    private final CorreoElectronico correo;
    private final DocumentoIdentidad documento;
    private final Rol rol;
    private String nombreCompleto;
    private String contrasenaCifrada;
    private boolean activo;

    private Usuario(UUID identificacion, CorreoElectronico correo, DocumentoIdentidad documento,
                    String nombreCompleto, Rol rol, String contrasenaCifrada, boolean activo) {
        if (identificacion == null || correo == null || documento == null || rol == null) {
            throw new ReglaDominioException("El usuario requiere identificación, correo, documento y rol");
        }
        if (nombreCompleto == null || nombreCompleto.isBlank()) {
            throw new ReglaDominioException("El usuario debe tener nombre");
        }
        if (contrasenaCifrada == null || contrasenaCifrada.isBlank()) {
            throw new ReglaDominioException("El usuario debe tener una contraseña cifrada");
        }
        this.identificacion = identificacion;
        this.correo = correo;
        this.documento = documento;
        this.nombreCompleto = nombreCompleto.trim();
        this.rol = rol;
        this.contrasenaCifrada = contrasenaCifrada;
        this.activo = activo;
    }

    /** Registro público de un huésped (7.2): siempre con rol HUESPED. */
    public static Usuario registrarHuesped(UUID identificacion, CorreoElectronico correo,
                                           DocumentoIdentidad documento, String nombreCompleto,
                                           String contrasenaCifrada) {
        return new Usuario(identificacion, correo, documento, nombreCompleto, Rol.HUESPED,
                contrasenaCifrada, true);
    }

    /** Reconstruye un usuario guardado (Guía 10). */
    public static Usuario reconstruir(UUID identificacion, CorreoElectronico correo, DocumentoIdentidad documento,
                                      String nombreCompleto, Rol rol, String contrasenaCifrada, boolean activo) {
        return new Usuario(identificacion, correo, documento, nombreCompleto, rol, contrasenaCifrada, activo);
    }

    /** Solo un usuario activo puede iniciar sesión. */
    public boolean puedeIniciarSesion() {
        return activo;
    }

    /** Eliminación lógica: el usuario se conserva, pero ya no inicia sesión. */
    public void desactivar() {
        if (!activo) {
            throw new ReglaDominioException("El usuario ya está inactivo");
        }
        activo = false;
    }

    public UUID getIdentificacion() { return identificacion; }
    public CorreoElectronico getCorreo() { return correo; }
    public DocumentoIdentidad getDocumento() { return documento; }
    public String getNombreCompleto() { return nombreCompleto; }
    public Rol getRol() { return rol; }
    public String getContrasenaCifrada() { return contrasenaCifrada; }
    public boolean estaActivo() { return activo; }

    @Override
    public boolean equals(Object objeto) {
        return objeto instanceof Usuario otro && identificacion.equals(otro.identificacion);
    }

    @Override
    public int hashCode() {
        return identificacion.hashCode();
    }
}
