package co.edu.uniquindio.sga.domain.valueobject;

/**
 * Condición física u operativa actual de un apartamento.
 *
 * <p>RN-11: únicamente un apartamento PREPARADO puede
 * recibir un grupo de ocupantes.</p>
 *
 * <p>Transiciones permitidas según la Guía 05:</p>
 * <ul>
 *     <li>PREPARADO a OCUPADO o FUERA_DE_SERVICIO.</li>
 *     <li>OCUPADO a PENDIENTE_PREPARACION.</li>
 *     <li>PENDIENTE_PREPARACION a EN_PREPARACION
 *         o FUERA_DE_SERVICIO.</li>
 *     <li>EN_PREPARACION a PREPARADO o FUERA_DE_SERVICIO.</li>
 *     <li>FUERA_DE_SERVICIO a PENDIENTE_PREPARACION.</li>
 * </ul>
 *
 * <p>El estado operativo no representa la disponibilidad
 * para un rango de fechas ni sustituye los bloqueos.</p>
 *
 * <p>Este enum permite consultar las transiciones.
 * Apartamento debe proteger los cambios mediante sus
 * comportamientos de negocio.</p>
 */
public enum EstadoOperativo {

    PREPARADO,
    OCUPADO,
    PENDIENTE_PREPARACION,
    EN_PREPARACION,
    FUERA_DE_SERVICIO;

    /**
     * Indica si el estado operativo permite recibir un grupo.
     *
     * <p>La actividad del apartamento y las condiciones de
     * la reserva se comprueban por separado.</p>
     *
     * @return true únicamente cuando el estado es PREPARADO
     */
    public boolean permiteRegistro() {
        return this == PREPARADO;
    }

    /**
     * Consulta si se permite pasar al estado indicado.
     *
     * <p>No se permiten transiciones al mismo estado
     * ni a null.</p>
     *
     * @param siguiente estado operativo solicitado
     * @return true únicamente si la transición está permitida
     */
    public boolean puedeTransicionarA(EstadoOperativo siguiente) {
        if (siguiente == null) {
            return false;
        }

        return switch (this) {
            case PREPARADO ->
                    siguiente == OCUPADO
                            || siguiente == FUERA_DE_SERVICIO;

            case OCUPADO ->
                    siguiente == PENDIENTE_PREPARACION;

            case PENDIENTE_PREPARACION ->
                    siguiente == EN_PREPARACION
                            || siguiente == FUERA_DE_SERVICIO;

            case EN_PREPARACION ->
                    siguiente == PREPARADO
                            || siguiente == FUERA_DE_SERVICIO;

            case FUERA_DE_SERVICIO ->
                    siguiente == PENDIENTE_PREPARACION;
        };
    }
}