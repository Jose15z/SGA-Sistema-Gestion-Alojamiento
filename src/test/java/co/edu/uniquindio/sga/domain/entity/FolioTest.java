package co.edu.uniquindio.sga.domain.entity;
import co.edu.uniquindio.sga.ReservasDePrueba;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.*;
import org.junit.jupiter.api.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class FolioTest {
    Folio folio;
    @BeforeEach void preparar() {
        folio = new Folio(new CodigoReserva("RES-2026-00001"), cargo(TipoCargo.ALOJAMIENTO,200_000));
    }
    Cargo cargo(TipoCargo tipo,long valor) { return new Cargo(tipo,"Concepto",Dinero.de(valor),ReservasDePrueba.CREACION,"recepcion"); }
    Pago pago(long valor) { return new Pago(UUID.randomUUID(),Dinero.de(valor),"Efectivo",ReservasDePrueba.CREACION,"Movimiento","recepcion"); }
    @Test void pagosParcialesYDevolucionMantienenSaldoDerivado() {
        folio.registrarPago(pago(50_000));folio.registrarPago(pago(30_000));folio.registrarPago(pago(-10_000));
        assertEquals(Dinero.de(130_000),folio.saldo());
        assertEquals(Dinero.de(70_000),folio.totalPagado());
    }
    @Test void pagoDuplicadoSeRechazaPorIdentidad() {
        Pago p=pago(50_000);folio.registrarPago(p);
        assertThrows(ReglaDominioException.class,()->folio.registrarPago(p));
        assertEquals(1,folio.obtenerPagos().size());
    }
    @Test void noDevuelveMasDeLoPagado() {
        folio.registrarPago(pago(50_000));
        assertThrows(ReglaDominioException.class,()->folio.registrarPago(pago(-50_001)));
        assertEquals(Dinero.de(50_000),folio.totalPagado());
    }
    @Test void listasDeMovimientosSonInmutables() {
        assertThrows(UnsupportedOperationException.class,()->folio.obtenerCargos().clear());
        assertThrows(UnsupportedOperationException.class,()->folio.obtenerPagos().add(pago(1)));
    }
    @Test void cierreConSaldoRequiereAutorizacion() {
        assertThrows(ReglaDominioException.class,()->folio.cerrar(" "));
        assertFalse(folio.estaCerrado());
        folio.cerrar("Administrador autoriza cobro posterior");
        assertTrue(folio.estaCerrado());
        assertEquals("Administrador autoriza cobro posterior",folio.getAutorizacionCierre());
        assertThrows(ReglaDominioException.class,()->folio.registrarPago(pago(1)));
        assertThrows(ReglaDominioException.class,()->folio.registrarCargo(cargo(TipoCargo.SERVICIO_ADICIONAL,1)));
    }
    @Test void cierreSinSaldoNoRequiereAutorizacion() {
        folio.registrarPago(pago(200_000));folio.cerrar(null);assertTrue(folio.estaCerrado());
    }
    @Test void cierreConSaldoAFavorTambienRequiereAutorizacion() {
        folio.registrarPago(pago(250_000));assertThrows(ReglaDominioException.class,()->folio.cerrar(null));
    }
    @Test void liquidacionRevierteAlojamientoYAjustesPeroConservaExtras() {
        folio.registrarCargo(cargo(TipoCargo.AJUSTE_ALOJAMIENTO,-50_000));
        folio.registrarCargo(cargo(TipoCargo.SERVICIO_ADICIONAL,25_000));
        folio.liquidarAlojamiento(Dinero.de(45_000),"Cancelación política 1","recepcion",ReservasDePrueba.CREACION);
        assertEquals(Dinero.de(70_000),folio.saldo());
        assertEquals(5,folio.obtenerCargos().size());
        assertThrows(ReglaDominioException.class,()->folio.registrarCargo(cargo(TipoCargo.AJUSTE_ALOJAMIENTO,1)));
    }
    @Test void noAdmitePenalidadNegativaNiLiquidacionRepetida() {
        assertThrows(ReglaDominioException.class,()->folio.liquidarAlojamiento(Dinero.de(-1),"Cancelación","recepcion",ReservasDePrueba.CREACION));
        assertEquals(1,folio.obtenerCargos().size());
        folio.liquidarAlojamiento(Dinero.CERO,"Cancelación","recepcion",ReservasDePrueba.CREACION);
        assertThrows(ReglaDominioException.class,()->folio.liquidarAlojamiento(Dinero.CERO,"Cancelación","recepcion",ReservasDePrueba.CREACION));
    }
    @Test void noAdmiteMovimientosAnterioresALaApertura() {
        assertThrows(ReglaDominioException.class,()->folio.registrarCargo(new Cargo(TipoCargo.SERVICIO_ADICIONAL,
                "Lavandería",Dinero.de(25_000),ReservasDePrueba.CREACION.minusSeconds(1),"recepcion")));
    }
    @Test void ajusteNoPuedeDejarAlojamientoNegativo() {
        assertThrows(ReglaDominioException.class,()->folio.registrarCargo(cargo(TipoCargo.AJUSTE_ALOJAMIENTO,-200_001)));
        assertEquals(Dinero.de(200_000),folio.saldo());
    }
    @Test void pagoEsEntidadPorIdentidadAunqueSusDatosDifieran() {
        UUID id=UUID.randomUUID();
        Pago a=new Pago(id,Dinero.de(1),"Efectivo",ReservasDePrueba.CREACION,"Pago","Ana");
        Pago b=new Pago(id,Dinero.de(2),"Tarjeta",ReservasDePrueba.CREACION,"Pago","Luis");
        assertEquals(a,b);assertEquals(a.hashCode(),b.hashCode());
    }
}
