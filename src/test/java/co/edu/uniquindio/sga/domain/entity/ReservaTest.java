package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.CargoNoche;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.EventoReserva;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.UmbralEdadFacturable;
import co.edu.uniquindio.sga.domain.valueobject.ValorCongelado;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pruebas de creación del agregado Reserva.
 *
 * <p>Verifican identidad, invariantes internas, conservación
 * de la cotización y protección de las colecciones.</p>
 *
 * <p>No requieren construir un Apartamento ni utilizar
 * Spring, repositorios o el reloj del sistema.</p>
 */
class ReservaTest {

    private static final LocalDateTime AHORA =
            LocalDateTime.of(2026, 12, 1, 10, 30);

    private static final Estancia ESTANCIA = new Estancia(
            LocalDate.of(2026, 12, 10),
            LocalDate.of(2026, 12, 12)
    );

    private static final IdentificacionApartamento APARTAMENTO =
            new IdentificacionApartamento("APT-301");

    private static final VersionPolitica POLITICA =
            new VersionPolitica(1);

    @Test
    void debeNacerPendienteConReferenciaAlApartamentoYMomentoExacto() {
        Reserva reserva = reservaValida();

        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
        assertEquals(APARTAMENTO, reserva.getApartamento());
        assertEquals(AHORA, reserva.getFechaCreacion().momento());
    }

    @Test
    void reservasConElMismoCodigoDebenSerIgualesAunqueCambieElGrupo() {
        Ocupante primero = adulto("CC-1");
        Ocupante segundo = adulto("CC-2");

        Reserva primera = crear(
                "RES-2026-00001", ESTANCIA, primero,
                List.of(primero), CanalOrigen.DIRECTO, null,
                cotizacion(ESTANCIA, 1), POLITICA
        );

        Reserva segunda = crear(
                "RES-2026-00001", ESTANCIA, segundo,
                List.of(segundo), CanalOrigen.DIRECTO, null,
                cotizacion(ESTANCIA, 1), POLITICA
        );

        assertEquals(primera, segunda);
        assertEquals(primera.hashCode(), segunda.hashCode());
    }

    @Test
    void reservasConCodigosDistintosNoDebenSerIguales() {
        Ocupante titular = adulto("CC-1");

        Reserva primera = reservaValida();
        Reserva segunda = crear(
                "RES-2026-00002", ESTANCIA, titular,
                List.of(titular), CanalOrigen.DIRECTO, null,
                cotizacion(ESTANCIA, 1), POLITICA
        );

        assertNotEquals(primera, segunda);
    }

    @Test
    void debeRegistrarLaCreacionConElAutorDeLaOperacion() {
        Reserva reserva = reservaValida();

        assertEquals(1, reserva.getHistorial().size());

        EventoReserva evento = reserva.getHistorial().get(0);
        assertEquals("CREACION", evento.accion());
        assertEquals("recepcion-1", evento.autor());
        assertEquals(AHORA, evento.momento());
        assertNull(evento.estadoAnterior());
        assertEquals(EstadoReserva.PENDIENTE, evento.estadoNuevo());
    }

    @Test
    void debeConservarElDesgloseAunqueCambieLaListaUsadaParaCotizar() {
        Ocupante titular = adulto("CC-1");
        List<CargoNoche> detalle = new ArrayList<>(
                cotizacion(ESTANCIA, 1).detalle()
        );
        ValorCongelado valorOriginal = new ValorCongelado(detalle);

        Reserva reserva = crear(
                "RES-2026-00001", ESTANCIA, titular,
                List.of(titular), CanalOrigen.DIRECTO, null,
                valorOriginal, POLITICA
        );

        detalle.clear();

        assertEquals(valorOriginal, reserva.getValor());
        assertEquals(Dinero.de(200_000), reserva.getValor().total());
        assertEquals(POLITICA, reserva.getPolitica());
    }

