package ecomarket.catalogo.controller;

import ecomarket.catalogo.model.Categoria;
import ecomarket.catalogo.service.CategoriaService;
import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoriaController.class)
@ActiveProfiles("test")
public class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoriaService categoriaService;

    @Autowired
    private ObjectMapper objectMapper;

    private Categoria categoria(Long id, String nombre, String tipo) {
        Categoria c = new Categoria();
        c.setIdCategoria(id);
        c.setNombreCategoria(nombre);
        c.setTipoProducto(tipo);
        return c;
    }

    @Test
    void testGetCategoriasConContenido() throws Exception {
        Mockito.when(categoriaService.listarCategorias())
                .thenReturn(List.of(categoria(1L, "Frutas", "ALIMENTO")));
        mockMvc.perform(get("/api/v1/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombreCategoria").value("Frutas"));
    }

    @Test
    void testGetCategoriasVacio204() throws Exception {
        Mockito.when(categoriaService.listarCategorias()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/v1/categorias"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetCategoriaExistente() throws Exception {
        Mockito.when(categoriaService.findById(1L))
                .thenReturn(Optional.of(categoria(1L, "Frutas", "ALIMENTO")));
        mockMvc.perform(get("/api/v1/categorias/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreCategoria").value("Frutas"));
    }

    @Test
    void testGetCategoriaInexistente204() throws Exception {
        Mockito.when(categoriaService.findById(99L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/v1/categorias/99"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testPostCategoria201() throws Exception {
        Categoria nueva = categoria(null, "Frutas", "ALIMENTO");
        Mockito.when(categoriaService.crearCategoria(any(Categoria.class)))
                .thenReturn(categoria(1L, "Frutas", "ALIMENTO"));
        mockMvc.perform(post("/api/v1/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nueva)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idCategoria").value(1L));
    }

    @Test
    void testPostCategoriaConflicto409() throws Exception {
        Categoria nueva = categoria(null, "Frutas", "ALIMENTO");
        Mockito.when(categoriaService.crearCategoria(any(Categoria.class)))
                .thenThrow(new RuntimeException("error"));
        mockMvc.perform(post("/api/v1/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nueva)))
                .andExpect(status().isConflict());
    }

    @Test
    void testDeleteCategoria204() throws Exception {
        Mockito.doNothing().when(categoriaService).eliminarCategoria(1L);
        mockMvc.perform(delete("/api/v1/categorias/1"))
                .andExpect(status().isNoContent());
    }
}