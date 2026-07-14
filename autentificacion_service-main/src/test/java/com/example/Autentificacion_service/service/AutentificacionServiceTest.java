package com.example.Autentificacion_service.service;

import com.example.Autentificacion_service.model.Autentificacion;
import com.example.Autentificacion_service.model.UsuarioDTO;
import com.example.Autentificacion_service.repository.AutentificacionRepository;

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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutentificacionServiceTest {

    @Mock
    private AutentificacionRepository autentificacionRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AutentificacionService autentificacionService;

    private UsuarioDTO crearUsuario() {
        UsuarioDTO u = new UsuarioDTO();
        u.setIdUsuario(5L);
        u.setNombre("Juan");
        u.setApellido("Perez");
        u.setEmailUsuario("juan@mail.com");
        u.setPassword("1234");
        u.setRol("CLIENTE");
        return u;
    }

    @Test
    void testLoginExitoso() {
        Autentificacion auth = new Autentificacion(null, "juan@mail.com", "1234", null, null);

        when(restTemplate.getForObject(anyString(), eq(UsuarioDTO.class))).thenReturn(crearUsuario());
        when(autentificacionRepository.save(any(Autentificacion.class))).thenAnswer(inv -> inv.getArgument(0));

        String resultado = autentificacionService.login(auth);

        assertTrue(resultado.startsWith("Bienvenido"));
        assertTrue(resultado.contains("CLIENTE"));
        verify(autentificacionRepository, times(1)).save(any(Autentificacion.class));
    }

    // rama: usuario == null
    @Test
    void testLoginUsuarioNull() {
        Autentificacion auth = new Autentificacion(null, "x@mail.com", "1234", null, null);

        when(restTemplate.getForObject(anyString(), eq(UsuarioDTO.class))).thenReturn(null);

        String resultado = autentificacionService.login(auth);

        assertEquals("Credenciales incorrectas", resultado);
        verify(autentificacionRepository, never()).save(any(Autentificacion.class));
    }

    // rama: password incorrecta
    @Test
    void testLoginPasswordIncorrecta() {
        Autentificacion auth = new Autentificacion(null, "juan@mail.com", "malaclave", null, null);

        when(restTemplate.getForObject(anyString(), eq(UsuarioDTO.class))).thenReturn(crearUsuario());

        String resultado = autentificacionService.login(auth);

        assertEquals("Credenciales incorrectas", resultado);
        verify(autentificacionRepository, never()).save(any(Autentificacion.class));
    }

    // rama: catch NotFound
    @Test
    void testLoginUsuarioNotFound() {
        Autentificacion auth = new Autentificacion(null, "noexiste@mail.com", "1234", null, null);

        HttpClientErrorException notFound = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, new byte[0], null);
        when(restTemplate.getForObject(anyString(), eq(UsuarioDTO.class))).thenThrow(notFound);

        String resultado = autentificacionService.login(auth);

        assertEquals("Credenciales incorrectas", resultado);
    }

    // rama: catch Exception -> lanza RuntimeException
    @Test
    void testLoginServicioNoDisponible() {
        Autentificacion auth = new Autentificacion(null, "juan@mail.com", "1234", null, null);

        when(restTemplate.getForObject(anyString(), eq(UsuarioDTO.class)))
                .thenThrow(new RuntimeException("Conexion rechazada"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> autentificacionService.login(auth));
        assertEquals("Servicio de autentificacion no disponible, intente mas tarde", ex.getMessage());
    }

    @Test
    void testListarLogins() {
        Autentificacion a1 = new Autentificacion(1L, "juan@mail.com", "1234", null, 5L);
        when(autentificacionRepository.findAll()).thenReturn(List.of(a1));

        List<Autentificacion> resultado = autentificacionService.listarLogins();

        assertEquals(1, resultado.size());
        verify(autentificacionRepository, times(1)).findAll();
    }
}