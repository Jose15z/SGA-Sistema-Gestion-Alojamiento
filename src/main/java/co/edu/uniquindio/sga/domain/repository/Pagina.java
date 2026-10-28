package co.edu.uniquindio.sga.domain.repository;

import java.util.List;
import java.util.function.Function;

/**
 * Página de resultados con el lenguaje del dominio: no expone
 * {@code Page} ni {@code Pageable} de Spring Data (Guía 11).
 *
 * @param contenido      elementos de la página
 * @param numero         número de página, desde 0
 * @param tamanio        tamaño solicitado
 * @param totalElementos total de elementos que cumplen el filtro
 */
public record Pagina<T>(List<T> contenido, int numero, int tamanio, long totalElementos) {

    public Pagina {
        contenido = List.copyOf(contenido);
    }

    public int totalPaginas() {
        return tamanio == 0 ? 0 : (int) Math.ceil((double) totalElementos / tamanio);
    }

    public <R> Pagina<R> map(Function<T, R> conversion) {
        return new Pagina<>(contenido.stream().map(conversion).toList(), numero, tamanio, totalElementos);
    }

    /** Pagina en memoria una lista ya filtrada por el dominio. */
    public static <T> Pagina<T> de(List<T> todos, PeticionPagina peticion) {
        int desde = Math.min(peticion.numero() * peticion.tamanio(), todos.size());
        int hasta = Math.min(desde + peticion.tamanio(), todos.size());
        return new Pagina<>(todos.subList(desde, hasta), peticion.numero(), peticion.tamanio(), todos.size());
    }
}
