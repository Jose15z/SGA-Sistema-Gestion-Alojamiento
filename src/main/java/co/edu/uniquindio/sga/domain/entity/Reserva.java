package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Compromiso de ocupar un apartamento durante una estancia.
 */
public class Reserva {

    /**
     * Identidad de la reserva.
     */
    private final CodigoReserva codigo;

    /**
     * El canal por donde nació la reserva no cambia.
     */
    private final CanalOrigen canalOrigen;

    /**
     * Momento de creación.
     */
    private final LocalDate fechaCreacion;

    /**
     * Persona responsable de la reserva.
     */
    private final Ocupante titular;

    private Apartamento apartamento;

    private Estancia estancia;

    private List<Ocupante> ocupantes;

    private EstadoReserva estado;

    /**
     * Constructor privado.
     *
     * Una reserva únicamente puede ser creada
     * mediante Reserva.crear(...).
     */
    private Reserva(
            CodigoReserva codigo,
            Apartamento apartamento,
            Estancia estancia,
            Ocupante titular,
            List<Ocupante> ocupantes,
            CanalOrigen canalOrigen,
            LocalDate fechaCreacion
    ) {

        this.codigo = codigo;
        this.apartamento = apartamento;
        this.estancia = estancia;
        this.titular = titular;

        /*
         * Copia inmutable para impedir que alguien modifique
         * la lista desde fuera y se salte RN-02.
         */
        this.ocupantes = List.copyOf(ocupantes);

        this.canalOrigen = canalOrigen;
        this.fechaCreacion = fechaCreacion;

        /*
         * Toda reserva nace PENDIENTE.
         */
        this.estado = EstadoReserva.PENDIENTE;
    }

    /**
     * Única puerta de entrada para crear una reserva.
     */
    public static Reserva crear(
            CodigoReserva codigo,
            Apartamento apartamento,
            Estancia estancia,
            Ocupante titular,
            List<Ocupante> ocupantes,
            CanalOrigen canalOrigen,
            LocalDate fechaActual
    ) {

        if (codigo == null) {
            throw new ReglaDominioException(
                    "La reserva debe tener un código"
            );
        }

        if (apartamento == null) {
            throw new ReglaDominioException(
                    "La reserva debe tener un apartamento"
            );
        }

        if (estancia == null) {
            throw new ReglaDominioException(
                    "La reserva debe tener una estancia"
            );
        }

        if (titular == null) {
            throw new ReglaDominioException(
                    "La reserva debe tener un titular"
            );
        }

        if (canalOrigen == null) {
            throw new ReglaDominioException(
                    "La reserva debe indicar el canal de origen"
            );
        }

        if (fechaActual == null) {
            throw new ReglaDominioException(
                    "La fecha actual es obligatoria"
            );
        }

        if (ocupantes == null || ocupantes.isEmpty()) {
            throw new ReglaDominioException(
                    "La reserva debe tener al menos un ocupante"
            );
        }

        if (ocupantes.stream().anyMatch(Objects::isNull)) {
            throw new ReglaDominioException(
                    "La lista de ocupantes no puede contener valores nulos"
            );
        }

        /*
         * El titular también debe formar parte del grupo.
         */
        if (!ocupantes.contains(titular)) {
            throw new ReglaDominioException(
                    "El titular debe ser uno de los ocupantes"
            );
        }

        /*
         * No permitimos repetir una misma persona.
         * Como Ocupante.equals() funciona por documento,
         * dos ocupantes con el mismo documento cuentan
         * como la misma persona.
         */
        if (ocupantes.stream().distinct().count() != ocupantes.size()) {
            throw new ReglaDominioException(
                    "No se puede repetir un ocupante en la reserva"
            );
        }

        /*
         * RN-04:
         * No se pueden crear reservas hacia el pasado.
         */
        if (estancia.fechaEntrada().isBefore(fechaActual)) {
            throw new ReglaDominioException(
                    "No se pueden crear reservas cuya fecha de entrada esté en el pasado"
            );
        }

        /*
         * El apartamento debe continuar activo.
         */
        if (!apartamento.estaActivo()) {
            throw new ReglaDominioException(
                    "El apartamento no está disponible para la venta"
            );
        }

        /*
         * RN-02:
         * Todos los ocupantes cuentan para la capacidad,
         * sean facturables o no.
         */
        if (!apartamento.admite(ocupantes.size())) {
            throw new ReglaDominioException(
                    "El número de ocupantes excede la capacidad del apartamento"
            );
        }

        return new Reserva(
                codigo,
                apartamento,
                estancia,
                titular,
                ocupantes,
                canalOrigen,
                fechaActual
        );
    }

    /**
     * Número total de personas que ocuparán el apartamento.
     */
    public int totalOcupantes() {
        return ocupantes.size();
    }

    public CodigoReserva getCodigo() {
        return codigo;
    }

    public CanalOrigen getCanalOrigen() {
        return canalOrigen;
    }

    public LocalDate getFechaCreacion() {
        return fechaCreacion;
    }

    public Ocupante getTitular() {
        return titular;
    }

    public Apartamento getApartamento() {
        return apartamento;
    }

    public Estancia getEstancia() {
        return estancia;
    }

    /**
     * La lista ya es inmutable.
     */
    public List<Ocupante> getOcupantes() {
        return ocupantes;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    @Override
    public boolean equals(Object objeto) {

        if (this == objeto) {
            return true;
        }

        if (!(objeto instanceof Reserva otra)) {
            return false;
        }

        return codigo.equals(otra.codigo);
    }

    @Override
    public int hashCode() {
        return codigo.hashCode();
    }
}