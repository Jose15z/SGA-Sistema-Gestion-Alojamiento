package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.ConfiguracionAlojamiento;
import co.edu.uniquindio.sga.domain.repository.TarifarioRepository;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.Tarifa;
import co.edu.uniquindio.sga.domain.valueobject.UmbralEdadFacturable;
import co.edu.uniquindio.sga.domain.valueobject.ValorCongelado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias del servicio de cotización.
 *
 * <p>Comprueban RN-05, RN-06, RN-22 y el descuento RP-03.
 * Los puertos se sustituyen mediante mocks y los objetos
 * del dominio se construyen con sus implementaciones reales.</p>
 *
 * <p>No requieren Spring, base de datos ni reloj del sistema.</p>
 */
class CotizadorEstanciaServiceTest {

    private static final LocalDate ENTRADA =
            LocalDate.of(2026, 12, 10);

    private static final LocalDate FECHA_ACTUAL =
            LocalDate.of(2026, 1, 1);

    private static final IdentificacionApartamento APARTAMENTO =
            new IdentificacionApartamento("APT-301");

    private TarifarioRepository tarifario;
    private ConfiguracionAlojamiento configuracion;
    private CotizadorEstanciaService servicio;

    @BeforeEach
    void prepararServicio() {
        tarifario = mock(TarifarioRepository.class);
        configuracion = mock(ConfiguracionAlojamiento.class);

        when(configuracion.umbralEdadFacturable())
                .thenReturn(new UmbralEdadFacturable(10));
        when(configuracion.nochesMinimasParaDescuento())
                .thenReturn(7);
        when(configuracion.porcentajeDescuentoEstanciaProlongada())
                .thenReturn(new BigDecimal("10"));

        servicio = new CotizadorEstanciaService(
                tarifario, configuracion
        );
    }

    @Test
    void debeConsultarCadaNocheYAplicarSuTarifaCorrespondiente() {
        Estancia estancia = estanciaDe(2);

        when(tarifario.tarifaDe(APARTAMENTO, ENTRADA))
                .thenReturn(new Tarifa(
                        APARTAMENTO, "Normal", Dinero.de(100_000)
                ));

        when(tarifario.tarifaDe(APARTAMENTO, ENTRADA.plusDays(1)))
                .thenReturn(new Tarifa(
                        APARTAMENTO, "Especial", Dinero.de(150_000)
                ));

        ValorCongelado resultado = servicio.cotizar(
                APARTAMENTO,
                estancia,
                List.of(adulto("CC-1"), adulto("CC-2"))
        );

        assertEquals(Dinero.de(500_000), resultado.total());
        assertEquals("Normal", resultado.detalle().get(0).temporada());
        assertEquals("Especial", resultado.detalle().get(1).temporada());
        assertTrue(resultado.correspondeA(estancia));

        verify(tarifario).tarifaDe(APARTAMENTO, ENTRADA);
        verify(tarifario).tarifaDe(APARTAMENTO, ENTRADA.plusDays(1));
        verifyNoMoreInteractions(tarifario);
    }

    @Test
    void debeFacturarAQuienCumpleElUmbralElDiaDeEntrada() {
        Estancia estancia = estanciaDe(2);
        configurarTarifas(estancia, 100_000);

        Ocupante cumpleDiez = ocupante(
                "TI-1", ENTRADA.minusYears(10)
        );

        ValorCongelado resultado = servicio.cotizar(
                APARTAMENTO,
                estancia,
                List.of(adulto("CC-1"), cumpleDiez)
        );

        assertEquals(Dinero.de(400_000), resultado.total());
        assertTrue(resultado.detalle().stream()
                .allMatch(cargo -> cargo.ocupantesFacturables() == 2));
    }

