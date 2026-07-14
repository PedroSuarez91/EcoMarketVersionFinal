package ecomarket.catalogo.controller;

import ecomarket.catalogo.model.Catalogo;
import ecomarket.catalogo.repository.CatalogoRepository;
import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CatalogoControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CatalogoRepository catalogoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void limpiar() {
        catalogoRepository.deleteAll();
    }

    private Catalogo catalogo(String nombre) {
        Catalogo c = new Catalogo();
        c.setNombreCatalogo(nombre);
        c.setFechaActualizacion(LocalDate.of(2026, 1, 1));
        return c;
    }

    @Test
    void testCrearYListar() throws Exception {
        Catalogo nuevo = catalogo("Catalogo Verano");
        mockMvc.perform(post("/api/v1/catalogos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idCatalogo").exists());
        mockMvc.perform(get("/api/v1/catalogos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombreCatalogo").value("Catalogo Verano"));
    }

    @Test
    void testObtenerPorId() throws Exception {
        Catalogo guardado = catalogoRepository.save(catalogo("Catalogo Invierno"));
        mockMvc.perform(get("/api/v1/catalogos/" + guardado.getIdCatalogo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreCatalogo").value("Catalogo Invierno"));
    }

    @Test
    void testActualizar() throws Exception {
        Catalogo guardado = catalogoRepository.save(catalogo("Original"));
        Catalogo datos = catalogo("Modificado");
        mockMvc.perform(put("/api/v1/catalogos/" + guardado.getIdCatalogo())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datos)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreCatalogo").value("Modificado"));
    }

    @Test
    void testEliminar() throws Exception {
        Catalogo guardado = catalogoRepository.save(catalogo("Borrar"));
        mockMvc.perform(delete("/api/v1/catalogos/" + guardado.getIdCatalogo()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/catalogos/" + guardado.getIdCatalogo()))
                .andExpect(status().isNoContent());
    }
}