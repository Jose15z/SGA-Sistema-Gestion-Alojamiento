package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.*;
import co.edu.uniquindio.sga.domain.entity.*;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.*;
import co.edu.uniquindio.sga.domain.service.*;
import co.edu.uniquindio.sga.domain.valueobject.*;
import java.time.*;
import java.util.*;

/** Coordina salida y liberación. Requiere folio cerrado, incluso si su cierre necesitó autorización. */
public final class RegistrarSalidaUseCase {
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
    public void ejecutar(CodigoReserva codigo, String autor) {
        Objects.requireNonNull(codigo, "El código es obligatorio");
        Reserva reserva = reservas.obtenerPorCodigo(codigo).orElseThrow(() -> new ReservaNoEncontradaException(codigo));
        Folio folio = folios.obtenerPorReserva(codigo).orElseThrow(() -> new FolioNoEncontradoException(codigo));
        Apartamento apartamento = apartamentos.obtenerPorIdentificacion(reserva.getApartamento())
                .orElseThrow(() -> new ApartamentoNoEncontradoException(reserva.getApartamento()));
        salida.verificar(reserva, apartamento, folio);
        reserva.registrarSalida(autor, LocalDateTime.now(reloj));
        apartamento.liberar();
        reservas.guardar(reserva);
        apartamentos.guardar(apartamento);
    }
}
