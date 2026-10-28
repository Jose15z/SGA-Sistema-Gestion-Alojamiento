package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.EscenarioReservas;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.Tarifa;
import co.edu.uniquindio.sga.domain.valueobject.TipoTemporada;
import java.util.List;
import co.edu.uniquindio.sga.domain.valueobject.Capacidad;
import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de identidad, capacidad y comportamiento operativo
 * del agregado Apartamento.
 *
 * <p>Los rechazos de operaciones verifican que el estado
 * permanezca intacto. No se utilizan Spring ni repositorios.</p>
 */
class ApartamentoTest {

    @Test
    void apartamentosConLaMismaIdentificacionDebenSerIguales() {
        Apartamento primero = apartamento();

        Apartamento segundo = EscenarioReservas.apartamento("APT-301", "Otro nombre", 3, 6);
        segundo.declararFueraDeServicio();

        assertEquals(primero, segundo);
        assertEquals(primero.hashCode(), segundo.hashCode());
    }

    @Test
    void apartamentosConIdentificacionesDistintasNoDebenSerIguales() {
        Apartamento primero = apartamento();

        Apartamento segundo = EscenarioReservas.apartamento("APT-302", "Balcón", 2, 4);

        assertNotEquals(primero, segundo);
    }

    @Test
    void debeAdmitirUnGrupoQueAlcanzaLaCapacidadExacta() {
        Apartamento apartamento = apartamento();

        assertTrue(apartamento.admite(4));
    }

    @Test
    void noDebeAdmitirUnGrupoQueSuperaLaCapacidad() {
        Apartamento apartamento = apartamento();

        assertFalse(apartamento.admite(5));
        assertEquals(new Capacidad(4), apartamento.getCapacidad());
    }

    @Test
    void noDebeAdmitirUnGrupoVacio() {
        Apartamento apartamento = apartamento();

        assertFalse(apartamento.admite(0));
    }

    @Test
    void debePermitirOcuparUnApartamentoActivoYPreparado() {
        Apartamento apartamento = apartamento();
        assertTrue(apartamento.puedeRecibirGrupo());

        apartamento.marcarOcupado();

        assertEquals(
                EstadoOperativo.OCUPADO,
                apartamento.getEstadoOperativo()
        );
        assertFalse(apartamento.puedeRecibirGrupo());
    }

    @Test
    void noDebePermitirOcuparUnApartamentoYaOcupado() {
        Apartamento apartamento = apartamento();
        apartamento.marcarOcupado();

        assertThrows(
                ReglaDominioException.class,
                apartamento::marcarOcupado
        );

        assertEquals(
                EstadoOperativo.OCUPADO,
                apartamento.getEstadoOperativo()
        );
    }

    @Test
    void debeDejarPendienteDePreparacionAlLiberar() {
        Apartamento apartamento = apartamento();
        apartamento.marcarOcupado();

        apartamento.liberar();

        assertEquals(
                EstadoOperativo.PENDIENTE_PREPARACION,
                apartamento.getEstadoOperativo()
        );
        assertFalse(apartamento.puedeRecibirGrupo());
    }

    @Test
    void noDebeLiberarUnApartamentoQueNoEstaOcupado() {
        Apartamento apartamento = apartamento();

        assertThrows(
                ReglaDominioException.class,
                apartamento::liberar
        );

        assertEquals(
                EstadoOperativo.PREPARADO,
                apartamento.getEstadoOperativo()
        );
    }

    @Test
    void debeIniciarLaPreparacionDespuesDeLiberar() {
        Apartamento apartamento = pendienteDePreparacion();

        apartamento.iniciarPreparacion();

        assertEquals(
                EstadoOperativo.EN_PREPARACION,
                apartamento.getEstadoOperativo()
        );
        assertFalse(apartamento.puedeRecibirGrupo());
    }

