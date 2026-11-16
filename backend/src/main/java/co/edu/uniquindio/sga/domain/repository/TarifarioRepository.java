package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.Tarifa;

import java.time.LocalDate;

/**
 * Puerto del dominio para consultar el tarifario del alojamiento.
 *
 * <p>Declara la información que necesita el cálculo de una estancia,
 * sin depender de bases de datos, Spring ni otros frameworks.</p>
 *
 * <p>La consulta considera el apartamento y la temporada
 * correspondiente a la noche solicitada. La temporada base
 * cubre las fechas no asignadas a otra temporada.</p>
 */
public interface TarifarioRepository {

    /**
     * Obtiene la tarifa configurada para un apartamento
     * en una noche concreta.
     *
     * <p>La tarifa devuelta debe pertenecer al apartamento
     * solicitado e indicar la temporada aplicada y el valor
     * por ocupante facturable y por noche.</p>
     *
     * <p>Siguiendo el contrato utilizado por el cotizador
     * de la Guía 05, devuelve null si no existe una tarifa.
     * El cotizador debe rechazar esa configuración incompleta
     * mediante ReglaDominioException.</p>
     *
     * @param apartamento identificación del apartamento solicitado
     * @param noche fecha de la noche que se desea cotizar
     * @return tarifa aplicable, o null si no está definida
     */
    Tarifa tarifaDe(
            IdentificacionApartamento apartamento,
            LocalDate noche
    );
}