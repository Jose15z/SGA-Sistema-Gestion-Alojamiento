package co.edu.uniquindio.sga.infrastructure;
import co.edu.uniquindio.sga.infrastructure.repository.memory.*;
import co.edu.uniquindio.sga.infrastructure.configuration.ParametrosAlojamiento;
import co.edu.uniquindio.sga.domain.valueobject.FechaCreacion;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.*;
class AdaptadoresMemoriaTest {
    @Test void secuenciaEsUnicaInclusoConLlamadasConcurrentes() {
        var generador=new GeneradorCodigoReservaEnMemoria();
        var fecha=new FechaCreacion(LocalDateTime.of(2026,12,1,10,0));
        var codigos=IntStream.range(0,1000).parallel().mapToObj(i -> generador.generar(fecha)).toList();
        assertEquals(1000,codigos.stream().distinct().count());
    }
    @Test void secuenciaSeReiniciaPorAnioSinRepetirCodigoCompleto() {
        var generador=new GeneradorCodigoReservaEnMemoria();
        assertEquals("RES-2026-00001",generador.generar(new FechaCreacion(LocalDateTime.of(2026,12,31,23,0))).valor());
        assertEquals("RES-2027-00001",generador.generar(new FechaCreacion(LocalDateTime.of(2027,1,1,0,0))).valor());
    }
    @Test void configuracionNoShowYTemporadasCorrespondenALaFicha() {
        var config=ParametrosAlojamiento.sieteRaices();
        assertEquals(LocalTime.of(20,0),config.horaLimiteNoShow());
        assertEquals(2,config.nochesMinimasEnTemporada(" TEMPORADA ESPECIAL "));
        assertEquals(1,config.nochesMinimasEnTemporada("Base"));
        assertThrows(UnsupportedOperationException.class,()->config.minimosPorTemporada().clear());
    }
}
