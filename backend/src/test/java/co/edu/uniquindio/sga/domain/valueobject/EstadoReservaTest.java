package co.edu.uniquindio.sga.domain.valueobject;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del ciclo de vida declarado por EstadoReserva.
 *
 * <p>Verifican las transiciones de RN-08 y la retención
 * de disponibilidad de RN-12.</p>
 *
 * <p>No sustituyen las pruebas de los comportamientos
 * del agregado Reserva ni de sus precondiciones.</p>
 */
class EstadoReservaTest {

    @Test
    void pendienteSoloDebePermitirConfirmacionOCancelacion() {
        verificarDestinosPermitidos(
                EstadoReserva.PENDIENTE,
                EnumSet.of(
                        EstadoReserva.CONFIRMADA,
                        EstadoReserva.CANCELADA
                )
        );
    }

    @Test
    void confirmadaSoloDebePermitirLlegadaCancelacionONoShow() {
        verificarDestinosPermitidos(
                EstadoReserva.CONFIRMADA,
                EnumSet.of(
                        EstadoReserva.EN_CURSO,
                        EstadoReserva.CANCELADA,
                        EstadoReserva.NO_SHOW
                )
        );
    }

    @Test
    void enCursoSoloDebePermitirFinalizacion() {
        verificarDestinosPermitidos(
                EstadoReserva.EN_CURSO,
                EnumSet.of(EstadoReserva.FINALIZADA)
        );
    }

    @Test
    void finalizadaNoDebePermitirNingunaTransicion() {
        verificarDestinosPermitidos(
                EstadoReserva.FINALIZADA,
                EnumSet.noneOf(EstadoReserva.class)
        );
    }

    @Test
    void canceladaNoDebePermitirNingunaTransicion() {
        verificarDestinosPermitidos(
                EstadoReserva.CANCELADA,
                EnumSet.noneOf(EstadoReserva.class)
        );
    }

    @Test
    void noShowNoDebePermitirNingunaTransicion() {
        verificarDestinosPermitidos(
                EstadoReserva.NO_SHOW,
                EnumSet.noneOf(EstadoReserva.class)
        );
    }

    @Test
    void ningunEstadoDebePermitirUnaTransicionANull() {
        for (EstadoReserva estado : EstadoReserva.values()) {
            assertFalse(
                    estado.puedeTransicionarA(null),
                    "No debe permitirse una transición desde "
                            + estado + " hacia null"
            );
        }
    }

    @Test
    void estadosActivosDebenRetenerDisponibilidad() {
        Set<EstadoReserva> activos = EnumSet.of(
                EstadoReserva.PENDIENTE,
                EstadoReserva.CONFIRMADA,
                EstadoReserva.EN_CURSO
        );

        for (EstadoReserva estado : activos) {
            assertTrue(
                    estado.esActiva(),
                    estado + " debe ser un estado activo"
            );

            assertFalse(
                    estado.esTerminal(),
                    estado + " no debe ser un estado terminal"
            );

            assertTrue(
                    estado.retieneDisponibilidad(),
                    estado + " debe retener disponibilidad"
            );
        }
    }

    @Test
    void estadosTerminalesNoDebenRetenerDisponibilidad() {
        Set<EstadoReserva> terminales = EnumSet.of(
                EstadoReserva.FINALIZADA,
                EstadoReserva.CANCELADA,
                EstadoReserva.NO_SHOW
        );

        for (EstadoReserva estado : terminales) {
            assertFalse(
                    estado.esActiva(),
                    estado + " no debe ser un estado activo"
            );

            assertTrue(
                    estado.esTerminal(),
                    estado + " debe ser un estado terminal"
            );

            assertFalse(
                    estado.retieneDisponibilidad(),
                    estado + " no debe retener disponibilidad"
            );
        }
    }

    /**
     * Comprueba todos los destinos para un estado de origen,
     * incluyendo la prohibición de transitar al mismo estado.
     */
    private void verificarDestinosPermitidos(
            EstadoReserva origen,
            Set<EstadoReserva> permitidos
    ) {
        for (EstadoReserva destino : EstadoReserva.values()) {
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