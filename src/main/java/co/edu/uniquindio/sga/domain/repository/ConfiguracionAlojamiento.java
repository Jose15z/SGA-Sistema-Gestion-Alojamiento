package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.valueobject.PlazoConfirmacion;
import co.edu.uniquindio.sga.domain.valueobject.TiempoPreparacion;
import co.edu.uniquindio.sga.domain.valueobject.UmbralEdadFacturable;

import java.math.BigDecimal;

/**
 * Puerto del dominio para consultar los parámetros vigentes
 * de disponibilidad, cotización y confirmación de reservas.
 *
 * <p>Declara la información necesaria sin conocer dónde
 * se almacena ni cómo la modifica el administrador.</p>
 *
 * <p>Los consumidores consultan los parámetros al ejecutar
 * cada operación. Los valores que deban congelarse se
 * conservan explícitamente en el agregado correspondiente.</p>
 *
 * <p>No contiene valores predeterminados ni depende
 * de Spring, JPA u otros frameworks.</p>
 */
public interface ConfiguracionAlojamiento {

    /**
     * Obtiene la edad mínima configurada para generar
     * cargos de alojamiento.
     *
     * <p>RN-06: la edad se evalúa en la fecha de entrada.</p>
     *
     * @return umbral vigente, nunca null
     */
    UmbralEdadFacturable umbralEdadFacturable();

    /**
     * Obtiene la duración de preparación y los horarios
     * configurados de salida y entrada.
     *
     * <p>RN-20: permite evaluar si el intervalo entre ambos
     * horarios admite una entrada el mismo día de la salida.</p>
     *
     * @return configuración de preparación, nunca null
     */
    TiempoPreparacion tiempoPreparacion();

    /**
     * Obtiene el plazo configurado para confirmar
     * una reserva pendiente.
     *
     * <p>RN-21: el límite se calcula utilizando la fecha
     * y hora de creación y la duración de este plazo.</p>
     *
     * <p>Este puerto proporciona la duración; no decide
     * si una reserva concreta debe vencer ni modifica
     * su estado.</p>
     *
     * @return plazo de confirmación, nunca null
     */
    PlazoConfirmacion plazoConfirmacion();

    /**
     * Obtiene la cantidad mínima de noches requerida
     * para aplicar el descuento por estancia prolongada.
     *
     * <p>RP-03: el cotizador compara este umbral
     * con la duración completa de la estancia.</p>
     *
     * @return cantidad configurada, mayor que cero
     */
    int nochesMinimasParaDescuento();

    /**
     * Obtiene el porcentaje de descuento por estancia prolongada.
     *
     * <p>El valor 10 representa un descuento del 10 %.</p>
     *
     * <p>RP-03: se aplica únicamente sobre el alojamiento.
     * El porcentaje aplicado se conserva en ValorCongelado
     * para que cambios posteriores de configuración
     * no alteren la cotización ya conservada.</p>
     *
     * @return porcentaje vigente entre cero y cien, nunca null
     */
    BigDecimal porcentajeDescuentoEstanciaProlongada();
    /** Porcentaje del total exigido para confirmar; cero desactiva el anticipo. */
    BigDecimal porcentajeAnticipo();

    /** Hora vigente de no-show, consultada al ejecutar la operación. */
    java.time.LocalTime horaLimiteNoShow();

    /** Mínimo de noches para la temporada identificada por el tarifario. */
    int nochesMinimasEnTemporada(String temporada);
}