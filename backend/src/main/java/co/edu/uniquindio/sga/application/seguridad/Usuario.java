package co.edu.uniquindio.sga.application.seguridad;

import co.edu.uniquindio.sga.application.exception.SolicitudUsuarioInvalidaException;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Usuario del SGA: credenciales y rol (3.6, 7.2).
 *
 * <p>Es un concepto de acceso, por eso vive fuera del dominio: el titular y los
 * ocupantes de una reserva existen aunque nunca tengan usuario. El vínculo con
 * el negocio es el documento de identidad: un huésped es dueño de las reservas
 * cuyo titular tiene su mismo documento.</p>
 *
 * <ul>
 *     <li>Nunca guarda la contraseña en claro, solo su forma cifrada.</li>
 *     <li>El correo no es editable; nombre y teléfono sí.</li>
 *     <li>La eliminación es lógica: un usuario inactivo no inicia sesión.</li>
 * </ul>
 */
public final class Usuario {

    private final UUID identificacion;
    private final CorreoElectronico correo;
    private final DocumentoIdentidad documento;
    private final LocalDate fechaNacimiento;
    private String nombreCompleto;
    private String telefono;
    private Rol rol;
    private String contrasenaCifrada;
    private boolean activo;

    private Usuario(UUID identificacion, CorreoElectronico correo, DocumentoIdentidad documento,
                    LocalDate fechaNacimiento, String nombreCompleto, String telefono, Rol rol,
                    String contrasenaCifrada, boolean activo) {
        if (identificacion == null || correo == null || documento == null || rol == null || fechaNacimiento == null) {
            throw new SolicitudUsuarioInvalidaException(
                    "El usuario requiere identificación, correo, documento, fecha de nacimiento y rol");
        }
        if (contrasenaCifrada == null || contrasenaCifrada.isBlank()) {
            throw new SolicitudUsuarioInvalidaException("El usuario debe tener una contraseña cifrada");
        }
        this.identificacion = identificacion;
        this.correo = correo;
        this.documento = documento;
        this.fechaNacimiento = fechaNacimiento;
        this.rol = rol;
        this.contrasenaCifrada = contrasenaCifrada;
        this.activo = activo;
        actualizarPerfil(nombreCompleto, telefono);
    }

    /** Registro público (7.2): siempre crea un HUESPED activo. */
    public static Usuario registrarHuesped(UUID identificacion, CorreoElectronico correo, DocumentoIdentidad documento,
                                           LocalDate fechaNacimiento, String nombreCompleto, String telefono,
                                           String contrasenaCifrada) {
        return new Usuario(identificacion, correo, documento, fechaNacimiento, nombreCompleto, telefono,
                Rol.HUESPED, contrasenaCifrada, true);
    }

    /** Usuario creado por un administrador con el rol que este le asigna (7.2). */
    public static Usuario crearConRol(UUID identificacion, CorreoElectronico correo, DocumentoIdentidad documento,
                                      LocalDate fechaNacimiento, String nombreCompleto, String telefono, Rol rol,
                                      String contrasenaCifrada) {
        return new Usuario(identificacion, correo, documento, fechaNacimiento, nombreCompleto, telefono,
                rol, contrasenaCifrada, true);
    }

    /** Reconstruye un usuario guardado. */
    public static Usuario reconstruir(UUID identificacion, CorreoElectronico correo, DocumentoIdentidad documento,
                                      LocalDate fechaNacimiento, String nombreCompleto, String telefono, Rol rol,
                                      String contrasenaCifrada, boolean activo) {
        return new Usuario(identificacion, correo, documento, fechaNacimiento, nombreCompleto, telefono, rol,
                contrasenaCifrada, activo);
    }

    public boolean puedeIniciarSesion() {
        return activo;
    }

    /** Nombre y teléfono son editables; el correo no. */
    public void actualizarPerfil(String nuevoNombre, String nuevoTelefono) {
        if (nuevoNombre == null || nuevoNombre.isBlank()) {
            throw new SolicitudUsuarioInvalidaException("El usuario debe tener nombre");
        }
        if (nuevoTelefono == null || nuevoTelefono.isBlank()) {
            throw new SolicitudUsuarioInvalidaException("El usuario debe tener teléfono");
        }
        this.nombreCompleto = nuevoNombre.trim();
        this.telefono = nuevoTelefono.trim();
    }

    public void cambiarContrasena(String nuevaContrasenaCifrada) {
        if (nuevaContrasenaCifrada == null || nuevaContrasenaCifrada.isBlank()) {
            throw new SolicitudUsuarioInvalidaException("La nueva contraseña es obligatoria");
        }
        this.contrasenaCifrada = nuevaContrasenaCifrada;
    }

    /** Los roles internos solo los asigna un administrador. */
    public void asignarRol(Rol nuevoRol) {
        if (nuevoRol == null) {
            throw new SolicitudUsuarioInvalidaException("El rol es obligatorio");
        }
        this.rol = nuevoRol;
    }

    /** Eliminación lógica (7.2): el usuario se conserva, pero ya no inicia sesión. */
    public void desactivar() {
        if (!activo) {
            throw new UsuarioInactivoException();
        }
        activo = false;
    }

    public UUID getIdentificacion() { return identificacion; }
    public CorreoElectronico getCorreo() { return correo; }
    public DocumentoIdentidad getDocumento() { return documento; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public String getNombreCompleto() { return nombreCompleto; }
    public String getTelefono() { return telefono; }
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

    /** El usuario ya estaba eliminado lógicamente. */
    public static final class UsuarioInactivoException extends RuntimeException {
        UsuarioInactivoException() {
            super("El usuario ya está inactivo");
        }
    }
}
