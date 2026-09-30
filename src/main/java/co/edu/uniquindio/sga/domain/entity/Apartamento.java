package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Capacidad;
import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;

/**
 * Raíz del agregado Apartamento.
 *
 * <p>Representa la unidad completa que puede reservarse.</p>
 *
 * <p>Invariantes que garantiza:</p>
 * <ul>
 *     <li>La identificación es obligatoria e inmutable.</li>
 *     <li>El nombre no está vacío.</li>
 *     <li>Existe al menos un dormitorio.</li>
 *     <li>La capacidad es un objeto de valor válido.</li>
 *     <li>Los cambios operativos respetan las transiciones permitidas.</li>
 *     <li>Solo un apartamento activo y PREPARADO puede ocuparse.</li>
 *     <li>La igualdad depende únicamente de la identificación.</li>
 * </ul>
 *
 * <p>Precondiciones externas que no garantiza:</p>
 * <ul>
 *     <li>La ausencia de reservas o bloqueos solapados.</li>
 *     <li>El cumplimiento del tiempo de preparación entre estancias.</li>
 *     <li>La ausencia de reservas activas o futuras antes del retiro.</li>
 *     <li>La existencia de tarifas completas para la venta.</li>
 *     <li>El estado de la reserva cuyo grupo recibe el apartamento.</li>
 *     <li>Los permisos de quien ejecuta cada operación.</li>
 * </ul>
 */
public class Apartamento {

    private final IdentificacionApartamento identificacion;

    private String nombre;
    private int dormitorios;
    private Capacidad capacidad;
    private EstadoOperativo estadoOperativo;
    private boolean activo;

    /**
     * Construye un apartamento conservando el estado inicial
     * del modelo existente: activo y PREPARADO.
     *
     * <p>Las comprobaciones que requieren tarifas u otros agregados
     * corresponden a los servicios de dominio pertinentes.</p>
     */
    public Apartamento(
            IdentificacionApartamento identificacion,
            String nombre,
            int dormitorios,
            Capacidad capacidad
    ) {
        if (identificacion == null) {
            throw new ReglaDominioException(
                    "El apartamento debe tener identificación"
            );
        }

        if (nombre == null || nombre.isBlank()) {
            throw new ReglaDominioException(
                    "El apartamento debe tener un nombre"
            );
        }

        if (dormitorios < 1) {
            throw new ReglaDominioException(
                    "El apartamento debe tener al menos un dormitorio"
            );
        }

        if (capacidad == null) {
            throw new ReglaDominioException(
                    "El apartamento debe tener una capacidad"
            );
        }

        this.identificacion = identificacion;
        this.nombre = nombre.trim();
        this.dormitorios = dormitorios;
        this.capacidad = capacidad;
        this.estadoOperativo = EstadoOperativo.PREPARADO;
        this.activo = true;
    }

    /**
     * RN-02: consulta si la capacidad admite al grupo completo.
     *
     * <p>Todos los ocupantes cuentan, sean facturables o no.</p>
     */
    public boolean admite(int totalOcupantes) {
        return capacidad.admite(totalOcupantes);
    }

    /**
     * RN-11: solo un apartamento activo y PREPARADO
     * puede recibir físicamente a un grupo.
     */
    public boolean puedeRecibirGrupo() {
        return activo && estadoOperativo.permiteRegistro();
    }

    /**
     * Registra la ocupación física: PREPARADO a OCUPADO.
     *
     * <p>La comprobación de la reserva y la coordinación de ambas
     * transiciones se realizan fuera de este agregado.</p>
     */
    public void marcarOcupado() {
        if (!activo) {
            throw new ReglaDominioException(
                    "Un apartamento inactivo no puede recibir un grupo"
            );
        }

        verificarTransicionOperativa(EstadoOperativo.OCUPADO);
        this.estadoOperativo = EstadoOperativo.OCUPADO;
    }

