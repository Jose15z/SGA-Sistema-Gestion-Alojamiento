package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.ApartamentoNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.FolioNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.ReservaNoEncontradaException;
import co.edu.uniquindio.sga.application.seguridad.Solicitante;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.ConfiguracionAlojamiento;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.service.ModificacionReservaService;
import co.edu.uniquindio.sga.domain.valueobject.Cargo;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.TipoCargo;
import co.edu.uniquindio.sga.domain.valueobject.ValorCongelado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Modificar fechas, acompañantes o apartamento de una reserva no iniciada (7.5, RN-14, RN-22).
 *
 * <p>Repite las seis validaciones de creación, recalcula el valor con las tarifas
 * vigentes y registra la diferencia como ajuste en el folio, en la misma transacción.
 * La política congelada no se recalcula.</p>
 */
@Service
public class ModificarReservaUseCase {
    private final ReservaRepository reservas;
    private final ApartamentoRepository apartamentos;
    private final FolioRepository folios;
    private final ModificacionReservaService modificacion;
    private final ConfiguracionAlojamiento configuracion;
    private final Clock reloj;

    public ModificarReservaUseCase(ReservaRepository reservas, ApartamentoRepository apartamentos,
                                   FolioRepository folios, ModificacionReservaService modificacion,
                                   ConfiguracionAlojamiento configuracion, Clock reloj) {
        this.reservas = Objects.requireNonNull(reservas);
        this.apartamentos = Objects.requireNonNull(apartamentos);
        this.folios = Objects.requireNonNull(folios);
        this.modificacion = Objects.requireNonNull(modificacion);
        this.configuracion = Objects.requireNonNull(configuracion);
        this.reloj = Objects.requireNonNull(reloj);
    }

    /** Solo cambian las fechas. */
    @Transactional
    public Reserva ejecutar(CodigoReserva codigo, Estancia nuevaEstancia, Solicitante solicitante) {
        return ejecutar(codigo, null, nuevaEstancia, null, solicitante);
    }

    /**
     * @param nuevoApartamento    apartamento asignado (null conserva el actual)
     * @param nuevaEstancia       nuevas fechas (null conserva las actuales)
     * @param nuevosAcompanantes  acompañantes del titular (null conserva el grupo actual)
     */
    @Transactional
    public Reserva ejecutar(CodigoReserva codigo, IdentificacionApartamento nuevoApartamento, Estancia nuevaEstancia,
                            List<Ocupante> nuevosAcompanantes, Solicitante solicitante) {
        Reserva reserva = reservas.obtenerPorCodigo(codigo).orElseThrow(() -> new ReservaNoEncontradaException(codigo));
        solicitante.verificarAcceso(reserva);
        IdentificacionApartamento destino = nuevoApartamento == null ? reserva.getApartamento() : nuevoApartamento;
        Apartamento apartamento = apartamentos.obtenerPorIdentificacion(destino)
                .orElseThrow(() -> new ApartamentoNoEncontradoException(destino));
        Estancia estancia = nuevaEstancia == null ? reserva.getEstancia() : nuevaEstancia;
        List<Ocupante> grupo = nuevosAcompanantes == null ? reserva.getOcupantes()
                : grupo(reserva.getTitular(), nuevosAcompanantes);
        Folio folio = folios.obtenerPorReserva(codigo).orElseThrow(() -> new FolioNoEncontradoException(codigo));
        String autor = solicitante.usuario();
        LocalDateTime ahora = LocalDateTime.now(reloj);

        ValorCongelado nuevoValor = modificacion.validarModificacion(reserva, apartamento, estancia, grupo,
                ahora.toLocalDate());
        Dinero ajuste = reserva.modificar(destino, estancia, grupo, nuevoValor,
                configuracion.umbralEdadFacturable(), autor, ahora);
        if (!ajuste.esCero()) {
            folio.registrarCargo(new Cargo(TipoCargo.AJUSTE_ALOJAMIENTO,
                    "Ajuste por modificación de la reserva", ajuste, ahora, autor));
        }
        reservas.guardar(reserva);
        folios.guardar(folio);
        return reserva;
    }

    private static List<Ocupante> grupo(Ocupante titular, List<Ocupante> acompanantes) {
        List<Ocupante> grupo = new ArrayList<>();
        grupo.add(titular);
        grupo.addAll(acompanantes);
        return grupo;
    }
}
