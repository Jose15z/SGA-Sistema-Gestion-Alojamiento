package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.ApartamentoNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.FolioNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.ReservaNoEncontradaException;
import co.edu.uniquindio.sga.application.seguridad.Solicitante;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.service.SalidaApartamentoService;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Registrar salida (RN-08, RN-17, 7.6).
 *
 * <p>Excepción documentada: reserva FINALIZADA y apartamento PENDIENTE_PREPARACION
 * en la misma transacción, verificando el folio cerrado.</p>
 */
@Service
public class RegistrarSalidaUseCase {
    private final ReservaRepository reservas;
    private final ApartamentoRepository apartamentos;
    private final FolioRepository folios;
    private final SalidaApartamentoService salida;
    private final Clock reloj;

    public RegistrarSalidaUseCase(ReservaRepository reservas, ApartamentoRepository apartamentos,
            FolioRepository folios, SalidaApartamentoService salida, Clock reloj) {
        this.reservas = Objects.requireNonNull(reservas);
        this.apartamentos = Objects.requireNonNull(apartamentos);
        this.folios = Objects.requireNonNull(folios);
        this.salida = Objects.requireNonNull(salida);
        this.reloj = Objects.requireNonNull(reloj);
    }

    @Transactional
    public Reserva ejecutar(CodigoReserva codigo, Solicitante solicitante) {
        Objects.requireNonNull(codigo, "El código es obligatorio");
        Reserva reserva = reservas.obtenerPorCodigo(codigo).orElseThrow(() -> new ReservaNoEncontradaException(codigo));
        Folio folio = folios.obtenerPorReserva(codigo).orElseThrow(() -> new FolioNoEncontradoException(codigo));
        Apartamento apartamento = apartamentos.obtenerPorIdentificacion(reserva.getApartamento())
                .orElseThrow(() -> new ApartamentoNoEncontradoException(reserva.getApartamento()));
        salida.verificar(reserva, apartamento, folio);
        reserva.registrarSalida(solicitante.usuario(), LocalDateTime.now(reloj));
        apartamento.liberar();
        reservas.guardar(reserva);
        apartamentos.guardar(apartamento);
        return reserva;
    }
}
