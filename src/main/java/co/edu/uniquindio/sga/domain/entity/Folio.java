package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Raíz de cuenta, identificada por la reserva. RN-15/16/17: saldo derivado,
 * movimientos inmutables, cierre con saldo solo con autorización registrada.
 * No cambia reservas ni ejecuta transferencias bancarias.
 */
public final class Folio {
    private final CodigoReserva reserva;
    private final List<Cargo> cargos = new ArrayList<>();
    private final List<Pago> pagos = new ArrayList<>();
    private boolean cerrado;
    private boolean alojamientoLiquidado;
    private String autorizacionCierre;

    public Folio(CodigoReserva reserva, Cargo alojamiento) {
        if (reserva == null || alojamiento == null || alojamiento.tipo() != TipoCargo.ALOJAMIENTO) {
            throw new ReglaDominioException("El folio requiere reserva y cargo inicial de alojamiento");
        }
        this.reserva = reserva;
        cargos.add(alojamiento);
    }

    public void registrarCargo(Cargo cargo) {
        if (cargo == null) throw new ReglaDominioException("El cargo es obligatorio");
        verificarMovimiento(cargo.momento());
        if (cargo.tipo() == TipoCargo.ALOJAMIENTO || cargo.tipo() == TipoCargo.REVERSO_ALOJAMIENTO
                || cargo.tipo() == TipoCargo.PENALIDAD) {
            throw new ReglaDominioException("El alojamiento inicial y su liquidación tienen operaciones propias");
        }
        if (cargo.tipo() == TipoCargo.AJUSTE_ALOJAMIENTO
                && (alojamientoLiquidado || totalAlojamiento().mas(cargo.valor()).esNegativo())) {
            throw new ReglaDominioException("El ajuste no puede alterar alojamiento liquidado ni dejarlo negativo");
        }
        cargos.add(cargo);
    }

    public void registrarPago(Pago pago) {
        if (pago == null) throw new ReglaDominioException("El pago es obligatorio");
        verificarMovimiento(pago.momento());
        if (pagos.contains(pago)) throw new ReglaDominioException("El pago ya fue registrado");
        if (totalPagado().mas(pago.valor()).esNegativo()) {
            throw new ReglaDominioException("No se puede devolver más de lo pagado");
        }
        pagos.add(pago);
    }

    /** Prevalidación sin cambios para coordinar la liquidación con la transición de la reserva. */
    public void verificarLiquidacion(Dinero penalidad, String concepto, String autor, LocalDateTime ahora) {
        verificarMovimiento(ahora);
        if (alojamientoLiquidado) throw new ReglaDominioException("El alojamiento ya fue liquidado");
        new Cargo(TipoCargo.PENALIDAD, concepto, penalidad, ahora, autor);
    }

    /** Conserva el cargo original y agrega su reverso y la penalidad. El saldo a favor queda visible. */
    public void liquidarAlojamiento(Dinero penalidad, String concepto, String autor, LocalDateTime ahora) {
        verificarLiquidacion(penalidad, concepto, autor, ahora);
        Cargo reverso = new Cargo(TipoCargo.REVERSO_ALOJAMIENTO, "Reverso: " + concepto,
                Dinero.CERO.menos(totalAlojamiento()), ahora, autor);
        Cargo retencion = new Cargo(TipoCargo.PENALIDAD, concepto, penalidad, ahora, autor);
        cargos.add(reverso);
        cargos.add(retencion);
        alojamientoLiquidado = true;
    }

    public Dinero totalPagado() { return pagos.stream().map(Pago::valor).reduce(Dinero.CERO, Dinero::mas); }
    public Dinero saldo() {
        return cargos.stream().map(Cargo::valor).reduce(Dinero.CERO, Dinero::mas).menos(totalPagado());
    }
    private Dinero totalAlojamiento() {
        return cargos.stream().filter(c -> c.tipo() == TipoCargo.ALOJAMIENTO
                        || c.tipo() == TipoCargo.AJUSTE_ALOJAMIENTO || c.tipo() == TipoCargo.REVERSO_ALOJAMIENTO)
                .map(Cargo::valor).reduce(Dinero.CERO, Dinero::mas);
    }
    public void cerrar(String autorizacion) {
        if (cerrado) throw new ReglaDominioException("El folio ya está cerrado");
        if (!saldo().esCero() && (autorizacion == null || autorizacion.isBlank())) {
            throw new ReglaDominioException("Cerrar con saldo requiere autorización registrada del administrador");
        }
        autorizacionCierre = autorizacion == null ? null : autorizacion.trim();
        cerrado = true;
    }
    private void verificarMovimiento(LocalDateTime momento) {
        if (cerrado) throw new ReglaDominioException("Un folio cerrado no admite movimientos");
        if (momento == null || cargos.stream().anyMatch(c -> c.momento().isAfter(momento))
                || pagos.stream().anyMatch(p -> p.momento().isAfter(momento))) {
            throw new ReglaDominioException("El movimiento no puede preceder a los movimientos del folio");
        }
    }
    public CodigoReserva getReserva() { return reserva; }
    public List<Cargo> obtenerCargos() { return List.copyOf(cargos); }
    public List<Pago> obtenerPagos() { return List.copyOf(pagos); }
    public boolean estaCerrado() { return cerrado; }
    public String getAutorizacionCierre() { return autorizacionCierre; }
    @Override public boolean equals(Object objeto) {
        return objeto instanceof Folio otro && reserva.equals(otro.reserva);
    }
    @Override public int hashCode() { return reserva.hashCode(); }
}
