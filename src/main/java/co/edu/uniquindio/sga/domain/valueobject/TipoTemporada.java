package co.edu.uniquindio.sga.domain.valueobject;

/**
 * Conjunto cerrado de temporadas de Siete Raíces (Ficha A.3).
 *
 * <p>BASE es obligatoria y cubre toda fecha que no pertenezca
 * a otra temporada (7.4); ALTA y ESPECIAL se configuran con fechas.</p>
 */
public enum TipoTemporada {
    BASE,
    ALTA,
    ESPECIAL;

    /** La temporada base no se define por fechas: es el valor por omisión. */
    public boolean seDefinePorFechas() {
        return this != BASE;
    }
}