    /**
     * Registra la liberación tras la salida del grupo:
     * OCUPADO a PENDIENTE_PREPARACION.
     */
    public void liberar() {
        if (estadoOperativo != EstadoOperativo.OCUPADO) {
            throw new ReglaDominioException(
                    "Solo se puede liberar un apartamento ocupado"
            );
        }

        verificarTransicionOperativa(
                EstadoOperativo.PENDIENTE_PREPARACION
        );

        this.estadoOperativo = EstadoOperativo.PENDIENTE_PREPARACION;
    }

    /**
     * Inicia la preparación:
     * PENDIENTE_PREPARACION a EN_PREPARACION.
     */
    public void iniciarPreparacion() {
        verificarTransicionOperativa(EstadoOperativo.EN_PREPARACION);
        this.estadoOperativo = EstadoOperativo.EN_PREPARACION;
    }

    /**
     * Finaliza la preparación:
     * EN_PREPARACION a PREPARADO.
     */
    public void marcarPreparado() {
        verificarTransicionOperativa(EstadoOperativo.PREPARADO);
        this.estadoOperativo = EstadoOperativo.PREPARADO;
    }

    /**
     * Declara el apartamento fuera de servicio.
     *
     * <p>No se permite desde OCUPADO. Esta operación no crea
     * un bloqueo de fechas ni desactiva el apartamento.</p>
     */
    public void declararFueraDeServicio() {
        verificarTransicionOperativa(EstadoOperativo.FUERA_DE_SERVICIO);
        this.estadoOperativo = EstadoOperativo.FUERA_DE_SERVICIO;
    }

    /**
     * Retira la condición de fuera de servicio y deja
     * el apartamento pendiente de preparación.
     *
     * <p>No permite entregarlo directamente a otro grupo.</p>
     */
    public void reincorporarAPreparacion() {
        if (estadoOperativo != EstadoOperativo.FUERA_DE_SERVICIO) {
            throw new ReglaDominioException(
                    "Solo se puede reincorporar a preparación "
                            + "un apartamento fuera de servicio"
            );
        }

        verificarTransicionOperativa(
                EstadoOperativo.PENDIENTE_PREPARACION
        );

        this.estadoOperativo = EstadoOperativo.PENDIENTE_PREPARACION;
    }

    /**
     * Sustituye la capacidad por otro objeto de valor válido.
     *
     * <p>No modifica reservas existentes. Consultar las reservas
     * afectadas y advertir al administrador corresponde
     * a la operación que coordina el cambio.</p>
     */
    public void cambiarCapacidad(Capacidad nuevaCapacidad) {
        if (nuevaCapacidad == null) {
            throw new ReglaDominioException(
                    "La nueva capacidad del apartamento es obligatoria"
            );
        }

        this.capacidad = nuevaCapacidad;
    }

    /**
     * Retira lógicamente el apartamento de la venta.
     *
     * <p>Precondición externa: antes de invocar este comportamiento,
     * debe comprobarse que no existen reservas activas ni futuras
     * que impidan el retiro.</p>
     *
     * <p>No elimina su identidad ni cambia su estado operativo.</p>
     */
    public void desactivar() {
        this.activo = false;
    }

    /**
     * Rechaza una transición antes de modificar el estado.
     */
    private void verificarTransicionOperativa(
            EstadoOperativo siguiente
    ) {
        if (!estadoOperativo.puedeTransicionarA(siguiente)) {
            throw new ReglaDominioException(
                    "No se puede pasar de "
                            + estadoOperativo
                            + " a "
                            + siguiente
            );
        }
    }

    public IdentificacionApartamento getIdentificacion() {
        return identificacion;
    }

    public String getNombre() {
        return nombre;
    }

    public int getDormitorios() {
        return dormitorios;
    }

    public Capacidad getCapacidad() {
        return capacidad;
    }

    public EstadoOperativo getEstadoOperativo() {
        return estadoOperativo;
    }

    public boolean estaActivo() {
        return activo;
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }

        if (!(objeto instanceof Apartamento otro)) {
            return false;
        }

        return identificacion.equals(otro.identificacion);
    }

    @Override
    public int hashCode() {
        return identificacion.hashCode();
    }
}