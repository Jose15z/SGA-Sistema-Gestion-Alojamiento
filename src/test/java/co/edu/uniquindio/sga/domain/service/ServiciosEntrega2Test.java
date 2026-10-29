package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.EscenarioReservas;
import co.edu.uniquindio.sga.ReservasDePrueba;
import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.TarifarioRepository;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.IdentificadorExterno;
import co.edu.uniquindio.sga.domain.valueobject.Tarifa;
import co.edu.uniquindio.sga.domain.valueobject.TipoTemporada;
import co.edu.uniquindio.sga.domain.valueobject.ValorCongelado;
import co.edu.uniquindio.sga.domain.valueobject.Vehiculo;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Servicios del catálogo incorporados en la Entrega 2, con los puertos en memoria. */
class ServiciosEntrega2Test {

    private final EscenarioReservas e = new EscenarioReservas();

    @Test
    void retiroRechazaApartamentoConReservasActivas() {
        RetiroApartamentoService retiro = new RetiroApartamentoService(e.reservas);
        assertDoesNotThrow(() -> retiro.validarRetiro(e.apartamento));

        e.crear();
        assertThrows(ReglaDominioException.class, () -> retiro.validarRetiro(e.apartamento));
    }

    @Test
    void bloqueoRechazaNochesConReservasYBloqueosVigentes() {
        BloqueoApartamentoService servicio = new BloqueoApartamentoService(e.reservas, e.bloqueos);
        Reserva reserva = e.crear();
        IdentificacionApartamento apartamento = e.apartamento.getIdentificacion();

        assertThrows(ReglaDominioException.class, () -> servicio.validarBloqueo(apartamento, reserva.getEstancia()));

        Estancia libre = new Estancia(LocalDate.of(2027, 2, 1), LocalDate.of(2027, 2, 3));
        assertDoesNotThrow(() -> servicio.validarBloqueo(apartamento, libre));
        e.bloqueos.guardar(new Bloqueo(UUID.randomUUID(), apartamento, libre, "Pintura"));
        assertThrows(ReglaDominioException.class, () -> servicio.validarBloqueo(apartamento, libre));
    }

    @Test
    void conciliacionRechazaElMismoIdentificadorExterno() {
        ConciliacionCanalExternoService servicio = new ConciliacionCanalExternoService(e.reservas);
        IdentificadorExterno id = new IdentificadorExterno("BK-1");
        Reserva pendiente = ReservasDePrueba.pendiente();
        Reserva externa = Reserva.reconstruir(pendiente.getCodigo(), pendiente.getApartamento(), pendiente.getEstancia(),
                pendiente.getTitular(), pendiente.getOcupantes(), CanalOrigen.EXTERNO, id, pendiente.getFechaCreacion(),
                pendiente.getValor(), pendiente.getPolitica(), pendiente.getEstado(), pendiente.getHistorial(),
                null, null, 0, java.util.List.of());

        assertDoesNotThrow(() -> servicio.verificarNoDuplicada(CanalOrigen.EXTERNO, id));
        e.reservas.guardar(externa);
        assertThrows(ReglaDominioException.class, () -> servicio.verificarNoDuplicada(CanalOrigen.EXTERNO, id));
        assertDoesNotThrow(() -> servicio.verificarNoDuplicada(CanalOrigen.EXTERNO, new IdentificadorExterno("BK-2")));
    }

    @Test
    void parqueaderoCobraElVehiculoAdicionalPorNocheYRespetaLosCupos() {
        Reserva reserva = e.crear();
        reserva.registrarVehiculo(new Vehiculo("ABC123"), 2, "recepcion", ReservasDePrueba.CREACION);
        DisponibilidadParqueaderoService servicio = new DisponibilidadParqueaderoService(e.reservas, e.config);

        assertEquals(Dinero.de(40_000), servicio.verificarVehiculoAdicional(reserva));

        var sinCupos = new co.edu.uniquindio.sga.infrastructure.config.ParametrosAlojamiento(
                e.config.umbralEdadFacturable(), e.config.tiempoPreparacion(), e.config.plazoConfirmacion(),
                7, e.config.porcentajeDescuentoEstanciaProlongada(), e.config.porcentajeAnticipo(),
                e.config.horaLimiteNoShow(), e.config.minimosPorTemporada(), 1, 2, 1,
                new co.edu.uniquindio.sga.domain.valueobject.CapacidadParqueadero(1), Dinero.de(20_000));
        assertThrows(ReglaDominioException.class,
                () -> new DisponibilidadParqueaderoService(e.reservas, sinCupos).verificarVehiculoAdicional(reserva));
    }

