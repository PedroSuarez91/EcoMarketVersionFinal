package ecomarket.catalogo.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.validation.constraints.NotBlank;
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
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long idCategoria;

    @Column(length = 100, nullable = false)
    @NotBlank(message = "La Categoria debe tener nombre")
    @Size(max=100, message = "el nombre no puede superar los 100 caracteres")
    private String nombreCategoria;

    @Column(length = 50, nullable= false)
    @NotBlank(message = "La Categoria debe incluir un tipo de Producto")
    @Size(max=100, message = "el nombre no puede superar los 50 caracteres")
    private String tipoProducto;

    @ManyToMany(mappedBy = "categorias")
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Producto> productos = new ArrayList<>();
}