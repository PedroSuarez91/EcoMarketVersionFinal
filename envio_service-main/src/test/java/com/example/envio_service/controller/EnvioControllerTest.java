package com.example.envio_service.controller;

import tools.jackson.databind.json.JsonMapper;

import com.example.envio_service.model.Envio;
import com.example.envio_service.model.EstadoEnvio;
import com.example.envio_service.service.EnvioService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
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

@WebMvcTest(EnvioController.class)
@ActiveProfiles("test")
public class EnvioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EnvioService envioService;

    private JsonMapper objectMapper = JsonMapper.builder().build();

    @Test
    void testCrearEnvio() throws Exception {
        Envio envio = new Envio(null, 7L, null, null, null, null);
        Envio guardado = new Envio(1L, 7L, "Calle 1", EstadoEnvio.PREPARANDO, LocalDate.now(), null);

        Mockito.when(envioService.crearEnvio(any(Envio.class))).thenReturn(guardado);

        mockMvc.perform(post("/api/v1/envios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(envio)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idEnvio").value(1L))
                .andExpect(jsonPath("$.estadoEnvio").value("PREPARANDO"));
    }

    @Test
    void testCrearEnvioPedidoNoEncontrado() throws Exception {
        Envio envio = new Envio(null, 7L, null, null, null, null);

        Mockito.when(envioService.crearEnvio(any(Envio.class))).thenReturn(null);

        mockMvc.perform(post("/api/v1/envios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(envio)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testListarEnvios() throws Exception {
        Envio e1 = new Envio(1L, 7L, "Calle 1", EstadoEnvio.PREPARANDO, LocalDate.now(), null);
        Mockito.when(envioService.listarEnvios()).thenReturn(List.of(e1));

        mockMvc.perform(get("/api/v1/envios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idPedido").value(7L));
    }

    @Test
    void testListarEnviosVacio() throws Exception {
        Mockito.when(envioService.listarEnvios()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/envios"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testFindById() throws Exception {
        Envio envio = new Envio(1L, 7L, "Calle 1", EstadoEnvio.PREPARANDO, LocalDate.now(), null);
        Mockito.when(envioService.findById(1L)).thenReturn(Optional.of(envio));

        mockMvc.perform(get("/api/v1/envios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idEnvio").value(1L));
    }

    @Test
    void testFindByIdNoExiste() throws Exception {
        Mockito.when(envioService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/envios/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testActualizarEstado() throws Exception {
        Envio actualizado = new Envio(1L, 7L, "Calle 1", EstadoEnvio.ENTREGADO, LocalDate.now(), LocalDate.now());
        Mockito.when(envioService.actualizarEstado(eq(1L), eq(EstadoEnvio.ENTREGADO))).thenReturn(actualizado);

        mockMvc.perform(put("/api/v1/envios/1/estado").param("estadoEnvio", "ENTREGADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoEnvio").value("ENTREGADO"));
    }

    @Test
    void testActualizarEstadoNoExiste() throws Exception {
        Mockito.when(envioService.actualizarEstado(eq(99L), any(EstadoEnvio.class))).thenReturn(null);

        mockMvc.perform(put("/api/v1/envios/99/estado").param("estadoEnvio", "ENTREGADO"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testEliminarEnvio() throws Exception {
        Mockito.when(envioService.eliminarEnvio(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/v1/envios/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testEliminarEnvioNoExiste() throws Exception {
        Mockito.when(envioService.eliminarEnvio(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/v1/envios/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCrearEnvioConflicto() throws Exception {
        Envio envio = new Envio(null, 7L, null, null, null, null);

        Mockito.when(envioService.crearEnvio(any(Envio.class)))
                .thenThrow(new RuntimeException("Error inesperado"));

        mockMvc.perform(post("/api/v1/envios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(envio)))
                .andExpect(status().isConflict());
    }
}