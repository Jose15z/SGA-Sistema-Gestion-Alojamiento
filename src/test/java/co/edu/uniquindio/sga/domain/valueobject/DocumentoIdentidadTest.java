package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pruebas unitarias del documento de identidad.
 *
 * <p>Verifican igualdad por valor, eliminación de espacios
 * exteriores y rechazo de documentos nulos o vacíos.</p>
 *
 * <p>No verifican la autenticidad del documento ni imponen
 * un formato que el dominio actual no exige.</p>
 */
class DocumentoIdentidadTest {

    @Test
    void documentosConElMismoNumeroDebenSerIguales() {
        DocumentoIdentidad primero =
                new DocumentoIdentidad("CC-1001");

        DocumentoIdentidad segundo =
                new DocumentoIdentidad("CC-1001");

        assertEquals(primero, segundo);
        assertEquals(primero.hashCode(), segundo.hashCode());
    }

    @Test
    void documentosConNumerosDistintosNoDebenSerIguales() {
        DocumentoIdentidad primero =
                new DocumentoIdentidad("CC-1001");

        DocumentoIdentidad segundo =
                new DocumentoIdentidad("CC-1002");

        assertNotEquals(primero, segundo);
    }

    @Test
    void debeEliminarLosEspaciosExteriores() {
        DocumentoIdentidad documento =
                new DocumentoIdentidad("  CC-1001  ");

        assertEquals("CC-1001", documento.numero());
    }

    @Test
    void espaciosExterioresNoDebenCambiarLaIgualdad() {
        DocumentoIdentidad conEspacios =
                new DocumentoIdentidad("  CC-1001  ");

        DocumentoIdentidad sinEspacios =
                new DocumentoIdentidad("CC-1001");

        assertEquals(sinEspacios, conEspacios);
        assertEquals(sinEspacios.hashCode(), conEspacios.hashCode());
    }

    @Test
    void debeRechazarUnDocumentoNulo() {
        assertThrows(
                ReglaDominioException.class,
                () -> new DocumentoIdentidad(null)
        );
    }

    @Test
    void debeRechazarUnDocumentoVacio() {
        assertThrows(
                ReglaDominioException.class,
                () -> new DocumentoIdentidad("")
        );
    }

    @Test
    void debeRechazarUnDocumentoCompuestoSoloPorEspacios() {
        assertThrows(
                ReglaDominioException.class,
                () -> new DocumentoIdentidad("   ")
        );
    }
}