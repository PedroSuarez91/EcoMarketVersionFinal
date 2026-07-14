package com.example.inventario_service.controller;

import com.example.inventario_service.model.Inventario;
import com.example.inventario_service.repository.InventarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InventarioControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InventarioRepository inventarioRepository;

    @BeforeEach
    void cleanDb() {
        inventarioRepository.deleteAll();
    }

    @Test
    void testListarYStockPorBodega() throws Exception {
        inventarioRepository.save(new Inventario(null, 100L, 5L, "Manzana", 20, 5, LocalDate.now()));
        inventarioRepository.save(new Inventario(null, 101L, 5L, "Pera", 30, 5, LocalDate.now()));

        mockMvc.perform(get("/api/v1/inventario"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Manzana"));

        mockMvc.perform(get("/api/v1/inventario/stockPorBodega/5"))
                .andExpect(status().isOk())
                .andExpect(content().string("Total de productos: 50"));
    }

    @Test
    void testDescontarStock() throws Exception {
        inventarioRepository.save(new Inventario(null, 100L, 5L, "Manzana", 20, 5, LocalDate.now()));

        mockMvc.perform(put("/api/v1/inventario/descontar/100/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockDisponible").value(15));
    }
}