package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Valor inmutable del alojamiento con su desglose por noche
 * y el porcentaje de descuento aplicado.
 *
 * <p>RN-22: conserva los datos utilizados al cotizar.
 * Los cambios posteriores del tarifario o de la configuración
 * de descuentos no alteran este objeto.</p>
 *
 * <p>RP-03: el descuento se calcula sobre la suma completa
 * del alojamiento. El resultado se redondea una sola vez,
 * al construir el importe total en pesos.</p>
 *
 * <p>No incluye cargos por parqueadero, lavandería,
 * desayuno u otros servicios adicionales.</p>
 *
 * <p>La decisión de aplicar un descuento corresponde
 * al cotizador. La versión de la política de cancelación
 * se conserva por separado en la reserva.</p>
 */
public record ValorCongelado(
        List<CargoNoche> detalle,
        BigDecimal porcentajeDescuento
) {

    /**
     * Construye un valor de alojamiento con desglose y descuento.
     *
     * @param detalle líneas correspondientes a las noches cotizadas
     * @param porcentajeDescuento porcentaje aplicado; 10 representa 10 %
     * @throws ReglaDominioException si el desglose es inválido
     *         o el porcentaje no está entre cero y cien
     */
    public ValorCongelado {
        if (detalle == null || detalle.isEmpty()) {
            throw new ReglaDominioException(
                    "El valor de la reserva debe tener desglose por noche"
            );
        }

        if (porcentajeDescuento == null) {
            throw new ReglaDominioException(
                    "El porcentaje de descuento es obligatorio"
            );
        }

        if (porcentajeDescuento.signum() < 0
                || porcentajeDescuento.compareTo(
                BigDecimal.valueOf(100)
        ) > 0) {
            throw new ReglaDominioException(
                    "El porcentaje de descuento debe estar entre 0 y 100"
            );
        }

        Set<LocalDate> nochesRegistradas = new HashSet<>();

        for (CargoNoche cargo : detalle) {
            if (cargo == null) {
                throw new ReglaDominioException(
                        "El desglose no puede contener cargos nulos"
                );
            }

            if (!nochesRegistradas.add(cargo.noche())) {
                throw new ReglaDominioException(
                        "El desglose no puede repetir una misma noche"
                );
            }
        }

        // Orden cronológico y copia inmutable del desglose.
        detalle = detalle.stream()
                .sorted((primero, segundo) ->
                        primero.noche().compareTo(segundo.noche()))
                .toList();

        // Hace equivalentes porcentajes como 10, 10.0 y 10.00.
        porcentajeDescuento = porcentajeDescuento.stripTrailingZeros();
    }

    /**
     * Construye un valor de alojamiento sin descuento.
     *
     * @param detalle líneas correspondientes a las noches cotizadas
     */
    public ValorCongelado(List<CargoNoche> detalle) {
        this(detalle, BigDecimal.ZERO);
    }

    /**
     * Suma los subtotales de alojamiento antes del descuento.
     *
     * <p>Los subtotales están expresados en pesos enteros,
     * por lo que su suma no pierde precisión.</p>
     *
     * @return importe de alojamiento sin descuento
     */
    public Dinero subtotal() {
        return detalle.stream()
                .map(CargoNoche::subtotal)
                .reduce(Dinero.CERO, Dinero::mas);
    }

    /**
     * Calcula el total del alojamiento después del descuento.
     *
     * <p>El descuento se calcula con precisión exacta sobre
     * el subtotal completo. El redondeo ocurre únicamente
     * al construir el Dinero resultante.</p>
     *
     * @return importe final del alojamiento en pesos enteros
     */
    public Dinero total() {
        BigDecimal importeBase = subtotal().valor();

        BigDecimal descuentoExacto = importeBase
                .multiply(porcentajeDescuento)
                .movePointLeft(2);

        return new Dinero(
                importeBase.subtract(descuentoExacto)
        );
    }

    /**
     * Obtiene la reducción efectiva en pesos.
     *
     * <p>Se deriva del total ya redondeado para garantizar
     * que subtotal menos descuento coincida con el total.</p>
     *
     * @return diferencia entre subtotal y total
     */
    public Dinero descuento() {
        return subtotal().menos(total());
    }

    /**
     * Obtiene la cantidad de noches incluidas en el desglose.
     *
     * @return número de noches cotizadas
     */
    public int noches() {
        return detalle.size();
    }

    /**
     * Verifica que el desglose contenga exactamente las noches
     * de una estancia, sin omisiones ni fechas adicionales.
     *
     * <p>No basta con comparar la cantidad de noches:
     * también deben coincidir sus fechas.</p>
     *
     * @param estancia estancia que se desea comprobar
     * @return true si todas las noches corresponden a la estancia
     * @throws ReglaDominioException si la estancia es nula
     */
    public boolean correspondeA(Estancia estancia) {
        if (estancia == null) {
            throw new ReglaDominioException(
                    "La estancia es obligatoria para verificar el desglose"
            );
        }

        List<LocalDate> nochesDelDetalle = detalle.stream()
                .map(CargoNoche::noche)
                .toList();

        return nochesDelDetalle.equals(estancia.nochesOcupadas());
    }
}