    @Test
    void noDebeIniciarPreparacionMientrasEstaOcupado() {
        Apartamento apartamento = apartamento();
        apartamento.marcarOcupado();

        assertThrows(
                ReglaDominioException.class,
                apartamento::iniciarPreparacion
        );

        assertEquals(
                EstadoOperativo.OCUPADO,
                apartamento.getEstadoOperativo()
        );
    }

    @Test
    void debePermitirRecibirOtroGrupoDespuesDeCompletarLaPreparacion() {
        Apartamento apartamento = pendienteDePreparacion();
        apartamento.iniciarPreparacion();

        apartamento.marcarPreparado();

        assertEquals(
                EstadoOperativo.PREPARADO,
                apartamento.getEstadoOperativo()
        );
        assertTrue(apartamento.puedeRecibirGrupo());
    }

    @Test
    void noDebeSaltarLaPreparacionParaQuedarPreparado() {
        Apartamento apartamento = pendienteDePreparacion();

        assertThrows(
                ReglaDominioException.class,
                apartamento::marcarPreparado
        );

        assertEquals(
                EstadoOperativo.PENDIENTE_PREPARACION,
                apartamento.getEstadoOperativo()
        );
        assertFalse(apartamento.puedeRecibirGrupo());
    }

    @Test
    void debePermitirDeclararFueraDeServicioUnApartamentoPreparado() {
        Apartamento apartamento = apartamento();

        apartamento.declararFueraDeServicio();

        assertEquals(
                EstadoOperativo.FUERA_DE_SERVICIO,
                apartamento.getEstadoOperativo()
        );
        assertFalse(apartamento.puedeRecibirGrupo());
        assertTrue(apartamento.estaActivo());
    }

    @Test
    void noDebeDeclararFueraDeServicioUnApartamentoOcupado() {
        Apartamento apartamento = apartamento();
        apartamento.marcarOcupado();

        assertThrows(
                ReglaDominioException.class,
                apartamento::declararFueraDeServicio
        );

        assertEquals(
                EstadoOperativo.OCUPADO,
                apartamento.getEstadoOperativo()
        );
    }

    @Test
    void debeReincorporarUnApartamentoFueraDeServicioComoPendiente() {
        Apartamento apartamento = apartamento();
        apartamento.declararFueraDeServicio();

        apartamento.reincorporarAPreparacion();

        assertEquals(
                EstadoOperativo.PENDIENTE_PREPARACION,
                apartamento.getEstadoOperativo()
        );
        assertFalse(apartamento.puedeRecibirGrupo());
    }

    @Test
    void noDebeReincorporarUnApartamentoQueNoEstaFueraDeServicio() {
        Apartamento apartamento = apartamento();

        assertThrows(
                ReglaDominioException.class,
                apartamento::reincorporarAPreparacion
        );

        assertEquals(
                EstadoOperativo.PREPARADO,
                apartamento.getEstadoOperativo()
        );
    }

    @Test
    void noDebeOcuparUnApartamentoFueraDeServicio() {
        Apartamento apartamento = apartamento();
        apartamento.declararFueraDeServicio();

        assertThrows(
                ReglaDominioException.class,
                apartamento::marcarOcupado
        );

        assertEquals(
                EstadoOperativo.FUERA_DE_SERVICIO,
                apartamento.getEstadoOperativo()
        );
    }

    @Test
    void desactivarDebeImpedirRecibirGruposSinCambiarLaIdentidad() {
        Apartamento apartamento = apartamento();
        IdentificacionApartamento identificacion =
                apartamento.getIdentificacion();
        int hashOriginal = apartamento.hashCode();

        apartamento.desactivar();

        assertFalse(apartamento.estaActivo());
        assertFalse(apartamento.puedeRecibirGrupo());
        assertEquals(identificacion, apartamento.getIdentificacion());
        assertEquals(hashOriginal, apartamento.hashCode());
        assertEquals(
                EstadoOperativo.PREPARADO,
                apartamento.getEstadoOperativo()
        );
    }

