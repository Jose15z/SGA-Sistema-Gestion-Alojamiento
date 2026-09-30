package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.ConfiguracionAlojamiento;
import co.edu.uniquindio.sga.domain.valueobject.ValorCongelado;
import java.util.Objects;

/** RP-01: verifica cada temporada del desglose ya cotizado, evitando una segunda lectura del tarifario. */
public final class EstanciaMinimaTemporadaService {
    private final ConfiguracionAlojamiento configuracion;
    public EstanciaMinimaTemporadaService(ConfiguracionAlojamiento configuracion) {
        this.configuracion = Objects.requireNonNull(configuracion);
    }
    public void verificar(ValorCongelado valor) {
        if (valor == null) throw new ReglaDominioException("La estancia mínima requiere una cotización");
        for (String temporada : valor.detalle().stream().map(c -> c.temporada()).distinct().toList()) {
            int minimo = configuracion.nochesMinimasEnTemporada(temporada);
            if (minimo < 1) throw new ReglaDominioException("El mínimo de noches debe ser positivo");
            if (valor.noches() < minimo) {
                throw new ReglaDominioException("La temporada " + temporada + " requiere al menos " + minimo + " noches");
            }
        }
    }
}
