package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.Pagina;
import co.edu.uniquindio.sga.domain.repository.PeticionPagina;
import co.edu.uniquindio.sga.domain.service.CotizadorEstanciaService;
import co.edu.uniquindio.sga.domain.service.DisponibilidadApartamentoService;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.ValorCongelado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Buscar apartamentos disponibles por fechas y composición del grupo (7.5).
 *
 * <p>El repositorio solo prefiltra los activos con capacidad suficiente; la
 * decisión de disponibilidad la toma el servicio de dominio para cada candidato.
 * Cada resultado lleva su valor estimado, calculado por el cotizador con las
 * fechas de nacimiento del grupo; los filtros de precio se aplican sobre ese valor.</p>
 */
@Service
public class BuscarApartamentosDisponiblesUseCase {

    /** Sin fechas de nacimiento se estima con adultos: el valor estimado nunca queda por debajo del real. */
    private static final int EDAD_SUPUESTA = 30;

    private final ApartamentoRepository apartamentos;
    private final DisponibilidadApartamentoService disponibilidad;
    private final CotizadorEstanciaService cotizador;
    private final Clock reloj;

    public BuscarApartamentosDisponiblesUseCase(ApartamentoRepository apartamentos,
                                                DisponibilidadApartamentoService disponibilidad,
                                                CotizadorEstanciaService cotizador, Clock reloj) {
        this.apartamentos = Objects.requireNonNull(apartamentos);
        this.disponibilidad = Objects.requireNonNull(disponibilidad);
        this.cotizador = Objects.requireNonNull(cotizador);
        this.reloj = Objects.requireNonNull(reloj);
    }

    @Transactional(readOnly = true)
    public Pagina<Resultado> ejecutar(Estancia estancia, int totalOcupantes, List<LocalDate> fechasNacimiento,
                                      Filtros filtros, PeticionPagina pagina) {
        LocalDate hoy = LocalDate.now(reloj);
        List<Ocupante> grupo = grupo(totalOcupantes, fechasNacimiento, hoy);
        List<Resultado> resultados = apartamentos.buscarActivosConCapacidadPara(grupo.size()).stream()
                .filter(apartamento -> filtros.cumpleDatos(apartamento))
                .filter(apartamento -> disponibilidad.estaDisponible(apartamento, estancia, grupo.size(), hoy))
                .sorted(Comparator.comparing(apartamento -> apartamento.getIdentificacion().valor()))
                .map(apartamento -> new Resultado(apartamento,
                        cotizador.cotizar(apartamento.getIdentificacion(), estancia, grupo)))
                .filter(resultado -> filtros.cumplePrecio(resultado.valorEstimado().total().valor()))
                .toList();
        return Pagina.de(resultados, pagina);
    }

    private static List<Ocupante> grupo(int totalOcupantes, List<LocalDate> fechasNacimiento, LocalDate hoy) {
        if (fechasNacimiento != null && !fechasNacimiento.isEmpty() && fechasNacimiento.size() != totalOcupantes) {
            throw new IllegalArgumentException("Debe indicar una fecha de nacimiento por cada ocupante");
        }
        List<Ocupante> grupo = new ArrayList<>();
        for (int i = 0; i < totalOcupantes; i++) {
            LocalDate nacimiento = fechasNacimiento == null || fechasNacimiento.isEmpty()
                    ? hoy.minusYears(EDAD_SUPUESTA)
                    : fechasNacimiento.get(i);
            grupo.add(new Ocupante(new DocumentoIdentidad("BUSQUEDA-" + (i + 1)), "Ocupante " + (i + 1),
                    nacimiento, hoy));
        }
        return grupo;
    }

    /**
     * Filtros opcionales de la búsqueda (7.5); un valor null no filtra.
     *
     * @param capacidadMinima    capacidad máxima del apartamento, al menos este valor
     * @param dormitoriosMinimos número mínimo de dormitorios
     * @param precioMinimo       valor estimado mínimo de la estancia
     * @param precioMaximo       valor estimado máximo de la estancia
     * @param caracteristicas    características que debe tener el apartamento
     */
    public record Filtros(Integer capacidadMinima, Integer dormitoriosMinimos, BigDecimal precioMinimo,
                          BigDecimal precioMaximo, List<String> caracteristicas) {

        public static final Filtros NINGUNO = new Filtros(null, null, null, null, List.of());

        public Filtros {
            if (precioMinimo != null && precioMaximo != null && precioMinimo.compareTo(precioMaximo) > 0) {
                throw new IllegalArgumentException("El precio mínimo no puede superar al máximo");
            }
            caracteristicas = caracteristicas == null ? List.of() : List.copyOf(caracteristicas);
        }

        boolean cumpleDatos(Apartamento apartamento) {
            return (capacidadMinima == null || apartamento.getCapacidad().valor() >= capacidadMinima)
                    && (dormitoriosMinimos == null || apartamento.getDormitorios() >= dormitoriosMinimos)
                    && apartamento.tieneCaracteristicas(caracteristicas);
        }

        boolean cumplePrecio(BigDecimal valor) {
            return (precioMinimo == null || valor.compareTo(precioMinimo) >= 0)
                    && (precioMaximo == null || valor.compareTo(precioMaximo) <= 0);
        }
    }

    /** Apartamento disponible y el valor estimado de la estancia para el grupo. */
    public record Resultado(Apartamento apartamento, ValorCongelado valorEstimado) {
    }
}
