package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.ApartamentoNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.FolioNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.ReservaNoEncontradaException;
import co.edu.uniquindio.sga.application.seguridad.Solicitante;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Folio;
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
import co.edu.uniquindio.sga.domain.valueobject.TipoCargo;
import co.edu.uniquindio.sga.domain.valueobject.ValorCongelado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Modificar las fechas de una reserva (RN-14, RN-22).
 *
 * <p>Excepción documentada: la diferencia de valor se registra en el folio como
 * ajuste en la misma transacción en que cambia la reserva.</p>
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

    @Transactional
    public Reserva ejecutar(CodigoReserva codigo, Estancia nuevaEstancia, Solicitante solicitante) {
        Reserva reserva = reservas.obtenerPorCodigo(codigo).orElseThrow(() -> new ReservaNoEncontradaException(codigo));
        solicitante.verificarAcceso(reserva);
        Apartamento apartamento = apartamentos.obtenerPorIdentificacion(reserva.getApartamento())
                .orElseThrow(() -> new ApartamentoNoEncontradoException(reserva.getApartamento()));
        Folio folio = folios.obtenerPorReserva(codigo).orElseThrow(() -> new FolioNoEncontradoException(codigo));
        String autor = solicitante.usuario();
        LocalDateTime ahora = LocalDateTime.now(reloj);

        ValorCongelado nuevoValor = modificacion.validarModificacion(reserva, apartamento, nuevaEstancia);
        Dinero ajuste = reserva.modificarEstancia(nuevaEstancia, nuevoValor,
                configuracion.umbralEdadFacturable(), autor, ahora);
        if (!ajuste.esCero()) {
            folio.registrarCargo(new Cargo(TipoCargo.AJUSTE_ALOJAMIENTO,
                    "Ajuste por modificación de estancia", ajuste, ahora, autor));
        }
        reservas.guardar(reserva);
        folios.guardar(folio);
        return reserva;
    }
}
