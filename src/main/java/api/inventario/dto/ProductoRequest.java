package api.inventario.dto;

// ProductoRequest.java
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductoRequest(
        @NotBlank(message = "El nombre del producto no puede estar vacío")
        String nombre,

        String descripcion,

        @NotNull(message = "Debes definir un umbral mínimo de stock")
        @Min(value = 0, message = "El umbral mínimo no puede ser negativo")
        Integer umbralMinimo
) {}