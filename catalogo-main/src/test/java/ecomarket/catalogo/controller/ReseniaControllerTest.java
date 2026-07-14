package ecomarket.catalogo.controller;

import ecomarket.catalogo.model.Producto;
import ecomarket.catalogo.model.Resenia;
import ecomarket.catalogo.service.ReseniaService;
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

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReseniaController.class)
@ActiveProfiles("test")
public class ReseniaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReseniaService reseniaService;

    @Autowired
    private ObjectMapper objectMapper;

    private Resenia resenia(Long id, String comentario, Integer calificacion) {
        Resenia r = new Resenia();
        r.setIdResenia(id);
        r.setComentario(comentario);
        r.setCalificacion(calificacion);
        r.setFechaResenia(LocalDate.now());
        return r;
    }

    private Resenia reseniaConProducto(Long idProducto) {
        Resenia r = resenia(null, "Buena", 5);
        Producto p = new Producto();
        p.setIdProducto(idProducto);
        r.setProducto(p);
        return r;
    }

    @Test
    void testGetReseniasConContenido() throws Exception {
        Mockito.when(reseniaService.listarResenias())
                .thenReturn(List.of(resenia(1L, "Buena", 5)));

        mockMvc.perform(get("/api/v1/resenias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].comentario").value("Buena"));
    }

    @Test
    void testGetReseniasVacio204() throws Exception {
        Mockito.when(reseniaService.listarResenias()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/resenias"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetPorProducto200() throws Exception {
        Mockito.when(reseniaService.listarPorProducto(1L))
                .thenReturn(List.of(resenia(1L, "Buena", 5)));

        mockMvc.perform(get("/api/v1/resenias/producto/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void testGetPorProducto204() throws Exception {
        Mockito.when(reseniaService.listarPorProducto(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/resenias/producto/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testPostReseniaOk201() throws Exception {
        Mockito.when(reseniaService.registrarResenia(any(Resenia.class)))
                .thenReturn(resenia(1L, "Buena", 5));

        mockMvc.perform(post("/api/v1/resenias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reseniaConProducto(1L))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idResenia").value(1L));
    }

    @Test
    void testPostReseniaProductoInexistente404() throws Exception {
        Mockito.when(reseniaService.registrarResenia(any(Resenia.class))).thenReturn(null);

        mockMvc.perform(post("/api/v1/resenias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reseniaConProducto(99L))))
                .andExpect(status().isNotFound());
    }

    @Test
    void testPutResenia200() throws Exception {
        Mockito.when(reseniaService.actualizarResenia(eq(1L), any(Resenia.class)))
                .thenReturn(resenia(1L, "Editada", 4));

        mockMvc.perform(put("/api/v1/resenias/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resenia(null, "Editada", 4))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comentario").value("Editada"));
    }

    @Test
    void testPutResenia404() throws Exception {
        Mockito.when(reseniaService.actualizarResenia(eq(99L), any(Resenia.class))).thenReturn(null);

        mockMvc.perform(put("/api/v1/resenias/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resenia(null, "X", 1))))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteResenia204() throws Exception {
        Mockito.doNothing().when(reseniaService).eliminarResenia(1L);

        mockMvc.perform(delete("/api/v1/resenias/1"))
                .andExpect(status().isNoContent());
    }
}