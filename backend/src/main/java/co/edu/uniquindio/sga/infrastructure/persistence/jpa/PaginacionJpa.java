package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import co.edu.uniquindio.sga.domain.repository.Pagina;
import co.edu.uniquindio.sga.domain.repository.PeticionPagina;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.function.Function;

/** Traduce la paginación del dominio a la de Spring Data y de vuelta (Guía 11). */
final class PaginacionJpa {

    private PaginacionJpa() {
    }

    static Pageable aPageable(PeticionPagina peticion, Sort orden) {
        return PageRequest.of(peticion.numero(), peticion.tamanio(), orden);
    }

    static <E, D> Pagina<D> aPagina(Page<E> pagina, Function<E, D> conversion) {
        return new Pagina<>(pagina.getContent().stream().map(conversion).toList(),
                pagina.getNumber(), pagina.getSize(), pagina.getTotalElements());
    }
}
