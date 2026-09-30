package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.*;
import java.util.UUID;

/** Raíz administrativa; referencia el apartamento por identidad y conserva el motivo de levantamiento. */
public final class Bloqueo {
    private final UUID identificacion;
    private final IdentificacionApartamento apartamento;
    private final Estancia periodo;
    private final String motivo;
    private boolean vigente = true;
    private String motivoLevantamiento;

    public Bloqueo(UUID identificacion, IdentificacionApartamento apartamento, Estancia periodo, String motivo) {
        if (identificacion == null || apartamento == null || periodo == null || motivo == null || motivo.isBlank()) {
            throw new ReglaDominioException("El bloqueo requiere identidad, apartamento, periodo y motivo");
        }
        this.identificacion = identificacion;
        this.apartamento = apartamento;
        this.periodo = periodo;
        this.motivo = motivo.trim();
    }
    public boolean afecta(Estancia estancia) {
        if (estancia == null) throw new ReglaDominioException("La estancia es obligatoria");
        return vigente && periodo.seSolapaCon(estancia);
    }
    public void levantar(String motivo) {
        if (!vigente || motivo == null || motivo.isBlank()) {
            throw new ReglaDominioException("Solo se levanta un bloqueo vigente indicando el motivo");
        }
        motivoLevantamiento = motivo.trim();
        vigente = false;
    }
    public UUID getIdentificacion() { return identificacion; }
    public IdentificacionApartamento getApartamento() { return apartamento; }
    public Estancia getPeriodo() { return periodo; }
    public String getMotivo() { return motivo; }
    public boolean estaVigente() { return vigente; }
    public String getMotivoLevantamiento() { return motivoLevantamiento; }
    @Override public boolean equals(Object objeto) {
        return objeto instanceof Bloqueo otro && identificacion.equals(otro.identificacion);
    }
    @Override public int hashCode() { return identificacion.hashCode(); }
}
