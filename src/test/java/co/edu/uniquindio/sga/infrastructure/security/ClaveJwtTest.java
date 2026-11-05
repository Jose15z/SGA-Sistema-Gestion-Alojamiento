package co.edu.uniquindio.sga.infrastructure.security;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClaveJwtTest {
    @Test void claveAusenteDebilOPlantillaImpideArrancar() {
        for (String clave : new String[]{"", " ", "corta", "REEMPLAZAR_CON_UNA_CLAVE_ALEATORIA_DE_MINIMO_32_BYTES"}) {
            assertThrows(IllegalStateException.class, () -> new ClaveJwt(clave));
        }
        assertThrows(IllegalStateException.class, () -> new ClaveJwt(null));
    }
    @Test void aceptaClaveExternaDeAlMenos256Bits() {
        assertEquals(32, new ClaveJwt("12345678901234567890123456789012").clave().getEncoded().length);
    }
}
