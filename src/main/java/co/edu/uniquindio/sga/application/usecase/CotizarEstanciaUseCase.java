package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.ApartamentoNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.service.CotizadorEstanciaService;
import co.edu.uniquindio.sga.domain.service.EstanciaMinimaTemporadaService;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.ValorCongelado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/** Cotizar estancia (RN-05, RN-06, RP-01, RP-03): calcula sin guardar nada. */
@Service
public class CotizarEstanciaUseCase {
    private final ApartamentoRepository apartamentos;
    private final CotizadorEstanciaService cotizador;
    private final EstanciaMinimaTemporadaService estanciaMinima;

    public CotizarEstanciaUseCase(ApartamentoRepository apartamentos, CotizadorEstanciaService cotizador,
                                  EstanciaMinimaTemporadaService estanciaMinima) {
        this.apartamentos = Objects.requireNonNull(apartamentos);
        this.cotizador = Objects.requireNonNull(cotizador);
        this.estanciaMinima = Objects.requireNonNull(estanciaMinima);
    }

    @Transactional(readOnly = true)
    public ValorCongelado ejecutar(IdentificacionApartamento apartamento, Estancia estancia, List<Ocupante> ocupantes) {
        if (!apartamentos.existe(apartamento)) {
            throw new ApartamentoNoEncontradoException(apartamento);
        }
        ValorCongelado valor = cotizador.cotizar(apartamento, estancia, ocupantes);
        estanciaMinima.verificar(valor);
        return valor;
    }
}
