package co.edu.uniquindio.sga.domain.valueobject;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de las transiciones del estado operativo.
 *
 * <p>Verifican los destinos permitidos por la Guía 05
 * y la condición operativa de registro de RN-11.</p>
 *
 * <p>No sustituyen las pruebas de los comportamientos
 * del agregado Apartamento ni verifican disponibilidad
 * para un intervalo de fechas.</p>
 */
class EstadoOperativoTest {

    @Test
    void preparadoSoloDebePermitirOcupacionOFueraDeServicio() {
        verificarDestinosPermitidos(
                EstadoOperativo.PREPARADO,
                EnumSet.of(
                        EstadoOperativo.OCUPADO,
                        EstadoOperativo.FUERA_DE_SERVICIO
                )
        );
    }

    @Test
    void ocupadoSoloDebePermitirQuedarPendienteDePreparacion() {
        verificarDestinosPermitidos(
                EstadoOperativo.OCUPADO,
                EnumSet.of(EstadoOperativo.PENDIENTE_PREPARACION)
        );
    }

    @Test
    void pendienteSoloDebePermitirPreparacionOFueraDeServicio() {
        verificarDestinosPermitidos(
                EstadoOperativo.PENDIENTE_PREPARACION,
                EnumSet.of(
                        EstadoOperativo.EN_PREPARACION,
                        EstadoOperativo.FUERA_DE_SERVICIO
                )
        );
    }

    @Test
    void enPreparacionSoloDebePermitirPreparadoOFueraDeServicio() {
        verificarDestinosPermitidos(
                EstadoOperativo.EN_PREPARACION,
                EnumSet.of(
                        EstadoOperativo.PREPARADO,
                        EstadoOperativo.FUERA_DE_SERVICIO
                )
        );
    }

    @Test
    void fueraDeServicioSoloDebePermitirQuedarPendienteDePreparacion() {
        verificarDestinosPermitidos(
                EstadoOperativo.FUERA_DE_SERVICIO,
                EnumSet.of(EstadoOperativo.PENDIENTE_PREPARACION)
        );
    }

    @Test
    void ningunEstadoDebePermitirUnaTransicionANull() {
        for (EstadoOperativo estado : EstadoOperativo.values()) {
            assertFalse(
                    estado.puedeTransicionarA(null),
                    "No debe permitirse una transición desde "
                            + estado + " hacia null"
            );
        }
    }

    @Test
    void preparadoDebePermitirRegistro() {
        assertTrue(EstadoOperativo.PREPARADO.permiteRegistro());
    }

    @Test
    void losDemasEstadosNoDebenPermitirRegistro() {
        Set<EstadoOperativo> estados = EnumSet.of(
                EstadoOperativo.OCUPADO,
                EstadoOperativo.PENDIENTE_PREPARACION,
                EstadoOperativo.EN_PREPARACION,
                EstadoOperativo.FUERA_DE_SERVICIO
        );

        for (EstadoOperativo estado : estados) {
            assertFalse(
                    estado.permiteRegistro(),
                    estado + " no debe permitir el registro"
            );
        }
    }

    /**
     * Comprueba todos los destinos posibles para un origen,
     * incluida la prohibición de transitar al mismo estado.
     */
    private void verificarDestinosPermitidos(
            EstadoOperativo origen,
            Set<EstadoOperativo> permitidos
    ) {
        for (EstadoOperativo destino : EstadoOperativo.values()) {
            if (permitidos.contains(destino)) {
                assertTrue(
                        origen.puedeTransicionarA(destino),
                        "Debe permitirse la transición de "
                                + origen + " a " + destino
                );
            } else {
                assertFalse(
                        origen.puedeTransicionarA(destino),
                        "Debe rechazarse la transición de "
                                + origen + " a " + destino
                );
            }
        }
    }
}