    @Test
    void modificarElGrupoOriginalNoDebeCambiarLaComposicionReservada() {
        Ocupante titular = adulto("CC-1");
        List<Ocupante> grupo = new ArrayList<>(List.of(titular));

        Reserva reserva = crear(
                "RES-2026-00001", ESTANCIA, titular,
                grupo, CanalOrigen.DIRECTO, null,
                cotizacion(ESTANCIA, 1), POLITICA
        );

        grupo.clear();

        assertEquals(1, reserva.totalOcupantes());
        assertEquals(List.of(titular), reserva.getOcupantes());
    }

    @Test
    void noDebePermitirRetirarOcupantesMedianteLaListaExpuesta() {
        Reserva reserva = reservaValida();
        List<Ocupante> originales = List.copyOf(reserva.getOcupantes());

        assertThrows(
                UnsupportedOperationException.class,
                () -> reserva.getOcupantes().clear()
        );

        assertEquals(originales, reserva.getOcupantes());
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
    }

    @Test
    void noDebePermitirBorrarElHistorialDesdeElExterior() {
        Reserva reserva = reservaValida();
        List<EventoReserva> original =
                List.copyOf(reserva.getHistorial());

        assertThrows(
                UnsupportedOperationException.class,
                () -> reserva.getHistorial().clear()
        );

        assertEquals(original, reserva.getHistorial());
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
    }

    @Test
    void debeRechazarUnaEntradaAnteriorALaFechaDeCreacion() {
        Ocupante titular = adulto("CC-1");
        Estancia pasada = new Estancia(
                AHORA.toLocalDate().minusDays(1),
                AHORA.toLocalDate().plusDays(1)
        );

        assertThrows(
                ReglaDominioException.class,
                () -> crear(
                        "RES-2026-00001", pasada, titular,
                        List.of(titular), CanalOrigen.DIRECTO, null,
                        cotizacion(pasada, 1), POLITICA
                )
        );
    }

    @Test
    void debeRechazarUnTitularQueNoPerteneceAlGrupo() {
        Ocupante titular = adulto("CC-1");
        List<Ocupante> grupo = List.of(adulto("CC-2"));

        assertThrows(
                ReglaDominioException.class,
                () -> crear(
                        "RES-2026-00001", ESTANCIA, titular,
                        grupo, CanalOrigen.DIRECTO, null,
                        cotizacion(ESTANCIA, 1), POLITICA
                )
        );
    }

    @Test
    void debeRechazarUnTitularNoFacturableAunqueHayaUnAdultoEnElGrupo() {
        Ocupante menor = new Ocupante(
                new DocumentoIdentidad("TI-1"),
                "Menor de prueba",
                LocalDate.of(2020, 1, 1),
                AHORA.toLocalDate()
        );
        List<Ocupante> grupo = List.of(menor, adulto("CC-1"));

        assertThrows(
                ReglaDominioException.class,
                () -> crear(
                        "RES-2026-00001", ESTANCIA, menor,
                        grupo, CanalOrigen.DIRECTO, null,
                        cotizacion(ESTANCIA, 1), POLITICA
                )
        );
    }

    @Test
    void debeRechazarUnaCotizacionConFechasDistintasAunqueTengaDosNoches() {
        Ocupante titular = adulto("CC-1");
        Estancia otra = new Estancia(
                ESTANCIA.fechaEntrada().plusDays(1),
                ESTANCIA.fechaSalida().plusDays(1)
        );

        assertThrows(
                ReglaDominioException.class,
                () -> crear(
                        "RES-2026-00001", ESTANCIA, titular,
                        List.of(titular), CanalOrigen.DIRECTO, null,
                        cotizacion(otra, 1), POLITICA
                )
        );
    }

