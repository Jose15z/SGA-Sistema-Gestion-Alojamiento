package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.EventoReserva;
import co.edu.uniquindio.sga.domain.valueobject.FechaCreacion;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.PlazoConfirmacion;
import co.edu.uniquindio.sga.domain.valueobject.UmbralEdadFacturable;
import co.edu.uniquindio.sga.domain.valueobject.ValorCongelado;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Raíz del agregado Reserva.
 *
 * <p>Referencia al apartamento mediante su identificación.
 * Conserva la cotización, la versión de política aplicable
 * y la fecha y hora de creación.</p>
 *
 * <p>Las condiciones que dependen de otros agregados,
 * como disponibilidad, capacidad y anticipo, se verifican
 * mediante las operaciones coordinadoras.</p>
 *
 * <p>La cancelación cambia el estado de la reserva.
 * Las retenciones y los movimientos del folio se coordinan
 * externamente utilizando la política congelada.</p>
 */
public class Reserva {

    private final CodigoReserva codigo;
    private final IdentificacionApartamento apartamento;
    private final CanalOrigen canalOrigen;
    private final String identificadorExterno;
    private final FechaCreacion fechaCreacion;
    private final Ocupante titular;
    private final VersionPolitica politica;

    private Estancia estancia;
    private List<Ocupante> ocupantes;
    private ValorCongelado valor;
    private EstadoReserva estado;
    private List<EventoReserva> historial;
    private LocalTime horaEstimadaLlegada;
    private String novedadLlegada;

    private Reserva(
            CodigoReserva codigo,
            IdentificacionApartamento apartamento,
            Estancia estancia,
            Ocupante titular,
            List<Ocupante> ocupantes,
            CanalOrigen canalOrigen,
            String identificadorExterno,
            FechaCreacion fechaCreacion,
            ValorCongelado valor,
            VersionPolitica politica,
            EventoReserva eventoCreacion
    ) {
        this.codigo = codigo;
        this.apartamento = apartamento;
        this.estancia = estancia;
        this.titular = titular;
        this.ocupantes = List.copyOf(ocupantes);
        this.canalOrigen = canalOrigen;
        this.identificadorExterno = identificadorExterno;
        this.fechaCreacion = fechaCreacion;
        this.valor = valor;
        this.politica = politica;
        this.estado = EstadoReserva.PENDIENTE;
        this.historial = List.of(eventoCreacion);
    }

