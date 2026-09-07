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
}