package co.edu.uniquindio.sga.domain.valueobject;

/**
 * Canal mediante el cual se creó una reserva.
 */
public enum CanalOrigen {

    PORTAL,
    DIRECTO,
    EXTERNO;

    public boolean exigeIdentificadorExterno() {
        return this == EXTERNO;
    }

    /**
     * Ficha A.5: el portal lo usan los huéspedes; los canales directo y
     * externo los registra el personal del alojamiento.
     */
    public boolean loRegistra(Rol rol) {
        if (rol == null) {
            return false;
        }
        return this == PORTAL ? rol == Rol.HUESPED : rol.esPersonal();
    }
}
