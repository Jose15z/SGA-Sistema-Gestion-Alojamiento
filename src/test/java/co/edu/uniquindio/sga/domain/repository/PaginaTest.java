package co.edu.uniquindio.sga.domain.repository;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaginaTest {
    @Test
    void unaPaginaExtremaQuedaVaciaSinDesbordarElIndice() {
        var pagina = Pagina.de(List.of("a", "b"), new PeticionPagina(Integer.MAX_VALUE, 10));
        assertTrue(pagina.contenido().isEmpty());
        assertEquals(2, pagina.totalElementos());
        assertEquals(Integer.MAX_VALUE, pagina.numero());
    }

    @Test
    void unTamanioExtremoConservaLosElementosSinDesbordarse() {
        var pagina = Pagina.de(List.of("a", "b"), new PeticionPagina(0, Integer.MAX_VALUE));
        assertEquals(List.of("a", "b"), pagina.contenido());
        assertEquals(1, pagina.totalPaginas());
    }

    @Test
    void paginaLosElementosEnOrdenSinRepetirLosDeLaPaginaAnterior() {
        var elementos = IntStream.range(0, 23).boxed().toList();
        var pagina = Pagina.de(elementos, new PeticionPagina(1, 10));
        assertEquals(IntStream.range(10, 20).boxed().toList(), pagina.contenido());
        assertEquals(23, pagina.totalElementos());
        assertEquals(3, pagina.totalPaginas());
    }
}
