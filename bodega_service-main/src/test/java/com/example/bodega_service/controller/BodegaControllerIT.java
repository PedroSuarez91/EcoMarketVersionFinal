package com.example.bodega_service.controller;

import tools.jackson.databind.json.JsonMapper;

import com.example.bodega_service.model.Bodega;
import com.example.bodega_service.repository.BodegaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BodegaControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BodegaRepository bodegaRepository;

    private JsonMapper objectMapper = JsonMapper.builder().build();

    @BeforeEach
    void cleanDb() {
        bodegaRepository.deleteAll();
    }

    @Test
    void testCrearYListarBodega() throws Exception {
        Bodega bodega = new Bodega(null, "Central", 1000, true, 10L, 20L);

        mockMvc.perform(post("/api/v1/bodega")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bodega)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idBodega").exists())
                .andExpect(jsonPath("$.nombreBodega").value("Central"));

        mockMvc.perform(get("/api/v1/bodega"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombreBodega").value("Central"));
    }

    @Test
    void testEliminarBodega() throws Exception {
        Bodega bodega = new Bodega(null, "Norte", 500, true, 11L, 21L);
        Bodega guardada = bodegaRepository.save(bodega);

        mockMvc.perform(delete("/api/v1/bodega/" + guardada.getIdBodega()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/bodega/" + guardada.getIdBodega()))
                .andExpect(status().isNotFound());
    }
}