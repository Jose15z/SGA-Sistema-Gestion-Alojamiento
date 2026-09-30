package co.edu.uniquindio.sga.infrastructure.repository.memory;
import co.edu.uniquindio.sga.domain.entity.*;
import co.edu.uniquindio.sga.domain.repository.*;
import co.edu.uniquindio.sga.domain.valueobject.*;
import java.util.*;

/** Secuencia anual para ejercicios. Una sola instancia por almacén; no persiste entre ejecuciones. */
public final class GeneradorCodigoReservaEnMemoria implements GeneradorCodigoReserva {
    private final Map<Integer, Integer> secuencias = new HashMap<>();
    @Override public synchronized CodigoReserva generar(FechaCreacion creacion) {
        int anio = Objects.requireNonNull(creacion).anio();
        int siguiente = secuencias.getOrDefault(anio, 0) + 1;
        if (siguiente > 99999) throw new IllegalStateException("La secuencia anual de reservas se agotó");
        CodigoReserva codigo = new CodigoReserva(String.format(Locale.ROOT, "RES-%04d-%05d", anio, siguiente));
        secuencias.put(anio, siguiente);
        return codigo;
    }
}
