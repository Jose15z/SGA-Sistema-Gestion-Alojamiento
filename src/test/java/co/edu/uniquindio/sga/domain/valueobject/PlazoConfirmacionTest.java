package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pruebas del plazo configurable de confirmación.
 *
 * <p>Verifican igualdad, validaciones y cálculo de la fecha
 * y hora límite relacionadas con RN-21.</p>
 *
 * <p>No consultan el reloj ni modifican reservas.</p>
 */
class PlazoConfirmacionTest {

    @Test
    void plazosConLaMismaDuracionDebenSerIguales() {
        PlazoConfirmacion primero = new PlazoConfirmacion(24);
        PlazoConfirmacion segundo = new PlazoConfirmacion(24);

        assertEquals(primero, segundo);
        assertEquals(primero.hashCode(), segundo.hashCode());
    }

    @Test
    void plazosConDistintaDuracionNoDebenSerIguales() {
        PlazoConfirmacion primero = new PlazoConfirmacion(24);
        PlazoConfirmacion segundo = new PlazoConfirmacion(48);

        assertNotEquals(primero, segundo);
    }

    @Test
    void debeCalcularVeinticuatroHorasConservandoMinutosYSegundos() {
        PlazoConfirmacion plazo = new PlazoConfirmacion(24);
        FechaCreacion creacion = new FechaCreacion(
                LocalDateTime.of(2026, 12, 10, 15, 37, 42)
        );

        LocalDateTime limite = plazo.limiteDesde(creacion);

        assertEquals(
                LocalDateTime.of(2026, 12, 11, 15, 37, 42),
                limite
        );
    }

    @Test
    void debeAceptarUnaHoraYCalcularElLimiteDentroDelMismoDia() {
        PlazoConfirmacion plazo = new PlazoConfirmacion(1);
        FechaCreacion creacion = new FechaCreacion(
                LocalDateTime.of(2026, 12, 10, 9, 15)
        );

        LocalDateTime limite = plazo.limiteDesde(creacion);

        assertEquals(
                LocalDateTime.of(2026, 12, 10, 10, 15),
                limite
        );
    }

    @Test
    void debeCalcularElLimiteAlCruzarElCambioDeAnio() {
        PlazoConfirmacion plazo = new PlazoConfirmacion(2);
        FechaCreacion creacion = new FechaCreacion(
                LocalDateTime.of(2026, 12, 31, 23, 30)
        );

        LocalDateTime limite = plazo.limiteDesde(creacion);

        assertEquals(
                LocalDateTime.of(2027, 1, 1, 1, 30),
                limite
        );
    }

    @Test
    void debeUtilizarElPlazoRecibidoAunqueSupereUnDia() {
        PlazoConfirmacion plazo = new PlazoConfirmacion(36);
        FechaCreacion creacion = new FechaCreacion(
                LocalDateTime.of(2026, 12, 10, 18, 0)
        );

        LocalDateTime limite = plazo.limiteDesde(creacion);

        assertEquals(
                LocalDateTime.of(2026, 12, 12, 6, 0),
                limite
        );
    }

    @Test
    void calcularElLimiteNoDebeModificarLaFechaDeCreacion() {
        LocalDateTime momentoOriginal =
                LocalDateTime.of(2026, 12, 10, 15, 30);
        FechaCreacion creacion = new FechaCreacion(momentoOriginal);
        PlazoConfirmacion plazo = new PlazoConfirmacion(24);

        plazo.limiteDesde(creacion);

        assertEquals(momentoOriginal, creacion.momento());
    }

    @Test
    void debeRechazarUnPlazoDeCeroHoras() {
        assertThrows(
                ReglaDominioException.class,
                () -> new PlazoConfirmacion(0)
        );
    }

    @Test
    void debeRechazarUnPlazoNegativo() {
        assertThrows(
                ReglaDominioException.class,
                () -> new PlazoConfirmacion(-1)
        );
    }

    @Test
    void debeRechazarUnaCreacionNulaSinAlterarElPlazo() {
        PlazoConfirmacion plazo = new PlazoConfirmacion(24);

        assertThrows(
                ReglaDominioException.class,
                () -> plazo.limiteDesde(null)
        );

        assertEquals(new PlazoConfirmacion(24), plazo);
    }
}