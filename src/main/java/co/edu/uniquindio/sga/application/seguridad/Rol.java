package co.edu.uniquindio.sga.application.seguridad;

/**
 * Roles de acceso de la matriz 7.1. Son un concepto de acceso, no del negocio:
 * el dominio no los conoce (3.6).
 */
public enum Rol {
    ADMINISTRADOR,
    RECEPCIONISTA,
    PERSONAL_SERVICIO,
    HUESPED;

    /** Recepción y administración operan sobre cualquier reserva. */
    public boolean gestionaReservas() {
        return this == ADMINISTRADOR || this == RECEPCIONISTA;
    }
}
