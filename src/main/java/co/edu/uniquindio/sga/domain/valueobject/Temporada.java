package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.LocalDate;

/**
 * Periodo del calendario con tarifas propias (glosario, 7.4).
 *
 * <p>Objeto de valor: dos temporadas con el mismo nombre, tipo y fechas
 * son la misma configuración; cambiar el rango produce otra temporada.
 * El rango es cerrado: {@code [fechaInicio, fechaFin]} (ambas noches incluidas).</p>
 *
 * @param nombre      nombre del periodo (por ejemplo, "Vacaciones de mitad de año")
 * @param tipo        ALTA o ESPECIAL; la BASE no se define por fechas
 * @param fechaInicio primera noche de la temporada
 * @param fechaFin    última noche de la temporada
 */
public record Temporada(
        String nombre,
        TipoTemporada tipo,
        LocalDate fechaInicio,
        LocalDate fechaFin
) {

    public Temporada {
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaDominioException("La temporada debe tener un nombre");
        }
        if (tipo == null) {
            throw new ReglaDominioException("La temporada debe indicar su tipo");
        }
        if (!tipo.seDefinePorFechas()) {
            throw new ReglaDominioException(
                    "La temporada BASE no se registra por fechas: "
                            + "cubre todas las fechas no asignadas");
        }
        if (fechaInicio == null || fechaFin == null) {
            throw new ReglaDominioException("La temporada requiere fecha de inicio y fecha de fin");
        }
        if (fechaFin.isBefore(fechaInicio)) {
            throw new ReglaDominioException(
                    "La fecha de fin de la temporada no puede ser anterior a su inicio");
        }
        nombre = nombre.trim();
    }

    /** Indica si la noche pertenece a la temporada. */
    public boolean incluye(LocalDate noche) {
        return noche != null && !noche.isBefore(fechaInicio) && !noche.isAfter(fechaFin);
    }

    /** Dos temporadas se solapan si comparten al menos una noche. */
    public boolean seSolapaCon(Temporada otra) {
        return otra != null
                && !fechaInicio.isAfter(otra.fechaFin)
                && !otra.fechaInicio.isAfter(fechaFin);
    }
}
