package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.EscenarioReservas;
import co.edu.uniquindio.sga.ReservasDePrueba;
import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.entity.Usuario;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.TarifarioRepository;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.CorreoElectronico;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.IdentificadorExterno;
import co.edu.uniquindio.sga.domain.valueobject.Tarifa;
import co.edu.uniquindio.sga.domain.valueobject.TipoTemporada;
import co.edu.uniquindio.sga.domain.valueobject.ValorCongelado;
import co.edu.uniquindio.sga.domain.valueobject.Vehiculo;
import co.edu.uniquindio.sga.infrastructure.persistence.inmemory.UsuarioRepositoryEnMemoria;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

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

        ValorCongelado valor = servicio.validarModificacion(reserva, e.apartamento, mas);

        assertEquals(3, valor.noches());
    }

    @Test
    void registroDeUsuarioExigeCorreoYDocumentoUnicos() {
        UsuarioRepositoryEnMemoria usuarios = new UsuarioRepositoryEnMemoria();
        RegistroUsuarioService servicio = new RegistroUsuarioService(usuarios);
        CorreoElectronico correo = new CorreoElectronico("ana@correo.com");
        DocumentoIdentidad documento = new DocumentoIdentidad("1094");
        usuarios.guardar(Usuario.registrarHuesped(UUID.randomUUID(), correo, documento, "Ana", "hash"));

        assertThrows(ReglaDominioException.class, () -> servicio.verificarDisponible(correo, new DocumentoIdentidad("9")));
        assertThrows(ReglaDominioException.class,
                () -> servicio.verificarDisponible(new CorreoElectronico("otra@correo.com"), documento));
        assertDoesNotThrow(() -> servicio.verificarDisponible(new CorreoElectronico("otra@correo.com"),
                new DocumentoIdentidad("9")));
    }

    @Test
    void registroDeApartamentoExigeIdentificacionUnica() {
        RegistroApartamentoService servicio = new RegistroApartamentoService(e.apartamentos);

        assertThrows(ReglaDominioException.class,
                () -> servicio.verificarIdentificacionDisponible(e.apartamento.getIdentificacion()));
        assertDoesNotThrow(() -> servicio.verificarIdentificacionDisponible(new IdentificacionApartamento("APT-999")));
    }
}
