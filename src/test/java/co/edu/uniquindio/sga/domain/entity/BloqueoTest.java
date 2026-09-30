package co.edu.uniquindio.sga.domain.entity;
import co.edu.uniquindio.sga.ReservasDePrueba;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.*;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
class BloqueoTest {
    @Test void intervaloExcluyeElDiaDeSalidaYLevantamientoConservaMotivo() {
        Estancia periodo=ReservasDePrueba.pendiente().getEstancia();
        Bloqueo b=new Bloqueo(UUID.randomUUID(),new IdentificacionApartamento("APT-301"),periodo,"Mantenimiento");
        assertTrue(b.afecta(periodo));
        assertFalse(b.afecta(new Estancia(periodo.fechaSalida(),periodo.fechaSalida().plusDays(1))));
        assertThrows(ReglaDominioException.class,()->b.levantar(" "));
        assertTrue(b.estaVigente());
        b.levantar("Trabajo terminado");
        assertFalse(b.afecta(periodo));assertEquals("Trabajo terminado",b.getMotivoLevantamiento());
        assertThrows(ReglaDominioException.class,()->b.levantar("Otra vez"));
    }
    @Test void identidadNoDependeDelMotivo() {
        UUID id=UUID.randomUUID();var r=ReservasDePrueba.pendiente();
        Bloqueo a=new Bloqueo(id,r.getApartamento(),r.getEstancia(),"Motivo A");
        Bloqueo b=new Bloqueo(id,r.getApartamento(),r.getEstancia(),"Motivo B");
        assertEquals(a,b);assertEquals(a.hashCode(),b.hashCode());
    }
}
