package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;

import java.util.Optional;

/**
 * Puerto del dominio para consultar y guardar apartamentos.
 *
 * <p>Declara las operaciones necesarias sin depender
 * de bases de datos, Spring ni otros frameworks.</p>
 *
 * <p>Los apartamentos se identifican mediante
 * IdentificacionApartamento. Su retiro de la venta
 * es lógico, por lo que este contrato no ofrece
 * una operación de eliminación.</p>
 */
public interface ApartamentoRepository {

    /**
     * Obtiene un apartamento mediante su identificación.
     *
     * <p>La consulta puede recuperar apartamentos activos
     * o inactivos. La posibilidad de utilizarlos en una
     * operación se determina mediante las reglas del dominio.</p>
     *
     * <p>Cuando el apartamento no existe, devuelve
     * Optional.empty(), nunca null.</p>
     *
     * @param identificacion identificación del apartamento buscado
     * @return apartamento encontrado o un Optional vacío
     */
    Optional<Apartamento> obtenerPorIdentificacion(
            IdentificacionApartamento identificacion
    );

    /**
     * Guarda un apartamento nuevo o conserva los cambios
     * realizados mediante sus comportamientos de negocio.
     *
     * <p>Esta operación no decide transiciones operativas,
     * no activa apartamentos y no sustituye las verificaciones
     * necesarias para retirarlos de la venta.</p>
     *
     * @param apartamento apartamento que se desea guardar
     */
    void guardar(Apartamento apartamento);
}