package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.FechaCreacion;
import co.edu.uniquindio.sga.domain.valueobject.UmbralEdadFacturable;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Compromiso de ocupar un apartamento durante una estancia,
 * para un conjunto definido de ocupantes.
 */
public class Reserva {

    private final CodigoReserva codigo;
    private final CanalOrigen canalOrigen;
    private final FechaCreacion fechaCreacion;
    private final Ocupante titular;

    private Apartamento apartamento;
    private Estancia estancia;
    private List<Ocupante> ocupantes;
    private EstadoReserva estado;

    private Reserva(
            CodigoReserva codigo,
            Apartamento apartamento,
            Estancia estancia,
            Ocupante titular,
            List<Ocupante> ocupantes,
            CanalOrigen canalOrigen,
            FechaCreacion fechaCreacion
    ) {
        this.codigo = codigo;
        this.apartamento = apartamento;
        this.estancia = estancia;
        this.titular = titular;
        this.ocupantes = List.copyOf(ocupantes);
        this.canalOrigen = canalOrigen;
        this.fechaCreacion = fechaCreacion;
        this.estado = EstadoReserva.PENDIENTE;
    }

    public static Reserva crear(
            CodigoReserva codigo,
            Apartamento apartamento,
            Estancia estancia,
            Ocupante titular,
            List<Ocupante> ocupantes,
            CanalOrigen canalOrigen,
            UmbralEdadFacturable umbralEdadFacturable,
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
                    "La reserva debe indicar su canal de origen"
            );
        }

        if (umbralEdadFacturable == null) {
            throw new ReglaDominioException(
                    "El umbral de edad facturable es obligatorio"
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

        if (!ocupantes.contains(titular)) {
            throw new ReglaDominioException(
                    "El titular debe ser uno de los ocupantes"
            );
        }

        if (ocupantes.stream().distinct().count() != ocupantes.size()) {
            throw new ReglaDominioException(
                    "No se puede repetir un ocupante en la reserva"
            );
        }

        /*
         * El titular debe ser facturable.
         */
        if (!titular.esFacturableEn(estancia, umbralEdadFacturable)) {
            throw new ReglaDominioException(
                    "El titular debe ser un ocupante facturable"
            );
        }

        /*
         * RN-04:
         * No se crean reservas hacia el pasado.
         */
        if (estancia.fechaEntrada().isBefore(fechaActual)) {
            throw new ReglaDominioException(
                    "No se pueden crear reservas cuya fecha de entrada esté en el pasado"
            );
        }

        if (!apartamento.estaActivo()) {
            throw new ReglaDominioException(
                    "El apartamento no está disponible para la venta"
            );
        }

        /*
         * RN-02:
         * Todos los ocupantes cuentan para la capacidad.
         */
        if (!apartamento.admite(ocupantes.size())) {
            throw new ReglaDominioException(
                    "El número de ocupantes excede la capacidad del apartamento"
            );
        }

        /*
         * La fecha actual pasa a convertirse en la fecha
         * de creación de la nueva reserva.
         */
        FechaCreacion fechaCreacion =
                new FechaCreacion(fechaActual);

        /*
         * RES-YYYY-NNNNN:
         * YYYY debe corresponder al año de creación.
         */
        if (!codigo.correspondeA(fechaCreacion)) {
            throw new ReglaDominioException(
                    "El año del código de reserva debe coincidir con el año de creación"
            );
        }

        return new Reserva(
                codigo,
                apartamento,
                estancia,
                titular,
                ocupantes,
                canalOrigen,
                fechaCreacion
        );
    }

    public int totalOcupantes() {
        return ocupantes.size();
    }

    public CodigoReserva getCodigo() {
        return codigo;
    }

    public CanalOrigen getCanalOrigen() {
        return canalOrigen;
    }

    public FechaCreacion getFechaCreacion() {
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

    public List<Ocupante> getOcupantes() {
        return ocupantes;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public void registrarSalida() {
        // TODO: Integrante 1 - Completar reglas de dominio e historial (Guía 05)
        this.estado = EstadoReserva.FINALIZADA;
    }

    public void declararNoShow() {
        // TODO: Integrante 1 - Completar reglas de dominio e historial (Guía 05)
        this.estado = EstadoReserva.NO_SHOW;
    }

    public void cancelar(String motivo) {
        // TODO: Integrante 1 - Completar reglas de dominio e historial (Guía 05)
        this.estado = EstadoReserva.CANCELADA;
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