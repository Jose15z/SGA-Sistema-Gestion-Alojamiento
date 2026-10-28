package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import co.edu.uniquindio.sga.domain.repository.GeneradorCodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.FechaCreacion;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.SecuenciaReservaEntity;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.SecuenciaReservaJpaDataRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * Genera códigos RES-YYYY-NNNNN con un consecutivo por año guardado en H2:
 * a diferencia de la versión en memoria, no se reinicia al reiniciar la aplicación.
 */
@Component
public class GeneradorCodigoReservaJpa implements GeneradorCodigoReserva {

    private final SecuenciaReservaJpaDataRepository secuencias;

    public GeneradorCodigoReservaJpa(SecuenciaReservaJpaDataRepository secuencias) {
        this.secuencias = secuencias;
    }

    @Override
    @Transactional
    public CodigoReserva generar(FechaCreacion fechaCreacion) {
        int anio = fechaCreacion.anio();
        SecuenciaReservaEntity secuencia = secuencias.bloquearPorAnio(anio).orElseGet(() -> {
            SecuenciaReservaEntity nueva = new SecuenciaReservaEntity();
            nueva.setAnio(anio);
            nueva.setUltimo(0);
            return nueva;
        });
        int siguiente = secuencia.getUltimo() + 1;
        if (siguiente > 99_999) {
            throw new IllegalStateException("La secuencia anual de reservas se agotó");
        }
        secuencia.setUltimo(siguiente);
        secuencias.save(secuencia);
        return new CodigoReserva(String.format(Locale.ROOT, "RES-%04d-%05d", anio, siguiente));
    }
}
