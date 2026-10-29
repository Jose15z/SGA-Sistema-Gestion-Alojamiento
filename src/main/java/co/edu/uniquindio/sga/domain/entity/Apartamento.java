package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Capacidad;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.Tarifa;
import co.edu.uniquindio.sga.domain.valueobject.TipoTemporada;
import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.NovedadApartamento;
import co.edu.uniquindio.sga.domain.valueobject.RegistroTarifa;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

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
 *     <li>7.4: tiene tarifa en todas las temporadas; no puede quedar activo
 *         (ni registrarse ni reactivarse) con tarifas incompletas.</li>
 *     <li>Cada tarifa corresponde a este apartamento y hay una sola por temporada.</li>
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
    private String descripcion;
    private int dormitorios;
    private Capacidad capacidad;
    private List<String> caracteristicas;
    private EstadoOperativo estadoOperativo;
    private boolean activo;
    private final Map<TipoTemporada, Tarifa> tarifas = new EnumMap<>(TipoTemporada.class);
    private final List<RegistroTarifa> historialTarifas = new ArrayList<>();
    private final List<NovedadApartamento> novedades = new ArrayList<>();

    /**
     * Registra un apartamento activo y PREPARADO.
     *
     * <p>7.4: exige tarifa en todas las temporadas, por lo que nunca
     * nace un apartamento vendible con tarifas incompletas.</p>
     *
     * @param registro momento del registro: inicia el histórico de tarifas
     */
    public Apartamento(
            IdentificacionApartamento identificacion,
            String nombre,
            String descripcion,
            int dormitorios,
            Capacidad capacidad,
            List<String> caracteristicas,
            Collection<Tarifa> tarifas,
            LocalDateTime registro
    ) {
        this(identificacion, nombre, descripcion, dormitorios, capacidad, caracteristicas);
        if (tarifas == null || tarifas.stream().anyMatch(Objects::isNull) || registro == null) {
            throw new ReglaDominioException("Las tarifas y el momento de registro del apartamento son obligatorios");
        }
        for (Tarifa tarifa : tarifas) {
            if (!identificacion.equals(tarifa.apartamento())) {
                throw new ReglaDominioException("Todas las tarifas deben corresponder al apartamento");
            }
            if (this.tarifas.put(tarifa.temporada(), tarifa) != null) {
                throw new ReglaDominioException(
                        "El apartamento no puede tener dos tarifas para la temporada " + tarifa.temporada());
            }
            historialTarifas.add(new RegistroTarifa(tarifa.temporada(), tarifa.valorPorOcupanteNoche(), registro));
        }
        if (!tieneTarifasCompletas()) {
            throw new ReglaDominioException(
                    "Un apartamento activo debe tener tarifa en todas las temporadas");
        }
        this.activo = true;
    }

    private Apartamento(
            IdentificacionApartamento identificacion,
            String nombre,
            String descripcion,
            int dormitorios,
            Capacidad capacidad,
            List<String> caracteristicas
    ) {
        if (identificacion == null) {
            throw new ReglaDominioException(
                    "El apartamento debe tener identificación"
            );
        }
        if (capacidad == null) {
            throw new ReglaDominioException(
                    "El apartamento debe tener una capacidad"
            );
        }
        this.identificacion = identificacion;
        actualizarDatos(nombre, descripcion, dormitorios, caracteristicas);
        this.capacidad = capacidad;
        this.estadoOperativo = EstadoOperativo.PREPARADO;
        this.activo = false;
    }

    /**
     * Reconstruye un apartamento guardado sin repetir las validaciones
     * de registro (Guía 10). Solo la usan los adaptadores de persistencia.
     * Las tarifas vigentes son la entrada más reciente de cada temporada.
     */
    public static Apartamento reconstruir(
            IdentificacionApartamento identificacion,
            String nombre,
            String descripcion,
            int dormitorios,
            Capacidad capacidad,
            List<String> caracteristicas,
            EstadoOperativo estadoOperativo,
            boolean activo,
            List<RegistroTarifa> historial,
            List<NovedadApartamento> novedades
    ) {
        Apartamento apartamento = new Apartamento(identificacion, nombre, descripcion, dormitorios, capacidad,
                caracteristicas);
        historial.stream()
                .sorted(Comparator.comparing(RegistroTarifa::vigenteDesde))
                .forEach(registro -> {
                    apartamento.historialTarifas.add(registro);
                    apartamento.tarifas.put(registro.temporada(),
                            new Tarifa(identificacion, registro.temporada(), registro.valor()));
                });
        apartamento.estadoOperativo = Objects.requireNonNull(estadoOperativo);
        apartamento.activo = activo;
        apartamento.novedades.addAll(novedades);
        return apartamento;
    }

    /**
     * Actualiza los datos descriptivos (7.3). La capacidad cambia con
     * {@link #cambiarCapacidad} y las tarifas con {@link #actualizarTarifa}.
     */
    public void actualizarDatos(String nuevoNombre, String nuevaDescripcion, int nuevosDormitorios,
                                List<String> nuevasCaracteristicas) {
        if (nuevoNombre == null || nuevoNombre.isBlank()) {
            throw new ReglaDominioException(
                    "El apartamento debe tener un nombre"
            );
        }
        if (nuevosDormitorios < 1) {
            throw new ReglaDominioException(
                    "El apartamento debe tener al menos un dormitorio"
            );
        }
        if (nuevasCaracteristicas == null
                || nuevasCaracteristicas.stream().anyMatch(c -> c == null || c.isBlank())) {
            throw new ReglaDominioException("Las características del apartamento no pueden estar vacías");
        }
        this.nombre = nuevoNombre.trim();
        this.descripcion = nuevaDescripcion == null ? "" : nuevaDescripcion.trim();
        this.dormitorios = nuevosDormitorios;
        this.caracteristicas = nuevasCaracteristicas.stream().map(String::trim).distinct().toList();
    }

    /** 7.5: tiene todas las características pedidas (sin distinguir mayúsculas). */
    public boolean tieneCaracteristicas(List<String> buscadas) {
        if (buscadas == null || buscadas.isEmpty()) {
            return true;
        }
        return buscadas.stream().allMatch(buscada -> caracteristicas.stream()
                .anyMatch(propia -> propia.toLowerCase(Locale.ROOT)
                        .contains(buscada.trim().toLowerCase(Locale.ROOT))));
    }

    /** 7.4: hay tarifa para cada temporada del alojamiento. */
    public boolean tieneTarifasCompletas() {
        return Arrays.stream(TipoTemporada.values()).allMatch(tarifas::containsKey);
    }

    /**
     * Reemplaza la tarifa de una temporada y la agrega al histórico (7.4).
     * No altera el valor congelado de las reservas existentes (RN-22, 3.5).
     */
    public void actualizarTarifa(TipoTemporada temporada, Dinero valorPorOcupanteNoche, LocalDateTime ahora) {
        Tarifa nueva = new Tarifa(identificacion, temporada, valorPorOcupanteNoche);
        if (ahora == null) {
            throw new ReglaDominioException("El momento del cambio de tarifa es obligatorio");
        }
        RegistroTarifa registro = new RegistroTarifa(temporada, valorPorOcupanteNoche, ahora);
        tarifas.put(temporada, nueva);
        historialTarifas.add(registro);
    }

    /** Tarifa vigente del apartamento para una temporada (RN-05). */
    public Tarifa tarifaPara(TipoTemporada temporada) {
        Tarifa tarifa = tarifas.get(temporada);
        if (tarifa == null) {
            throw new ReglaDominioException(
                    "El apartamento " + identificacion.valor() + " no tiene tarifa para la temporada " + temporada);
        }
        return tarifa;
    }

    /** Tarifas vigentes, una por temporada. */
    public List<Tarifa> getTarifas() {
        return List.copyOf(tarifas.values());
    }

    /** Histórico completo de tarifas, del más reciente al más antiguo (7.4). */
    public List<RegistroTarifa> getHistorialTarifas() {
        return historialTarifas.stream()
                .sorted(Comparator.comparing(RegistroTarifa::vigenteDesde).reversed())
                .toList();
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
        if (!activo) {
            throw new ReglaDominioException("El apartamento ya está retirado de la venta");
        }
        this.activo = false;
    }

    /**
     * Devuelve el apartamento a la venta.
     *
     * <p>7.4: no se reactiva un apartamento sin tarifas completas.</p>
     */
    public void reactivar() {
        if (activo) {
            throw new ReglaDominioException("El apartamento ya está activo para la venta");
        }
        if (!tieneTarifasCompletas()) {
            throw new ReglaDominioException(
                    "No se puede activar un apartamento sin tarifas en todas las temporadas");
        }
        this.activo = true;
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

    public String getDescripcion() {
        return descripcion;
    }

    public List<String> getCaracteristicas() {
        return caracteristicas;
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

    /** 7.6: registra una novedad; el historial solo crece. */
    public void registrarNovedad(NovedadApartamento novedad) {
        if (novedad == null) {
            throw new ReglaDominioException("La novedad es obligatoria");
        }
        novedades.add(novedad);
    }

    /** Historial de novedades, de la más reciente a la más antigua (7.6). */
    public List<NovedadApartamento> getNovedades() {
        return novedades.stream()
                .sorted(Comparator.comparing(NovedadApartamento::fecha).reversed())
                .toList();
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