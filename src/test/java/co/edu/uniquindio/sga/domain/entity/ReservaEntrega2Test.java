package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.ReservasDePrueba;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Vehiculo;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Invariantes de la reserva agregadas en la Entrega 2: L-16, RP-02, propietario y reconstrucción. */
class ReservaEntrega2Test {

    private static final LocalDateTime AHORA = ReservasDePrueba.CREACION;

    @Test
    void rechazarMasDeUnaMascota() {
        Reserva reserva = ReservasDePrueba.pendiente();

        assertThrows(ReglaDominioException.class, () -> reserva.indicarMascotas(2, 1, "recepcion", AHORA));
        assertEquals(0, reserva.getMascotas());

        reserva.indicarMascotas(1, 1, "recepcion", AHORA);
        assertEquals(1, reserva.getMascotas());
    }

    @Test
    void rechazarMasDeDosVehiculosYPlacasRepetidas() {
        Reserva reserva = ReservasDePrueba.pendiente();
        reserva.registrarVehiculo(new Vehiculo("ABC123"), 2, "recepcion", AHORA);

        assertThrows(ReglaDominioException.class,
                () -> reserva.registrarVehiculo(new Vehiculo("ABC123"), 2, "recepcion", AHORA));
        assertTrue(reserva.siguienteVehiculoEsAdicional(1));

        reserva.registrarVehiculo(new Vehiculo("XYZ789"), 2, "recepcion", AHORA);
        assertThrows(ReglaDominioException.class,
                () -> reserva.registrarVehiculo(new Vehiculo("JKL456"), 2, "recepcion", AHORA));
        assertEquals(2, reserva.getVehiculos().size());
    }

    @Test
    void unaReservaTerminadaNoRegistraVehiculos() {
        Reserva reserva = ReservasDePrueba.pendiente();
        reserva.cancelar("Cambio", "recepcion", AHORA);

        assertThrows(ReglaDominioException.class,
                () -> reserva.registrarVehiculo(new Vehiculo("ABC123"), 2, "recepcion", AHORA));
    }

    @Test
    void elPropietarioEsElTitular() {
        Reserva reserva = ReservasDePrueba.pendiente();

        assertTrue(reserva.perteneceA(new DocumentoIdentidad("CC-1")));
        assertFalse(reserva.perteneceA(new DocumentoIdentidad("CC-2")));
    }

    @Test
    void reconstruirConservaElEstadoSinRevalidarLaCreacion() {
        Reserva original = ReservasDePrueba.confirmada();

        Reserva reconstruida = Reserva.reconstruir(original.getCodigo(), original.getApartamento(),
                original.getEstancia(), original.getTitular(), original.getOcupantes(), original.getCanalOrigen(),
                original.getIdentificadorExterno(), original.getFechaCreacion(), original.getValor(),
                original.getPolitica(), original.getEstado(), original.getHistorial(),
                original.getHoraEstimadaLlegada(), null, 0, original.getVehiculos());

        assertEquals(original, reconstruida);
        assertEquals(EstadoReserva.CONFIRMADA, reconstruida.getEstado());
        assertEquals(original.getHistorial(), reconstruida.getHistorial());
    }
}
