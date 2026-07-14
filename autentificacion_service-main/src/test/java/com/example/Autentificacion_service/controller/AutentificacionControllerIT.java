package com.example.Autentificacion_service.controller;

import com.example.Autentificacion_service.model.Autentificacion;
import com.example.Autentificacion_service.repository.AutentificacionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AutentificacionControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AutentificacionRepository autentificacionRepository;

    @BeforeEach
    void cleanDb() {
        autentificacionRepository.deleteAll();
    }

    @Test
    void testListarLoginsConDatos() throws Exception {
        Autentificacion auth = new Autentificacion(null, "juan@mail.com", "1234", LocalDateTime.now(), 5L);
        autentificacionRepository.save(auth);

        mockMvc.perform(get("/api/v1/autentificacion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].emailUsuario").value("juan@mail.com"));
    }

    @Test
    void testListarLoginsVacio() throws Exception {
        mockMvc.perform(get("/api/v1/autentificacion"))
                .andExpect(status().isNotFound());
    }
}