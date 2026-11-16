package co.edu.uniquindio.sga.domain.repository;

/**
 * Página solicitada por un listado.
 *
 * @param numero  número de página, desde 0
 * @param tamanio cantidad de elementos por página
 */
public record PeticionPagina(int numero, int tamanio) {

    public PeticionPagina {
        if (numero < 0) {
            throw new IllegalArgumentException("La página no puede ser negativa");
        }
        if (tamanio < 1) {
            throw new IllegalArgumentException("El tamaño de página debe ser positivo");
        }
    }
}
