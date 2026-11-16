package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** L-08 y 7.4: temporadas sin solapamiento y BASE para toda fecha no asignada. */
class CalendarioTemporadasTest {

    private static final Temporada ALTA = new Temporada("Fin de año", TipoTemporada.ALTA,
            LocalDate.of(2026, 12, 1), LocalDate.of(2027, 1, 15));
    private static final Temporada ESPECIAL = new Temporada("Semana Santa", TipoTemporada.ESPECIAL,
            LocalDate.of(2027, 3, 21), LocalDate.of(2027, 3, 28));

    @Test
    void identificarTemporadaSegunFecha() {
        CalendarioTemporadas calendario = new CalendarioTemporadas(List.of(ALTA, ESPECIAL));

        assertEquals(TipoTemporada.ALTA, calendario.tipoDe(LocalDate.of(2026, 12, 1)));
        assertEquals(TipoTemporada.ALTA, calendario.tipoDe(LocalDate.of(2027, 1, 15)));
        assertEquals(TipoTemporada.ESPECIAL, calendario.tipoDe(LocalDate.of(2027, 3, 25)));
        assertEquals(TipoTemporada.BASE, calendario.tipoDe(LocalDate.of(2027, 1, 16)));
    }

    @Test
    void laBaseCubreTodoCuandoNoHayTemporadas() {
        assertEquals(TipoTemporada.BASE, CalendarioTemporadas.soloBase().tipoDe(LocalDate.of(2027, 8, 1)));
    }

    @Test
    void rechazaTemporadasSolapadas() {
        Temporada solapada = new Temporada("Navidad", TipoTemporada.ESPECIAL,
                LocalDate.of(2026, 12, 24), LocalDate.of(2026, 12, 25));

        assertThrows(ReglaDominioException.class, () -> new CalendarioTemporadas(List.of(ALTA, solapada)));
        assertThrows(ReglaDominioException.class, () -> new CalendarioTemporadas(List.of(ALTA)).con(solapada));
    }

    @Test
    void temporadasContiguasNoSeSolapan() {
        Temporada siguiente = new Temporada("Enero", TipoTemporada.ESPECIAL,
                LocalDate.of(2027, 1, 16), LocalDate.of(2027, 1, 20));

        assertFalse(ALTA.seSolapaCon(siguiente));
        assertDoesNotThrow(() -> new CalendarioTemporadas(List.of(ALTA)).con(siguiente));
    }

    @Test
    void laTemporadaBaseNoSeDefinePorFechas() {
        assertThrows(ReglaDominioException.class, () -> new Temporada("Base", TipoTemporada.BASE,
                LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 2)));
    }

    @Test
    void rechazaFinAnteriorAlInicioYNombreVacio() {
        assertThrows(ReglaDominioException.class, () -> new Temporada("X", TipoTemporada.ALTA,
                LocalDate.of(2027, 1, 2), LocalDate.of(2027, 1, 1)));
        assertThrows(ReglaDominioException.class, () -> new Temporada(" ", TipoTemporada.ALTA,
                LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 2)));
    }

    @Test
    void incluyeAmbosExtremos() {
        assertTrue(ESPECIAL.incluye(LocalDate.of(2027, 3, 21)));
        assertTrue(ESPECIAL.incluye(LocalDate.of(2027, 3, 28)));
        assertFalse(ESPECIAL.incluye(LocalDate.of(2027, 3, 29)));
    }
}