    @Test
    void noDebeOcuparUnApartamentoInactivoAunqueEstePreparado() {
        Apartamento apartamento = apartamento();
        apartamento.desactivar();

        assertThrows(
                ReglaDominioException.class,
                apartamento::marcarOcupado
        );

        assertFalse(apartamento.estaActivo());
        assertEquals(
                EstadoOperativo.PREPARADO,
                apartamento.getEstadoOperativo()
        );
    }

    @Test
    void cambiarCapacidadDebeActualizarElLimiteSinCambiarLaIdentidad() {
        Apartamento apartamento = apartamento();
        IdentificacionApartamento identificacion =
                apartamento.getIdentificacion();
        int hashOriginal = apartamento.hashCode();

        apartamento.cambiarCapacidad(new Capacidad(2));

        assertTrue(apartamento.admite(2));
        assertFalse(apartamento.admite(3));
        assertEquals(identificacion, apartamento.getIdentificacion());
        assertEquals(hashOriginal, apartamento.hashCode());
    }

    @Test
    void debeRechazarUnaCapacidadNulaSinAlterarLaAnterior() {
        Apartamento apartamento = apartamento();

        assertThrows(
                ReglaDominioException.class,
                () -> apartamento.cambiarCapacidad(null)
        );

        assertEquals(new Capacidad(4), apartamento.getCapacidad());
        assertEquals(
                EstadoOperativo.PREPARADO,
                apartamento.getEstadoOperativo()
        );
    }

    @Test
    void noDebeRegistrarseActivoSinTarifasEnTodasLasTemporadas() {
        IdentificacionApartamento id = new IdentificacionApartamento("APT-999");
        List<Tarifa> incompletas = List.of(new Tarifa(id, TipoTemporada.BASE, Dinero.de(70_000)),
                new Tarifa(id, TipoTemporada.ALTA, Dinero.de(80_000)));

        assertThrows(ReglaDominioException.class,
                () -> new Apartamento(id, "Sin especial", 1, new Capacidad(2), incompletas));
    }

    @Test
    void noDebeAceptarTarifasDeOtroApartamento() {
        IdentificacionApartamento id = new IdentificacionApartamento("APT-999");
        IdentificacionApartamento otro = new IdentificacionApartamento("APT-998");
        List<Tarifa> tarifas = List.of(new Tarifa(id, TipoTemporada.BASE, Dinero.de(70_000)),
                new Tarifa(id, TipoTemporada.ALTA, Dinero.de(80_000)),
                new Tarifa(otro, TipoTemporada.ESPECIAL, Dinero.de(90_000)));

        assertThrows(ReglaDominioException.class,
                () -> new Apartamento(id, "Mezclado", 1, new Capacidad(2), tarifas));
    }

    @Test
    void actualizarTarifaReemplazaElValorDeLaTemporada() {
        Apartamento apartamento = apartamento();

        apartamento.actualizarTarifa(TipoTemporada.ALTA, Dinero.de(95_000));

        assertEquals(Dinero.de(95_000), apartamento.tarifaPara(TipoTemporada.ALTA).valorPorOcupanteNoche());
        assertTrue(apartamento.tieneTarifasCompletas());
    }

    @Test
    void reactivarDevuelveALaVentaUnApartamentoRetirado() {
        Apartamento apartamento = apartamento();
        apartamento.desactivar();

        apartamento.reactivar();

        assertTrue(apartamento.estaActivo());
        assertThrows(ReglaDominioException.class, apartamento::reactivar);
    }

    @Test
    void noDebeRetirarDosVecesElMismoApartamento() {
        Apartamento apartamento = apartamento();
        apartamento.desactivar();

        assertThrows(ReglaDominioException.class, apartamento::desactivar);
    }

    private Apartamento apartamento() {
        return EscenarioReservas.apartamento("APT-301", "Balcón", 2, 4);
    }

    private Apartamento pendienteDePreparacion() {
        Apartamento apartamento = apartamento();
        apartamento.marcarOcupado();
        apartamento.liberar();
        return apartamento;
    }
}