package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.ApartamentoNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.*;
import co.edu.uniquindio.sga.domain.repository.*;
import co.edu.uniquindio.sga.domain.service.*;
import co.edu.uniquindio.sga.domain.valueobject.*;
import java.time.*;
import java.util.*;

/** Crea la reserva y abre su folio. La infraestructura debe dar atomicidad y exclusión por apartamento. */
public final class CrearReservaUseCase {
    private final ReservaRepository reservas;
    private final ApartamentoRepository apartamentos;
    private final FolioRepository folios;
    private final GeneradorCodigoReserva codigos;
    private final PoliticaCancelacionRepository politicas;
    private final ConfiguracionAlojamiento configuracion;
    private final DisponibilidadApartamentoService disponibilidad;
    private final CotizadorEstanciaService cotizador;
    private final EstanciaMinimaTemporadaService estanciaMinima;
    private final Clock reloj;

    public CrearReservaUseCase(ReservaRepository reservas, ApartamentoRepository apartamentos, FolioRepository folios,
            GeneradorCodigoReserva codigos, PoliticaCancelacionRepository politicas, ConfiguracionAlojamiento configuracion,
            DisponibilidadApartamentoService disponibilidad, CotizadorEstanciaService cotizador,
            EstanciaMinimaTemporadaService estanciaMinima, Clock reloj) {
        this.reservas = Objects.requireNonNull(reservas);
        this.apartamentos = Objects.requireNonNull(apartamentos);
        this.folios = Objects.requireNonNull(folios);
        this.codigos = Objects.requireNonNull(codigos);
        this.politicas = Objects.requireNonNull(politicas);
        this.configuracion = Objects.requireNonNull(configuracion);
        this.disponibilidad = Objects.requireNonNull(disponibilidad);
        this.cotizador = Objects.requireNonNull(cotizador);
        this.estanciaMinima = Objects.requireNonNull(estanciaMinima);
        this.reloj = Objects.requireNonNull(reloj);
    }

    public Reserva ejecutar(IdentificacionApartamento identificacion, Estancia estancia, Ocupante titular,
                            List<Ocupante> ocupantes, CanalOrigen canal, String identificadorExterno,
                            LocalTime horaLlegada, String autor) {
        Objects.requireNonNull(identificacion, "El apartamento es obligatorio");
        List<Ocupante> grupo = List.copyOf(Objects.requireNonNull(ocupantes, "El grupo es obligatorio"));
        Apartamento apartamento = apartamentos.obtenerPorIdentificacion(identificacion)
                .orElseThrow(() -> new ApartamentoNoEncontradoException(identificacion));
        LocalDateTime ahora = LocalDateTime.now(reloj);
        disponibilidad.verificarDisponible(apartamento, estancia, grupo.size(), null);
        ValorCongelado valor = cotizador.cotizar(identificacion, estancia, grupo);
        estanciaMinima.verificar(valor);
        VersionPolitica politica = politicas.obtenerVigente().getVersion();
        CodigoReserva codigo = codigos.generar(new FechaCreacion(ahora));
        Reserva reserva = Reserva.crear(codigo, identificacion, estancia, titular, grupo, canal,
                identificadorExterno, configuracion.umbralEdadFacturable(), valor, politica, autor, ahora);
        if (horaLlegada != null) reserva.indicarHoraEstimadaLlegada(horaLlegada, autor, ahora);
        Folio folio = new Folio(codigo, new Cargo(TipoCargo.ALOJAMIENTO, "Alojamiento", valor.total(), ahora, autor));
        reservas.guardar(reserva);
        folios.guardar(folio);
        return reserva;
    }
}
