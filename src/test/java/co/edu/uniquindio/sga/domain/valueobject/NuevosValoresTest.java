package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Objetos de valor incorporados en la Entrega 2. */
class NuevosValoresTest {

    @Test
    void vehiculoNormalizaLaPlaca() {
        assertEquals(new Vehiculo("ABC123"), new Vehiculo(" abc-123 "));
        assertThrows(ReglaDominioException.class, () -> new Vehiculo("A1"));
        assertThrows(ReglaDominioException.class, () -> new Vehiculo(null));
    }

    @Test
    void identificadorExternoNoPuedeEstarVacio() {
        assertEquals("BK-1", new IdentificadorExterno(" BK-1 ").valor());
        assertThrows(ReglaDominioException.class, () -> new IdentificadorExterno("   "));
    }

    @Test
    void capacidadParqueaderoAdmiteHastaSusCupos() {
        CapacidadParqueadero capacidad = new CapacidadParqueadero(2);
        assertTrue(capacidad.admite(2));
        assertFalse(capacidad.admite(3));
        assertThrows(ReglaDominioException.class, () -> new CapacidadParqueadero(-1));
    }

    @Test
    void servicioAdicionalConCargoCobraPorUnidad() {
        ServicioAdicional lavanderia = new ServicioAdicional("lavanderia", "Lavandería", true, Dinero.de(25_000),
                UnidadCobro.POR_SOLICITUD);
        assertEquals("LAVANDERIA", lavanderia.codigo());
        assertEquals(Dinero.de(50_000), lavanderia.cargoPor(2));
        assertThrows(ReglaDominioException.class, () -> lavanderia.cargoPor(0));
        ServicioAdicional wifi = new ServicioAdicional("WIFI", "Wifi", false, Dinero.CERO, UnidadCobro.POR_NOCHE);
        assertThrows(ReglaDominioException.class, () -> wifi.cargoPor(1));
        assertThrows(ReglaDominioException.class,
                () -> new ServicioAdicional("SPA", "Spa", true, Dinero.CERO, UnidadCobro.POR_PERSONA));
    }

    @Test
    void ubicacionYAutorizacionDeCierreSeValidanAlCrearse() {
        assertThrows(ReglaDominioException.class,
                () -> new Ubicacion(java.math.BigDecimal.valueOf(91), java.math.BigDecimal.ZERO));
        assertThrows(ReglaDominioException.class,
                () -> new AutorizacionCierre("admin", " ", java.time.LocalDateTime.now()));
        assertEquals("motivo", new AutorizacionCierre(" admin ", " motivo ", java.time.LocalDateTime.now()).motivo());
    }

    @Test
    void versionSiguiente() {
        assertEquals(new VersionPolitica(2), new VersionPolitica(1).siguiente());
    }
}
