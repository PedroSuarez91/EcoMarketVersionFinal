package com.example.soporte_service.service;

import com.example.soporte_service.model.Soporte;
import com.example.soporte_service.model.UsuarioDTO;
import com.example.soporte_service.repository.SoporteRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SoporteServiceTest {

    @Mock
    private SoporteRepository soporteRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private SoporteService soporteService;

    private UsuarioDTO crearUsuario() {
        UsuarioDTO u = new UsuarioDTO();
        u.setIdUsuario(5L);
        u.setNombre("Juan");
        u.setApellido("Perez");
        return u;
    }

    @Test
    void testCrearTicketExitoso() {
        Soporte soporte = new Soporte(null, 5L, "No llega mi pedido", "Hace 5 dias", false);

        when(restTemplate.getForObject(anyString(), eq(UsuarioDTO.class))).thenReturn(crearUsuario());
        when(soporteRepository.save(any(Soporte.class))).thenAnswer(inv -> inv.getArgument(0));

        Soporte resultado = soporteService.crearTicket(soporte);

        assertNotNull(resultado);
        assertTrue(resultado.isEstado());
        verify(soporteRepository, times(1)).save(any(Soporte.class));
    }

    @Test
    void testCrearTicketUsuarioNull() {
        Soporte soporte = new Soporte(null, 5L, "Asunto", "Descripcion", false);

        when(restTemplate.getForObject(anyString(), eq(UsuarioDTO.class))).thenReturn(null);

        Soporte resultado = soporteService.crearTicket(soporte);

        assertNull(resultado);
        verify(soporteRepository, never()).save(any(Soporte.class));
    }

    @Test
    void testCrearTicketUsuarioNotFound() {
        Soporte soporte = new Soporte(null, 5L, "Asunto", "Descripcion", false);

        HttpClientErrorException notFound = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, new byte[0], null);
        when(restTemplate.getForObject(anyString(), eq(UsuarioDTO.class))).thenThrow(notFound);

        Soporte resultado = soporteService.crearTicket(soporte);

        assertNull(resultado);
        verify(soporteRepository, never()).save(any(Soporte.class));
    }

    @Test
    void testCrearTicketServicioNoDisponible() {
        Soporte soporte = new Soporte(null, 5L, "Asunto", "Descripcion", false);

        when(restTemplate.getForObject(anyString(), eq(UsuarioDTO.class)))
                .thenThrow(new RuntimeException("Conexion rechazada"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> soporteService.crearTicket(soporte));
        assertEquals("Servicio de Usuario no disponible, intente mas tarde", ex.getMessage());
    }

    @Test
    void testCerrarTicketExistente() {
        Soporte existente = new Soporte(1L, 5L, "Asunto", "Descripcion", true);

        when(soporteRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(soporteRepository.save(any(Soporte.class))).thenAnswer(inv -> inv.getArgument(0));

        Soporte resultado = soporteService.cerrarTicket(1L);

        assertNotNull(resultado);
        assertFalse(resultado.isEstado());
    }

    @Test
    void testCerrarTicketNoExistente() {
        when(soporteRepository.findById(99L)).thenReturn(Optional.empty());

        Soporte resultado = soporteService.cerrarTicket(99L);

        assertNull(resultado);
        verify(soporteRepository, never()).save(any(Soporte.class));
    }

    @Test
    void testListarSoportes() {
        Soporte s1 = new Soporte(1L, 5L, "Asunto 1", "Desc 1", true);
        Soporte s2 = new Soporte(2L, 6L, "Asunto 2", "Desc 2", false);
        when(soporteRepository.findAll()).thenReturn(List.of(s1, s2));

        List<Soporte> resultado = soporteService.listarSoportes();

        assertEquals(2, resultado.size());
        verify(soporteRepository, times(1)).findAll();
    }

    @Test
    void testFindByIdExistente() {
        Soporte soporte = new Soporte(1L, 5L, "Asunto", "Desc", true);
        when(soporteRepository.findById(1L)).thenReturn(Optional.of(soporte));

        Soporte resultado = soporteService.findById(1L);

        assertNotNull(resultado);
        assertEquals("Asunto", resultado.getAsunto());
    }

    @Test
    void testFindByIdNoExistente() {
        when(soporteRepository.findById(99L)).thenReturn(Optional.empty());

        Soporte resultado = soporteService.findById(99L);

        assertNull(resultado);
    }
}