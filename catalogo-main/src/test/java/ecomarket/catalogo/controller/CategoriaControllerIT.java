package ecomarket.catalogo.controller;


import ecomarket.catalogo.model.Categoria;
import ecomarket.catalogo.repository.CategoriaRepository;
import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CategoriaControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void limpiar() {
        categoriaRepository.deleteAll();
    }

    private Categoria categoria(String nombre, String tipo) {
        Categoria c = new Categoria();
        c.setNombreCategoria(nombre);
        c.setTipoProducto(tipo);
        return c;
    }

    @Test
    void testCrearYListar() throws Exception {
        Categoria nueva = categoria("Frutas", "ALIMENTO");
        mockMvc.perform(post("/api/v1/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nueva)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idCategoria").exists());
        mockMvc.perform(get("/api/v1/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombreCategoria").value("Frutas"));
    }

    @Test
    void testObtenerPorId() throws Exception {
        Categoria guardada = categoriaRepository.save(categoria("Lacteos", "ALIMENTO"));
        mockMvc.perform(get("/api/v1/categorias/" + guardada.getIdCategoria()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreCategoria").value("Lacteos"));
    }

    @Test
    void testEliminar() throws Exception {
        Categoria guardada = categoriaRepository.save(categoria("Borrar", "X"));
        mockMvc.perform(delete("/api/v1/categorias/" + guardada.getIdCategoria()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/categorias/" + guardada.getIdCategoria()))
                .andExpect(status().isNoContent());
    }
}