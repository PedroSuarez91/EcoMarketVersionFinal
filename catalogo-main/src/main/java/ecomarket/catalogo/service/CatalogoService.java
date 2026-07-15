package ecomarket.catalogo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ecomarket.catalogo.model.Catalogo;
import ecomarket.catalogo.model.Producto;
import ecomarket.catalogo.repository.CatalogoRepository;
import ecomarket.catalogo.repository.ProductoRepository;


@Service
public class CatalogoService {
    @Autowired
    private CatalogoRepository catalogoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    public Catalogo crearCatalogo(Catalogo catalogo) {
        return catalogoRepository.save(catalogo);
    }

    public List<Catalogo> listarCatalogo() {
        return catalogoRepository.findAll();
    }

    public Optional<Catalogo> findById(Long id) {
        return catalogoRepository.findById(id);
    }

    public Catalogo actualizarCatalogo(Long id, Catalogo datos) {
        return catalogoRepository.findById(id).map(catalogo -> {
            catalogo.setNombreCatalogo(datos.getNombreCatalogo());
            catalogo.setFechaActualizacion(datos.getFechaActualizacion());
            return catalogoRepository.save(catalogo);
        }).orElse(null);
    }

    public void eliminarCatalogo(Long id) {
        catalogoRepository.deleteById(id);
    }

    /**
     * Asocia un producto ya existente al catalogo indicado.
     * Retorna null si el catalogo o el producto no existen.
     */
    @Transactional
    public Catalogo agregarProductoExistente(Long idCatalogo, Long idProducto) {
        Catalogo catalogo = catalogoRepository.findById(idCatalogo).orElse(null);
        if (catalogo == null) {
            return null;
        }
        Producto producto = productoRepository.findById(idProducto).orElse(null);
        if (producto == null) {
            return null;
        }
        Catalogo actual = producto.getCatalogo();
        if (actual != null && idCatalogo.equals(actual.getIdCatalogo())) {
            return catalogo;
        }
        producto.setCatalogo(catalogo);
        productoRepository.save(producto);
        if (!catalogo.getProductos().contains(producto)) {
            catalogo.getProductos().add(producto);
        }
        return catalogoRepository.save(catalogo);
    }
}