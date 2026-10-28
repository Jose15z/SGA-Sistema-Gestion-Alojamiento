package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.ApartamentoNoEncontradoException;
import co.edu.uniquindio.sga.application.seguridad.Solicitante;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.ConfiguracionAlojamiento;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.GeneradorCodigoReserva;
import co.edu.uniquindio.sga.domain.repository.PoliticaCancelacionRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.service.ConciliacionCanalExternoService;
import co.edu.uniquindio.sga.domain.service.CotizadorEstanciaService;
import co.edu.uniquindio.sga.domain.service.DisponibilidadApartamentoService;
import co.edu.uniquindio.sga.domain.service.EstanciaMinimaTemporadaService;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.Cargo;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.FechaCreacion;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.IdentificadorExterno;
import co.edu.uniquindio.sga.domain.valueobject.TipoCargo;
import co.edu.uniquindio.sga.domain.valueobject.ValorCongelado;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

/**
 * Crear reserva (7.5). Orquesta; las decisiones son del dominio.
 *
 * <p>Excepción documentada a "un agregado = una transacción": la reserva y su
 * folio nacen juntos (F-08, 7.7), por eso se guardan en la misma transacción.</p>
 */
@Service
public class CrearReservaUseCase {
    private final ReservaRepository reservas;
    private final ApartamentoRepository apartamentos;
    private final FolioRepository folios;
    private final GeneradorCodigoReserva codigos;
    private final PoliticaCancelacionRepository politicas;
    private final ConfiguracionAlojamiento configuracion;
    private final DisponibilidadApartamentoService disponibilidad;
    private final CotizadorEstanciaService cotizador;
    private final EstanciaMinimaTemporadaService estanciaMinima;
    private final ConciliacionCanalExternoService conciliacion;
    private final Clock reloj;

    public CrearReservaUseCase(ReservaRepository reservas, ApartamentoRepository apartamentos, FolioRepository folios,
            GeneradorCodigoReserva codigos, PoliticaCancelacionRepository politicas, ConfiguracionAlojamiento configuracion,
            DisponibilidadApartamentoService disponibilidad, CotizadorEstanciaService cotizador,
            EstanciaMinimaTemporadaService estanciaMinima, ConciliacionCanalExternoService conciliacion, Clock reloj) {
        this.reservas = Objects.requireNonNull(reservas);
        this.apartamentos = Objects.requireNonNull(apartamentos);
        this.folios = Objects.requireNonNull(folios);
        this.codigos = Objects.requireNonNull(codigos);
        this.politicas = Objects.requireNonNull(politicas);
        this.configuracion = Objects.requireNonNull(configuracion);
        this.disponibilidad = Objects.requireNonNull(disponibilidad);
        this.cotizador = Objects.requireNonNull(cotizador);
        this.estanciaMinima = Objects.requireNonNull(estanciaMinima);
        this.conciliacion = Objects.requireNonNull(conciliacion);
        this.reloj = Objects.requireNonNull(reloj);
    }

    @Transactional
    public Reserva ejecutar(IdentificacionApartamento identificacion, Estancia estancia, Ocupante titular,
                            List<Ocupante> ocupantes, CanalOrigen canal, IdentificadorExterno identificadorExterno,
                            LocalTime horaLlegada, int mascotas, Solicitante solicitante) {
        Objects.requireNonNull(identificacion, "El apartamento es obligatorio");
        Objects.requireNonNull(solicitante, "El solicitante es obligatorio");
        solicitante.verificarPuedeReservar(canal, titular == null ? null : titular.getDocumento());
        List<Ocupante> grupo = List.copyOf(Objects.requireNonNull(ocupantes, "El grupo es obligatorio"));
        Apartamento apartamento = apartamentos.obtenerPorIdentificacion(identificacion)
                .orElseThrow(() -> new ApartamentoNoEncontradoException(identificacion));
        String autor = solicitante.usuario();
        LocalDateTime ahora = LocalDateTime.now(reloj);

        // Servicios de dominio: lo que ningún agregado decide solo
        conciliacion.verificarNoDuplicada(canal, identificadorExterno);            // RN-19
        disponibilidad.verificarDisponible(apartamento, estancia, grupo.size(), null); // RN-01, 02, 07, 20, 18
        ValorCongelado valor = cotizador.cotizar(identificacion, estancia, grupo);    // RN-05, 06, RP-03
        estanciaMinima.verificar(valor);                                              // RP-01
        VersionPolitica politica = politicas.obtenerVigente().getVersion();           // RN-22

        // Agregado: nace PENDIENTE con sus propias invariantes (RN-03, RN-04, RN-06, L-16)
        CodigoReserva codigo = codigos.generar(new FechaCreacion(ahora));
        Reserva reserva = Reserva.crear(codigo, identificacion, estancia, titular, grupo, canal,
                identificadorExterno, configuracion.umbralEdadFacturable(), valor, politica, autor, ahora);
        if (horaLlegada != null) {
            reserva.indicarHoraEstimadaLlegada(horaLlegada, autor, ahora);
        }
        if (mascotas > 0) {
            reserva.indicarMascotas(mascotas, configuracion.mascotasMaximasPorReserva(), autor, ahora);
        }
        Folio folio = new Folio(codigo, new Cargo(TipoCargo.ALOJAMIENTO, "Alojamiento", valor.total(), ahora, autor));

        reservas.guardar(reserva);
        folios.guardar(folio);
        return reserva;
    }
}
