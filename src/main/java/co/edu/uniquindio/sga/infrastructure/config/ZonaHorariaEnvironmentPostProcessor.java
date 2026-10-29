package co.edu.uniquindio.sga.infrastructure.config;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import java.time.ZoneId;
import java.util.TimeZone;

/**
 * Fija la zona horaria de la JVM con {@code sga.alojamiento.zona-horaria} antes de
 * crear el contexto, para que el reloj de la aplicación, la base de datos
 * (CURRENT_DATE y LOCALTIMESTAMP de data.sql) y JDBC usen la misma zona sin
 * importar dónde se ejecute (un servidor en UTC o un equipo en Colombia).
 *
 * <p>Se registra en {@code META-INF/spring.factories} y corre después de cargar
 * application.properties, por eso la zona sigue siendo configuración.</p>
 */
public class ZonaHorariaEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    static final String PROPIEDAD = "sga.alojamiento.zona-horaria";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String zona = environment.getProperty(PROPIEDAD);
        if (zona != null && !zona.isBlank()) {
            TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of(zona.trim())));
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
