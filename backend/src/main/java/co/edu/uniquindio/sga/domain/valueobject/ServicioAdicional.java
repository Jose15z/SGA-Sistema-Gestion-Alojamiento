package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Servicio adicional del alojamiento, indicando si genera cargo y su valor (7.3, L-17).
 *
 * @param codigo      identificador corto, por ejemplo LAVANDERIA
 * @param nombre      nombre visible
 * @param generaCargo si su uso suma al folio
 * @param valor       valor por unidad; cero si no genera cargo
 * @param unidad      cómo se cuenta la cantidad
 */
public record ServicioAdicional(String codigo, String nombre, boolean generaCargo, Dinero valor, UnidadCobro unidad) {

    private static final Pattern CODIGO = Pattern.compile("^[A-Z][A-Z0-9_]{1,29}$");

    public ServicioAdicional {
        if (codigo == null || !CODIGO.matcher(codigo.trim().toUpperCase(Locale.ROOT)).matches()) {
            throw new ReglaDominioException("El código del servicio debe tener letras mayúsculas, números o guiones bajos");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaDominioException("El servicio adicional debe tener nombre");
        }
        if (valor == null || unidad == null) {
            throw new ReglaDominioException("El servicio adicional requiere valor y unidad de cobro");
        }
        if (generaCargo && (valor.esCero() || valor.esNegativo())) {
            throw new ReglaDominioException("Un servicio que genera cargo debe tener un valor positivo");
        }
        if (!generaCargo && !valor.esCero()) {
            throw new ReglaDominioException("Un servicio sin cargo debe tener valor cero");
        }
        codigo = codigo.trim().toUpperCase(Locale.ROOT);
        nombre = nombre.trim();
    }

    /**
     * L-17: cargo por usar el servicio la cantidad indicada (personas, noches o solicitudes).
     *
     * @throws ReglaDominioException si el servicio no genera cargo o la cantidad no es positiva
     */
    public Dinero cargoPor(int cantidad) {
        if (!generaCargo) {
            throw new ReglaDominioException("El servicio " + nombre + " no genera cargo");
        }
        if (cantidad < 1) {
            throw new ReglaDominioException("La cantidad del servicio debe ser al menos 1");
        }
        return valor.por(cantidad);
    }
}
