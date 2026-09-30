package co.edu.uniquindio.sga.domain.service;
import co.edu.uniquindio.sga.*;
import co.edu.uniquindio.sga.domain.entity.*;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.ConfiguracionAlojamiento;
import co.edu.uniquindio.sga.domain.valueobject.*;
import org.junit.jupiter.api.*;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DisponibilidadApartamentoServiceTest {
    EscenarioReservas e;
    ConfiguracionAlojamiento config;
    DisponibilidadApartamentoService servicio;
    Estancia estancia;
    @BeforeEach void preparar() {
        e=new EscenarioReservas();config=mock(ConfiguracionAlojamiento.class);
        when(config.tiempoPreparacion()).thenReturn(new TiempoPreparacion(3,LocalTime.of(11,0),LocalTime.of(15,0)));
        servicio=new DisponibilidadApartamentoService(e.reservas,e.bloqueos,config);
        estancia=ReservasDePrueba.pendiente().getEstancia();
    }
    @Test void validaCapacidadIncluyendoNoFacturables() {
        assertTrue(servicio.estaDisponible(e.apartamento,estancia,4));
        assertFalse(servicio.estaDisponible(e.apartamento,estancia,5));
        assertFalse(servicio.estaDisponible(e.apartamento,estancia,0));
    }
    @Test void noVendeInactivo() {
        e.apartamento.desactivar();assertFalse(servicio.estaDisponible(e.apartamento,estancia,1));
    }
    @Test void estadoOperativoActualNoImpideReservaFutura() {
        e.apartamento.marcarOcupado();assertTrue(servicio.estaDisponible(e.apartamento,estancia,1));
    }
    @Test void bloqueoSolapadoImpideReservaYLevantadoLaPermite() {
        Bloqueo bloqueo=new Bloqueo(UUID.randomUUID(),e.apartamento.getIdentificacion(),estancia,"Mantenimiento");
        e.bloqueos.guardar(bloqueo);
        assertFalse(servicio.estaDisponible(e.apartamento,estancia,1));
        bloqueo.levantar("Mantenimiento terminado");
        assertTrue(servicio.estaDisponible(e.apartamento,estancia,1));
    }
    @Test void bloqueoDeOtroApartamentoNoInterfiere() {
        e.bloqueos.guardar(new Bloqueo(UUID.randomUUID(),new IdentificacionApartamento("APT-101"),estancia,"Mantenimiento"));
        assertTrue(servicio.estaDisponible(e.apartamento,estancia,1));
    }
    @Test void reservaExcluidaNoCompiteConsigoMisma() {
        Reserva r=e.crear();
        assertDoesNotThrow(()->servicio.verificarDisponible(e.apartamento,estancia,1,r.getCodigo()));
        assertFalse(servicio.estaDisponible(e.apartamento,estancia,1));
    }
    @Test void preparacionSeValidaAntesYDespuesDeLaNuevaReserva() {
        e.crear();
        Estancia antes=new Estancia(estancia.fechaEntrada().minusDays(2),estancia.fechaEntrada());
        Estancia despues=new Estancia(estancia.fechaSalida(),estancia.fechaSalida().plusDays(2));
        assertTrue(servicio.estaDisponible(e.apartamento,antes,1));
        assertTrue(servicio.estaDisponible(e.apartamento,despues,1));
        when(config.tiempoPreparacion()).thenReturn(new TiempoPreparacion(5,LocalTime.of(11,0),LocalTime.of(15,0)));
        assertFalse(servicio.estaDisponible(e.apartamento,antes,1));
        assertFalse(servicio.estaDisponible(e.apartamento,despues,1));
    }
    @Test void preparacionDeMasDeUnDiaNoSeEvadeDejandoUnaNocheLibre() {
        e.crear();
        when(config.tiempoPreparacion()).thenReturn(new TiempoPreparacion(30,LocalTime.of(11,0),LocalTime.of(15,0)));
        assertFalse(servicio.estaDisponible(e.apartamento,new Estancia(estancia.fechaSalida().plusDays(1),estancia.fechaSalida().plusDays(3)),1));
    }
    @Test void ventanaExactaDePreparacionEsValida() {
        e.crear();
        when(config.tiempoPreparacion()).thenReturn(new TiempoPreparacion(4,LocalTime.of(11,0),LocalTime.of(15,0)));
        assertTrue(servicio.estaDisponible(e.apartamento,new Estancia(estancia.fechaSalida(),estancia.fechaSalida().plusDays(2)),1));
    }
    @Test void reservaCanceladaLiberaNoches() {
        Reserva r=e.crear();r.cancelar("Cambio","recepcion",ReservasDePrueba.CREACION);
        assertTrue(servicio.estaDisponible(e.apartamento,estancia,1));
    }
}
