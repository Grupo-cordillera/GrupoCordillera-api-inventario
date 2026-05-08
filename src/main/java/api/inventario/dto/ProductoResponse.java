package api.inventario.dto;

// ProductoResponse.java
// Fíjate que aquí no devolvemos el ID interno de la base de datos, solo el SKU público.
public record ProductoResponse(
        String sku,
        String nombre,
        String descripcion,
        String estadoStock
) {}