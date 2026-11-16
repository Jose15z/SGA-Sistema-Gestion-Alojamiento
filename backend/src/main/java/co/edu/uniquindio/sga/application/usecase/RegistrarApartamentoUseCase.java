package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.service.RegistroApartamentoService;
import co.edu.uniquindio.sga.domain.valueobject.Capacidad;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.Tarifa;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/** Registrar apartamento (7.3, 7.4): nace activo solo con tarifas en todas las temporadas. */
@Service
public class RegistrarApartamentoUseCase {
    private final ApartamentoRepository apartamentos;
    private final RegistroApartamentoService registro;

    private final Clock reloj;

    public RegistrarApartamentoUseCase(ApartamentoRepository apartamentos, RegistroApartamentoService registro,
                                       Clock reloj) {
        this.apartamentos = Objects.requireNonNull(apartamentos);
        this.registro = Objects.requireNonNull(registro);
        this.reloj = Objects.requireNonNull(reloj);
    }

    @Transactional
    public Apartamento ejecutar(IdentificacionApartamento identificacion, String nombre, String descripcion,
                                int dormitorios, Capacidad capacidad, List<String> caracteristicas,
                                List<Tarifa> tarifas) {
        registro.verificarIdentificacionDisponible(identificacion);   // L-04
        Apartamento apartamento = new Apartamento(identificacion, nombre, descripcion, dormitorios, capacidad,
                caracteristicas, tarifas, LocalDateTime.now(reloj));
        apartamentos.guardar(apartamento);
        return apartamento;
    }
}
