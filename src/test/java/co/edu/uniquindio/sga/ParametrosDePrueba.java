package co.edu.uniquindio.sga;

import co.edu.uniquindio.sga.domain.entity.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.valueobject.CapacidadParqueadero;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.PlazoConfirmacion;
import co.edu.uniquindio.sga.domain.valueobject.TiempoPreparacion;
import co.edu.uniquindio.sga.domain.valueobject.TipoTemporada;
import co.edu.uniquindio.sga.domain.valueobject.TramoCancelacion;
import co.edu.uniquindio.sga.domain.valueobject.UmbralEdadFacturable;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;
import co.edu.uniquindio.sga.infrastructure.config.ParametrosAlojamiento;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * Valores de la Ficha de Siete Raíces para las pruebas sin Spring. En la
 * aplicación estos valores vienen de {@code application.properties}.
 */
public final class ParametrosDePrueba {

    private ParametrosDePrueba() {
    }

    public static ParametrosAlojamiento sieteRaices() {
        return new ParametrosAlojamiento(new UmbralEdadFacturable(10),
                new TiempoPreparacion(3, LocalTime.of(11, 0), LocalTime.of(15, 0)),
                new PlazoConfirmacion(24), 7, new BigDecimal("10"), new BigDecimal("30"),
                LocalTime.of(20, 0), Map.of(TipoTemporada.ESPECIAL, 2),
                1, 2, 1, new CapacidadParqueadero(9), Dinero.de(20_000));
    }

    public static PoliticaCancelacion politicaInicialSieteRaices() {
        return new PoliticaCancelacion(new VersionPolitica(1), List.of(
                new TramoCancelacion(Duration.ofDays(7), BigDecimal.ZERO),
                new TramoCancelacion(Duration.ofHours(48), new BigDecimal("30")),
                new TramoCancelacion(Duration.ZERO, new BigDecimal("50"))),
                new BigDecimal("50"), LocalTime.of(15, 0));
    }
}
