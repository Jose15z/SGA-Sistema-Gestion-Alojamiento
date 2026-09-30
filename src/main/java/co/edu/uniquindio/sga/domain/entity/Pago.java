package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import java.time.LocalDateTime;
import java.util.UUID;

/** Entidad inmutable del folio. Identidad propia; importes negativos documentan devoluciones. */
public final class Pago {
    private final UUID identificacion;
    private final Dinero valor;
    private final String medio;
    private final LocalDateTime momento;
    private final String concepto;
    private final String autor;

    public Pago(UUID identificacion, Dinero valor, String medio, LocalDateTime momento,
                String concepto, String autor) {
        if (identificacion == null || valor == null || valor.esCero() || momento == null
                || medio == null || medio.isBlank() || concepto == null || concepto.isBlank()
                || autor == null || autor.isBlank()) {
            throw new ReglaDominioException("El pago requiere identidad, importe no nulo ni cero, medio, fecha, concepto y autor");
        }
        this.identificacion = identificacion;
        this.valor = valor;
        this.medio = medio.trim();
        this.momento = momento;
        this.concepto = concepto.trim();
        this.autor = autor.trim();
    }

    public UUID getIdentificacion() { return identificacion; }
    public Dinero valor() { return valor; }
    public String medio() { return medio; }
    public LocalDateTime momento() { return momento; }
    public String concepto() { return concepto; }
    public String autor() { return autor; }

    @Override public boolean equals(Object objeto) {
        return objeto instanceof Pago otro && identificacion.equals(otro.identificacion);
    }
    @Override public int hashCode() { return identificacion.hashCode(); }
}
