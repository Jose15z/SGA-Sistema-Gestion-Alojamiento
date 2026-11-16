package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.valueobject.CalendarioTemporadas;
import co.edu.uniquindio.sga.domain.valueobject.Temporada;

/** Puerto para consultar y ampliar el calendario de temporadas (7.4). */
public interface TemporadaRepository {

    /** Calendario completo; la temporada BASE cubre lo no asignado. */
    CalendarioTemporadas obtenerCalendario();

    Pagina<Temporada> listar(PeticionPagina pagina);

    void guardar(Temporada temporada);
}
