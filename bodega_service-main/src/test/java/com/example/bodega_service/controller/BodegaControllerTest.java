package com.example.bodega_service.controller;

import tools.jackson.databind.json.JsonMapper;

import com.example.bodega_service.model.Bodega;
import com.example.bodega_service.model.BodegaDTO;
import com.example.bodega_service.service.BodegaService;

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
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BodegaController.class)
@ActiveProfiles("test")
public class BodegaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BodegaService bodegaService;

    private JsonMapper objectMapper = JsonMapper.builder().build();


    @Test
    void testCrearBodega() throws Exception {
        Bodega bodega = new Bodega(null, "Central", 1000, true, 10L, 20L);
        Bodega guardada = new Bodega(1L, "Central", 1000, true, 10L, 20L);

        Mockito.when(bodegaService.crearBodega(any(Bodega.class))).thenReturn(guardada);

        mockMvc.perform(post("/api/v1/bodega")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bodega)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idBodega").value(1L))
                .andExpect(jsonPath("$.nombreBodega").value("Central"));
    }

    @Test
    void testCrearBodegaConflicto() throws Exception {
        Bodega bodega = new Bodega(null, "Central", 1000, true, 10L, 20L);

        Mockito.when(bodegaService.crearBodega(any(Bodega.class)))
                .thenThrow(new RuntimeException("Error en BD"));

        mockMvc.perform(post("/api/v1/bodega")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bodega)))
                .andExpect(status().isConflict());
    }


    @Test
    void testListarBodegas() throws Exception {
        Bodega b1 = new Bodega(1L, "Central", 1000, true, 10L, 20L);
        Bodega b2 = new Bodega(2L, "Norte", 500, true, 11L, 21L);

        Mockito.when(bodegaService.listarBodegas()).thenReturn(Arrays.asList(b1, b2));

        mockMvc.perform(get("/api/v1/bodega"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nombreBodega", is("Central")));
    }

    @Test
    void testListarBodegasVacio() throws Exception {
        Mockito.when(bodegaService.listarBodegas()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/bodega"))
                .andExpect(status().isNotFound());
    }


    @Test
    void testObtenerBodega() throws Exception {
        Bodega bodega = new Bodega(1L, "Central", 1000, true, 10L, 20L);
        Mockito.when(bodegaService.findById(1L)).thenReturn(Optional.of(bodega));

        mockMvc.perform(get("/api/v1/bodega/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idBodega").value(1L));
    }

    @Test
    void testObtenerBodegaNoExiste() throws Exception {
        Mockito.when(bodegaService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/bodega/99"))
                .andExpect(status().isNotFound());
    }


    @Test
    void testObtenerDetalle() throws Exception {
        BodegaDTO dto = new BodegaDTO();
        dto.setIdBodega(1L);
        dto.setNombreBodega("Central");
        dto.setDireccionSucursal("Av Principal 123");
        dto.setCapacidadOcupada(50);

        Mockito.when(bodegaService.obtenerBodegaDTO(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/bodega/1/detalle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.direccionSucursal").value("Av Principal 123"))
                .andExpect(jsonPath("$.capacidadOcupada").value(50));
    }

    @Test
    void testObtenerDetalleNoExiste() throws Exception {
        Mockito.when(bodegaService.obtenerBodegaDTO(99L)).thenReturn(null);

        mockMvc.perform(get("/api/v1/bodega/99/detalle"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testEliminarBodega() throws Exception {
        Mockito.when(bodegaService.eliminarBodega(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/v1/bodega/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testEliminarBodegaNoExiste() throws Exception {
        Mockito.when(bodegaService.eliminarBodega(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/v1/bodega/99"))
                .andExpect(status().isNotFound());
    }


    @Test
    void testListarConCapacidad() throws Exception {
        BodegaDTO dto = new BodegaDTO();
        dto.setIdBodega(1L);
        dto.setNombreBodega("Central");
        dto.setCapacidadOcupada(50);

        Mockito.when(bodegaService.listarBodegasDTO()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/bodega/listar_con_capacidad"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].capacidadOcupada").value(50));
    }

    @Test
    void testListarConCapacidadVacio() throws Exception {
        Mockito.when(bodegaService.listarBodegasDTO()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/bodega/listar_con_capacidad"))
                .andExpect(status().isNotFound());
    }
}