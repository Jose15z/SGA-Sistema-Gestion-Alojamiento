package co.edu.uniquindio.sga;

import co.edu.uniquindio.sga.application.seguridad.Rol;
import co.edu.uniquindio.sga.application.seguridad.Solicitante;
import co.edu.uniquindio.sga.application.usecase.*;
import co.edu.uniquindio.sga.domain.entity.*;
import co.edu.uniquindio.sga.domain.repository.TarifarioRepository;
import co.edu.uniquindio.sga.domain.service.*;
import co.edu.uniquindio.sga.domain.valueobject.*;
import co.edu.uniquindio.sga.infrastructure.config.ParametrosAlojamiento;
import co.edu.uniquindio.sga.infrastructure.persistence.inmemory.*;
import java.time.*;
import java.util.*;

/** Ensamble real de puertos en memoria para pruebas de los flujos de la Guía 06. */
public final class EscenarioReservas {
    public static final Solicitante RECEPCION = new Solicitante("recepcion", Rol.RECEPCIONISTA,
            new DocumentoIdentidad("RECEPCION-1"));

    public final ReservaRepositoryEnMemoria reservas = new ReservaRepositoryEnMemoria();
    public final ApartamentoRepositoryEnMemoria apartamentos = new ApartamentoRepositoryEnMemoria();
    public final FolioRepositoryEnMemoria folios = new FolioRepositoryEnMemoria();
    public final BloqueoRepositoryEnMemoria bloqueos = new BloqueoRepositoryEnMemoria();
    public final PoliticaCancelacionRepositoryEnMemoria politicas = new PoliticaCancelacionRepositoryEnMemoria(
            ParametrosDePrueba.politicaInicialSieteRaices());
    public final GeneradorCodigoReservaEnMemoria codigos = new GeneradorCodigoReservaEnMemoria();
    public final ParametrosAlojamiento config = ParametrosDePrueba.sieteRaices();
    public final Apartamento apartamento = apartamento("APT-301", "Orquídea", 2, 4);

    public EscenarioReservas() { apartamentos.guardar(apartamento); }

    /** Apartamento activo con tarifa en todas las temporadas (7.4). */
    public static Apartamento apartamento(String id, String nombre, int dormitorios, int capacidad) {
        IdentificacionApartamento identificacion = new IdentificacionApartamento(id);
        List<Tarifa> tarifas = Arrays.stream(TipoTemporada.values())
                .map(t -> new Tarifa(identificacion, t, Dinero.de(100_000))).toList();
        return new Apartamento(identificacion, nombre, "Apartamento " + nombre, dormitorios, new Capacidad(capacidad),
                List.of("Cocina equipada", "Balcón"), tarifas, ReservasDePrueba.CREACION);
    }

    public Clock reloj(LocalDateTime momento) {
        ZoneId zona = ZoneId.of("America/Bogota");
        return Clock.fixed(momento.atZone(zona).toInstant(), zona);
    }
    public CrearReservaUseCase creador(LocalDateTime ahora, TipoTemporada temporada, long tarifa) {
        TarifarioRepository tarifario = (apartamento, noche) -> new Tarifa(apartamento, temporada, Dinero.de(tarifa));
        return new CrearReservaUseCase(reservas, apartamentos, folios, codigos, politicas, config,
                new DisponibilidadApartamentoService(reservas, bloqueos, config),
                new CotizadorEstanciaService(tarifario, config), new EstanciaMinimaTemporadaService(config),
                new ConciliacionCanalExternoService(reservas), reloj(ahora));
    }
    public Reserva crear() { return crear(ReservasDePrueba.pendiente().getEstancia(), TipoTemporada.BASE); }
    public Reserva crear(Estancia estancia, TipoTemporada temporada) {
        Ocupante titular = ReservasDePrueba.pendiente().getTitular();
        return creador(ReservasDePrueba.CREACION, temporada, 100_000).ejecutar(apartamento.getIdentificacion(), estancia,
                titular, List.of(titular), CanalOrigen.DIRECTO, null, LocalTime.of(15, 0), 0, RECEPCION);
    }
    public Folio folio(Reserva reserva) { return folios.obtenerPorReserva(reserva.getCodigo()).orElseThrow(); }
    public void pagar(Reserva reserva, long valor) {
        folio(reserva).registrarPago(new Pago(UUID.randomUUID(), Dinero.de(valor), MedioPago.TRANSFERENCIA_BANCARIA,
                ReservasDePrueba.CREACION, "Anticipo", "recepcion"));
    }
    public void confirmar(Reserva reserva) {
        new ConfirmarReservaUseCase(reservas, folios, new AnticipoReservaService(config), config,
                reloj(ReservasDePrueba.CREACION)).ejecutar(reserva.getCodigo(), RECEPCION);
    }
}
