package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.ServicioAdicional;
import co.edu.uniquindio.sga.domain.valueobject.Ubicacion;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Raíz del agregado Alojamiento: el negocio completo (F-13, 7.3).
 *
 * <p>Invariantes:</p>
 * <ul>
 *     <li>Nombre, ciudad, dirección y ubicación válida obligatorios.</li>
 *     <li>Las normas de convivencia no tienen textos vacíos.</li>
 *     <li>Cada servicio adicional tiene un código único y, si genera cargo, un valor positivo (L-17).</li>
 * </ul>
 *
 * <p>Los parámetros de la sección 6 (horarios, umbral, plazos) son configuración
 * externa; aquí viven los datos descriptivos y el catálogo de servicios.</p>
 */
public final class Alojamiento {

    private final UUID identificacion;
    private String nombre;
    private String descripcion;
    private String ciudad;
    private String direccion;
    private Ubicacion ubicacion;
    private List<String> normas;
    private List<ServicioAdicional> servicios;

    public Alojamiento(UUID identificacion, String nombre, String descripcion, String ciudad, String direccion,
                       Ubicacion ubicacion, List<String> normas, List<ServicioAdicional> servicios) {
        this.identificacion = Objects.requireNonNull(identificacion, "La identificación del alojamiento es obligatoria");
        actualizarDatos(nombre, descripcion, ciudad, direccion, ubicacion, normas);
        definirServicios(servicios);
    }

    /** Actualiza los datos descriptivos del alojamiento. */
    public void actualizarDatos(String nuevoNombre, String nuevaDescripcion, String nuevaCiudad, String nuevaDireccion,
                                Ubicacion nuevaUbicacion, List<String> nuevasNormas) {
        if (nuevoNombre == null || nuevoNombre.isBlank()) {
            throw new ReglaDominioException("El alojamiento debe tener nombre");
        }
        if (nuevaCiudad == null || nuevaCiudad.isBlank() || nuevaDireccion == null || nuevaDireccion.isBlank()) {
            throw new ReglaDominioException("El alojamiento debe tener ciudad y dirección");
        }
        if (nuevaUbicacion == null) {
            throw new ReglaDominioException("El alojamiento debe tener ubicación");
        }
        if (nuevasNormas == null || nuevasNormas.stream().anyMatch(norma -> norma == null || norma.isBlank())) {
            throw new ReglaDominioException("Las normas de convivencia no pueden estar vacías");
        }
        this.nombre = nuevoNombre.trim();
        this.descripcion = nuevaDescripcion == null ? "" : nuevaDescripcion.trim();
        this.ciudad = nuevaCiudad.trim();
        this.direccion = nuevaDireccion.trim();
        this.ubicacion = nuevaUbicacion;
        this.normas = nuevasNormas.stream().map(String::trim).toList();
    }

    /** Reemplaza el catálogo de servicios adicionales; los códigos no se repiten. */
    public void definirServicios(List<ServicioAdicional> nuevosServicios) {
        if (nuevosServicios == null || nuevosServicios.stream().anyMatch(Objects::isNull)) {
            throw new ReglaDominioException("La lista de servicios adicionales es obligatoria");
        }
        if (nuevosServicios.stream().map(ServicioAdicional::codigo).distinct().count() != nuevosServicios.size()) {
            throw new ReglaDominioException("No se puede repetir el código de un servicio adicional");
        }
        this.servicios = List.copyOf(new ArrayList<>(nuevosServicios));
    }

    /** Servicio del catálogo con el código indicado. */
    public ServicioAdicional servicio(String codigo) {
        return servicios.stream()
                .filter(servicio -> servicio.codigo().equalsIgnoreCase(codigo == null ? "" : codigo.trim()))
                .findFirst()
                .orElseThrow(() -> new ReglaDominioException("El alojamiento no ofrece el servicio " + codigo));
    }

    public UUID getIdentificacion() { return identificacion; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public String getCiudad() { return ciudad; }
    public String getDireccion() { return direccion; }
    public Ubicacion getUbicacion() { return ubicacion; }
    public List<String> getNormas() { return normas; }
    public List<ServicioAdicional> getServicios() { return servicios; }

    @Override
    public boolean equals(Object objeto) {
        return objeto instanceof Alojamiento otro && identificacion.equals(otro.identificacion);
    }

    @Override
    public int hashCode() {
        return identificacion.hashCode();
    }
}
