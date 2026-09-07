package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.UmbralEdadFacturable;

import java.time.LocalDate;
import java.time.Period;

/**
 * Persona incluida dentro de una reserva.
 */
public class Ocupante {

    /**
     * Identidad del ocupante.
     */
    private final DocumentoIdentidad documento;

    /**
     * La fecha de nacimiento no cambia.
     * La edad NO se almacena, se calcula.
     */
    private final LocalDate fechaNacimiento;

    private String nombre;

    public Ocupante(
            DocumentoIdentidad documento,
            String nombre,
            LocalDate fechaNacimiento,
            LocalDate fechaActual
    ) {

        if (documento == null) {
            throw new ReglaDominioException(
                    "El ocupante debe tener documento de identidad"
            );
        }

        if (nombre == null || nombre.isBlank()) {
            throw new ReglaDominioException(
                    "El ocupante debe tener nombre"
            );
        }

        if (fechaNacimiento == null) {
            throw new ReglaDominioException(
                    "El ocupante debe tener fecha de nacimiento"
            );
        }

        if (fechaActual == null) {
            throw new ReglaDominioException(
                    "La fecha actual es obligatoria"
            );
        }

        if (fechaNacimiento.isAfter(fechaActual)) {
            throw new ReglaDominioException(
                    "La fecha de nacimiento no puede ser futura"
            );
        }

        this.documento = documento;
        this.nombre = nombre.trim();
        this.fechaNacimiento = fechaNacimiento;
    }

    /**
     * Calcula la edad del ocupante en una fecha determinada.
     */
    public int edadA(LocalDate fecha) {

        if (fecha == null) {
            throw new ReglaDominioException(
                    "La fecha para calcular la edad es obligatoria"
            );
        }

        if (fecha.isBefore(fechaNacimiento)) {
            throw new ReglaDominioException(
                    "No se puede calcular la edad antes de la fecha de nacimiento"
            );
        }

        return Period.between(
                fechaNacimiento,
                fecha
        ).getYears();
    }

    /**
     * RN-06:
     * Un ocupante es facturable si alcanza el umbral
     * en la fecha de entrada de la estancia.
     */
    public boolean esFacturableEn(
            Estancia estancia,
            UmbralEdadFacturable umbral
    ) {

        if (estancia == null) {
            throw new ReglaDominioException(
                    "La estancia es obligatoria"
            );
        }

        if (umbral == null) {
            throw new ReglaDominioException(
                    "El umbral de edad facturable es obligatorio"
            );
        }

        return edadA(estancia.fechaEntrada())
                >= umbral.anios();
    }

    /**
     * Comportamiento explícito del dominio.
     */
    public void actualizarNombre(String nuevoNombre) {

        if (nuevoNombre == null || nuevoNombre.isBlank()) {
            throw new ReglaDominioException(
                    "El ocupante debe tener nombre"
            );
        }

        this.nombre = nuevoNombre.trim();
    }

    public DocumentoIdentidad getDocumento() {
        return documento;
    }

    public String getNombre() {
        return nombre;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    @Override
    public boolean equals(Object objeto) {

        if (this == objeto) {
            return true;
        }

        if (!(objeto instanceof Ocupante otro)) {
            return false;
        }

        return documento.equals(otro.documento);
    }

    @Override
    public int hashCode() {
        return documento.hashCode();
    }
}