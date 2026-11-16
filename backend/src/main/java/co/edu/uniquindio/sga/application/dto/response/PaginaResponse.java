package co.edu.uniquindio.sga.application.dto.response;

import java.util.List;

/** Página de resultados (10 elementos por página como máximo). */
public record PaginaResponse<T>(
        List<T> contenido,
        int pagina,
        int tamanio,
        long totalElementos,
        int totalPaginas
) {
}
