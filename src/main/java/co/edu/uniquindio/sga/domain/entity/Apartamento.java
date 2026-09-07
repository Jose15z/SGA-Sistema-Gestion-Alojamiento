package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Capacidad;
import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;

/**
 * Unidad completa que puede ser reservada dentro del alojamiento.
 */
public class Apartamento {

    /**
     * Identidad del apartamento.
     * Nunca cambia.
     */
    private final IdentificacionApartamento identificacion;

    private String nombre;
    private int dormitorios;
    private Capacidad capacidad;
    private EstadoOperativo estadoOperativo;
    private boolean activo;

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

        /*
         * Un apartamento inicialmente queda preparado y activo.
         */
        this.estadoOperativo = EstadoOperativo.PREPARADO;
        this.activo = true;
    }

    /**
     * RN-02:
     * Determina si el apartamento admite determinada
     * cantidad de ocupantes.
     */
    public boolean admite(int totalOcupantes) {
        return capacidad.admite(totalOcupantes);
    }

    /**
     * Determina si el apartamento puede recibir físicamente
     * a un grupo.
     */
    public boolean puedeRecibirGrupo() {
        return activo && estadoOperativo.permiteRegistro();
    }

    /**
     * La eliminación del apartamento es lógica.
     */
    public void desactivar() {
        this.activo = false;
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

    /**
     * Las entidades se comparan únicamente por identidad.
     */
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