package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.LocalDateTime;

/**
 * Entrada del histórico de tarifas de un apartamento (7.4): qué valor tuvo
 * una temporada desde qué momento. Inmutable; un cambio agrega otra entrada.
 *
 * @param temporada    temporada a la que aplica
 * @param valor        valor por ocupante facturable y noche
 * @param vigenteDesde momento desde el que rige
 */
public record RegistroTarifa(TipoTemporada temporada, Dinero valor, LocalDateTime vigenteDesde) {

    public RegistroTarifa {
        if (temporada == null || valor == null || vigenteDesde == null) {
            throw new ReglaDominioException("El registro de tarifa requiere temporada, valor y fecha de vigencia");
        }
        if (valor.esNegativo()) {
            throw new ReglaDominioException("La tarifa por ocupante y noche no puede ser negativa");
        }
    }
}
