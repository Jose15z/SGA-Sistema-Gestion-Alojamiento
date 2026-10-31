package co.edu.uniquindio.sga.infrastructure.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * El openapi.yaml es el contrato: cada operación documentada existe en el código y
 * cada endpoint del código está documentado. Si un endpoint cambia sin actualizar
 * docs/api/openapi.yaml en el mismo commit, esta prueba falla. También se
 * comprueban los campos de los DTOs, enums cerrados, presencia de obligatorios
 * y que Swagger no pida el usuario autenticado como parámetro del cliente.
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

    @Test
    void losCamposDeLosDtosCoincidenConLosEsquemasDelContrato() throws Exception {
        int comprobados = 0;
        for (var esquema : esquemasDelContrato().entrySet()) {
            Class<?> dto = buscarClase(esquema.getKey(), List.of(
                    "co.edu.uniquindio.sga.application.dto.request.",
                    "co.edu.uniquindio.sga.application.dto.response."));
            if (dto == null || !dto.isRecord()) {
                continue; // Las páginas del contrato tienen nombres específicos para cada recurso.
            }
            Set<String> campos = Arrays.stream(dto.getRecordComponents())
                    .map(RecordComponent::getName).collect(Collectors.toCollection(TreeSet::new));
            @SuppressWarnings("unchecked")
            Map<String, Object> propiedades = (Map<String, Object>) esquema.getValue().get("properties");
            assertEquals(campos, propiedades.keySet(), "Campos de " + dto.getSimpleName());
            comprobados++;
        }
        assertTrue(comprobados >= 50, "Debe comprobarse el conjunto de DTOs, no solo una muestra");
    }

    @Test
    void losEnumsDelContratoCoincidenConLosValoresAdmitidos() throws Exception {
        int comprobados = 0;
        for (var esquema : esquemasDelContrato().entrySet()) {
            if (!esquema.getValue().containsKey("enum")) {
                continue;
            }
            Class<?> tipo = buscarClase(esquema.getKey(), List.of(
                    "co.edu.uniquindio.sga.domain.valueobject.",
                    "co.edu.uniquindio.sga.application.seguridad."));
            assertTrue(tipo != null && tipo.isEnum(), "Enum del contrato sin tipo: " + esquema.getKey());
            Set<String> valores = Arrays.stream(tipo.getEnumConstants())
                    .map(valor -> ((Enum<?>) valor).name()).collect(Collectors.toSet());
            @SuppressWarnings("unchecked")
            List<String> documentados = (List<String>) esquema.getValue().get("enum");
            assertEquals(valores, Set.copyOf(documentados), "Valores de " + esquema.getKey());
            comprobados++;
        }
        assertTrue(comprobados >= 10, "Deben comprobarse los enums cerrados de la API");
    }

    @Test
    void losCamposObligatoriosDelContratoExigenPresenciaEnElRequest() throws Exception {
        for (var esquema : esquemasDelContrato().entrySet()) {
            Class<?> dto = buscarClase(esquema.getKey(), List.of("co.edu.uniquindio.sga.application.dto.request."));
            if (dto == null || !dto.isRecord()) {
                continue;
            }
            @SuppressWarnings("unchecked")
            List<String> obligatorios = (List<String>) esquema.getValue().getOrDefault("required", List.of());
            for (String nombre : obligatorios) {
                var campo = dto.getDeclaredField(nombre);
                assertTrue(campo.isAnnotationPresent(NotNull.class)
                                || campo.isAnnotationPresent(NotBlank.class)
                                || campo.isAnnotationPresent(NotEmpty.class),
                        dto.getSimpleName() + "." + nombre + ": debe rechazarse si falta o llega null");
            }
        }
    }

    @Test
    void swaggerNoPideElSolicitanteQueSeObtieneDelJwt() throws Exception {
        String generado = mockMvc.perform(get("/v3/api-docs")).andReturn().getResponse().getContentAsString();
        JsonNode rutas = JsonMapper.builder().build().readTree(generado).get("paths");
        for (String ruta : rutas.propertyNames()) {
            for (String metodo : rutas.get(ruta).propertyNames()) {
                if (!METODOS.contains(metodo)) {
                    continue;
                }
                JsonNode parametros = rutas.get(ruta).get(metodo).get("parameters");
                if (parametros != null) {
                    for (JsonNode parametro : parametros) {
                        assertNotEquals("solicitante", parametro.get("name").asString(),
                                metodo + " " + ruta + ": el usuario autenticado no es un parámetro del cliente");
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Map<String, Object>> esquemasDelContrato() throws Exception {
        try (Reader lector = new FileReader("docs/api/openapi.yaml")) {
            Map<String, Object> contrato = new Yaml().load(lector);
            Map<String, Object> componentes = (Map<String, Object>) contrato.get("components");
            return (Map<String, Map<String, Object>>) componentes.get("schemas");
        }
    }

    private Class<?> buscarClase(String nombre, List<String> paquetes) {
        for (String paquete : paquetes) {
            try {
                return Class.forName(paquete + nombre);
            } catch (ClassNotFoundException ignorada) {
                // Un esquema puede ser una página o un tipo de otra capa.
            }
        }
        return null;
    }
}
