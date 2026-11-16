package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.util.regex.Pattern;

/**
 * Vehículo asociado a una reserva (RP-02), identificado por su placa.
 *
 * @param placa placa del vehículo, por ejemplo ABC123
 */
public record Vehiculo(String placa) {

    private static final Pattern FORMATO = Pattern.compile("^[A-Z0-9]{5,7}$");

    public Vehiculo {
        if (placa == null || placa.isBlank()) {
            throw new ReglaDominioException("El vehículo debe indicar su placa");
        }
        placa = placa.trim().toUpperCase().replace("-", "").replace(" ", "");
        if (!FORMATO.matcher(placa).matches()) {
            throw new ReglaDominioException("La placa del vehículo debe tener entre 5 y 7 letras o números");
        }
    }
}
