package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;

import java.util.List;
import java.util.Optional;

/**
 * Puerto del dominio para almacenar y consultar reservas.
 *
 * <p>Declara las operaciones necesarias sin depender
 * de bases de datos, Spring ni otros frameworks.</p>
 *
 * <p>Las reservas se identifican mediante CodigoReserva.
 * No se ofrece eliminación: una reserva conserva su
 * identidad e historial aunque termine su ciclo de vida.</p>
 *
 * <p>Las consultas de colecciones devuelven listas vacías
 * cuando no existen coincidencias, nunca null.</p>
 */
public interface ReservaRepository {

    /**
     * Obtiene una reserva mediante su identidad.
     *
     * <p>La ausencia se expresa mediante Optional.empty().
     * El caso de uso determina cómo responder cuando
     * la operación requiere una reserva existente.</p>
     *
     * @param codigo código de la reserva buscada
     * @return reserva encontrada o un Optional vacío
     */
    Optional<Reserva> obtenerPorCodigo(CodigoReserva codigo);

    /**
     * Guarda una reserva nueva o conserva los cambios
     * realizados mediante sus comportamientos de negocio.
     *
     * <p>Guardar no ejecuta transiciones ni sustituye
     * las validaciones del dominio.</p>
     *
     * @param reserva reserva que se desea guardar
     */
    void guardar(Reserva reserva);

    /**
     * Busca las reservas que se encuentran en un estado.
     *
     * <p>Permite consultar el ciclo de vida y recuperar
     * las pendientes para evaluar su vencimiento.
     * La decisión de vencer corresponde al dominio.</p>
     *
     * @param estado estado solicitado
     * @return reservas coincidentes o una lista vacía
     */
    List<Reserva> buscarPorEstado(EstadoReserva estado);

    /**
     * Busca las reservas activas de un apartamento,
     * independientemente de su canal de origen.
     *
     * <p>Incluye únicamente reservas en PENDIENTE,
     * CONFIRMADA o EN_CURSO. Excluye FINALIZADA,
     * CANCELADA y NO_SHOW.</p>
     *
     * <p>No filtra por una estancia solicitada:
     * el servicio de disponibilidad utiliza los resultados
     * para comprobar solapamientos y preparación.</p>
     *
     * @param apartamento identificación del apartamento
     * @return reservas activas del apartamento o una lista vacía
     */
    List<Reserva> buscarActivasPorApartamento(
            IdentificacionApartamento apartamento
    );
}