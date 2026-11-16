package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del código de identificación de una reserva.
 *
 * <p>Verifican formato, normalización, igualdad por valor
 * y correspondencia con el año de creación.</p>
 *
 * <p>La creación incluye fecha y hora. Estas pruebas
 * no verifican la unicidad global de los códigos.</p>
 */
class CodigoReservaTest {

    @Test
    void debeAceptarElFormatoYConservarLosCerosDelConsecutivo() {
        CodigoReserva codigo = new CodigoReserva("RES-2026-00001");

        assertEquals("RES-2026-00001", codigo.valor());
        assertEquals(2026, codigo.anio());
    }

    @Test
    void debeNormalizarMinusculasYEspaciosExteriores() {
        CodigoReserva codigo = new CodigoReserva("  res-2026-00001  ");

        assertEquals("RES-2026-00001", codigo.valor());
    }

    @Test
    void codigosConElMismoValorNormalizadoDebenSerIguales() {
        CodigoReserva primero = new CodigoReserva("res-2026-00001");
        CodigoReserva segundo = new CodigoReserva("RES-2026-00001");

        assertEquals(primero, segundo);
        assertEquals(primero.hashCode(), segundo.hashCode());
    }

    @Test
    void codigosConConsecutivosDistintosNoDebenSerIguales() {
        CodigoReserva primero = new CodigoReserva("RES-2026-00001");
        CodigoReserva segundo = new CodigoReserva("RES-2026-00002");

        assertNotEquals(primero, segundo);
    }

    @Test
    void codigosConAniosDistintosNoDebenSerIguales() {
        CodigoReserva primero = new CodigoReserva("RES-2026-00001");
        CodigoReserva segundo = new CodigoReserva("RES-2027-00001");

        assertNotEquals(primero, segundo);
    }

    @Test
    void debeCorresponderAlAnioDeCreacionHastaSuUltimoSegundo() {
        CodigoReserva codigo = new CodigoReserva("RES-2026-00001");
        FechaCreacion fecha = new FechaCreacion(
                LocalDateTime.of(2026, 12, 31, 23, 59, 59)
        );

        assertTrue(codigo.correspondeA(fecha));
    }

    @Test
    void noDebeCorresponderAUnaCreacionAlInicioDelAnioSiguiente() {
        CodigoReserva codigo = new CodigoReserva("RES-2026-00001");
        FechaCreacion fecha = new FechaCreacion(
                LocalDateTime.of(2027, 1, 1, 0, 0)
        );

        assertFalse(codigo.correspondeA(fecha));
    }

    @Test
    void debeRechazarUnaFechaDeCreacionNulaSinAlterarElCodigo() {
        CodigoReserva codigo = new CodigoReserva("RES-2026-00001");

        assertThrows(
                ReglaDominioException.class,
                () -> codigo.correspondeA(null)
        );

        assertEquals("RES-2026-00001", codigo.valor());
    }

    @Test
    void debeRechazarUnCodigoNulo() {
        assertThrows(
                ReglaDominioException.class,
                () -> new CodigoReserva(null)
        );
    }

    @Test
    void debeRechazarUnCodigoVacio() {
        assertThrows(
                ReglaDominioException.class,
                () -> new CodigoReserva("")
        );
    }

    @Test
    void debeRechazarUnCodigoCompuestoSoloPorEspacios() {
        assertThrows(
                ReglaDominioException.class,
                () -> new CodigoReserva("   ")
        );
    }

    @Test
    void debeRechazarUnPrefijoDistintoDeRes() {
        assertThrows(
                ReglaDominioException.class,
                () -> new CodigoReserva("SGA-2026-00001")
        );
    }

    @Test
    void debeRechazarUnAnioQueNoTengaCuatroDigitos() {
        assertThrows(
                ReglaDominioException.class,
                () -> new CodigoReserva("RES-026-00001")
        );
    }

    @Test
    void debeRechazarUnConsecutivoDeMenosDeCincoDigitos() {
        assertThrows(
                ReglaDominioException.class,
                () -> new CodigoReserva("RES-2026-0001")
        );
    }

    @Test
    void debeRechazarUnConsecutivoDeMasDeCincoDigitos() {
        assertThrows(
                ReglaDominioException.class,
                () -> new CodigoReserva("RES-2026-000001")
        );
    }

    @Test
    void debeRechazarLetrasEnElConsecutivo() {
        assertThrows(
                ReglaDominioException.class,
                () -> new CodigoReserva("RES-2026-00A01")
        );
    }

    @Test
    void debeRechazarSeparadoresIncorrectos() {
        assertThrows(
                ReglaDominioException.class,
                () -> new CodigoReserva("RES/2026/00001")
        );
    }

    @Test
    void debeRechazarEspaciosDentroDelCodigo() {
        assertThrows(
                ReglaDominioException.class,
                () -> new CodigoReserva("RES-2026-00 01")
        );
    }
}