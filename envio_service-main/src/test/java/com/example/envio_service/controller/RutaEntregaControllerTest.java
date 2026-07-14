package com.example.envio_service.controller;

import tools.jackson.databind.json.JsonMapper;

import com.example.envio_service.model.RutaEntrega;
import com.example.envio_service.service.RutaEntregaService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RutaEntregaController.class)
@ActiveProfiles("test")
public class RutaEntregaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RutaEntregaService rutaEntregaService;

    private JsonMapper objectMapper = JsonMapper.builder().build();

    @Test
    void testCrearRuta() throws Exception {
        RutaEntrega ruta = new RutaEntrega(null, "Concepcion", List.of(1L, 2L));
        RutaEntrega guardada = new RutaEntrega(1L, "Concepcion", List.of(1L, 2L));

        Mockito.when(rutaEntregaService.crearRuta(any(RutaEntrega.class))).thenReturn(guardada);

        mockMvc.perform(post("/api/v1/rutas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ruta)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idRuta").value(1L))
                .andExpect(jsonPath("$.comunaDestino").value("Concepcion"));
    }

    @Test
    void testListarRutas() throws Exception {
        RutaEntrega r1 = new RutaEntrega(1L, "Concepcion", List.of(1L));
        Mockito.when(rutaEntregaService.listarRutas()).thenReturn(List.of(r1));

        mockMvc.perform(get("/api/v1/rutas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].comunaDestino").value("Concepcion"));
    }

    @Test
    void testListarRutasVacio() throws Exception {
        Mockito.when(rutaEntregaService.listarRutas()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/rutas"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testFindById() throws Exception {
        RutaEntrega ruta = new RutaEntrega(1L, "Concepcion", List.of(1L));
        Mockito.when(rutaEntregaService.findById(1L)).thenReturn(Optional.of(ruta));

        mockMvc.perform(get("/api/v1/rutas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idRuta").value(1L));
    }

    @Test
    void testFindByIdNoExiste() throws Exception {
        Mockito.when(rutaEntregaService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/rutas/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testActualizarRuta() throws Exception {
        RutaEntrega datos = new RutaEntrega(null, "Talcahuano", List.of(2L, 3L));
        RutaEntrega actualizada = new RutaEntrega(1L, "Talcahuano", List.of(2L, 3L));

        Mockito.when(rutaEntregaService.actualizarRuta(eq(1L), any(RutaEntrega.class))).thenReturn(actualizada);

        mockMvc.perform(put("/api/v1/rutas/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(datos)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comunaDestino").value("Talcahuano"));
    }

    @Test
    void testActualizarRutaNoExiste() throws Exception {
        RutaEntrega datos = new RutaEntrega(null, "Talcahuano", List.of(2L));

        Mockito.when(rutaEntregaService.actualizarRuta(eq(99L), any(RutaEntrega.class))).thenReturn(null);

        mockMvc.perform(put("/api/v1/rutas/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(datos)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testEliminarRuta() throws Exception {
        Mockito.when(rutaEntregaService.eliminarRuta(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/v1/rutas/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testEliminarRutaNoExiste() throws Exception {
        Mockito.when(rutaEntregaService.eliminarRuta(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/v1/rutas/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCrearRutaConflicto() throws Exception {
        RutaEntrega ruta = new RutaEntrega(null, "Concepcion", List.of(1L, 2L));

        Mockito.when(rutaEntregaService.crearRuta(any(RutaEntrega.class)))
                .thenThrow(new RuntimeException("Error en BD"));

        mockMvc.perform(post("/api/v1/rutas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ruta)))
                .andExpect(status().isConflict());
    }
}