    @Test
    void noDebeAgregarCobroPorUnCumpleaniosDuranteLaEstancia() {
        Estancia estancia = estanciaDe(2);
        configurarTarifas(estancia, 100_000);

        Ocupante cumpleAlDiaSiguiente = ocupante(
                "TI-1", ENTRADA.minusYears(10).plusDays(1)
        );

        ValorCongelado resultado = servicio.cotizar(
                APARTAMENTO,
                estancia,
                List.of(adulto("CC-1"), cumpleAlDiaSiguiente)
        );

        assertEquals(Dinero.de(200_000), resultado.total());
        assertTrue(resultado.detalle().stream()
                .allMatch(cargo -> cargo.ocupantesFacturables() == 1));
    }

    @Test
    void noDebeAplicarDescuentoPorSeisNoches() {
        Estancia estancia = estanciaDe(6);
        configurarTarifas(estancia, 100_000);

        ValorCongelado resultado = servicio.cotizar(
                APARTAMENTO, estancia, List.of(adulto("CC-1"))
        );

        assertEquals(Dinero.de(600_000), resultado.total());
        assertEquals(Dinero.CERO, resultado.descuento());
    }

    @Test
    void debeAplicarDiezPorCientoDesdeExactamenteSieteNoches() {
        Estancia estancia = estanciaDe(7);
        configurarTarifas(estancia, 100_000);

        ValorCongelado resultado = servicio.cotizar(
                APARTAMENTO, estancia, List.of(adulto("CC-1"))
        );

        assertEquals(Dinero.de(700_000), resultado.subtotal());
        assertEquals(Dinero.de(70_000), resultado.descuento());
        assertEquals(Dinero.de(630_000), resultado.total());
    }

    @Test
    void debeMantenerElDescuentoPorEncimaDeSieteNoches() {
        Estancia estancia = estanciaDe(8);
        configurarTarifas(estancia, 100_000);

        ValorCongelado resultado = servicio.cotizar(
                APARTAMENTO, estancia, List.of(adulto("CC-1"))
        );

        assertEquals(Dinero.de(80_000), resultado.descuento());
        assertEquals(Dinero.de(720_000), resultado.total());
    }

    @Test
    void debeConsultarLaConfiguracionVigenteSinAlterarUnaCotizacionAnterior() {
        Estancia estancia = estanciaDe(2);
        configurarTarifas(estancia, 100_000);

        List<Ocupante> grupo = List.of(
                adulto("CC-1"),
                ocupante("TI-1", ENTRADA.minusYears(9))
        );

        ValorCongelado anterior = servicio.cotizar(
                APARTAMENTO, estancia, grupo
        );

        // Configuración alternativa para verificar que no hay
        // valores fijos dentro del servicio.
        when(configuracion.umbralEdadFacturable())
                .thenReturn(new UmbralEdadFacturable(9));
        when(configuracion.nochesMinimasParaDescuento())
                .thenReturn(2);
        when(configuracion.porcentajeDescuentoEstanciaProlongada())
                .thenReturn(new BigDecimal("20"));

        configurarTarifas(estancia, 200_000);

        ValorCongelado nueva = servicio.cotizar(
                APARTAMENTO, estancia, grupo
        );

        assertEquals(Dinero.de(200_000), anterior.total());
        assertEquals(Dinero.CERO, anterior.descuento());
        assertTrue(anterior.detalle().stream().allMatch(cargo ->
                cargo.tarifaPorOcupante().equals(Dinero.de(100_000))
                        && cargo.ocupantesFacturables() == 1
        ));

        assertEquals(Dinero.de(800_000), nueva.subtotal());
        assertEquals(Dinero.de(160_000), nueva.descuento());
        assertEquals(Dinero.de(640_000), nueva.total());
    }

    @Test
    void debeRechazarUnaEstanciaConUnaNocheSinTarifa() {
        Estancia estancia = estanciaDe(2);
        List<Ocupante> grupo = List.of(adulto("CC-1"));

        when(tarifario.tarifaDe(APARTAMENTO, ENTRADA))
                .thenReturn(new Tarifa(
                        APARTAMENTO, "Normal", Dinero.de(100_000)
                ));

        when(tarifario.tarifaDe(APARTAMENTO, ENTRADA.plusDays(1)))
                .thenReturn(null);

        assertThrows(
                ReglaDominioException.class,
                () -> servicio.cotizar(APARTAMENTO, estancia, grupo)
        );
    }

