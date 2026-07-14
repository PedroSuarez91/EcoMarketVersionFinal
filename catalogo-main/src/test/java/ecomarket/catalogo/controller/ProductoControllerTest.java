package ecomarket.catalogo.controller;


import ecomarket.catalogo.model.Producto;
import ecomarket.catalogo.service.ProductoService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductoController.class)
@ActiveProfiles("test")
public class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private ProductoService productoService;

    @Autowired
    private ObjectMapper objectMapper;

    private Producto producto(Long id, String nombre, String marca, Integer precio) {
        Producto p = new Producto();
        p.setIdProducto(id);
        p.setNombre(nombre);
        p.setMarca(marca);
        p.setPrecioUnitario(precio);
        p.setEstado(true);
        return p;
    }

    @Test
    void testGetProductosConContenido() throws Exception {
        Mockito.when(productoService.listarProductos())
                .thenReturn(List.of(producto(1L, "Manzana", "Marca1", 500)));
        mockMvc.perform(get("/api/v1/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre").value("Manzana"));
    }

    @Test
    void testGetProductosVacio204() throws Exception {
        Mockito.when(productoService.listarProductos()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/v1/productos"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetProductoExistente() throws Exception {
        Mockito.when(productoService.findByIdProducto(1L))
                .thenReturn(Optional.of(producto(1L, "Manzana", "Marca1", 500)));
        mockMvc.perform(get("/api/v1/productos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Manzana"));
    }

    @Test
    void testGetProductoInexistente204() throws Exception {
        Mockito.when(productoService.findByIdProducto(99L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/v1/productos/99"))
                .andExpect(status().isNoContent());
    }

        @Test
        void testPostProducto201() throws Exception {
        Mockito.when(productoService.registrarProducto(any(Producto.class)))
                .thenReturn(producto(1L, "Manzana", "Marca1", 500));
                String json = """
                        {
                        "idInventario": 1,
                        "tipoProducto": "ALIMENTO",
                        "nombre": "Manzana",
                        "marca": "Marca1",
                        "descripcion": "desc",
                        "precioUnitario": 500,
                        "estado": true
                        }
                        """;
        mockMvc.perform(post("/api/v1/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idProducto").value(1L));
}
        @Test
        void testPostProductoConflicto409() throws Exception {
        Mockito.when(productoService.registrarProducto(any(Producto.class)))
                .thenThrow(new RuntimeException("error"));
        String json = """
                {
                "idInventario": 1,
                "tipoProducto": "ALIMENTO",
                "nombre": "Manzana",
                "marca": "Marca1",
                "descripcion": "desc",
                "precioUnitario": 500,
                "estado": true
                }
                """;
        mockMvc.perform(post("/api/v1/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict());
        }

    @Test
    void testPutProducto200() throws Exception {
        Mockito.when(productoService.actualizarProducto(eq(1L), any(Producto.class)))
                .thenReturn(producto(1L, "Nuevo", "M2", 600));

        mockMvc.perform(put("/api/v1/productos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(producto(null, "Nuevo", "M2", 600))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nuevo"));
    }

    @Test
    void testPutProducto404() throws Exception {
        Mockito.when(productoService.actualizarProducto(eq(99L), any(Producto.class))).thenReturn(null);

        mockMvc.perform(put("/api/v1/productos/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(producto(null, "X", "Y", 1))))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetPorCategoria200() throws Exception {
        Mockito.when(productoService.findByCategoria(3L))
                .thenReturn(List.of(producto(1L, "Manzana", "Marca1", 500)));

        mockMvc.perform(get("/api/v1/productos/categoria/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void testGetPorCategoria204() throws Exception {
        Mockito.when(productoService.findByCategoria(3L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/productos/categoria/3"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testBuscarPorNombre200() throws Exception {
        Mockito.when(productoService.buscarPorNombre("man"))
                .thenReturn(List.of(producto(1L, "Manzana", "Marca1", 500)));

        mockMvc.perform(get("/api/v1/productos/buscar").param("nombre", "man"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void testBuscarPorNombre204() throws Exception {
        Mockito.when(productoService.buscarPorNombre("zzz")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/productos/buscar").param("nombre", "zzz"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetPorMarca200() throws Exception {
        Mockito.when(productoService.findByMarca("Marca1"))
                .thenReturn(List.of(producto(1L, "Manzana", "Marca1", 500)));

        mockMvc.perform(get("/api/v1/productos/marca/Marca1"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetPorMarca204() throws Exception {
        Mockito.when(productoService.findByMarca("Nada")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/productos/marca/Nada"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetPorRangoPrecio200() throws Exception {
        Mockito.when(productoService.findByRangoPrecio(100, 500))
                .thenReturn(List.of(producto(1L, "Manzana", "Marca1", 300)));

        mockMvc.perform(get("/api/v1/productos/precio/rango").param("min", "100").param("max", "500"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetPorRangoPrecio204() throws Exception {
        Mockito.when(productoService.findByRangoPrecio(100, 500)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/productos/precio/rango").param("min", "100").param("max", "500"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetPorPrecioMaximo200() throws Exception {
        Mockito.when(productoService.findByPrecioMaximo(500))
                .thenReturn(List.of(producto(1L, "Manzana", "Marca1", 300)));

        mockMvc.perform(get("/api/v1/productos/precio/maximo").param("max", "500"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetPorPrecioMaximo204() throws Exception {
        Mockito.when(productoService.findByPrecioMaximo(500)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/productos/precio/maximo").param("max", "500"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetPorPrecioMinimo200() throws Exception {
        Mockito.when(productoService.findByPrecioMinimo(100))
                .thenReturn(List.of(producto(1L, "Manzana", "Marca1", 300)));

        mockMvc.perform(get("/api/v1/productos/precio/minimo").param("min", "100"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetPorPrecioMinimo204() throws Exception {
        Mockito.when(productoService.findByPrecioMinimo(100)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/productos/precio/minimo").param("min", "100"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testEliminarProducto204() throws Exception {
        Mockito.when(productoService.eliminarProducto(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/v1/productos/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testEliminarProducto404() throws Exception {
        Mockito.when(productoService.eliminarProducto(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/v1/productos/99"))
                .andExpect(status().isNotFound());
    }
}