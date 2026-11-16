package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.UmbralEdadFacturable;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias de identidad y comportamiento de Ocupante.
 *
 * <p>Comprueban el límite de edad facturable de RN-06
 * y la conservación del estado ante cambios rechazados.</p>
 *
 * <p>Utilizan fechas fijas, sin Spring ni reloj del sistema.</p>
 */
class OcupanteTest {

    private static final LocalDate FECHA_ACTUAL =
            LocalDate.of(2026, 1, 1);

    private static final LocalDate NACIMIENTO =
            LocalDate.of(2016, 5, 20);

    private static final DocumentoIdentidad DOCUMENTO =
            new DocumentoIdentidad("TI-1001");

    @Test
    void ocupantesConElMismoDocumentoDebenSerIgualesAunqueCambienSusDatos() {
        Ocupante primero = ocupante();

        Ocupante segundo = new Ocupante(
                new DocumentoIdentidad("TI-1001"),
                "Otro nombre",
                LocalDate.of(2015, 3, 10),
                FECHA_ACTUAL
        );

        assertEquals(primero, segundo);
        assertEquals(primero.hashCode(), segundo.hashCode());
    }

    @Test
    void ocupantesConDocumentosDistintosNoDebenSerIguales() {
        Ocupante primero = ocupante();

        Ocupante segundo = new Ocupante(
                new DocumentoIdentidad("TI-1002"),
                "Ana",
                NACIMIENTO,
                FECHA_ACTUAL
        );

        assertNotEquals(primero, segundo);
    }

    @Test
    void debeCalcularLaEdadAntesYEnElDiaDelCumpleanios() {
        Ocupante ocupante = ocupante();

        assertEquals(9, ocupante.edadA(LocalDate.of(2026, 5, 19)));
        assertEquals(10, ocupante.edadA(LocalDate.of(2026, 5, 20)));
    }

    @Test
    void debeSerFacturableSiCumpleDiezElDiaDeEntrada() {
        Ocupante ocupante = ocupante();
        Estancia estancia = new Estancia(
                LocalDate.of(2026, 5, 20),
                LocalDate.of(2026, 5, 22)
        );

        boolean facturable = ocupante.esFacturableEn(
                estancia, new UmbralEdadFacturable(10)
        );

        assertTrue(facturable);
    }

    @Test
    void noDebeSerFacturableSiCumpleDiezDuranteLaEstancia() {
        Ocupante ocupante = ocupante();
        Estancia estancia = new Estancia(
                LocalDate.of(2026, 5, 19),
                LocalDate.of(2026, 5, 22)
        );

        boolean facturable = ocupante.esFacturableEn(
                estancia, new UmbralEdadFacturable(10)
        );

        assertFalse(facturable);
    }

    @Test
    void debeUtilizarElUmbralRecibidoComoParametro() {
        Ocupante ocupante = ocupante();
        Estancia estancia = new Estancia(
                LocalDate.of(2026, 5, 20),
                LocalDate.of(2026, 5, 22)
        );

        assertTrue(ocupante.esFacturableEn(
                estancia, new UmbralEdadFacturable(10)
        ));
        assertFalse(ocupante.esFacturableEn(
                estancia, new UmbralEdadFacturable(11)
        ));
    }

    @Test
    void debeActualizarElNombreSinCambiarLaIdentidadNiElNacimiento() {
        Ocupante ocupante = ocupante();
        int hashOriginal = ocupante.hashCode();

        ocupante.actualizarNombre("  Ana María  ");

        assertEquals("Ana María", ocupante.getNombre());
        assertEquals(DOCUMENTO, ocupante.getDocumento());
        assertEquals(NACIMIENTO, ocupante.getFechaNacimiento());
        assertEquals(hashOriginal, ocupante.hashCode());
    }

    @Test
    void debeRechazarUnNombreNuloSinAlterarElOcupante() {
        Ocupante ocupante = ocupante();

        assertThrows(
                ReglaDominioException.class,
                () -> ocupante.actualizarNombre(null)
        );

        verificarDatosOriginales(ocupante);
    }