    @Test
    void modificacionRecalculaElValorExcluyendoLaPropiaReserva() {
        Reserva reserva = e.crear();
        TarifarioRepository tarifario = (apartamento, noche) -> new Tarifa(apartamento, TipoTemporada.BASE, Dinero.de(100_000));
        ModificacionReservaService servicio = new ModificacionReservaService(
                new DisponibilidadApartamentoService(e.reservas, e.bloqueos, e.config),
                new CotizadorEstanciaService(tarifario, e.config), new EstanciaMinimaTemporadaService(e.config));
        Estancia mas = new Estancia(reserva.getEstancia().fechaEntrada(), reserva.getEstancia().fechaSalida().plusDays(1));

        ValorCongelado valor = servicio.validarModificacion(reserva, e.apartamento, mas,
                ReservasDePrueba.CREACION.toLocalDate());

        assertEquals(3, valor.noches());
    }

    @Test
    void unTitularConReservasActivasNoPuedeEliminarse() {
        Reserva reserva = e.crear();
        TitularReservasService servicio = new TitularReservasService(e.reservas);

        assertThrows(ReglaDominioException.class,
                () -> servicio.verificarSinReservasActivas(reserva.getTitular().getDocumento()));
        assertDoesNotThrow(() -> servicio.verificarSinReservasActivas(new DocumentoIdentidad("SIN-RESERVAS")));
    }

    @Test
    void modificarElGrupoRepiteLaCapacidadYRecalculaElValor() {
        Reserva reserva = e.crear();
        ModificacionReservaService servicio = modificacion();
        List<Ocupante> grupo = new ArrayList<>(reserva.getOcupantes());
        grupo.add(new Ocupante(new DocumentoIdentidad("CC-ACOMP"), "Acompañante", LocalDate.of(1990, 1, 1),
                LocalDate.of(2026, 1, 1)));

        ValorCongelado valor = servicio.validarModificacion(reserva, e.apartamento, reserva.getEstancia(), grupo,
                ReservasDePrueba.CREACION.toLocalDate());

        assertEquals(Dinero.de(400_000), valor.total());
        List<Ocupante> excedido = new ArrayList<>(grupo);
        for (int i = 0; i < 4; i++) {
            excedido.add(new Ocupante(new DocumentoIdentidad("CC-X" + i), "Extra " + i, LocalDate.of(1990, 1, 1),
                    LocalDate.of(2026, 1, 1)));
        }
        assertThrows(ReglaDominioException.class, () -> servicio.validarModificacion(reserva, e.apartamento,
                reserva.getEstancia(), excedido, ReservasDePrueba.CREACION.toLocalDate()));
    }

    @Test
    void modificarElApartamentoExigeQueElDestinoEsteDisponible() {
        Reserva reserva = e.crear();
        Apartamento destino = EscenarioReservas.apartamento("APT-302", "Destino", 2, 4);
        e.apartamentos.guardar(destino);
        ModificacionReservaService servicio = modificacion();

        assertEquals(2, servicio.validarModificacion(reserva, destino, reserva.getEstancia(),
                ReservasDePrueba.CREACION.toLocalDate()).noches());
        destino.desactivar();
        assertThrows(ReglaDominioException.class, () -> servicio.validarModificacion(reserva, destino,
                reserva.getEstancia(), ReservasDePrueba.CREACION.toLocalDate()));
    }

    private ModificacionReservaService modificacion() {
        TarifarioRepository tarifario = (apartamento, noche) -> new Tarifa(apartamento, TipoTemporada.BASE, Dinero.de(100_000));
        return new ModificacionReservaService(new DisponibilidadApartamentoService(e.reservas, e.bloqueos, e.config),
                new CotizadorEstanciaService(tarifario, e.config), new EstanciaMinimaTemporadaService(e.config));
    }

    @Test
    void registroDeApartamentoExigeIdentificacionUnica() {
        RegistroApartamentoService servicio = new RegistroApartamentoService(e.apartamentos);

        assertThrows(ReglaDominioException.class,
                () -> servicio.verificarIdentificacionDisponible(e.apartamento.getIdentificacion()));
        assertDoesNotThrow(() -> servicio.verificarIdentificacionDisponible(new IdentificacionApartamento("APT-999")));
    }
}
