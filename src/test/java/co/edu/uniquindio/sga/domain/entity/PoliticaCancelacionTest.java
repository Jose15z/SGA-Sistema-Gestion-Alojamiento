package co.edu.uniquindio.sga.domain.entity;
import co.edu.uniquindio.sga.ReservasDePrueba;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.*;
import co.edu.uniquindio.sga.infrastructure.configuration.ParametrosAlojamiento;
import co.edu.uniquindio.sga.infrastructure.repository.memory.PoliticaCancelacionRepositoryEnMemoria;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class PoliticaCancelacionTest {
    @ParameterizedTest @CsvSource({"604800,0","604799,60000","172800,60000","172799,100000","0,100000","-1,100000"})
    void limitesEnSegundos(long anticipacion,long esperado) {
        var politica=ParametrosAlojamiento.politicaInicialSieteRaices();
        Dinero valor=politica.retencionPorCancelacion(Dinero.de(200_000),ReservasDePrueba.pendiente().getEstancia(),
                ReservasDePrueba.LLEGADA.minusSeconds(anticipacion));
        assertEquals(Dinero.de(esperado),valor);
    }
    @Test void redondeaUnaSolaVezAlPeso() {
        assertEquals(Dinero.de(51),ParametrosAlojamiento.politicaInicialSieteRaices().retencionPorNoShow(Dinero.de(101)));
    }
    @Test void rechazaHuecosEnTramos() {
        assertThrows(ReglaDominioException.class,()->new PoliticaCancelacion(new VersionPolitica(1),
                List.of(new TramoCancelacion(Duration.ofHours(48),BigDecimal.TEN)),BigDecimal.TEN,LocalTime.NOON));
    }
    @Test void rechazaUmbralesDuplicados() {
        assertThrows(ReglaDominioException.class,()->new PoliticaCancelacion(new VersionPolitica(1),
                List.of(new TramoCancelacion(Duration.ZERO,BigDecimal.TEN),new TramoCancelacion(Duration.ZERO,BigDecimal.ZERO)),
                BigDecimal.TEN,LocalTime.NOON));
    }
    @Test void noPermiteReescribirVersionHistorica() {
        var politica=ParametrosAlojamiento.politicaInicialSieteRaices();
        var repo=new PoliticaCancelacionRepositoryEnMemoria(politica);
        assertThrows(IllegalArgumentException.class,()->repo.publicar(politica));
        assertSame(politica,repo.obtenerPorVersion(new VersionPolitica(1)).orElseThrow());
    }
}