    @Test
    void debeRechazarUnNombreVacioSinAlterarElOcupante() {
        Ocupante ocupante = ocupante();

        assertThrows(
                ReglaDominioException.class,
                () -> ocupante.actualizarNombre("")
        );

        verificarDatosOriginales(ocupante);
    }

    @Test
    void debeRechazarUnNombreEnBlancoSinAlterarElOcupante() {
        Ocupante ocupante = ocupante();

        assertThrows(
                ReglaDominioException.class,
                () -> ocupante.actualizarNombre("   ")
        );

        verificarDatosOriginales(ocupante);
    }

    @Test
    void debeRechazarUnaFechaDeNacimientoFutura() {
        assertThrows(
                ReglaDominioException.class,
                () -> new Ocupante(
                        DOCUMENTO,
                        "Ana",
                        FECHA_ACTUAL.plusDays(1),
                        FECHA_ACTUAL
                )
        );
    }

    @Test
    void debePermitirNacimientoEnLaFechaActualConEdadCero() {
        Ocupante recienNacido = new Ocupante(
                DOCUMENTO,
                "Ana",
                FECHA_ACTUAL,
                FECHA_ACTUAL
        );

        assertEquals(0, recienNacido.edadA(FECHA_ACTUAL));
    }

    @Test
    void debeRechazarUnDocumentoNulo() {
        assertThrows(
                ReglaDominioException.class,
                () -> new Ocupante(
                        null, "Ana", NACIMIENTO, FECHA_ACTUAL
                )
        );
    }

    @Test
    void debeRechazarUnNombreEnBlancoAlConstruirse() {
        assertThrows(
                ReglaDominioException.class,
                () -> new Ocupante(
                        DOCUMENTO, "   ", NACIMIENTO, FECHA_ACTUAL
                )
        );
    }

    @Test
    void debeRechazarUnNacimientoNulo() {
        assertThrows(
                ReglaDominioException.class,
                () -> new Ocupante(
                        DOCUMENTO, "Ana", null, FECHA_ACTUAL
                )
        );
    }

    @Test
    void debeRechazarUnaFechaActualNula() {
        assertThrows(
                ReglaDominioException.class,
                () -> new Ocupante(
                        DOCUMENTO, "Ana", NACIMIENTO, null
                )
        );
    }

    @Test
    void noDebeCalcularLaEdadAntesDelNacimiento() {
        Ocupante ocupante = ocupante();

        assertThrows(
                ReglaDominioException.class,
                () -> ocupante.edadA(NACIMIENTO.minusDays(1))
        );

        verificarDatosOriginales(ocupante);
    }

    @Test
    void debeRechazarUnaFechaNulaAlCalcularLaEdad() {
        Ocupante ocupante = ocupante();

        assertThrows(
                ReglaDominioException.class,
                () -> ocupante.edadA(null)
        );

        verificarDatosOriginales(ocupante);
    }

    @Test
    void debeRechazarUnaEstanciaNulaAlEvaluarFacturacion() {
        Ocupante ocupante = ocupante();

        assertThrows(
                ReglaDominioException.class,
                () -> ocupante.esFacturableEn(
                        null, new UmbralEdadFacturable(10)
                )
        );

        verificarDatosOriginales(ocupante);
    }

    @Test
    void debeRechazarUnUmbralNuloAlEvaluarFacturacion() {
        Ocupante ocupante = ocupante();
        Estancia estancia = new Estancia(
                LocalDate.of(2026, 5, 20),
                LocalDate.of(2026, 5, 22)
        );

        assertThrows(
                ReglaDominioException.class,
                () -> ocupante.esFacturableEn(estancia, null)
        );

        verificarDatosOriginales(ocupante);
    }

    private Ocupante ocupante() {
        return new Ocupante(
                DOCUMENTO,
                "Ana",
                NACIMIENTO,
                FECHA_ACTUAL
        );
    }

    private void verificarDatosOriginales(Ocupante ocupante) {
        assertEquals(DOCUMENTO, ocupante.getDocumento());
        assertEquals("Ana", ocupante.getNombre());
        assertEquals(NACIMIENTO, ocupante.getFechaNacimiento());
    }
}