package com.example.inventario_service.controller;

import tools.jackson.databind.json.JsonMapper;

import com.example.inventario_service.model.Inventario;
import com.example.inventario_service.service.InventarioService;

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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventarioController.class)
@ActiveProfiles("test")
public class InventarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InventarioService inventarioService;

    private JsonMapper objectMapper = JsonMapper.builder().build();
    @Test
    void testPostInventario() throws Exception {
        Inventario inventario = new Inventario(null, 100L, 5L, null, 20, 5, null);
        Inventario guardado = new Inventario(1L, 100L, 5L, "Manzana", 20, 5, LocalDate.now());

        Mockito.when(inventarioService.crearInventario(any(Inventario.class))).thenReturn(guardado);

        mockMvc.perform(post("/api/v1/inventario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inventario)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idInventario").value(1L))
                .andExpect(jsonPath("$.nombre").value("Manzana"));
    }

    @Test
    void testPostInventarioProductoNoEncontrado() throws Exception {
        Inventario inventario = new Inventario(null, 100L, 5L, null, 20, 5, null);

        Mockito.when(inventarioService.crearInventario(any(Inventario.class))).thenReturn(null);

        mockMvc.perform(post("/api/v1/inventario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inventario)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testPostInventarioConflicto() throws Exception {
        Inventario inventario = new Inventario(null, 100L, 5L, null, 20, 5, null);

        Mockito.when(inventarioService.crearInventario(any(Inventario.class)))
                .thenThrow(new RuntimeException("Servicio de Catalogo no disponible, intente mas tarde"));

        mockMvc.perform(post("/api/v1/inventario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inventario)))
                .andExpect(status().isConflict());
    }

    @Test
    void testGetInventarios() throws Exception {
        Inventario i1 = new Inventario(1L, 100L, 5L, "Manzana", 20, 5, LocalDate.now());
        Mockito.when(inventarioService.obtenerInventarios()).thenReturn(List.of(i1));

        mockMvc.perform(get("/api/v1/inventario"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Manzana"));
    }

    @Test
    void testGetInventariosError() throws Exception {
        Mockito.when(inventarioService.obtenerInventarios())
                .thenThrow(new RuntimeException("Error inesperado"));

        mockMvc.perform(get("/api/v1/inventario"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetStockPorBodega() throws Exception {
        Mockito.when(inventarioService.getStockPorBodega(5L)).thenReturn(50);

        mockMvc.perform(get("/api/v1/inventario/stockPorBodega/5"))
                .andExpect(status().isOk())
                .andExpect(content().string("Total de productos: 50"));
    }

    @Test
    void testActualizarInventario() throws Exception {
        Inventario datos = new Inventario(null, 100L, 5L, "Manzana", 40, 8, null);
        Inventario actualizado = new Inventario(1L, 100L, 5L, "Manzana", 40, 8, LocalDate.now());

        Mockito.when(inventarioService.actualizarInventario(eq(1L), any(Inventario.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/v1/inventario/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(datos)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockDisponible").value(40));
    }
    @Test
    void testActualizarInventarioNoExiste() throws Exception {
        Inventario datos = new Inventario(null, 100L, 5L, "Manzana", 40, 8, null);

        Mockito.when(inventarioService.actualizarInventario(eq(99L), any(Inventario.class))).thenReturn(null);

        mockMvc.perform(put("/api/v1/inventario/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(datos)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDescontarStock() throws Exception {
        Inventario actualizado = new Inventario(1L, 100L, 5L, "Manzana", 15, 5, LocalDate.now());
        Mockito.when(inventarioService.descontarStock(100L, 5)).thenReturn(actualizado);

        mockMvc.perform(put("/api/v1/inventario/descontar/100/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockDisponible").value(15));
    }

    @Test
    void testDescontarStockConflicto() throws Exception {
        Mockito.when(inventarioService.descontarStock(100L, 50)).thenReturn(null);

        mockMvc.perform(put("/api/v1/inventario/descontar/100/50"))
                .andExpect(status().isConflict());
    }

    @Test
    void testAlertasStockMinimoConDatos() throws Exception {
        Inventario i1 = new Inventario(1L, 100L, 5L, "Manzana", 2, 5, LocalDate.now());
        Mockito.when(inventarioService.alertasStockMinimo()).thenReturn(List.of(i1));

        mockMvc.perform(get("/api/v1/inventario/alertas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Manzana"));
    }

    @Test
    void testAlertasStockMinimoVacio() throws Exception {
        Mockito.when(inventarioService.alertasStockMinimo()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/inventario/alertas"))
                .andExpect(status().isOk())
                .andExpect(content().string("No hay productos bajo stock minimo"));
    }
}