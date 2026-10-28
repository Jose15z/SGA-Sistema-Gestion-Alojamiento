package co.edu.uniquindio.sga.application.seguridad;

import co.edu.uniquindio.sga.application.exception.AccesoDenegadoException;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.Rol;

import java.util.Objects;

/**
 * Usuario autenticado que invoca un caso de uso. El adaptador de seguridad
 * lo construye a partir del JWT; nunca a partir del cuerpo de la petición.
 *
 * <p>El rol ya lo verificó la matriz de Spring Security (7.1). Aquí se
 * verifica el propietario: un huésped solo opera sobre sus reservas.
 * Quién es el titular lo decide el dominio con {@link Reserva#perteneceA}.</p>
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

    /** El personal accede a todas las reservas; un huésped, solo a las suyas. */
    public void verificarAcceso(Reserva reserva) {
        if (esHuesped() && !reserva.perteneceA(documento)) {
            throw new AccesoDenegadoException("La reserva " + reserva.getCodigo().valor()
                    + " no pertenece al usuario autenticado");
        }
    }

    /**
     * Un huésped solo reserva por el portal y a su nombre; el personal
     * registra reservas directas o externas.
     */
    public void verificarPuedeReservar(CanalOrigen canal, DocumentoIdentidad titular) {
        if (canal != null && !canal.loRegistra(rol)) {
            throw new AccesoDenegadoException("El rol " + rol + " no puede registrar reservas por el canal " + canal);
        }
        if (esHuesped() && !documento.equals(titular)) {
            throw new AccesoDenegadoException("Un huésped solo puede reservar a su propio nombre");
        }
    }
}
