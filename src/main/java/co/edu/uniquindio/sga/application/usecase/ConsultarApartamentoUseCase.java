package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.ApartamentoNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.Pagina;
import co.edu.uniquindio.sga.domain.repository.PeticionPagina;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.RegistroTarifa;
import co.edu.uniquindio.sga.domain.valueobject.NovedadApartamento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/** Consultar el detalle de un apartamento. */
@Service
public class ConsultarApartamentoUseCase {
    private final ApartamentoRepository apartamentos;

    public ConsultarApartamentoUseCase(ApartamentoRepository apartamentos) {
        this.apartamentos = Objects.requireNonNull(apartamentos);
    }

    @Transactional(readOnly = true)
    public Apartamento ejecutar(IdentificacionApartamento identificacion) {
        return apartamentos.obtenerPorIdentificacion(identificacion)
                .orElseThrow(() -> new ApartamentoNoEncontradoException(identificacion));
    }

    @Transactional(readOnly = true)
    public Pagina<RegistroTarifa> historialTarifas(IdentificacionApartamento identificacion, PeticionPagina pagina) {
        return Pagina.de(ejecutar(identificacion).getHistorialTarifas(), pagina);
    }

    @Transactional(readOnly = true)
    public Pagina<NovedadApartamento> novedades(IdentificacionApartamento identificacion, PeticionPagina pagina) {
        return Pagina.de(ejecutar(identificacion).getNovedades(), pagina);
    }
}
