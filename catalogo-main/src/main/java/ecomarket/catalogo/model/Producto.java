package ecomarket.catalogo.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long idProducto;

    private Long idInventario;

    @Column(length = 50)
    @NotBlank(message = "El Porducto debe tener un tipo")
    @Size(max=50, message = "el nombre no del tipo de producto no debe superar los 50 caracteres")
    private String tipoProducto;

    @Column(length = 150, nullable = false)
    @NotBlank(message = "El Producto debe tener un nombre")
    @Size(max=100, message = "el nombre no puede superar los 150 caracteres")
    private String nombre;

    @Column(length = 150, nullable = false)
    @NotBlank(message = "El Producto debe tener una Marca")
    @Size(max=100, message = "el nombre de la marca no puede superar los 150 caracteres")
    private String marca;

    @Column(length = 500)
    @NotBlank(message = "El Producto debe de tener una Descripcion")
    @Size(max=100, message = "la descripcion tiene un limite de 500 caracteres")
    private String descripcion;

    @Column(nullable = false)
    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser mayor que 0")
    private Integer precioUnitario;

    private Boolean estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_catalogo")
    @JsonBackReference("catalogo-producto")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Catalogo catalogo;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("producto-resenia")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Resenia> resenias = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "producto_categoria",
            joinColumns = @JoinColumn(name = "id_producto"),
            inverseJoinColumns = @JoinColumn(name = "id_categoria"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Categoria> categorias = new ArrayList<>();
}
