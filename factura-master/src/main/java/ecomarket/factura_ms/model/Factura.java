package ecomarket.factura_ms.model;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long idFactura;
    
    @NotNull
    private Long idPedido;

    @NotNull
    private String nombreCliente;

    @NotNull
    private LocalDate fechaEmision;

    @NotNull
    @PositiveOrZero(message = "El neto no puede ser negativo")
    private Integer neto;

    @NotNull
    @PositiveOrZero(message = "El IVA no puede ser negativo")
    private Integer iva;

    @NotNull
    @PositiveOrZero(message = "El total no puede ser negativo")
    private Integer total;
}
