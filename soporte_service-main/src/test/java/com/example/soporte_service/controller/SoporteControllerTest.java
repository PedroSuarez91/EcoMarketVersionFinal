package com.example.soporte_service.controller;

import tools.jackson.databind.json.JsonMapper;

import com.example.soporte_service.model.Soporte;
import com.example.soporte_service.service.SoporteService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SoporteController.class)
@ActiveProfiles("test")
public class SoporteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SoporteService soporteService;

    private JsonMapper objectMapper = JsonMapper.builder().build();

    @Test
    void testCrearTicket() throws Exception {
        Soporte soporte = new Soporte(null, 5L, "No llega mi pedido", "Hace 5 dias", false);
        Soporte guardado = new Soporte(1L, 5L, "No llega mi pedido", "Hace 5 dias", true);

        Mockito.when(soporteService.crearTicket(any(Soporte.class))).thenReturn(guardado);

        mockMvc.perform(post("/api/v1/soporte")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(soporte)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idSoporte").value(1L))
                .andExpect(jsonPath("$.estado").value(true));
    }

    @Test
    void testCrearTicketUsuarioNoEncontrado() throws Exception {
        Soporte soporte = new Soporte(null, 5L, "Asunto", "Descripcion", false);

        Mockito.when(soporteService.crearTicket(any(Soporte.class))).thenReturn(null);

        mockMvc.perform(post("/api/v1/soporte")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(soporte)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCrearTicketConflicto() throws Exception {
        Soporte soporte = new Soporte(null, 5L, "Asunto", "Descripcion", false);

        Mockito.when(soporteService.crearTicket(any(Soporte.class)))
                .thenThrow(new RuntimeException("Servicio de Usuario no disponible, intente mas tarde"));

        mockMvc.perform(post("/api/v1/soporte")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(soporte)))
                .andExpect(status().isConflict());
    }

    @Test
    void testListarSoportes() throws Exception {
        Soporte s1 = new Soporte(1L, 5L, "Asunto 1", "Desc 1", true);
        Soporte s2 = new Soporte(2L, 6L, "Asunto 2", "Desc 2", false);

        Mockito.when(soporteService.listarSoportes()).thenReturn(Arrays.asList(s1, s2));

        mockMvc.perform(get("/api/v1/soporte"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void testListarSoportesVacio() throws Exception {
        Mockito.when(soporteService.listarSoportes()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/soporte"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testObtenerSoporte() throws Exception {
        Soporte soporte = new Soporte(1L, 5L, "Asunto", "Desc", true);
        Mockito.when(soporteService.findById(1L)).thenReturn(soporte);

        mockMvc.perform(get("/api/v1/soporte/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.asunto").value("Asunto"));
    }

    @Test
    void testObtenerSoporteNoExiste() throws Exception {
        Mockito.when(soporteService.findById(99L)).thenReturn(null);

        mockMvc.perform(get("/api/v1/soporte/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCerrarTicket() throws Exception {
        Soporte cerrado = new Soporte(1L, 5L, "Asunto", "Desc", false);
        Mockito.when(soporteService.cerrarTicket(1L)).thenReturn(cerrado);

        mockMvc.perform(put("/api/v1/soporte/1/cerrar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value(false));
    }

    @Test
    void testCerrarTicketNoExiste() throws Exception {
        Mockito.when(soporteService.cerrarTicket(99L)).thenReturn(null);

        mockMvc.perform(put("/api/v1/soporte/99/cerrar"))
                .andExpect(status().isNotFound());
    }
}