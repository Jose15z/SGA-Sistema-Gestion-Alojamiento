package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Conjunto de temporadas configuradas por el alojamiento (7.4, L-08).
 *
 * <p>Invariantes:</p>
 * <ul>
 *     <li>Las temporadas no se solapan entre sí: cada noche tiene una sola temporada.</li>
 *     <li>Existe una temporada BASE obligatoria que cubre toda noche no asignada.</li>
 * </ul>
 *
 * @param temporadas temporadas con fechas (ALTA y ESPECIAL)
 */
public record CalendarioTemporadas(List<Temporada> temporadas) {

    public CalendarioTemporadas {
        if (temporadas == null || temporadas.stream().anyMatch(Objects::isNull)) {
            throw new ReglaDominioException("El calendario no admite temporadas nulas");
        }
        for (int i = 0; i < temporadas.size(); i++) {
            for (int j = i + 1; j < temporadas.size(); j++) {
                if (temporadas.get(i).seSolapaCon(temporadas.get(j))) {
                    throw new ReglaDominioException(
                            "Las temporadas no pueden solaparse: "
                                    + temporadas.get(i).nombre() + " y " + temporadas.get(j).nombre());
                }
            }
        }
        temporadas = temporadas.stream()
                .sorted(Comparator.comparing(Temporada::fechaInicio))
                .toList();
    }

    /** Calendario que solo tiene la temporada BASE. */
    public static CalendarioTemporadas soloBase() {
        return new CalendarioTemporadas(List.of());
    }

    /**
     * L-08: determina sin ambigüedad la temporada de una noche.
     * Si ninguna temporada con fechas la incluye, aplica la BASE.
     */
    public TipoTemporada tipoDe(LocalDate noche) {
        if (noche == null) {
            throw new ReglaDominioException("La noche es obligatoria para determinar la temporada");
        }
        return temporadas.stream()
                .filter(temporada -> temporada.incluye(noche))
                .map(Temporada::tipo)
                .findFirst()
                .orElse(TipoTemporada.BASE);
    }

    /**
     * Devuelve un nuevo calendario con la temporada agregada.
     *
     * @throws ReglaDominioException si la nueva temporada se solapa con otra
     */
    public CalendarioTemporadas con(Temporada nueva) {
        List<Temporada> todas = new ArrayList<>(temporadas);
        todas.add(nueva);
        return new CalendarioTemporadas(todas);
    }
}
