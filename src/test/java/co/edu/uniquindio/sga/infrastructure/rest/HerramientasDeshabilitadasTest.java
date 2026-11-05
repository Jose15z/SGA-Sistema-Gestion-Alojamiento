package co.edu.uniquindio.sga.infrastructure.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "sga.seguridad.herramientas-desarrollo=false")
@AutoConfigureMockMvc
class HerramientasDeshabilitadasTest {
    @Autowired MockMvc mvc;
    @Test void swaggerYH2SeCierranFueraDelDesarrollo() throws Exception {
        for (String ruta : new String[]{"/swagger-ui/index.html", "/v3/api-docs", "/openapi.yaml", "/h2-console/"}) {
            mvc.perform(get(ruta)).andExpect(status().isUnauthorized());
        }
    }
}
