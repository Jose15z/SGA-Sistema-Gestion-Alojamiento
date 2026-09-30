package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import java.time.LocalDateTime;

/** Movimiento inmutable del folio; los ajustes pueden tener valor negativo (RN-16). */
public record Cargo(TipoCargo tipo, String concepto, Dinero valor, LocalDateTime momento, String autor) {
    public Cargo {
        if (tipo == null || valor == null || momento == null
                || concepto == null || concepto.isBlank() || autor == null || autor.isBlank()) {
            throw new ReglaDominioException("El cargo requiere tipo, concepto, valor, momento y autor");
        }
        if ((tipo == TipoCargo.ALOJAMIENTO || tipo == TipoCargo.PENALIDAD) && valor.esNegativo()) {
            throw new ReglaDominioException("El alojamiento y la penalidad no pueden ser negativos");
        }
        if (tipo == TipoCargo.REVERSO_ALOJAMIENTO && valor.valor().signum() > 0) {
            throw new ReglaDominioException("El reverso de alojamiento no puede ser positivo");
        }
        concepto = concepto.trim();
        autor = autor.trim();
    }
}