    /**
     * Crea una reserva pendiente con cotización y política.
     *
     * <p>Las condiciones que dependen de otros agregados
     * deben verificarse antes de invocar este método.</p>
     *
     * @param codigo identidad de la reserva
     * @param apartamento identificación del apartamento
     * @param estancia intervalo solicitado
     * @param titular responsable incluido en el grupo
     * @param ocupantes composición completa del grupo
     * @param canalOrigen canal de creación
     * @param identificadorExterno referencia obligatoria para EXTERNO
     * @param umbralEdadFacturable umbral vigente del alojamiento
     * @param valor cotización de la estancia
     * @param politica versión de política aplicable
     * @param autor identificación de quien ejecuta la operación
     * @param ahora fecha y hora recibidas desde la aplicación
     * @return reserva en estado PENDIENTE
     * @throws ReglaDominioException si los datos son inválidos
     */
    public static Reserva crear(
            CodigoReserva codigo,
            IdentificacionApartamento apartamento,
            Estancia estancia,
            Ocupante titular,
            List<Ocupante> ocupantes,
            CanalOrigen canalOrigen,
            String identificadorExterno,
            UmbralEdadFacturable umbralEdadFacturable,
            ValorCongelado valor,
            VersionPolitica politica,
            String autor,
            LocalDateTime ahora
    ) {
        if (codigo == null) {
            throw new ReglaDominioException(
                    "La reserva debe tener un código"
            );
        }
        if (apartamento == null) {
            throw new ReglaDominioException(
                    "La reserva debe indicar el apartamento"
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
        if (valor == null || politica == null) {
            throw new ReglaDominioException(
                    "La reserva debe nacer con su valor "
                            + "y su política congelados"
            );
        }

        FechaCreacion fechaCreacion = new FechaCreacion(ahora);

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

        List<Ocupante> grupo = List.copyOf(ocupantes);

        if (grupo.stream().distinct().count() != grupo.size()) {
            throw new ReglaDominioException(
                    "No se puede repetir un ocupante en la reserva"
            );
        }

        Ocupante titularDelGrupo = grupo.stream()
                .filter(titular::equals)
                .findFirst()
                .orElseThrow(() -> new ReglaDominioException(
                        "El titular debe ser uno de los ocupantes"
                ));

        if (!titularDelGrupo.esFacturableEn(
                estancia, umbralEdadFacturable
        )) {
            throw new ReglaDominioException(
                    "El titular debe ser un ocupante facturable"
            );
        }
        if (estancia.fechaEntrada().isBefore(fechaCreacion.fecha())) {
            throw new ReglaDominioException(
                    "No se pueden crear reservas cuya fecha "
                            + "de entrada esté en el pasado"
            );
        }
        if (!codigo.correspondeA(fechaCreacion)) {
            throw new ReglaDominioException(
                    "El año del código de reserva debe coincidir "
                            + "con el año de creación"
            );
        }
        if (!valor.correspondeA(estancia)) {
            throw new ReglaDominioException(
                    "El desglose del valor debe corresponder "
                            + "exactamente a las noches de la estancia"
            );
        }

        long facturables = grupo.stream()
                .filter(ocupante -> ocupante.esFacturableEn(
                        estancia, umbralEdadFacturable
                ))
                .count();

        if (valor.detalle().stream().anyMatch(cargo ->
                cargo.ocupantesFacturables() != facturables
        )) {
            throw new ReglaDominioException(
                    "El desglose del valor no corresponde "
                            + "a los ocupantes facturables del grupo"
            );
        }

        if (canalOrigen.exigeIdentificadorExterno()
                && (identificadorExterno == null
                || identificadorExterno.isBlank())) {
            throw new ReglaDominioException(
                    "Una reserva externa debe indicar "
                            + "su identificador externo"
            );
        }

        EventoReserva eventoCreacion = new EventoReserva(
                ahora,
                "CREACION",
                autor,
                null,
                EstadoReserva.PENDIENTE,
                "Reserva creada por el canal " + canalOrigen
        );

        return new Reserva(
                codigo,
                apartamento,
                estancia,
                titularDelGrupo,
                grupo,
                canalOrigen,
                identificadorExterno,
                fechaCreacion,
                valor,
                politica,
                eventoCreacion
        );
    }

    /**
     * Registra o actualiza la hora estimada de llegada.
     *
     * <p>RN-09: esta información es necesaria para confirmar.
     * No puede modificarse en estados terminales.</p>
     *
     * @param hora hora estimada de llegada
     * @param autor identificación de quien registra el dato
     * @param ahora momento de la operación
     * @throws ReglaDominioException si la reserva terminó,
     *         falta un dato o el momento contradice el historial
     */
    public void indicarHoraEstimadaLlegada(
            LocalTime hora,
            String autor,
            LocalDateTime ahora
    ) {
        if (estado.esTerminal()) {
            throw new ReglaDominioException(
                    "No se puede indicar la hora de llegada "
                            + "de una reserva terminada"
            );
        }
        if (hora == null) {
            throw new ReglaDominioException(
                    "Debe indicarse la hora estimada de llegada"
            );
        }

        List<EventoReserva> nuevoHistorial = historialConEvento(
                "HORA_LLEGADA",
                autor,
                estado,
                "Hora estimada de llegada: " + hora,
                ahora
        );

        this.horaEstimadaLlegada = hora;
        this.historial = nuevoHistorial;
    }

    /**
     * Confirma una reserva pendiente con hora estimada.
     *
     * <p>RN-08 y RN-09: permite PENDIENTE a CONFIRMADA.
     * La comprobación del anticipo requiere consultar el folio
     * antes de invocar este comportamiento.</p>
     *
     * @param autor identificación de quien confirma
     * @param ahora momento de la operación
     * @throws ReglaDominioException si la transición no procede,
     *         falta la hora o la trazabilidad es inválida
     */
    public void confirmar(String autor, LocalDateTime ahora) {
        if (estado != EstadoReserva.PENDIENTE) {
            throw new ReglaDominioException(
                    "Solo una reserva PENDIENTE puede confirmarse"
            );
        }
        if (horaEstimadaLlegada == null) {
            throw new ReglaDominioException(
                    "No se puede confirmar una reserva "
                            + "sin hora estimada de llegada"
            );
        }

        List<EventoReserva> nuevoHistorial = historialConEvento(
                "CONFIRMACION",
                autor,
                EstadoReserva.CONFIRMADA,
                "Reserva confirmada",
                ahora
        );

        this.estado = EstadoReserva.CONFIRMADA;
        this.historial = nuevoHistorial;
    }

    /** Confirma dentro del plazo vigente; evita confirmar una pendiente vencida antes del siguiente barrido. */
    public void confirmar(String autor, LocalDateTime ahora, PlazoConfirmacion plazo) {
        if (estaVencida(plazo, ahora)) {
            throw new ReglaDominioException("La reserva superó el plazo de confirmación");
        }
        confirmar(autor, ahora);
    }

    /**
     * Cancela una reserva pendiente o confirmada.
     *
     * <p>RN-08 y RN-12: CANCELADA es un estado terminal
     * que deja de retener disponibilidad.</p>
     *
     * <p>Conserva el valor cotizado y la versión de política.
     * El cálculo de retención y los movimientos del folio
     * corresponden a la operación coordinadora y al servicio
     * de dominio que utiliza esa versión histórica.</p>
     *
     * @param motivo explicación obligatoria de la cancelación
     * @param autor identificación de quien cancela
     * @param ahora momento de la operación
     * @throws ReglaDominioException si el estado no permite
     *         cancelar, falta el motivo o la trazabilidad es inválida
     */
    public void cancelar(
            String motivo,
            String autor,
            LocalDateTime ahora
    ) {
        if (!estado.puedeTransicionarA(EstadoReserva.CANCELADA)) {
            throw new ReglaDominioException(
                    "Solo una reserva PENDIENTE o CONFIRMADA "
                            + "puede cancelarse"
            );
        }

        if (motivo == null || motivo.isBlank()) {
            throw new ReglaDominioException(
                    "Toda cancelación debe registrar un motivo"
            );
        }

        List<EventoReserva> nuevoHistorial = historialConEvento(
                "CANCELACION",
                autor,
                EstadoReserva.CANCELADA,
                motivo.trim()
                        + " | política de la reserva: versión "
                        + politica.numero(),
                ahora
        );

        this.estado = EstadoReserva.CANCELADA;
        this.historial = nuevoHistorial;
    }

    /**
     * Registra la llegada de una reserva confirmada (RN-08 y RN-10).
     *
     * <p>La operación coordinadora debe comprobar previamente que
     * el apartamento esté activo y preparado (RN-11), y coordinar
     * su ocupación. Esta reserva solo conserva su identificación.</p>
     *
     * @param autor identificación de quien registra la llegada
     * @param ahora fecha y hora recibidas desde la aplicación
     * @throws ReglaDominioException si el estado, la fecha o la
     *         trazabilidad no permiten registrar la llegada
     */
    public void registrarLlegada(String autor, LocalDateTime ahora) {
        if (estado != EstadoReserva.CONFIRMADA) {
            throw new ReglaDominioException(
                    "Solo una reserva CONFIRMADA puede registrar la llegada"
            );
        }
        if (ahora == null) {
            throw new ReglaDominioException(
                    "La fecha y hora de llegada son obligatorias"
            );
        }
        if (ahora.toLocalDate().isBefore(estancia.fechaEntrada())) {
            throw new ReglaDominioException(
                    "No se puede registrar la llegada antes de la fecha de entrada"
            );
        }

        List<EventoReserva> nuevoHistorial = historialConEvento(
                "REGISTRO", autor, EstadoReserva.EN_CURSO,
                "El grupo tomó el apartamento", ahora
        );
        this.estado = EstadoReserva.EN_CURSO;
        this.historial = nuevoHistorial;
    }

    /**
     * Cancela por vencimiento una reserva pendiente.
     *
     * <p>RN-21: debe haberse superado estrictamente el límite.
     * En el instante exacto del límite todavía no vence.</p>
     *
     * <p>La ejecución automática y la persistencia corresponden
     * a las capas externas al dominio.</p>
     *
     * @param plazo plazo obtenido de la configuración
     * @param ahora momento de ejecución
     * @throws ReglaDominioException si faltan datos, la reserva
     *         no está pendiente o no ha superado el plazo
     */
    public void vencer(
            PlazoConfirmacion plazo,
            LocalDateTime ahora
    ) {
        if (plazo == null) {
            throw new ReglaDominioException(
                    "El plazo de confirmación es obligatorio"
            );
        }
        if (ahora == null) {
            throw new ReglaDominioException(
                    "La fecha y hora del vencimiento son obligatorias"
            );
        }
        if (estado != EstadoReserva.PENDIENTE) {
            throw new ReglaDominioException(
                    "Solo vence una reserva PENDIENTE"
            );
        }
        if (!ahora.isAfter(plazo.limiteDesde(fechaCreacion))) {
            throw new ReglaDominioException(
                    "La reserva todavía está dentro "
                            + "del plazo de confirmación"
            );
        }

        List<EventoReserva> nuevoHistorial = historialConEvento(
                "VENCIMIENTO",
                "SISTEMA",
                EstadoReserva.CANCELADA,
                "Cancelada por vencimiento del plazo de confirmación",
                ahora
        );

        this.estado = EstadoReserva.CANCELADA;
        this.historial = nuevoHistorial;
    }

    /** Consulta de RN-21 sin mutación; el instante exacto del límite aún es válido. */
    public boolean estaVencida(PlazoConfirmacion plazo, LocalDateTime ahora) {
        if (plazo == null || ahora == null) throw new ReglaDominioException("Se requieren plazo y momento");
        return estado == EstadoReserva.PENDIENTE && ahora.isAfter(plazo.limiteDesde(fechaCreacion));
    }

    /** RN-08: finaliza únicamente una estancia en curso; el folio se verifica fuera del agregado. */
    public void registrarSalida(String autor, LocalDateTime ahora) {
        if (estado != EstadoReserva.EN_CURSO) {
            throw new ReglaDominioException("Solo una reserva EN_CURSO puede registrar la salida");
        }
        List<EventoReserva> eventos = historialConEvento("SALIDA", autor, EstadoReserva.FINALIZADA,
                "El grupo salió del apartamento", ahora);
        estado = EstadoReserva.FINALIZADA;
        historial = eventos;
    }

    /** Registra una novedad previa; impide declarar no-show hasta que recepción la resuelva. */
    public void informarNovedadLlegada(String novedad, String autor, LocalDateTime ahora) {
        if (estado != EstadoReserva.CONFIRMADA || novedad == null || novedad.isBlank()) {
            throw new ReglaDominioException("La novedad requiere reserva confirmada y descripción");
        }
        List<EventoReserva> eventos = historialConEvento("NOVEDAD_LLEGADA", autor, estado, novedad.trim(), ahora);
        novedadLlegada = novedad.trim();
        historial = eventos;
    }

    public void resolverNovedadLlegada(String motivo, String autor, LocalDateTime ahora) {
        if (estado != EstadoReserva.CONFIRMADA || novedadLlegada == null || motivo == null || motivo.isBlank()) {
            throw new ReglaDominioException("Debe existir una novedad de llegada y motivo para resolverla");
        }
        List<EventoReserva> eventos = historialConEvento("RESOLUCION_NOVEDAD", autor, estado, motivo.trim(), ahora);
        novedadLlegada = null;
        historial = eventos;
    }

    /** Solo desde CONFIRMADA, a partir del límite configurable, sin novedades pendientes. */
    public void declararNoShow(LocalTime horaLimite, String autor, LocalDateTime ahora) {
        if (estado != EstadoReserva.CONFIRMADA || horaLimite == null || ahora == null) {
            throw new ReglaDominioException("No-show requiere reserva CONFIRMADA, hora límite y momento");
        }
        if (ahora.isBefore(estancia.fechaEntrada().atTime(horaLimite))) {
            throw new ReglaDominioException("No se puede declarar no-show antes de la hora límite de entrada");
        }
        if (novedadLlegada != null) {
            throw new ReglaDominioException("Hay una novedad de llegada pendiente de resolver");
        }
        List<EventoReserva> eventos = historialConEvento("NO_SHOW", autor, EstadoReserva.NO_SHOW,
                "El titular no se presentó | política de la reserva: versión " + politica.numero(), ahora);
        estado = EstadoReserva.NO_SHOW;
        historial = eventos;
    }

    /** RN-14/22: modifica una reserva no iniciada con cotización validada y conserva la política. */
    public Dinero modificarEstancia(
            Estancia nuevaEstancia, ValorCongelado nuevoValor, UmbralEdadFacturable umbral,
            String autor, LocalDateTime ahora) {
        if (estado != EstadoReserva.PENDIENTE && estado != EstadoReserva.CONFIRMADA) {
            throw new ReglaDominioException("Solo se modifica una reserva pendiente o confirmada");
        }
        if (nuevaEstancia == null || nuevoValor == null || umbral == null || ahora == null
                || nuevaEstancia.fechaEntrada().isBefore(ahora.toLocalDate())
                || !nuevoValor.correspondeA(nuevaEstancia)) {
            throw new ReglaDominioException("La modificación requiere estancia futura y desglose correspondiente");
        }
        long facturables = ocupantes.stream().filter(o -> o.esFacturableEn(nuevaEstancia, umbral)).count();
        if (!titular.esFacturableEn(nuevaEstancia, umbral)
                || nuevoValor.detalle().stream().anyMatch(c -> c.ocupantesFacturables() != facturables)) {
            throw new ReglaDominioException("La nueva cotización no corresponde al grupo o su titular");
        }
        Dinero ajuste = nuevoValor.total().menos(valor.total());
        List<EventoReserva> eventos = historialConEvento("MODIFICACION", autor, estado,
                "Nueva estancia: " + nuevaEstancia + " | ajuste: " + ajuste.valor(), ahora);
        estancia = nuevaEstancia;
        valor = nuevoValor;
        historial = eventos;
        return ajuste;
    }

    public String getNovedadLlegada() { return novedadLlegada; }

    /**
     * Prepara un historial nuevo sin modificar el agregado.
     *
     * <p>Valida la trazabilidad antes de que el comportamiento
     * modifique sus atributos. Los eventos pueden compartir
     * momento, pero no retroceder en el tiempo.</p>
     */
    private List<EventoReserva> historialConEvento(
            String accion,
            String autor,
            EstadoReserva estadoNuevo,
            String observacion,
            LocalDateTime ahora
    ) {
        if (ahora == null) {
            throw new ReglaDominioException(
                    "La fecha y hora de la operación son obligatorias"
            );
        }

        EventoReserva ultimo = historial.get(historial.size() - 1);

        if (ahora.isBefore(ultimo.momento())) {
            throw new ReglaDominioException(
                    "La operación no puede ser anterior "
                            + "al último evento de la reserva"
            );
        }

        EventoReserva evento = new EventoReserva(
                ahora,
                accion,
                autor,
                estado,
                estadoNuevo,
                observacion
        );

        List<EventoReserva> eventos = new ArrayList<>(historial);
        eventos.add(evento);
        return List.copyOf(eventos);
    }

    /**
     * Cuenta a todos los integrantes, incluidos los no facturables.
     *
     * @return cantidad de ocupantes
     */
    public int totalOcupantes() {
        return ocupantes.size();
    }

    public CodigoReserva getCodigo() {
        return codigo;
    }

    public IdentificacionApartamento getApartamento() {
        return apartamento;
    }

    public CanalOrigen getCanalOrigen() {
        return canalOrigen;
    }

    public String getIdentificadorExterno() {
        return identificadorExterno;
    }

    public FechaCreacion getFechaCreacion() {
        return fechaCreacion;
    }

    public Ocupante getTitular() {
        return titular;
    }

    public Estancia getEstancia() {
        return estancia;
    }

    /**
     * Obtiene la composición sin permitir modificar la lista.
     *
     * @return lista inmutable de ocupantes
     */
    public List<Ocupante> getOcupantes() {
        return List.copyOf(ocupantes);
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public ValorCongelado getValor() {
        return valor;
    }

    public VersionPolitica getPolitica() {
        return politica;
    }

    /**
     * Obtiene la hora estimada registrada.
     *
     * @return hora estimada, o null si todavía no se ha indicado
     */
    public LocalTime getHoraEstimadaLlegada() {
        return horaEstimadaLlegada;
    }

    /**
     * Obtiene una instantánea inmutable de la trazabilidad.
     *
     * @return eventos registrados
     */
    public List<EventoReserva> getHistorial() {
        return List.copyOf(historial);
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
