package com.example.envio_service.service;

import com.example.envio_service.model.Envio;
import com.example.envio_service.model.EstadoEnvio;
import com.example.envio_service.model.PedidoDTO;
import com.example.envio_service.repository.EnvioRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnvioServiceTest {

    @Mock
    private EnvioRepository envioRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private EnvioService envioService;

    private PedidoDTO crearPedido() {
        PedidoDTO pedido = new PedidoDTO();
        pedido.setIdPedido(7L);
        pedido.setNombreCliente("Juan Perez");
        PedidoDTO.Direccion dir = new PedidoDTO.Direccion();
        dir.setCalle("Av Siempre Viva");
        dir.setNumero("742");
        dir.setComuna("Springfield");
        dir.setCiudad("Springfield");
        pedido.setDireccionEnvio(dir);
        return pedido;
    }

    @Test
    void testCrearEnvioExitoso() {
        Envio envio = new Envio(null, 7L, null, null, null, null);

        when(restTemplate.getForObject(anyString(), eq(PedidoDTO.class))).thenReturn(crearPedido());
        when(envioRepository.save(any(Envio.class))).thenAnswer(inv -> inv.getArgument(0));

        Envio resultado = envioService.crearEnvio(envio);

        assertNotNull(resultado);
        assertEquals(EstadoEnvio.PREPARANDO, resultado.getEstadoEnvio());
        assertNotNull(resultado.getFechaEnvio());
        verify(envioRepository, times(1)).save(any(Envio.class));
    }

    @Test
    void testCrearEnvioPedidoNull() {
        Envio envio = new Envio(null, 7L, null, null, null, null);

        when(restTemplate.getForObject(anyString(), eq(PedidoDTO.class))).thenReturn(null);

        Envio resultado = envioService.crearEnvio(envio);

        assertNull(resultado);
        verify(envioRepository, never()).save(any(Envio.class));
    }

    @Test
    void testCrearEnvioPedidoNotFound() {
        Envio envio = new Envio(null, 7L, null, null, null, null);

        HttpClientErrorException notFound = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, new byte[0], null);
        when(restTemplate.getForObject(anyString(), eq(PedidoDTO.class))).thenThrow(notFound);

        Envio resultado = envioService.crearEnvio(envio);

        assertNull(resultado);
        verify(envioRepository, never()).save(any(Envio.class));
    }

    @Test
    void testCrearEnvioServicioNoDisponible() {
        Envio envio = new Envio(null, 7L, null, null, null, null);

        when(restTemplate.getForObject(anyString(), eq(PedidoDTO.class)))
                .thenThrow(new RuntimeException("Conexion rechazada"));

        Envio resultado = envioService.crearEnvio(envio);

        assertNull(resultado);
        verify(envioRepository, never()).save(any(Envio.class));
    }

    @Test
    void testListarEnvios() {
        Envio e1 = new Envio(1L, 7L, "Calle 1", EstadoEnvio.PREPARANDO, LocalDate.now(), null);
        when(envioRepository.findAll()).thenReturn(List.of(e1));

        List<Envio> resultado = envioService.listarEnvios();

        assertEquals(1, resultado.size());
        verify(envioRepository, times(1)).findAll();
    }

    @Test
    void testFindByIdExistente() {
        Envio envio = new Envio(1L, 7L, "Calle 1", EstadoEnvio.PREPARANDO, LocalDate.now(), null);
        when(envioRepository.findById(1L)).thenReturn(Optional.of(envio));

        Optional<Envio> resultado = envioService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals(7L, resultado.get().getIdPedido());
    }

    @Test
    void testFindByIdNoExistente() {
        when(envioRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Envio> resultado = envioService.findById(99L);

        assertFalse(resultado.isPresent());
    }

    @Test
    void testActualizarEstadoEnCamino() {
        Envio existente = new Envio(1L, 7L, "Calle 1", EstadoEnvio.PREPARANDO, LocalDate.now(), null);
        when(envioRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(envioRepository.save(any(Envio.class))).thenAnswer(inv -> inv.getArgument(0));

        Envio resultado = envioService.actualizarEstado(1L, EstadoEnvio.EN_CAMINO);

        assertEquals(EstadoEnvio.EN_CAMINO, resultado.getEstadoEnvio());
        assertNull(resultado.getFechaEntrega());
    }

    @Test
    void testActualizarEstadoEntregadoFijaFechaEntrega() {
        Envio existente = new Envio(1L, 7L, "Calle 1", EstadoEnvio.EN_CAMINO, LocalDate.now(), null);
        when(envioRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(envioRepository.save(any(Envio.class))).thenAnswer(inv -> inv.getArgument(0));

        Envio resultado = envioService.actualizarEstado(1L, EstadoEnvio.ENTREGADO);

        assertEquals(EstadoEnvio.ENTREGADO, resultado.getEstadoEnvio());
        assertNotNull(resultado.getFechaEntrega());
    }

    @Test
    void testActualizarEstadoNoExiste() {
        when(envioRepository.findById(99L)).thenReturn(Optional.empty());

        Envio resultado = envioService.actualizarEstado(99L, EstadoEnvio.ENTREGADO);

        assertNull(resultado);
        verify(envioRepository, never()).save(any(Envio.class));
    }

    @Test
    void testEliminarExistente() {
        when(envioRepository.existsById(1L)).thenReturn(true);
        doNothing().when(envioRepository).deleteById(1L);

        boolean resultado = envioService.eliminarEnvio(1L);

        assertTrue(resultado);
        verify(envioRepository, times(1)).deleteById(1L);
    }

    @Test
    void testEliminarNoExistente() {
        when(envioRepository.existsById(99L)).thenReturn(false);

        boolean resultado = envioService.eliminarEnvio(99L);

        assertFalse(resultado);
        verify(envioRepository, never()).deleteById(anyLong());
    }

    @Test
    void testCrearEnvioConEstadoYaDefinido() {
        Envio envio = new Envio(null, 7L, null, EstadoEnvio.EN_CAMINO, null, null);

        when(restTemplate.getForObject(anyString(), eq(PedidoDTO.class))).thenReturn(crearPedido());
        when(envioRepository.save(any(Envio.class))).thenAnswer(inv -> inv.getArgument(0));

        Envio resultado = envioService.crearEnvio(envio);

        assertNotNull(resultado);
        assertEquals(EstadoEnvio.EN_CAMINO, resultado.getEstadoEnvio());
        verify(envioRepository, times(1)).save(any(Envio.class));
    }
}