package co.edu.uniquindio.sga.domain.valueobject;

/**
 * Estados del ciclo de vida de una reserva.
 *
 * <p>RN-08: únicamente se permiten las siguientes transiciones:</p>
 * <ul>
 *     <li>PENDIENTE a CONFIRMADA.</li>
 *     <li>PENDIENTE a CANCELADA.</li>
 *     <li>CONFIRMADA a EN_CURSO.</li>
 *     <li>CONFIRMADA a CANCELADA.</li>
 *     <li>CONFIRMADA a NO_SHOW.</li>
 *     <li>EN_CURSO a FINALIZADA.</li>
 * </ul>
 *
 * <p>RN-12: las reservas activas retienen disponibilidad.
 * Las reservas en estados terminales dejan de retenerla.</p>
 *
 * <p>Este enum describe las transiciones posibles. Las condiciones
 * específicas de cada operación se validan en los comportamientos
 * de Reserva y en los servicios de dominio correspondientes.</p>
 */
public enum EstadoReserva {

    PENDIENTE(true),
    CONFIRMADA(true),
    EN_CURSO(true),
    FINALIZADA(false),
    CANCELADA(false),
    NO_SHOW(false);

    private final boolean activa;

    EstadoReserva(boolean activa) {
        this.activa = activa;
    }

    /**
     * Indica si la reserva permanece activa.
     *
     * @return true para PENDIENTE, CONFIRMADA y EN_CURSO
     */
    public boolean esActiva() {
        return activa;
    }

    /**
     * Indica si la reserva terminó su ciclo de vida.
     *
     * @return true para FINALIZADA, CANCELADA y NO_SHOW
     */
    public boolean esTerminal() {
        return !activa;
    }

    /**
     * Determina si la reserva retiene las noches de su estancia.
     *
     * @return true cuando la reserva está activa
     */
    public boolean retieneDisponibilidad() {
        return activa;
    }

    /**
     * Consulta si el ciclo de vida permite pasar al estado indicado.
     *
     * <p>Los estados terminales no admiten transiciones.
     * Tampoco se permiten transiciones al mismo estado ni a null.</p>
     *
     * @param siguiente estado al que se pretende transitar
     * @return true únicamente si la transición está permitida
     */
    public boolean puedeTransicionarA(EstadoReserva siguiente) {
        if (siguiente == null) {
            return false;
        }

        return switch (this) {
            case PENDIENTE ->
                    siguiente == CONFIRMADA
                            || siguiente == CANCELADA;

            case CONFIRMADA ->
                    siguiente == EN_CURSO
                            || siguiente == CANCELADA
                            || siguiente == NO_SHOW;

            case EN_CURSO ->
                    siguiente == FINALIZADA;

            case FINALIZADA, CANCELADA, NO_SHOW -> false;
        };
    }
}