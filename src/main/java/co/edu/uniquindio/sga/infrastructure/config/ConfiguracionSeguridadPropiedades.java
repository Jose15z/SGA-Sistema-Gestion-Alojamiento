package co.edu.uniquindio.sga.infrastructure.config;

import co.edu.uniquindio.sga.application.port.ConfiguracionSeguridad;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Parámetros de seguridad leídos de application.properties. */
@Component
public class ConfiguracionSeguridadPropiedades implements ConfiguracionSeguridad {

    private final int vigenciaCodigoRecuperacionMinutos;

    public ConfiguracionSeguridadPropiedades(
            @Value("${sga.seguridad.recuperacion.vigencia-minutos}") int vigenciaCodigoRecuperacionMinutos) {
        if (vigenciaCodigoRecuperacionMinutos <= 0) {
            throw new IllegalStateException("La vigencia del código de recuperación debe ser positiva");
        }
        this.vigenciaCodigoRecuperacionMinutos = vigenciaCodigoRecuperacionMinutos;
    }

    @Override
    public int vigenciaCodigoRecuperacionMinutos() {
        return vigenciaCodigoRecuperacionMinutos;
    }
}
