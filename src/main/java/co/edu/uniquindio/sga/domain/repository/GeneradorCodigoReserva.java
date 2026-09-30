package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.FechaCreacion;

/**
 * Puerto del dominio para generar códigos de reserva.
 *
 * <p>Los códigos tienen el formato RES-YYYY-NNNNN:
 * el año corresponde a la fecha de creación y los últimos
 * cinco dígitos representan una secuencia incremental.</p>
 *
 * <p>La implementación administra la secuencia y garantiza
 * que no se entregue el mismo código a reservas distintas,
 * incluso ante solicitudes simultáneas.</p>
 *
 * <p>Este contrato no depende de bases de datos ni frameworks.
 * Tampoco crea o guarda reservas.</p>
 */
public interface GeneradorCodigoReserva {

    /**
     * Genera un código utilizando el año de creación recibido.
     *
     * <p>La implementación debe utilizar fechaCreacion.anio()
     * y garantizar la unicidad del código completo.
     * No debe reutilizar códigos de reservas terminadas.</p>
     *
     * <p>La misma fecha de creación debe utilizarse
     * posteriormente al construir la reserva.</p>
     *
     * @param fechaCreacion fecha de creación de la nueva reserva,
     *                      obligatoria
     * @return código generado, nunca null
     */
    CodigoReserva generar(FechaCreacion fechaCreacion);
}