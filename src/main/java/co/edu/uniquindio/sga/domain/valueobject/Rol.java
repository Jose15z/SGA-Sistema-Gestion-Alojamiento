package co.edu.uniquindio.sga.domain.valueobject;

/** Roles de la matriz de permisos (7.1). */
public enum Rol {
    ADMINISTRADOR,
    RECEPCIONISTA,
    HUESPED;

    /** El personal del alojamiento opera sobre cualquier reserva. */
    public boolean esPersonal() {
        return this != HUESPED;
    }
}
