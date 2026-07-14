package ecomarket.catalogo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ecomarket.catalogo.model.Producto;
import ecomarket.catalogo.repository.ProductoRepository;

@ExtendWith(MockitoExtension.class)
public class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    private Producto producto(Long id, String nombre, String marca, Integer precio) {
        Producto p = new Producto();
        p.setIdProducto(id);
        p.setNombre(nombre);
        p.setMarca(marca);
        p.setPrecioUnitario(precio);
        p.setTipoProducto("ALIMENTO");
        p.setDescripcion("desc");
        p.setEstado(true);
        p.setIdInventario(1L);
        return p;
    }

    @Test
    void testRegistrarProducto() {
        Producto nuevo = producto(null, "Manzana", "Marca1", 500);
        Producto guardado = producto(1L, "Manzana", "Marca1", 500);
        when(productoRepository.save(nuevo)).thenReturn(guardado);
        Producto resultado = productoService.registrarProducto(nuevo);
        assertThat(resultado.getIdProducto()).isEqualTo(1L);
        verify(productoRepository).save(nuevo);
    }

    @Test
    void testListarProductos() {
        when(productoRepository.findAll()).thenReturn(List.of(producto(1L, "Manzana", "Marca1", 500)));
        assertThat(productoService.listarProductos()).hasSize(1);
    }

    @Test
    void testFindByIdProducto() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto(1L, "Manzana", "Marca1", 500)));
        assertThat(productoService.findByIdProducto(1L)).isPresent();
    }

    @Test
    void testFindByCategoria() {
        when(productoRepository.findByCategorias_IdCategoria(3L))
                .thenReturn(List.of(producto(1L, "Manzana", "Marca1", 500)));
        assertThat(productoService.findByCategoria(3L)).hasSize(1);
        verify(productoRepository).findByCategorias_IdCategoria(3L);
    }

    @Test
    void testActualizarProductoOk() {
        Producto existente = producto(1L, "Viejo", "M1", 100);
        Producto datos = producto(null, "Nuevo", "M2", 200);
        datos.setCategorias(new ArrayList<>());
        when(productoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));
        Producto resultado = productoService.actualizarProducto(1L, datos);
        assertThat(resultado.getNombre()).isEqualTo("Nuevo");
        assertThat(resultado.getMarca()).isEqualTo("M2");
        assertThat(resultado.getPrecioUnitario()).isEqualTo(200);
        verify(productoRepository).save(existente);
    }

    @Test
    void testActualizarProductoInexistente() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThat(productoService.actualizarProducto(99L, new Producto())).isNull();
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void testFindByRangoPrecio() {
        when(productoRepository.findByPrecioUnitarioBetween(100, 500))
                .thenReturn(List.of(producto(1L, "Manzana", "Marca1", 300)));
        assertThat(productoService.findByRangoPrecio(100, 500)).hasSize(1);
    }

    @Test
    void testFindByPrecioMaximo() {
        when(productoRepository.findByPrecioUnitarioLessThanEqual(500))
                .thenReturn(List.of(producto(1L, "Manzana", "Marca1", 300)));
        assertThat(productoService.findByPrecioMaximo(500)).hasSize(1);
    }

    @Test
    void testFindByPrecioMinimo() {
        when(productoRepository.findByPrecioUnitarioGreaterThanEqual(100))
                .thenReturn(List.of(producto(1L, "Manzana", "Marca1", 300)));
        assertThat(productoService.findByPrecioMinimo(100)).hasSize(1);
    }

    @Test
    void testBuscarPorNombre() {
        when(productoRepository.findByNombreContainingIgnoreCase("man"))
                .thenReturn(List.of(producto(1L, "Manzana", "Marca1", 300)));
        assertThat(productoService.buscarPorNombre("man")).hasSize(1);
    }

    @Test
    void testFindByMarca() {
        when(productoRepository.findByMarca("Marca1"))
                .thenReturn(List.of(producto(1L, "Manzana", "Marca1", 300)));
        assertThat(productoService.findByMarca("Marca1")).hasSize(1);
    }

    @Test
    void testEliminarProductoExiste() {
        when(productoRepository.existsById(1L)).thenReturn(true);
        assertThat(productoService.eliminarProducto(1L)).isTrue();
        verify(productoRepository).deleteById(1L);
    }

    @Test
    void testEliminarProductoNoExiste() {
        when(productoRepository.existsById(99L)).thenReturn(false);
        assertThat(productoService.eliminarProducto(99L)).isFalse();
        verify(productoRepository, never()).deleteById(any());
    }
}