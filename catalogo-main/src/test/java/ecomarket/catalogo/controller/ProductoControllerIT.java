package ecomarket.catalogo.controller;

import ecomarket.catalogo.model.Producto;
import ecomarket.catalogo.repository.ProductoRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductoControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void limpiar() {
        productoRepository.deleteAll();
    }

    private Producto producto(String nombre, String marca, Integer precio) {
        Producto p = new Producto();
        p.setNombre(nombre);
        p.setMarca(marca);
        p.setPrecioUnitario(precio);
        p.setTipoProducto("ALIMENTO");
        p.setDescripcion("desc");
        p.setEstado(true);
        p.setIdInventario(1L);
        return p;
    }

    @Test
    void testCrearYListar() throws Exception {
        Producto nuevo = producto("Manzana", "Marca1", 500);
        mockMvc.perform(post("/api/v1/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idProducto").exists());
        mockMvc.perform(get("/api/v1/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Manzana"));
    }

    @Test
    void testObtenerPorId() throws Exception {
        Producto guardado = productoRepository.save(producto("Pera", "Marca2", 700));
        mockMvc.perform(get("/api/v1/productos/" + guardado.getIdProducto()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.marca").value("Marca2"));
    }

    @Test
    void testActualizar() throws Exception {
        Producto guardado = productoRepository.save(producto("Pera", "Marca2", 700));
        Producto datos = producto("Pera Premium", "Marca2", 950);
        mockMvc.perform(put("/api/v1/productos/" + guardado.getIdProducto())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datos)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Pera Premium"))
                .andExpect(jsonPath("$.precioUnitario").value(950));
    }

    @Test
    void testEliminar() throws Exception {
        Producto guardado = productoRepository.save(producto("Borrar", "MarcaX", 100));
        mockMvc.perform(delete("/api/v1/productos/" + guardado.getIdProducto()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/productos/" + guardado.getIdProducto()))
                .andExpect(status().isNoContent());
    }

    @Test
    void testBuscarPorNombre() throws Exception {
        productoRepository.save(producto("Manzana Verde", "Marca1", 500));
        mockMvc.perform(get("/api/v1/productos/buscar").param("nombre", "manzana"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Manzana Verde"));
    }

    @Test
    void testBuscarPorNombreSinResultados204() throws Exception {
        productoRepository.save(producto("Manzana", "Marca1", 500));
        mockMvc.perform(get("/api/v1/productos/buscar").param("nombre", "zzz"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testPorMarca() throws Exception {
        productoRepository.save(producto("Manzana", "Marca1", 500));
        mockMvc.perform(get("/api/v1/productos/marca/Marca1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].marca").value("Marca1"));
    }

    @Test
    void testRangoPrecio() throws Exception {
        productoRepository.save(producto("Barato", "M", 200));
        productoRepository.save(producto("Caro", "M", 900));
        mockMvc.perform(get("/api/v1/productos/precio/rango").param("min", "100").param("max", "500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Barato"));
    }

    @Test
    void testPrecioMaximoYMinimo() throws Exception {
        productoRepository.save(producto("Barato", "M", 200));
        productoRepository.save(producto("Caro", "M", 900));
        mockMvc.perform(get("/api/v1/productos/precio/maximo").param("max", "500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Barato"));
        mockMvc.perform(get("/api/v1/productos/precio/minimo").param("min", "800"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Caro"));
    }
}