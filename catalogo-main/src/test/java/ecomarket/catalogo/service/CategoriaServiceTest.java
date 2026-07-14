package ecomarket.catalogo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ecomarket.catalogo.model.Categoria;
import ecomarket.catalogo.repository.CategoriaRepository;

@ExtendWith(MockitoExtension.class)
public class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    private Categoria categoria(Long id, String nombre, String tipo) {
        Categoria c = new Categoria();
        c.setIdCategoria(id);
        c.setNombreCategoria(nombre);
        c.setTipoProducto(tipo);
        return c;
    }

    @Test
    void testCrearCategoria() {
        Categoria nueva = categoria(null, "Frutas", "ALIMENTO");
        Categoria guardada = categoria(1L, "Frutas", "ALIMENTO");
        when(categoriaRepository.save(nueva)).thenReturn(guardada);

        Categoria resultado = categoriaService.crearCategoria(nueva);

        assertThat(resultado.getIdCategoria()).isEqualTo(1L);
        assertThat(resultado.getNombreCategoria()).isEqualTo("Frutas");
        verify(categoriaRepository).save(nueva);
    }

    @Test
    void testListarCategorias() {
        when(categoriaRepository.findAll()).thenReturn(List.of(categoria(1L, "Frutas", "ALIMENTO")));

        assertThat(categoriaService.listarCategorias()).hasSize(1);
        verify(categoriaRepository).findAll();
    }

    @Test
    void testFindById() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria(1L, "Frutas", "ALIMENTO")));

        assertThat(categoriaService.findById(1L)).isPresent();
        assertThat(categoriaService.findById(1L).get().getNombreCategoria()).isEqualTo("Frutas");
    }

    @Test
    void testFindByIdInexistente() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(categoriaService.findById(99L)).isEmpty();
    }

    @Test
    void testEliminarCategoria() {
        categoriaService.eliminarCategoria(1L);

        verify(categoriaRepository).deleteById(1L);
    }
}