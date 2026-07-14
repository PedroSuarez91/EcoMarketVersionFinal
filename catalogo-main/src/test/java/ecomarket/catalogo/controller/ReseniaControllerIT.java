package ecomarket.catalogo.controller;

import ecomarket.catalogo.model.Producto;
import ecomarket.catalogo.repository.ProductoRepository;
import ecomarket.catalogo.repository.ReseniaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReseniaControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReseniaRepository reseniaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @BeforeEach
    void limpiar() {
        reseniaRepository.deleteAll();
        productoRepository.deleteAll();
    }

    private Producto producto() {
        Producto p = new Producto();
        p.setNombre("Manzana");
        p.setMarca("Marca1");
        p.setPrecioUnitario(500);
        p.setTipoProducto("ALIMENTO");
        p.setDescripcion("Producto de prueba"); 
        p.setEstado(true);
        p.setIdInventario(1L);
        return p;
    }

    private String jsonResenia(Long idProducto) {
        return """
                {
                  "comentario": "Excelente",
                  "calificacion": 5,
                  "fechaResenia": "%s",
                  "producto": { "idProducto": %d }
                }
                """.formatted(LocalDate.now(), idProducto);
    }

    @Test
    void testCrearReseniaSobreProductoExistente201() throws Exception {
        Producto guardado = productoRepository.save(producto());
        mockMvc.perform(post("/api/v1/resenias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonResenia(guardado.getIdProducto())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idResenia").exists())
                .andExpect(jsonPath("$.comentario").value("Excelente"));
    }

    @Test
    void testCrearReseniaProductoInexistente404() throws Exception {
        mockMvc.perform(post("/api/v1/resenias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonResenia(999L)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testListarPorProducto() throws Exception {
        Producto guardado = productoRepository.save(producto());
        mockMvc.perform(post("/api/v1/resenias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonResenia(guardado.getIdProducto())))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/api/v1/resenias/producto/" + guardado.getIdProducto()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].calificacion").value(5));
    }
}