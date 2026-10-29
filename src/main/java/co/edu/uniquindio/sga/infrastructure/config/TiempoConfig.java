package co.edu.uniquindio.sga.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;
import java.time.ZoneId;

/** Un solo reloj para toda la aplicación, en la zona horaria del alojamiento (Guía 09). */
@Configuration
@EnableScheduling
public class TiempoConfig {

    @Bean
    public Clock reloj(@Value("${sga.alojamiento.zona-horaria}") String zona) {
        return Clock.system(ZoneId.of(zona));
    }
}
