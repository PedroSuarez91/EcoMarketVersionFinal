package com.example.envio_service.service;

import com.example.envio_service.model.RutaEntrega;
import com.example.envio_service.repository.RutaEntregaRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RutaEntregaServiceTest {

    @Mock
    private RutaEntregaRepository rutaEntregaRepository;

    @InjectMocks
    private RutaEntregaService rutaEntregaService;

    @Test
    void testCrearRuta() {
        RutaEntrega ruta = new RutaEntrega(null, "Concepcion", List.of(1L, 2L));
        RutaEntrega guardada = new RutaEntrega(1L, "Concepcion", List.of(1L, 2L));

        when(rutaEntregaRepository.save(ruta)).thenReturn(guardada);

        RutaEntrega resultado = rutaEntregaService.crearRuta(ruta);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdRuta());
        verify(rutaEntregaRepository, times(1)).save(ruta);
    }

    @Test
    void testListarRutas() {
        RutaEntrega r1 = new RutaEntrega(1L, "Concepcion", List.of(1L));
        when(rutaEntregaRepository.findAll()).thenReturn(List.of(r1));

        List<RutaEntrega> resultado = rutaEntregaService.listarRutas();

        assertEquals(1, resultado.size());
        verify(rutaEntregaRepository, times(1)).findAll();
    }

    @Test
    void testFindByIdExistente() {
        RutaEntrega ruta = new RutaEntrega(1L, "Concepcion", List.of(1L));
        when(rutaEntregaRepository.findById(1L)).thenReturn(Optional.of(ruta));

        Optional<RutaEntrega> resultado = rutaEntregaService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals("Concepcion", resultado.get().getComunaDestino());
    }

    @Test
    void testFindByIdNoExistente() {
        when(rutaEntregaRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<RutaEntrega> resultado = rutaEntregaService.findById(99L);

        assertFalse(resultado.isPresent());
    }

    @Test
    void testActualizarRutaExistente() {
        RutaEntrega existente = new RutaEntrega(1L, "Concepcion", List.of(1L));
        RutaEntrega datos = new RutaEntrega(null, "Talcahuano", List.of(2L, 3L));

        when(rutaEntregaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(rutaEntregaRepository.save(any(RutaEntrega.class))).thenAnswer(inv -> inv.getArgument(0));

        RutaEntrega resultado = rutaEntregaService.actualizarRuta(1L, datos);

        assertEquals("Talcahuano", resultado.getComunaDestino());
        assertEquals(2, resultado.getEnviosIds().size());
    }

    @Test
    void testActualizarRutaNoExiste() {
        RutaEntrega datos = new RutaEntrega(null, "Talcahuano", List.of(2L));
        when(rutaEntregaRepository.findById(99L)).thenReturn(Optional.empty());

        RutaEntrega resultado = rutaEntregaService.actualizarRuta(99L, datos);

        assertNull(resultado);
        verify(rutaEntregaRepository, never()).save(any(RutaEntrega.class));
    }

    @Test
    void testEliminarExistente() {
        when(rutaEntregaRepository.existsById(1L)).thenReturn(true);
        doNothing().when(rutaEntregaRepository).deleteById(1L);

        boolean resultado = rutaEntregaService.eliminarRuta(1L);

        assertTrue(resultado);
        verify(rutaEntregaRepository, times(1)).deleteById(1L);
    }

    @Test
    void testEliminarNoExistente() {
        when(rutaEntregaRepository.existsById(99L)).thenReturn(false);

        boolean resultado = rutaEntregaService.eliminarRuta(99L);

        assertFalse(resultado);
        verify(rutaEntregaRepository, never()).deleteById(anyLong());
    }
}