package com.example.inventario_service.service;

import com.example.inventario_service.model.Inventario;
import com.example.inventario_service.model.ProductoDTO;
import com.example.inventario_service.repository.InventarioRepository;

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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {

    @Mock
    private InventarioRepository inventarioRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private InventarioService inventarioService;

    private ProductoDTO crearProducto() {
        ProductoDTO p = new ProductoDTO();
        p.setIdProducto(100L);
        p.setNombre("Manzana");
        return p;
    }

    @Test
    void testCrearInventarioNuevo() {
        Inventario inventario = new Inventario(null, 100L, 5L, null, 20, 5, null);

        when(restTemplate.getForObject(anyString(), eq(ProductoDTO.class))).thenReturn(crearProducto());
        when(inventarioRepository.findByIdProducto(100L)).thenReturn(Optional.empty());
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(inv -> inv.getArgument(0));

        Inventario resultado = inventarioService.crearInventario(inventario);

        assertNotNull(resultado);
        assertEquals("Manzana", resultado.getNombre());
        verify(inventarioRepository, times(1)).save(any(Inventario.class));
    }

    @Test
    void testCrearInventarioExistenteSumaStock() {
        Inventario entrada = new Inventario(null, 100L, 5L, null, 10, 5, null);
        Inventario existente = new Inventario(1L, 100L, 5L, "Manzana", 20, 5, LocalDate.now());

        when(restTemplate.getForObject(anyString(), eq(ProductoDTO.class))).thenReturn(crearProducto());
        when(inventarioRepository.findByIdProducto(100L)).thenReturn(Optional.of(existente));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(inv -> inv.getArgument(0));

        Inventario resultado = inventarioService.crearInventario(entrada);

        assertEquals(30, resultado.getStockDisponible());
    }

    @Test
    void testCrearInventarioProductoNotFound() {
        Inventario inventario = new Inventario(null, 100L, 5L, null, 20, 5, null);

        HttpClientErrorException notFound = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, new byte[0], null);
        when(restTemplate.getForObject(anyString(), eq(ProductoDTO.class))).thenThrow(notFound);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> inventarioService.crearInventario(inventario));
        assertEquals("Producto no encontrado en Catalogo", ex.getMessage());
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    void testCrearInventarioCatalogoNoDisponible() {
        Inventario inventario = new Inventario(null, 100L, 5L, null, 20, 5, null);

        when(restTemplate.getForObject(anyString(), eq(ProductoDTO.class)))
                .thenThrow(new RuntimeException("Conexion rechazada"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> inventarioService.crearInventario(inventario));
        assertEquals("Servicio de Catalogo no disponible, intente mas tarde", ex.getMessage());
    }

    @Test
    void testObtenerInventarios() {
        Inventario i1 = new Inventario(1L, 100L, 5L, "Manzana", 20, 5, LocalDate.now());
        when(inventarioRepository.findAll()).thenReturn(List.of(i1));

        List<Inventario> resultado = inventarioService.obtenerInventarios();

        assertEquals(1, resultado.size());
        verify(inventarioRepository, times(1)).findAll();
    }

    @Test
    void testGetStockPorBodega() {
        Inventario i1 = new Inventario(1L, 100L, 5L, "Manzana", 20, 5, LocalDate.now());
        Inventario i2 = new Inventario(2L, 101L, 5L, "Pera", 30, 5, LocalDate.now());
        when(inventarioRepository.findByIdBodega(5L)).thenReturn(Arrays.asList(i1, i2));

        Integer total = inventarioService.getStockPorBodega(5L);

        assertEquals(50, total);
    }

    @Test
    void testActualizarInventarioExistente() {
        Inventario existente = new Inventario(1L, 100L, 5L, "Manzana", 20, 5, LocalDate.now());
        Inventario datos = new Inventario(null, 100L, 5L, "Manzana Verde", 40, 8, null);

        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(inv -> inv.getArgument(0));

        Inventario resultado = inventarioService.actualizarInventario(1L, datos);

        assertEquals(40, resultado.getStockDisponible());
        assertEquals("Manzana Verde", resultado.getNombre());
    }

    @Test
    void testActualizarInventarioNoExiste() {
        Inventario datos = new Inventario(null, 100L, 5L, "X", 40, 8, null);
        when(inventarioRepository.findById(99L)).thenReturn(Optional.empty());

        Inventario resultado = inventarioService.actualizarInventario(99L, datos);

        assertNull(resultado);
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    void testDescontarStockOk() {
        Inventario existente = new Inventario(1L, 100L, 5L, "Manzana", 20, 5, LocalDate.now());
        when(inventarioRepository.findByIdProducto(100L)).thenReturn(Optional.of(existente));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(inv -> inv.getArgument(0));

        Inventario resultado = inventarioService.descontarStock(100L, 5);

        assertEquals(15, resultado.getStockDisponible());
    }

    @Test
    void testDescontarStockInsuficiente() {
        Inventario existente = new Inventario(1L, 100L, 5L, "Manzana", 3, 5, LocalDate.now());
        when(inventarioRepository.findByIdProducto(100L)).thenReturn(Optional.of(existente));

        Inventario resultado = inventarioService.descontarStock(100L, 10);

        assertNull(resultado);
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    void testDescontarStockProductoNoExiste() {
        when(inventarioRepository.findByIdProducto(100L)).thenReturn(Optional.empty());

        Inventario resultado = inventarioService.descontarStock(100L, 5);

        assertNull(resultado);
    }

    @Test
    void testAlertasStockMinimo() {
        Inventario i1 = new Inventario(1L, 100L, 5L, "Manzana", 2, 5, LocalDate.now());
        when(inventarioRepository.findProductosBajoStockMinimo()).thenReturn(List.of(i1));

        List<Inventario> resultado = inventarioService.alertasStockMinimo();

        assertEquals(1, resultado.size());
        verify(inventarioRepository, times(1)).findProductosBajoStockMinimo();
    }

    @Test
    void testCrearInventarioProductoNull() {
        Inventario inventario = new Inventario(null, 100L, 5L, null, 20, 5, null);

        when(restTemplate.getForObject(anyString(), eq(ProductoDTO.class))).thenReturn(null);

        Inventario resultado = inventarioService.crearInventario(inventario);

        assertNull(resultado);
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }
}