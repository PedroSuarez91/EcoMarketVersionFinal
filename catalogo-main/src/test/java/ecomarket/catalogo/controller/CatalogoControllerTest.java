package ecomarket.catalogo.controller;

import ecomarket.catalogo.model.Catalogo;
import ecomarket.catalogo.model.Producto;
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

    private Producto producto(Long id, String nombre) {
        Producto p = new Producto();
        p.setIdProducto(id);
        p.setNombre(nombre);
        p.setMarca("EcoMarket");
        p.setTipoProducto("Fruta");
        p.setDescripcion("Producto de prueba");
        p.setPrecioUnitario(1000);
        p.setEstado(true);
        return p;
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

    // ---------- PUT /api/v1/catalogos/{id}/productos/{idProducto} ----------

    @Test
    void testPutProductoExistenteEnCatalogo200() throws Exception {
        Catalogo conProducto = catalogo(1L, "Catalogo Verano");
        conProducto.getProductos().add(producto(10L, "Manzana"));
        Mockito.when(catalogoService.agregarProductoExistente(1L, 10L)).thenReturn(conProducto);

        mockMvc.perform(put("/api/v1/catalogos/1/productos/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCatalogo").value(1L))
                .andExpect(jsonPath("$.productos", hasSize(1)))
                .andExpect(jsonPath("$.productos[0].idProducto").value(10L));
    }

    @Test
    void testPutProductoExistenteNoEncontrado404() throws Exception {
        Mockito.when(catalogoService.agregarProductoExistente(99L, 10L)).thenReturn(null);

        mockMvc.perform(put("/api/v1/catalogos/99/productos/10"))
                .andExpect(status().isNotFound());
    }
}