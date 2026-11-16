package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.entity.Alojamiento;

import java.util.Optional;

/** Puerto del agregado Alojamiento. El sistema administra un único alojamiento (F-13). */
public interface AlojamientoRepository {

    /** El alojamiento registrado; vacío si aún no se ha configurado. */
    Optional<Alojamiento> obtener();

    void guardar(Alojamiento alojamiento);
}
