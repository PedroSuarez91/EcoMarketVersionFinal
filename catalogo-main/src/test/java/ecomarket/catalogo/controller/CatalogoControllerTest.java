package ecomarket.catalogo.controller;

import ecomarket.catalogo.model.Catalogo;
import ecomarket.catalogo.service.CatalogoService;
import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CatalogoController.class)
@ActiveProfiles("test")
public class CatalogoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CatalogoService catalogoService;

    @Autowired
    private ObjectMapper objectMapper;

    private Catalogo catalogo(Long id, String nombre) {
        Catalogo c = new Catalogo();
        c.setIdCatalogo(id);
        c.setNombreCatalogo(nombre);
        c.setFechaActualizacion(LocalDate.of(2026, 1, 1));
        return c;
    }

    @Test
    void testGetCatalogosConContenido() throws Exception {
        Mockito.when(catalogoService.listarCatalogo())
                .thenReturn(List.of(catalogo(1L, "Catalogo Verano")));
        mockMvc.perform(get("/api/v1/catalogos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombreCatalogo").value("Catalogo Verano"));
    }

    @Test
    void testGetCatalogosVacio204() throws Exception {
        Mockito.when(catalogoService.listarCatalogo()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/v1/catalogos"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetCatalogoExistente() throws Exception {
        Mockito.when(catalogoService.findById(1L))
                .thenReturn(Optional.of(catalogo(1L, "Catalogo Verano")));
        mockMvc.perform(get("/api/v1/catalogos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreCatalogo").value("Catalogo Verano"));
    }

    @Test
    void testGetCatalogoInexistente204() throws Exception {
        Mockito.when(catalogoService.findById(99L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/v1/catalogos/99"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testPostCatalogo201() throws Exception {
        Catalogo nuevo = catalogo(null, "Catalogo Verano");
        Mockito.when(catalogoService.crearCatalogo(any(Catalogo.class)))
                .thenReturn(catalogo(1L, "Catalogo Verano"));
        mockMvc.perform(post("/api/v1/catalogos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idCatalogo").value(1L));
    }

    @Test
    void testPostCatalogoConflicto409() throws Exception {
        Catalogo nuevo = catalogo(null, "Catalogo Verano");
        Mockito.when(catalogoService.crearCatalogo(any(Catalogo.class)))
                .thenThrow(new RuntimeException("error"));
        mockMvc.perform(post("/api/v1/catalogos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevo)))
                .andExpect(status().isConflict());
    }

    @Test
    void testPutCatalogo200() throws Exception {
        Mockito.when(catalogoService.actualizarCatalogo(eq(1L), any(Catalogo.class)))
                .thenReturn(catalogo(1L, "Catalogo Actualizado"));
        mockMvc.perform(put("/api/v1/catalogos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(catalogo(null, "Catalogo Actualizado"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreCatalogo").value("Catalogo Actualizado"));
    }

    @Test
    void testPutCatalogo404() throws Exception {
        Mockito.when(catalogoService.actualizarCatalogo(eq(99L), any(Catalogo.class))).thenReturn(null);

        mockMvc.perform(put("/api/v1/catalogos/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(catalogo(null, "X"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteCatalogo204() throws Exception {
        Mockito.doNothing().when(catalogoService).eliminarCatalogo(1L);
        mockMvc.perform(delete("/api/v1/catalogos/1"))
                .andExpect(status().isNoContent());
    }
}