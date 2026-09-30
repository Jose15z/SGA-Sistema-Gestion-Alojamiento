package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.Duration;
import java.time.LocalTime;

/**
 * Configuración inmutable para evaluar el tiempo de preparación
 * entre la salida de un grupo y la entrada del siguiente.
 *
 * <p>RN-20: una entrada el mismo día de una salida solo es
 * posible cuando la ventana entre ambos horarios alcanza
 * para completar la preparación configurada.</p>
 *
 * <p>Los valores se reciben como parámetros. Esta clase
 * no fija los horarios ni la duración del alojamiento.</p>
 *
 * <p>El servicio de disponibilidad debe obtener este objeto
 * desde la configuración vigente en cada operación.</p>
 *
 * @param horas duración requerida para preparar el apartamento
 * @param horaSalida horario configurado de salida
 * @param horaEntrada horario configurado de entrada
 */
public record TiempoPreparacion(
        int horas,
        LocalTime horaSalida,
        LocalTime horaEntrada
) {

    /**
     * Construye una configuración válida de preparación.
     *
     * <p>Que los horarios no permitan una entrada el mismo día
     * no invalida la configuración: en ese caso, la consulta
     * permiteEntradaElMismoDia devuelve false.</p>
     *
     * @throws ReglaDominioException si la duración es negativa
     *         o falta alguno de los horarios
     */
    public TiempoPreparacion {
        if (horas < 0) {
            throw new ReglaDominioException(
                    "El tiempo de preparación no puede ser negativo"
            );
        }

        if (horaSalida == null) {
            throw new ReglaDominioException(
                    "La hora de salida es obligatoria "
                            + "para evaluar el tiempo de preparación"
            );
        }

        if (horaEntrada == null) {
            throw new ReglaDominioException(
                    "La hora de entrada es obligatoria "
                            + "para evaluar el tiempo de preparación"
            );
        }
    }

    /**
     * Determina si puede recibirse otro grupo el mismo día
     * en que sale el grupo anterior.
     *
     * <p>Una ventana exactamente igual al tiempo requerido
     * es suficiente. Si la entrada ocurre antes de la salida
     * dentro del mismo día, la operación no es posible.</p>
     *
     * @return true si la ventana disponible alcanza
     *         para completar la preparación
     */
    public boolean permiteEntradaElMismoDia() {
        if (horaEntrada.isBefore(horaSalida)) {
            return false;
        }

        Duration ventanaDisponible = Duration.between(
                horaSalida,
                horaEntrada
        );

        Duration preparacionRequerida = Duration.ofHours(horas);

        return ventanaDisponible.compareTo(preparacionRequerida) >= 0;
    }
}