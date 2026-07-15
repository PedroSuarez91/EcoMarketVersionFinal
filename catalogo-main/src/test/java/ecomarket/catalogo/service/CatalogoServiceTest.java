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

import ecomarket.catalogo.model.Catalogo;
import ecomarket.catalogo.model.Producto;
import ecomarket.catalogo.repository.CatalogoRepository;
import ecomarket.catalogo.repository.ProductoRepository;

@ExtendWith(MockitoExtension.class)
public class CatalogoServiceTest {

    @Mock
    private CatalogoRepository catalogoRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private CatalogoService catalogoService;

    private Catalogo catalogo(Long id, String nombre, LocalDate fecha) {
        Catalogo c = new Catalogo();
        c.setIdCatalogo(id);
        c.setNombreCatalogo(nombre);
        c.setFechaActualizacion(fecha);
        return c;
    }

    private Producto producto(Long id, String nombre) {
        Producto p = new Producto();
        p.setIdProducto(id);
        p.setNombre(nombre);
        p.setMarca("EcoMarket");
        p.setTipoProducto("Fruta");
        p.setDescripcion("Producto de prueba");
        p.setPrecioUnitario(1000);
        p.setEstado(true);
        return p;
    }

    @Test
    void testCrearCatalogo() {
        Catalogo nuevo = catalogo(null, "Catalogo Verano", LocalDate.of(2026, 1, 1));
        Catalogo guardado = catalogo(1L, "Catalogo Verano", LocalDate.of(2026, 1, 1));
        when(catalogoRepository.save(nuevo)).thenReturn(guardado);
        Catalogo resultado = catalogoService.crearCatalogo(nuevo);
        assertThat(resultado.getIdCatalogo()).isEqualTo(1L);
        assertThat(resultado.getNombreCatalogo()).isEqualTo("Catalogo Verano");
        verify(catalogoRepository).save(nuevo);
    }

    @Test
    void testListarCatalogo() {
        when(catalogoRepository.findAll())
                .thenReturn(List.of(catalogo(1L, "Catalogo Verano", LocalDate.now())));
        assertThat(catalogoService.listarCatalogo()).hasSize(1);
        verify(catalogoRepository).findAll();
    }

    @Test
    void testFindById() {
        when(catalogoRepository.findById(1L))
                .thenReturn(Optional.of(catalogo(1L, "Catalogo Verano", LocalDate.now())));
        assertThat(catalogoService.findById(1L)).isPresent();
        assertThat(catalogoService.findById(1L).get().getNombreCatalogo()).isEqualTo("Catalogo Verano");
    }

    @Test
    void testFindByIdInexistente() {
        when(catalogoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThat(catalogoService.findById(99L)).isEmpty();
    }

    @Test
    void testActualizarCatalogoOk() {
        Catalogo existente = catalogo(1L, "Viejo", LocalDate.of(2025, 1, 1));
        Catalogo datos = catalogo(null, "Nuevo", LocalDate.of(2026, 6, 1));
        when(catalogoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(catalogoRepository.save(any(Catalogo.class))).thenAnswer(inv -> inv.getArgument(0));
        Catalogo resultado = catalogoService.actualizarCatalogo(1L, datos);
        assertThat(resultado.getNombreCatalogo()).isEqualTo("Nuevo");
        assertThat(resultado.getFechaActualizacion()).isEqualTo(LocalDate.of(2026, 6, 1));
        verify(catalogoRepository).save(existente);
    }

    @Test
    void testActualizarCatalogoInexistente() {
        when(catalogoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThat(catalogoService.actualizarCatalogo(99L, new Catalogo())).isNull();
        verify(catalogoRepository, never()).save(any(Catalogo.class));
    }

    @Test
    void testEliminarCatalogo() {
        catalogoService.eliminarCatalogo(1L);
        verify(catalogoRepository).deleteById(1L);
    }

    // ---------- agregarProductoExistente(idCatalogo, idProducto) ----------

    @Test
    void testAgregarProductoExistenteOk() {
        Catalogo existente = catalogo(1L, "Catalogo Verano", LocalDate.of(2026, 1, 1));
        Producto p = producto(10L, "Manzana");

        when(catalogoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(p));
        when(catalogoRepository.save(any(Catalogo.class))).thenAnswer(inv -> inv.getArgument(0));

        Catalogo resultado = catalogoService.agregarProductoExistente(1L, 10L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getProductos()).containsExactly(p);
        assertThat(p.getCatalogo()).isEqualTo(existente);
        verify(productoRepository).save(p);
        verify(catalogoRepository).save(existente);
    }

    @Test
    void testAgregarProductoExistenteCatalogoInexistente() {
        when(catalogoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(catalogoService.agregarProductoExistente(99L, 10L)).isNull();
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void testAgregarProductoExistenteProductoInexistente() {
        when(catalogoRepository.findById(1L))
                .thenReturn(Optional.of(catalogo(1L, "Catalogo Verano", LocalDate.of(2026, 1, 1))));
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(catalogoService.agregarProductoExistente(1L, 99L)).isNull();
        verify(productoRepository, never()).save(any(Producto.class));
        verify(catalogoRepository, never()).save(any(Catalogo.class));
    }

    @Test
    void testAgregarProductoExistenteYaAsociadoNoDuplica() {
        Catalogo existente = catalogo(1L, "Catalogo Verano", LocalDate.of(2026, 1, 1));
        Producto p = producto(10L, "Manzana");
        p.setCatalogo(existente);
        existente.getProductos().add(p);

        when(catalogoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(p));

        Catalogo resultado = catalogoService.agregarProductoExistente(1L, 10L);

        assertThat(resultado.getProductos()).hasSize(1);
        verify(productoRepository, never()).save(any(Producto.class));
        verify(catalogoRepository, never()).save(any(Catalogo.class));
    }
}