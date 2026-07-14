package com.example.soporte_service.controller;

import com.example.soporte_service.model.Soporte;
import com.example.soporte_service.repository.SoporteRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SoporteControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SoporteRepository soporteRepository;

    @BeforeEach
    void cleanDb() {
        soporteRepository.deleteAll();
    }

    @Test
    void testListarYObtener() throws Exception {
        Soporte soporte = new Soporte(null, 5L, "No llega mi pedido", "Hace 5 dias", true);
        Soporte guardado = soporteRepository.save(soporte);

        mockMvc.perform(get("/api/v1/soporte"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].asunto").value("No llega mi pedido"));

        mockMvc.perform(get("/api/v1/soporte/" + guardado.getIdSoporte()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idSoporte").value(guardado.getIdSoporte()));
    }

    @Test
    void testCerrarTicket() throws Exception {
        Soporte soporte = new Soporte(null, 5L, "Asunto", "Desc", true);
        Soporte guardado = soporteRepository.save(soporte);

        mockMvc.perform(put("/api/v1/soporte/" + guardado.getIdSoporte() + "/cerrar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value(false));
    }
}