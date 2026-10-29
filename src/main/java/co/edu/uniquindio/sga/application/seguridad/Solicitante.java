package co.edu.uniquindio.sga.application.seguridad;

import co.edu.uniquindio.sga.application.exception.AccesoDenegadoException;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;

import java.util.Objects;

/**
 * Usuario autenticado que invoca un caso de uso. El adaptador de seguridad
 * lo construye a partir del JWT; nunca a partir del cuerpo de la petición.
 *
 * <p>El rol ya lo verificó la matriz de Spring Security (7.1). Aquí se verifica
 * el propietario (7.2): un huésped solo opera sobre sus reservas. Quién es el
 * titular lo decide el dominio con {@link Reserva#perteneceA}.</p>
 *
 * @param usuario   correo del usuario autenticado; queda como autor en el historial
 * @param rol       rol del usuario
 * @param documento documento de identidad del usuario
 */
public record Solicitante(String usuario, Rol rol, DocumentoIdentidad documento) {

    public Solicitante {
        Objects.requireNonNull(usuario, "El usuario es obligatorio");
        Objects.requireNonNull(rol, "El rol es obligatorio");
        Objects.requireNonNull(documento, "El documento es obligatorio");
    }

    public boolean esHuesped() {
        return rol == Rol.HUESPED;
    }

    public boolean esAdministrador() {
        return rol == Rol.ADMINISTRADOR;
    }

    /** Recepción y administración acceden a todas las reservas; un huésped, solo a las suyas. */
    public void verificarAcceso(Reserva reserva) {
        if (esHuesped() && !reserva.perteneceA(documento)) {
            throw new AccesoDenegadoException("La reserva " + reserva.getCodigo().valor()
                    + " no pertenece al usuario autenticado");
        }
        if (!esHuesped() && !rol.gestionaReservas()) {
            throw new AccesoDenegadoException("El rol " + rol + " no opera sobre reservas");
        }
    }

    /**
     * 2.4 y 7.1: el huésped reserva por el PORTAL a su nombre; la recepción y la
     * administración registran reservas DIRECTO (o EXTERNO, mientras llega el
     * canal externo del corte 4) a nombre de un titular que puede no tener cuenta.
     */
    public void verificarPuedeReservar(CanalOrigen canal, DocumentoIdentidad titular) {
        if (esHuesped()) {
            if (canal != CanalOrigen.PORTAL) {
                throw new AccesoDenegadoException("Un huésped solo reserva por el canal PORTAL");
            }
            if (!documento.equals(titular)) {
                throw new AccesoDenegadoException("Un huésped solo puede reservar a su propio nombre");
            }
            return;
        }
        if (!rol.gestionaReservas()) {
            throw new AccesoDenegadoException("El rol " + rol + " no registra reservas");
        }
        if (canal == CanalOrigen.PORTAL) {
            throw new AccesoDenegadoException("El canal PORTAL es exclusivo de los huéspedes");
        }
    }
}
