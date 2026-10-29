package co.edu.uniquindio.sga.infrastructure.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

/**
 * Matriz de roles (7.1). El rol se verifica aquí; el propietario de cada
 * reserva y folio lo verifica el caso de uso con el {@code Solicitante}.
 */
@Configuration
@EnableWebSecurity
public class SeguridadConfig {

    private static final String ADMIN = "ADMINISTRADOR";
    private static final String RECEPCION = "RECEPCIONISTA";
    private static final String SERVICIO = "PERSONAL_SERVICIO";
    private static final String HUESPED = "HUESPED";

    @Bean
    public SecurityFilterChain cadenaSeguridad(HttpSecurity http, RespuestaErrorSeguridad errores,
                                               JwtAuthenticationConverter convertidor) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(cabeceras -> cabeceras.frameOptions(opciones -> opciones.sameOrigin()))
                .authorizeHttpRequests(rutas -> rutas
                        // Público
                        .requestMatchers(HttpMethod.POST, "/api/auth/registro", "/api/auth/login",
                                "/api/auth/recuperacion", "/api/auth/recuperacion/restablecer").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/alojamiento", "/api/apartamentos/disponibles",
                                "/api/apartamentos/*", "/api/apartamentos/*/calendario").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/cotizaciones").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/politicas-cancelacion/vigente").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/openapi.yaml",
                                "/h2-console/**", "/error").permitAll()
                        // Usuarios (7.2): cada uno su perfil; la gestión de usuarios y roles es del administrador
                        .requestMatchers("/api/usuarios/yo", "/api/usuarios/yo/**").authenticated()
                        .requestMatchers("/api/usuarios", "/api/usuarios/**").hasRole(ADMIN)
                        // Alojamiento y apartamentos (7.3); el personal de servicio prepara y reporta novedades (7.1)
                        .requestMatchers(HttpMethod.PUT, "/api/alojamiento").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/apartamentos/*/iniciar-preparacion",
                                "/api/apartamentos/*/marcar-preparado").hasAnyRole(ADMIN, RECEPCION, SERVICIO)
                        .requestMatchers("/api/apartamentos/*/novedades").hasAnyRole(ADMIN, RECEPCION, SERVICIO)
                        .requestMatchers(HttpMethod.GET, "/api/apartamentos").hasAnyRole(ADMIN, RECEPCION, SERVICIO)
                        .requestMatchers(HttpMethod.POST, "/api/apartamentos", "/api/apartamentos/*/bloqueos").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/apartamentos/*", "/api/apartamentos/*/**",
                                "/api/bloqueos/*/levantar").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/apartamentos/**").hasAnyRole(ADMIN, RECEPCION)
                        // Temporadas, tarifas y política (7.4)
                        .requestMatchers(HttpMethod.POST, "/api/temporadas", "/api/politicas-cancelacion").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/temporadas").hasAnyRole(ADMIN, RECEPCION)
                        // Reservas (7.5, 7.6): el huésped opera sobre las suyas; el caso de uso verifica el propietario
                        .requestMatchers(HttpMethod.PUT, "/api/reservas/*/confirmar", "/api/reservas/*/registrar-llegada",
                                "/api/reservas/*/registrar-salida", "/api/reservas/*/no-show",
                                "/api/reservas/*/resolver-novedad", "/api/reservas/*/modificacion")
                        .hasAnyRole(ADMIN, RECEPCION)
                        .requestMatchers(HttpMethod.GET, "/api/reservas/movimientos-del-dia").hasAnyRole(ADMIN, RECEPCION)
                        .requestMatchers("/api/reservas", "/api/reservas/**").hasAnyRole(ADMIN, RECEPCION, HUESPED)
                        // Folios (7.7)
                        .requestMatchers(HttpMethod.GET, "/api/folios/*").hasAnyRole(ADMIN, RECEPCION, HUESPED)
                        .requestMatchers("/api/folios/**").hasAnyRole(ADMIN, RECEPCION)
                        .anyRequest().authenticated())
                .oauth2ResourceServer(servidor -> servidor
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(convertidor))
                        .authenticationEntryPoint(errores)
                        .accessDeniedHandler(errores))
                .exceptionHandling(excepciones -> excepciones
                        .authenticationEntryPoint(errores)
                        .accessDeniedHandler(errores));
        return http.build();
    }

    /** El claim "rol" del token se convierte en la autoridad ROLE_{rol}. */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter convertidor = new JwtAuthenticationConverter();
        convertidor.setJwtGrantedAuthoritiesConverter(jwt ->
                List.of(new SimpleGrantedAuthority("ROLE_" + jwt.getClaimAsString(EmisorTokensJwt.CLAIM_ROL))));
        return convertidor;
    }

    @Bean
    public JwtEncoder jwtEncoder(ClaveJwt clave) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(clave.clave()));
    }

    @Bean
    public JwtDecoder jwtDecoder(ClaveJwt clave) {
        return NimbusJwtDecoder.withSecretKey(clave.clave()).macAlgorithm(MacAlgorithm.HS256).build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
