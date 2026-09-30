package co.edu.uniquindio.sga.application.exception;

import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;

import java.util.Objects;

/**
 * Indica que no existe un apartamento con la identificación solicitada.
 *
 * <p>El repositorio expresa la ausencia mediante Optional.empty().
 * El caso de uso lanza esta excepción cuando necesita
 * un apartamento existente para completar la operación.</p>
 *
 * <p>No representa falta de disponibilidad, capacidad insuficiente
 * ni un estado operativo que impida recibir un grupo.
 * Esas condiciones corresponden a las reglas del dominio.</p>
 */
public class ApartamentoNoEncontradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final IdentificacionApartamento identificacion;

    /**
     * Construye la excepción conservando la identificación solicitada.
     *
     * @param identificacion identificación del apartamento no encontrado
     * @throws NullPointerException si se intenta construir
     *         la excepción sin una identificación
     */
    public ApartamentoNoEncontradoException(
            IdentificacionApartamento identificacion
    ) {
        super(
                "No se encontró un apartamento con la identificación "
                        + Objects.requireNonNull(
                        identificacion,
                        "La identificación del apartamento es obligatoria"
                ).valor()
        );

        this.identificacion = identificacion;
    }

    /**
     * Obtiene la identificación que originó la búsqueda.
     *
     * @return identificación del apartamento no encontrado
     */
    public IdentificacionApartamento getIdentificacion() {
        return identificacion;
    }
}