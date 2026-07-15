package ecomarket.catalogo.controller;

import ecomarket.catalogo.model.Catalogo;
import ecomarket.catalogo.model.Producto;
import ecomarket.catalogo.repository.CatalogoRepository;
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

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
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
    private ProductoRepository productoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void limpiar() {
        productoRepository.deleteAll();
        catalogoRepository.deleteAll();
    }

    private Catalogo catalogo(String nombre) {
        Catalogo c = new Catalogo();
        c.setNombreCatalogo(nombre);
        c.setFechaActualizacion(LocalDate.of(2026, 1, 1));
        return c;
    }

    private Producto producto(String nombre) {
        Producto p = new Producto();
        p.setNombre(nombre);
        p.setMarca("EcoMarket");
        p.setTipoProducto("ALIMENTO");
        p.setDescripcion("Producto de prueba");
        p.setPrecioUnitario(1000);
        p.setEstado(true);
        p.setIdInventario(1L);
        return p;
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

    // ---------- PUT /api/v1/catalogos/{id}/productos/{idProducto} ----------

    @Test
    void testAgregarProductoExistenteACatalogo() throws Exception {
        Catalogo cat = catalogoRepository.save(catalogo("Catalogo Verano"));
        Producto prod = productoRepository.save(producto("Manzana"));

        mockMvc.perform(put("/api/v1/catalogos/" + cat.getIdCatalogo()
                        + "/productos/" + prod.getIdProducto()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCatalogo").value(cat.getIdCatalogo()))
                .andExpect(jsonPath("$.productos", hasSize(1)))
                .andExpect(jsonPath("$.productos[0].nombre").value("Manzana"));

        Producto enBd = productoRepository.findById(prod.getIdProducto()).orElseThrow();
        assertThat(enBd.getCatalogo().getIdCatalogo()).isEqualTo(cat.getIdCatalogo());
    }

    @Test
    void testAgregarProductoSeVeAlConsultarCatalogo() throws Exception {
        Catalogo cat = catalogoRepository.save(catalogo("Catalogo Invierno"));
        Producto prod = productoRepository.save(producto("Naranja"));

        mockMvc.perform(put("/api/v1/catalogos/" + cat.getIdCatalogo()
                        + "/productos/" + prod.getIdProducto()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/catalogos/" + cat.getIdCatalogo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productos", hasSize(1)))
                .andExpect(jsonPath("$.productos[0].idProducto").value(prod.getIdProducto()));
    }

    @Test
    void testAgregarProductoCatalogoInexistente404() throws Exception {
        Producto prod = productoRepository.save(producto("Manzana"));

        mockMvc.perform(put("/api/v1/catalogos/9999/productos/" + prod.getIdProducto()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testAgregarProductoInexistente404() throws Exception {
        Catalogo cat = catalogoRepository.save(catalogo("Catalogo Verano"));

        mockMvc.perform(put("/api/v1/catalogos/" + cat.getIdCatalogo() + "/productos/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testAgregarProductoDosVecesNoDuplica() throws Exception {
        Catalogo cat = catalogoRepository.save(catalogo("Catalogo Verano"));
        Producto prod = productoRepository.save(producto("Manzana"));
        String url = "/api/v1/catalogos/" + cat.getIdCatalogo() + "/productos/" + prod.getIdProducto();

        mockMvc.perform(put(url)).andExpect(status().isOk());
        mockMvc.perform(put(url))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productos", hasSize(1)));
    }

    @Test
    void testMoverProductoDeCatalogo() throws Exception {
        Catalogo origen = catalogoRepository.save(catalogo("Origen"));
        Catalogo destino = catalogoRepository.save(catalogo("Destino"));
        Producto prod = productoRepository.save(producto("Manzana"));

        mockMvc.perform(put("/api/v1/catalogos/" + origen.getIdCatalogo()
                        + "/productos/" + prod.getIdProducto()))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/v1/catalogos/" + destino.getIdCatalogo()
                        + "/productos/" + prod.getIdProducto()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productos", hasSize(1)));

        Producto enBd = productoRepository.findById(prod.getIdProducto()).orElseThrow();
        assertThat(enBd.getCatalogo().getIdCatalogo()).isEqualTo(destino.getIdCatalogo());

        mockMvc.perform(get("/api/v1/catalogos/" + origen.getIdCatalogo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productos", hasSize(0)));
    }
}