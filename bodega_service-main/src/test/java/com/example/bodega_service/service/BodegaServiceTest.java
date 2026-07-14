package com.example.bodega_service.service;

import com.example.bodega_service.model.Bodega;
import com.example.bodega_service.model.BodegaDTO;
import com.example.bodega_service.model.SucursalDTO;
import com.example.bodega_service.repository.BodegaRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BodegaServiceTest {

    @Mock
    private BodegaRepository bodegaRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private BodegaService bodegaService;

    @Test
    void testCrearBodega() {
        Bodega bodega = new Bodega(null, "Central", 1000, true, 10L, 20L);
        Bodega guardada = new Bodega(1L, "Central", 1000, true, 10L, 20L);

        when(bodegaRepository.save(bodega)).thenReturn(guardada);

        Bodega resultado = bodegaService.crearBodega(bodega);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdBodega());
        verify(bodegaRepository, times(1)).save(bodega);
    }

    @Test
    void testListarBodegas() {
        Bodega b1 = new Bodega(1L, "Central", 1000, true, 10L, 20L);
        Bodega b2 = new Bodega(2L, "Norte", 500, true, 11L, 21L);

        when(bodegaRepository.findAll()).thenReturn(Arrays.asList(b1, b2));

        List<Bodega> resultado = bodegaService.listarBodegas();

        assertEquals(2, resultado.size());
        verify(bodegaRepository, times(1)).findAll();
    }

    @Test
    void testFindByIdExistente() {
        Bodega bodega = new Bodega(1L, "Central", 1000, true, 10L, 20L);
        when(bodegaRepository.findById(1L)).thenReturn(Optional.of(bodega));

        Optional<Bodega> resultado = bodegaService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals("Central", resultado.get().getNombreBodega());
    }

    @Test
    void testFindByIdNoExistente() {
        when(bodegaRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Bodega> resultado = bodegaService.findById(99L);

        assertFalse(resultado.isPresent());
    }

    @Test
    void testEliminarExistente() {
        when(bodegaRepository.existsById(1L)).thenReturn(true);
        doNothing().when(bodegaRepository).deleteById(1L);

        boolean resultado = bodegaService.eliminarBodega(1L);

        assertTrue(resultado);
        verify(bodegaRepository, times(1)).deleteById(1L);
    }

    @Test
    void testEliminarNoExistente() {
        when(bodegaRepository.existsById(99L)).thenReturn(false);

        boolean resultado = bodegaService.eliminarBodega(99L);

        assertFalse(resultado);
        verify(bodegaRepository, never()).deleteById(anyLong());
    }

    @Test
    void testObtenerBodegaDTOCompleto() {
        Bodega bodega = new Bodega(1L, "Central", 1000, true, 10L, 20L);
        when(bodegaRepository.findById(1L)).thenReturn(Optional.of(bodega));

        SucursalDTO sucursal = new SucursalDTO();
        sucursal.setDireccionSucursal("Av Principal 123");
        when(restTemplate.getForObject(contains("/sucursales/"), eq(SucursalDTO.class))).thenReturn(sucursal);
        when(restTemplate.getForObject(contains("/stockPorBodega/"), eq(String.class))).thenReturn("Total de productos: 50");

        BodegaDTO dto = bodegaService.obtenerBodegaDTO(1L);

        assertNotNull(dto);
        assertEquals("Av Principal 123", dto.getDireccionSucursal());
        assertEquals(50, dto.getCapacidadOcupada());
    }

    @Test
    void testObtenerBodegaDTONoExiste() {
        when(bodegaRepository.findById(99L)).thenReturn(Optional.empty());

        BodegaDTO dto = bodegaService.obtenerBodegaDTO(99L);

        assertNull(dto);
    }

    @Test
    void testObtenerBodegaDTOSucursalEInventarioNull() {
        Bodega bodega = new Bodega(1L, "Central", 1000, true, 10L, 20L);
        when(bodegaRepository.findById(1L)).thenReturn(Optional.of(bodega));

        when(restTemplate.getForObject(contains("/sucursales/"), eq(SucursalDTO.class))).thenReturn(null);
        when(restTemplate.getForObject(contains("/stockPorBodega/"), eq(String.class))).thenReturn(null);

        BodegaDTO dto = bodegaService.obtenerBodegaDTO(1L);

        assertNotNull(dto);
        assertNull(dto.getDireccionSucursal());
    }

    @Test
    void testObtenerBodegaDTOServiciosCaidos() {
        Bodega bodega = new Bodega(1L, "Central", 1000, true, 10L, 20L);
        when(bodegaRepository.findById(1L)).thenReturn(Optional.of(bodega));

        when(restTemplate.getForObject(contains("/sucursales/"), eq(SucursalDTO.class)))
                .thenThrow(new RuntimeException("Sucursal caida"));
        when(restTemplate.getForObject(contains("/stockPorBodega/"), eq(String.class)))
                .thenThrow(new RuntimeException("Inventario caido"));

        BodegaDTO dto = bodegaService.obtenerBodegaDTO(1L);

        assertNotNull(dto);
        assertNull(dto.getDireccionSucursal());
        assertNull(dto.getCapacidadOcupada());
    }

    @Test
    void testListarBodegasDTOCompleto() {
        Bodega bodega = new Bodega(1L, "Central", 1000, true, 10L, 20L);
        when(bodegaRepository.findAll()).thenReturn(List.of(bodega));

        SucursalDTO sucursal = new SucursalDTO();
        sucursal.setDireccionSucursal("Av Principal 123");
        when(restTemplate.getForObject(contains("/sucursales/"), eq(SucursalDTO.class))).thenReturn(sucursal);
        when(restTemplate.getForObject(contains("/stockPorBodega/"), eq(String.class))).thenReturn("Total de productos: 50");

        List<BodegaDTO> lista = bodegaService.listarBodegasDTO();

        assertEquals(1, lista.size());
        assertEquals(50, lista.get(0).getCapacidadOcupada());
    }

    @Test
    void testListarBodegasDTONullYCatch() {
        Bodega b1 = new Bodega(1L, "Central", 1000, true, 10L, 20L);
        Bodega b2 = new Bodega(2L, "Norte", 500, true, 11L, 21L);
        when(bodegaRepository.findAll()).thenReturn(List.of(b1, b2));

        // primera bodega: ambos null ; segunda bodega: ambos lanzan excepcion
        when(restTemplate.getForObject(contains("/sucursales/"), eq(SucursalDTO.class)))
                .thenReturn(null)
                .thenThrow(new RuntimeException("Sucursal caida"));
        when(restTemplate.getForObject(contains("/stockPorBodega/"), eq(String.class)))
                .thenReturn(null)
                .thenThrow(new RuntimeException("Inventario caido"));

        List<BodegaDTO> lista = bodegaService.listarBodegasDTO();

        assertEquals(2, lista.size());
    }

    @Test
    void testListarBodegasDTOVacio() {
        when(bodegaRepository.findAll()).thenReturn(List.of());

        List<BodegaDTO> lista = bodegaService.listarBodegasDTO();

        assertTrue(lista.isEmpty());
    }
}