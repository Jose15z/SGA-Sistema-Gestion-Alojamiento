package co.edu.uniquindio.sga.domain.valueobject;

/**
 * Estado físico u operativo actual de un apartamento.
 */
public enum EstadoOperativo {

    PREPARADO,
    OCUPADO,
    PENDIENTE_PREPARACION,
    EN_PREPARACION,
    FUERA_DE_SERVICIO;

    /**
     * Solo un apartamento preparado puede recibir
     * un grupo de ocupantes.
     */
    public boolean permiteRegistro() {
        return this == PREPARADO;
    }
}