    @Test
    void debeRechazarUnDesgloseConOtraCantidadDeFacturables() {
        Ocupante titular = adulto("CC-1");

        assertThrows(
                ReglaDominioException.class,
                () -> crear(
                        "RES-2026-00001", ESTANCIA, titular,
                        List.of(titular), CanalOrigen.DIRECTO, null,
                        cotizacion(ESTANCIA, 2), POLITICA
                )
        );
    }

    @Test
    void debeRechazarUnaReservaSinPoliticaCongelada() {
        Ocupante titular = adulto("CC-1");

        assertThrows(
                ReglaDominioException.class,
                () -> crear(
                        "RES-2026-00001", ESTANCIA, titular,
                        List.of(titular), CanalOrigen.DIRECTO, null,
                        cotizacion(ESTANCIA, 1), null
                )
        );
    }

    @Test
    void debeRechazarUnaReservaSinValorCongelado() {
        Ocupante titular = adulto("CC-1");

        assertThrows(
                ReglaDominioException.class,
                () -> crear(
                        "RES-2026-00001", ESTANCIA, titular,
                        List.of(titular), CanalOrigen.DIRECTO, null,
                        null, POLITICA
                )
        );
    }

    @Test
    void debeRechazarUnCanalExternoSinIdentificador() {
        Ocupante titular = adulto("CC-1");

        assertThrows(
                ReglaDominioException.class,
                () -> crear(
                        "RES-2026-00001", ESTANCIA, titular,
                        List.of(titular), CanalOrigen.EXTERNO, null,
                        cotizacion(ESTANCIA, 1), POLITICA
                )
        );
    }

    @Test
    void debeRechazarUnIdentificadorExternoEnBlanco() {
        Ocupante titular = adulto("CC-1");

        assertThrows(
                ReglaDominioException.class,
                () -> crear(
                        "RES-2026-00001", ESTANCIA, titular,
                        List.of(titular), CanalOrigen.EXTERNO, "   ",
                        cotizacion(ESTANCIA, 1), POLITICA
                )
        );
    }

    @Test
    void debeConservarElIdentificadorProporcionadoPorElCanalExterno() {
        Ocupante titular = adulto("CC-1");

        Reserva reserva = crear(
                "RES-2026-00001", ESTANCIA, titular,
                List.of(titular), CanalOrigen.EXTERNO, "Ext-Ab123",
                cotizacion(ESTANCIA, 1), POLITICA
        );

        assertEquals("Ext-Ab123", reserva.getIdentificadorExterno());
        assertEquals(CanalOrigen.EXTERNO, reserva.getCanalOrigen());
    }

    private Reserva reservaValida() {
        Ocupante titular = adulto("CC-1");

        return crear(
                "RES-2026-00001", ESTANCIA, titular,
                List.of(titular), CanalOrigen.DIRECTO, null,
                cotizacion(ESTANCIA, 1), POLITICA
        );
    }

    private Reserva crear(
            String codigo,
            Estancia estancia,
            Ocupante titular,
            List<Ocupante> grupo,
            CanalOrigen canal,
            String identificadorExterno,
            ValorCongelado valor,
            VersionPolitica politica
    ) {
        return Reserva.crear(
                new CodigoReserva(codigo),
                APARTAMENTO,
                estancia,
                titular,
                grupo,
                canal,
                identificadorExterno,
                new UmbralEdadFacturable(10),
                valor,
                politica,
                "recepcion-1",
                AHORA
        );
    }

    private Ocupante adulto(String documento) {
        return new Ocupante(
                new DocumentoIdentidad(documento),
                "Persona de prueba",
                LocalDate.of(1990, 1, 1),
                AHORA.toLocalDate()
        );
    }

    private ValorCongelado cotizacion(
            Estancia estancia,
            int facturables
    ) {
        List<CargoNoche> detalle = estancia.nochesOcupadas().stream()
                .map(noche -> new CargoNoche(
                        noche,
                        "Normal",
                        Dinero.de(100_000),
                        facturables
                ))
                .toList();

        return new ValorCongelado(detalle);
    }
}