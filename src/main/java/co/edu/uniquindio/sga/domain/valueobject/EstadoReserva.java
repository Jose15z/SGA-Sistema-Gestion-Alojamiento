package co.edu.uniquindio.sga.domain.valueobject;

/**
 * Estados posibles dentro del ciclo de vida de una reserva.
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

    public boolean esActiva() {
        return activa;
    }

    public boolean esTerminal() {
        return !activa;
    }

    public boolean retieneDisponibilidad() {
        return activa;
    }
}