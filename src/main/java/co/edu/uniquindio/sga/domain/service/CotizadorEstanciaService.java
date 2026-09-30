package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.ConfiguracionAlojamiento;
import co.edu.uniquindio.sga.domain.repository.TarifarioRepository;
import co.edu.uniquindio.sga.domain.valueobject.CargoNoche;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.Tarifa;
import co.edu.uniquindio.sga.domain.valueobject.UmbralEdadFacturable;
import co.edu.uniquindio.sga.domain.valueobject.ValorCongelado;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Servicio de dominio que cotiza el alojamiento de una estancia.
 *
 * <p>RN-05: consulta la tarifa correspondiente a cada noche.</p>
 *
 * <p>RN-06: determina los ocupantes facturables una sola vez,
 * utilizando su edad en la fecha de entrada.</p>
 *
 * <p>RP-03: aplica el descuento configurado cuando la estancia
 * alcanza la cantidad mínima de noches requerida.</p>
 *
 * <p>RN-22: devuelve un desglose inmutable con el porcentaje
 * aplicado, apto para conservarse en la reserva.</p>
 *
 * <p>No verifica disponibilidad, capacidad, estancia mínima
 * por temporada ni la condición del titular. Tampoco agrega
 * cargos por servicios adicionales ni modifica reservas.</p>
 */
public class CotizadorEstanciaService {

    private final TarifarioRepository tarifarioRepository;
    private final ConfiguracionAlojamiento configuracion;

    /**
     * Construye el servicio con los puertos que necesita.
     *
     * <p>Conserva los puertos, no los valores configurables.
     * Estos se consultan al realizar cada cotización.</p>
     */
    public CotizadorEstanciaService(
            TarifarioRepository tarifarioRepository,
            ConfiguracionAlojamiento configuracion
    ) {
        this.tarifarioRepository = Objects.requireNonNull(
                tarifarioRepository,
                "El puerto del tarifario es obligatorio"
        );

        this.configuracion = Objects.requireNonNull(
                configuracion,
                "El puerto de configuración es obligatorio"
        );
    }

    /**
     * Cotiza una estancia para un apartamento y un grupo.
     *
     * <p>El descuento se aplica sobre el alojamiento completo.
     * ValorCongelado calcula el total con un único redondeo
     * final, sin descontar ni redondear por separado cada noche.</p>
     *
     * @param apartamento identificación del apartamento
     * @param estancia rango de noches solicitado
     * @param ocupantes composición completa del grupo
     * @return desglose de alojamiento y descuento aplicado
     * @throws ReglaDominioException si los datos del grupo
     *         o la configuración no permiten cotizar
     */
    public ValorCongelado cotizar(
            IdentificacionApartamento apartamento,
            Estancia estancia,
            List<Ocupante> ocupantes
    ) {
        if (apartamento == null) {
            throw new ReglaDominioException(
                    "La cotización debe indicar el apartamento"
            );
        }

        if (estancia == null) {
            throw new ReglaDominioException(
                    "La cotización debe indicar la estancia"
            );
        }

        if (ocupantes == null || ocupantes.isEmpty()) {
            throw new ReglaDominioException(
                    "La cotización requiere la composición del grupo"
            );
        }

        if (ocupantes.stream().anyMatch(Objects::isNull)) {
            throw new ReglaDominioException(
                    "El grupo no puede contener ocupantes nulos"
            );
        }

        List<Ocupante> grupo = List.copyOf(ocupantes);

        if (grupo.stream().distinct().count() != grupo.size()) {
            throw new ReglaDominioException(
                    "No se puede repetir un ocupante en la cotización"
            );
        }

        UmbralEdadFacturable umbral =
                configuracion.umbralEdadFacturable();

        if (umbral == null) {
            throw new ReglaDominioException(
                    "El alojamiento debe configurar el umbral "
                            + "de edad facturable"
            );
        }

        int nochesMinimas =
                configuracion.nochesMinimasParaDescuento();

        if (nochesMinimas < 1) {
            throw new ReglaDominioException(
                    "El mínimo de noches para el descuento "
                            + "debe ser mayor que cero"
            );
        }

        BigDecimal porcentajeConfigurado =
                configuracion.porcentajeDescuentoEstanciaProlongada();

        if (porcentajeConfigurado == null
                || porcentajeConfigurado.signum() < 0
                || porcentajeConfigurado.compareTo(
                BigDecimal.valueOf(100)
        ) > 0) {
            throw new ReglaDominioException(
                    "El descuento por estancia prolongada "
                            + "debe estar configurado entre 0 y 100"
            );
        }

        int facturables = (int) grupo.stream()
                .filter(ocupante ->
                        ocupante.esFacturableEn(estancia, umbral))
                .count();

        if (facturables == 0) {
            throw new ReglaDominioException(
                    "La cotización requiere al menos "
                            + "un ocupante facturable"
            );
        }

        List<CargoNoche> detalle = new ArrayList<>();

        for (LocalDate noche : estancia.nochesOcupadas()) {
            Tarifa tarifa = tarifarioRepository.tarifaDe(
                    apartamento,
                    noche
            );

            if (tarifa == null) {
                throw new ReglaDominioException(
                        "El apartamento no tiene tarifa definida "
                                + "para la noche del " + noche
                );
            }

            if (!apartamento.equals(tarifa.apartamento())) {
                throw new ReglaDominioException(
                        "La tarifa consultada no corresponde "
                                + "al apartamento solicitado"
                );
            }

            detalle.add(new CargoNoche(
                    noche,
                    tarifa.temporada(),
                    tarifa.valorPorOcupanteNoche(),
                    facturables
            ));
        }

        BigDecimal porcentajeAplicado =
                estancia.noches() >= nochesMinimas
                        ? porcentajeConfigurado
                        : BigDecimal.ZERO;

        return new ValorCongelado(detalle, porcentajeAplicado);
    }
}