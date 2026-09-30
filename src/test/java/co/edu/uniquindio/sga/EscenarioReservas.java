package co.edu.uniquindio.sga;

import co.edu.uniquindio.sga.application.usecase.*;
import co.edu.uniquindio.sga.domain.entity.*;
import co.edu.uniquindio.sga.domain.repository.TarifarioRepository;
import co.edu.uniquindio.sga.domain.service.*;
import co.edu.uniquindio.sga.domain.valueobject.*;
import co.edu.uniquindio.sga.infrastructure.configuration.ParametrosAlojamiento;
import co.edu.uniquindio.sga.infrastructure.repository.memory.*;
import java.time.*;
import java.util.*;

/** Ensamble real de puertos en memoria para pruebas de los flujos de la Guía 06. */
public final class EscenarioReservas {
    public final ReservaRepositoryEnMemoria reservas = new ReservaRepositoryEnMemoria();
    public final ApartamentoRepositoryEnMemoria apartamentos = new ApartamentoRepositoryEnMemoria();
    public final FolioRepositoryEnMemoria folios = new FolioRepositoryEnMemoria();
    public final BloqueoRepositoryEnMemoria bloqueos = new BloqueoRepositoryEnMemoria();
    public final PoliticaCancelacionRepositoryEnMemoria politicas = new PoliticaCancelacionRepositoryEnMemoria(
            ParametrosAlojamiento.politicaInicialSieteRaices());
    public final GeneradorCodigoReservaEnMemoria codigos = new GeneradorCodigoReservaEnMemoria();
    public final ParametrosAlojamiento config = ParametrosAlojamiento.sieteRaices();
    public final Apartamento apartamento = new Apartamento(new IdentificacionApartamento("APT-301"),
            "Orquídea", 2, new Capacidad(4));
    public EscenarioReservas() { apartamentos.guardar(apartamento); }
    public Clock reloj(LocalDateTime momento) {
        ZoneId zona = ZoneId.of("America/Bogota");
        return Clock.fixed(momento.atZone(zona).toInstant(), zona);
    }
    public CrearReservaUseCase creador(LocalDateTime ahora, String temporada, long tarifa) {
        TarifarioRepository tarifario = (apartamento, noche) -> new Tarifa(apartamento, temporada, Dinero.de(tarifa));
        return new CrearReservaUseCase(reservas, apartamentos, folios, codigos, politicas, config,
                new DisponibilidadApartamentoService(reservas, bloqueos, config),
                new CotizadorEstanciaService(tarifario, config), new EstanciaMinimaTemporadaService(config), reloj(ahora));
    }
    public Reserva crear() { return crear(ReservasDePrueba.pendiente().getEstancia(), "Base"); }
    public Reserva crear(Estancia estancia, String temporada) {
        Ocupante titular = ReservasDePrueba.pendiente().getTitular();
        return creador(ReservasDePrueba.CREACION, temporada, 100_000).ejecutar(apartamento.getIdentificacion(), estancia,
                titular, List.of(titular), CanalOrigen.DIRECTO, null, LocalTime.of(15, 0), "recepcion");
    }
    public Folio folio(Reserva reserva) { return folios.obtenerPorReserva(reserva.getCodigo()).orElseThrow(); }
    public void pagar(Reserva reserva, long valor) {
        folio(reserva).registrarPago(new Pago(UUID.randomUUID(), Dinero.de(valor), "Transferencia",
                ReservasDePrueba.CREACION, "Anticipo", "recepcion"));
    }
    public void confirmar(Reserva reserva) {
        new ConfirmarReservaUseCase(reservas, folios, new AnticipoReservaService(config), config,
                reloj(ReservasDePrueba.CREACION)).ejecutar(reserva.getCodigo(), "recepcion");
    }
}
