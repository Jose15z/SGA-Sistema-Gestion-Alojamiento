package co.edu.uniquindio.sga.infrastructure.config;

import co.edu.uniquindio.sga.infrastructure.security.SolicitanteArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/** Registra el resolvedor que entrega el Solicitante autenticado a los controllers. */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final SolicitanteArgumentResolver solicitanteResolver;

    public WebConfig(SolicitanteArgumentResolver solicitanteResolver) {
        this.solicitanteResolver = solicitanteResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(solicitanteResolver);
    }
}
