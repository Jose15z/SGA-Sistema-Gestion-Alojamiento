package co.edu.uniquindio.sga.infrastructure.config;

import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.BloqueoRepository;
import co.edu.uniquindio.sga.domain.repository.ConfiguracionAlojamiento;
import co.edu.uniquindio.sga.domain.repository.PoliticaCancelacionRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.repository.TarifarioRepository;
import co.edu.uniquindio.sga.domain.repository.UsuarioRepository;
import co.edu.uniquindio.sga.domain.service.AnticipoReservaService;
import co.edu.uniquindio.sga.domain.service.BloqueoApartamentoService;
import co.edu.uniquindio.sga.domain.service.ConciliacionCanalExternoService;
import co.edu.uniquindio.sga.domain.service.CotizadorEstanciaService;
import co.edu.uniquindio.sga.domain.service.DisponibilidadApartamentoService;
import co.edu.uniquindio.sga.domain.service.DisponibilidadParqueaderoService;
import co.edu.uniquindio.sga.domain.service.EntregaApartamentoService;
import co.edu.uniquindio.sga.domain.service.EstanciaMinimaTemporadaService;
import co.edu.uniquindio.sga.domain.service.ModificacionReservaService;
import co.edu.uniquindio.sga.domain.service.RegistroApartamentoService;
import co.edu.uniquindio.sga.domain.service.RegistroUsuarioService;
import co.edu.uniquindio.sga.domain.service.RetencionCancelacionService;
import co.edu.uniquindio.sga.domain.service.RetiroApartamentoService;
import co.edu.uniquindio.sga.domain.service.SalidaApartamentoService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra los servicios de dominio como beans (Guía 09, 6.3).
 *
 * <p>Es el punto donde Spring adopta al dominio sin que el dominio se entere:
 * las clases de {@code domain.service} siguen siendo Java puro.</p>
 */
@Configuration
@EnableConfigurationProperties(PropiedadesAlojamiento.class)
public class DomainServiceConfig {

    @Bean
    public ConfiguracionAlojamiento configuracionAlojamiento(PropiedadesAlojamiento propiedades) {
        return propiedades.aConfiguracion();
    }

    @Bean
    public DisponibilidadApartamentoService disponibilidadApartamentoService(
            ReservaRepository reservas, BloqueoRepository bloqueos, ConfiguracionAlojamiento configuracion) {
        return new DisponibilidadApartamentoService(reservas, bloqueos, configuracion);
    }

    @Bean
    public CotizadorEstanciaService cotizadorEstanciaService(TarifarioRepository tarifario,
                                                             ConfiguracionAlojamiento configuracion) {
        return new CotizadorEstanciaService(tarifario, configuracion);
    }

    @Bean
    public EstanciaMinimaTemporadaService estanciaMinimaTemporadaService(ConfiguracionAlojamiento configuracion) {
        return new EstanciaMinimaTemporadaService(configuracion);
    }

    @Bean
    public RetencionCancelacionService retencionCancelacionService(PoliticaCancelacionRepository politicas) {
        return new RetencionCancelacionService(politicas);
    }

    @Bean
    public EntregaApartamentoService entregaApartamentoService(ApartamentoRepository apartamentos) {
        return new EntregaApartamentoService(apartamentos);
    }

    @Bean
    public SalidaApartamentoService salidaApartamentoService() {
        return new SalidaApartamentoService();
    }

    @Bean
    public AnticipoReservaService anticipoReservaService(ConfiguracionAlojamiento configuracion) {
        return new AnticipoReservaService(configuracion);
    }

    @Bean
    public ModificacionReservaService modificacionReservaService(DisponibilidadApartamentoService disponibilidad,
                                                                 CotizadorEstanciaService cotizador,
                                                                 EstanciaMinimaTemporadaService estanciaMinima) {
        return new ModificacionReservaService(disponibilidad, cotizador, estanciaMinima);
    }

    @Bean
    public ConciliacionCanalExternoService conciliacionCanalExternoService(ReservaRepository reservas) {
        return new ConciliacionCanalExternoService(reservas);
    }

    @Bean
    public DisponibilidadParqueaderoService disponibilidadParqueaderoService(ReservaRepository reservas,
                                                                             ConfiguracionAlojamiento configuracion) {
        return new DisponibilidadParqueaderoService(reservas, configuracion);
    }

    @Bean
    public BloqueoApartamentoService bloqueoApartamentoService(ReservaRepository reservas, BloqueoRepository bloqueos) {
        return new BloqueoApartamentoService(reservas, bloqueos);
    }

    @Bean
    public RetiroApartamentoService retiroApartamentoService(ReservaRepository reservas) {
        return new RetiroApartamentoService(reservas);
    }

    @Bean
    public RegistroApartamentoService registroApartamentoService(ApartamentoRepository apartamentos) {
        return new RegistroApartamentoService(apartamentos);
    }

    @Bean
    public RegistroUsuarioService registroUsuarioService(UsuarioRepository usuarios) {
        return new RegistroUsuarioService(usuarios);
    }
}