    @Test
    void debeRechazarUnaTarifaDeOtroApartamento() {
        Estancia estancia = estanciaDe(1);
        List<Ocupante> grupo = List.of(adulto("CC-1"));

        when(tarifario.tarifaDe(APARTAMENTO, ENTRADA))
                .thenReturn(new Tarifa(
                        new IdentificacionApartamento("APT-302"),
                        "Normal",
                        Dinero.de(100_000)
                ));

        assertThrows(
                ReglaDominioException.class,
                () -> servicio.cotizar(APARTAMENTO, estancia, grupo)
        );
    }

    @Test
    void debeRechazarUnGrupoSinOcupantesFacturables() {
        Estancia estancia = estanciaDe(2);
        List<Ocupante> grupo = List.of(
                ocupante("TI-1", ENTRADA.minusYears(9))
        );

        assertThrows(
                ReglaDominioException.class,
                () -> servicio.cotizar(APARTAMENTO, estancia, grupo)
        );

        verifyNoInteractions(tarifario);
    }

    @Test
    void debeRechazarOcupantesRepetidosPorDocumento() {
        Estancia estancia = estanciaDe(2);
        List<Ocupante> grupo = List.of(
                adulto("CC-1"), adulto("CC-1")
        );

        assertThrows(
                ReglaDominioException.class,
                () -> servicio.cotizar(APARTAMENTO, estancia, grupo)
        );

        verifyNoInteractions(tarifario);
    }

    @Test
    void debeRechazarUnGrupoVacio() {
        Estancia estancia = estanciaDe(2);

        assertThrows(
                ReglaDominioException.class,
                () -> servicio.cotizar(
                        APARTAMENTO, estancia, List.of()
                )
        );

        verifyNoInteractions(tarifario);
    }

    @Test
    void debeRechazarUnMinimoDeNochesConfiguradoEnCero() {
        Estancia estancia = estanciaDe(2);
        List<Ocupante> grupo = List.of(adulto("CC-1"));

        when(configuracion.nochesMinimasParaDescuento())
                .thenReturn(0);

        assertThrows(
                ReglaDominioException.class,
                () -> servicio.cotizar(APARTAMENTO, estancia, grupo)
        );

        verifyNoInteractions(tarifario);
    }

    @Test
    void debeRechazarUnDescuentoConfiguradoMayorQueCien() {
        Estancia estancia = estanciaDe(2);
        List<Ocupante> grupo = List.of(adulto("CC-1"));

        when(configuracion.porcentajeDescuentoEstanciaProlongada())
                .thenReturn(new BigDecimal("100.01"));

        assertThrows(
                ReglaDominioException.class,
                () -> servicio.cotizar(APARTAMENTO, estancia, grupo)
        );

        verifyNoInteractions(tarifario);
    }

    private Estancia estanciaDe(int noches) {
        return new Estancia(ENTRADA, ENTRADA.plusDays(noches));
    }

    private Ocupante adulto(String documento) {
        return ocupante(documento, LocalDate.of(1990, 1, 1));
    }

    private Ocupante ocupante(
            String documento,
            LocalDate fechaNacimiento
    ) {
        return new Ocupante(
                new DocumentoIdentidad(documento),
                "Persona de prueba",
                fechaNacimiento,
                FECHA_ACTUAL
        );
    }

    private void configurarTarifas(Estancia estancia, long importe) {
        for (LocalDate noche : estancia.nochesOcupadas()) {
            when(tarifario.tarifaDe(APARTAMENTO, noche))
                    .thenReturn(new Tarifa(
                            APARTAMENTO,
                            "Normal",
                            Dinero.de(importe)
                    ));
        }
    }
}