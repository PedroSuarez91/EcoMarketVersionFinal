package ecomarket.catalogo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ecomarket.catalogo.model.Producto;
import ecomarket.catalogo.model.Resenia;
import ecomarket.catalogo.repository.ProductoRepository;
import ecomarket.catalogo.repository.ReseniaRepository;

@ExtendWith(MockitoExtension.class)
public class ReseniaServiceTest {

    @Mock
    private ReseniaRepository reseniaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ReseniaService reseniaService;

    private Resenia resenia(String comentario, Integer calificacion, Producto producto) {
        Resenia r = new Resenia();
        r.setComentario(comentario);
        r.setCalificacion(calificacion);
        r.setFechaResenia(LocalDate.now());
        r.setProducto(producto);
        return r;
    }

    private Producto productoConId(Long id) {
        Producto p = new Producto();
        p.setIdProducto(id);
        p.setNombre("Manzana");
        return p;
    }

    @Test
    void testRegistrarReseniaOk() {
        Resenia entrante = resenia("Buena", 5, productoConId(1L));
        Producto productoReal = productoConId(1L);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoReal));
        when(reseniaRepository.save(any(Resenia.class))).thenAnswer(inv -> inv.getArgument(0));
        Resenia resultado = reseniaService.registrarResenia(entrante);
        assertThat(resultado).isNotNull();
        assertThat(resultado.getProducto()).isEqualTo(productoReal);
        verify(reseniaRepository).save(entrante);
    }

    @Test
    void testRegistrarReseniaSinProductoDevuelveNull() {
        Resenia entrante = resenia("Sin producto", 4, null);
        Resenia resultado = reseniaService.registrarResenia(entrante);
        assertThat(resultado).isNull();
        verify(productoRepository, never()).findById(any());
        verify(reseniaRepository, never()).save(any(Resenia.class));
    }

    @Test
    void testRegistrarReseniaProductoSinIdDevuelveNull() {
        Resenia entrante = resenia("Producto sin id", 4, new Producto());
        Resenia resultado = reseniaService.registrarResenia(entrante);
        assertThat(resultado).isNull();
        verify(productoRepository, never()).findById(any());
        verify(reseniaRepository, never()).save(any(Resenia.class));
    }

    @Test
    void testRegistrarReseniaProductoInexistenteDevuelveNull() {
        Resenia entrante = resenia("Producto fantasma", 3, productoConId(99L));
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());
        Resenia resultado = reseniaService.registrarResenia(entrante);
        assertThat(resultado).isNull();
        verify(reseniaRepository, never()).save(any(Resenia.class));
    }

    @Test
    void testListarResenias() {
        when(reseniaRepository.findAll()).thenReturn(List.of(resenia("A", 5, productoConId(1L))));
        assertThat(reseniaService.listarResenias()).hasSize(1);
    }

    @Test
    void testListarPorProducto() {
        when(reseniaRepository.findByProducto_IdProducto(1L))
                .thenReturn(List.of(resenia("A", 5, productoConId(1L))));
        assertThat(reseniaService.listarPorProducto(1L)).hasSize(1);
        verify(reseniaRepository).findByProducto_IdProducto(1L);
    }

    @Test
    void testFindById() {
        when(reseniaRepository.findById(1L)).thenReturn(Optional.of(resenia("A", 5, productoConId(1L))));
        assertThat(reseniaService.findById(1L)).isPresent();
    }

    @Test
    void testActualizarReseniaOk() {
        Resenia existente = resenia("Vieja", 2, productoConId(1L));
        Resenia datos = resenia("Nueva", 5, null);
        when(reseniaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(reseniaRepository.save(any(Resenia.class))).thenAnswer(inv -> inv.getArgument(0));
        Resenia resultado = reseniaService.actualizarResenia(1L, datos);
        assertThat(resultado.getComentario()).isEqualTo("Nueva");
        assertThat(resultado.getCalificacion()).isEqualTo(5);
    }

    @Test
    void testActualizarReseniaInexistente() {
        when(reseniaRepository.findById(99L)).thenReturn(Optional.empty());
        assertThat(reseniaService.actualizarResenia(99L, new Resenia())).isNull();
        verify(reseniaRepository, never()).save(any(Resenia.class));
    }

    @Test
    void testEliminarResenia() {
        reseniaService.eliminarResenia(1L);
        verify(reseniaRepository).deleteById(1L);
    }
}