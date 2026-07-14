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
import ecomarket.catalogo.repository.CatalogoRepository;

@ExtendWith(MockitoExtension.class)
public class CatalogoServiceTest {

    @Mock
    private CatalogoRepository catalogoRepository;

    @InjectMocks
    private CatalogoService catalogoService;

    private Catalogo catalogo(Long id, String nombre, LocalDate fecha) {
        Catalogo c = new Catalogo();
        c.setIdCatalogo(id);
        c.setNombreCatalogo(nombre);
        c.setFechaActualizacion(fecha);
        return c;
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
}