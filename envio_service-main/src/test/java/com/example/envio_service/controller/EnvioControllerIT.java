package com.example.envio_service.controller;

import tools.jackson.databind.json.JsonMapper;

import com.example.envio_service.model.Envio;
import com.example.envio_service.model.EstadoEnvio;
import com.example.envio_service.model.RutaEntrega;
import com.example.envio_service.repository.EnvioRepository;
import com.example.envio_service.repository.RutaEntregaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EnvioControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EnvioRepository envioRepository;

    @Autowired
    private RutaEntregaRepository rutaEntregaRepository;

    private JsonMapper objectMapper = JsonMapper.builder().build();

    @BeforeEach
    void cleanDb() {
        envioRepository.deleteAll();
        rutaEntregaRepository.deleteAll();
    }

    @Test
    void testListarYActualizarEstadoEnvio() throws Exception {
        Envio envio = new Envio(null, 7L, "Calle 1", EstadoEnvio.PREPARANDO, LocalDate.now(), null);
        Envio guardado = envioRepository.save(envio);

        mockMvc.perform(get("/api/v1/envios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idPedido").value(7L));

        mockMvc.perform(put("/api/v1/envios/" + guardado.getIdEnvio() + "/estado")
                .param("estadoEnvio", "ENTREGADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoEnvio").value("ENTREGADO"));
    }

    @Test
    void testEliminarEnvio() throws Exception {
        Envio guardado = envioRepository.save(
                new Envio(null, 7L, "Calle 1", EstadoEnvio.PREPARANDO, LocalDate.now(), null));

        mockMvc.perform(delete("/api/v1/envios/" + guardado.getIdEnvio()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/envios/" + guardado.getIdEnvio()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCrearYListarRuta() throws Exception {
        RutaEntrega ruta = new RutaEntrega(null, "Concepcion", List.of(1L, 2L));

        mockMvc.perform(post("/api/v1/rutas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ruta)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idRuta").exists())
                .andExpect(jsonPath("$.comunaDestino").value("Concepcion"));

        mockMvc.perform(get("/api/v1/rutas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].comunaDestino").value("Concepcion"));
    }

    @Test
    void testEliminarRuta() throws Exception {
        RutaEntrega guardada = rutaEntregaRepository.save(
                new RutaEntrega(null, "Talcahuano", List.of(1L)));

        mockMvc.perform(delete("/api/v1/rutas/" + guardada.getIdRuta()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/rutas/" + guardada.getIdRuta()))
                .andExpect(status().isNotFound());
    }
}