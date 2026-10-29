package co.edu.uniquindio.sga.infrastructure.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.yaml.snakeyaml.Yaml;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.FileReader;
import java.io.Reader;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * El openapi.yaml es el contrato: cada operación documentada existe en el código y
 * cada endpoint del código está documentado. Si un endpoint cambia sin actualizar
 * docs/api/openapi.yaml en el mismo commit, esta prueba falla.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ContratoOpenApiTest {

    private static final Set<String> METODOS = Set.of("get", "post", "put", "patch", "delete");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void lasOperacionesDelContratoCoincidenConLasDelCodigo() throws Exception {
        String generado = mockMvc.perform(get("/v3/api-docs")).andReturn().getResponse().getContentAsString();
        Set<String> implementadas = new TreeSet<>();
        JsonNode rutas = JsonMapper.builder().build().readTree(generado).get("paths");
        for (String ruta : rutas.propertyNames()) {
            for (String metodo : rutas.get(ruta).propertyNames()) {
                if (METODOS.contains(metodo)) {
                    implementadas.add(metodo.toUpperCase() + " " + ruta.replaceFirst("^/api", ""));
                }
            }
        }

        Set<String> documentadas = new TreeSet<>();
        try (Reader lector = new FileReader("docs/api/openapi.yaml")) {
            Map<String, Object> contrato = new Yaml().load(lector);
            @SuppressWarnings("unchecked")
            Map<String, Map<String, Object>> paths = (Map<String, Map<String, Object>>) contrato.get("paths");
            paths.forEach((ruta, operaciones) -> operaciones.keySet().stream()
                    .filter(METODOS::contains)
                    .forEach(metodo -> documentadas.add(metodo.toUpperCase() + " " + ruta)));
        }

        assertEquals(documentadas, implementadas);
    }
}
