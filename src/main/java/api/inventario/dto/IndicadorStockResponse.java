package api.inventario.dto;

public record IndicadorStockResponse(
        Long id,
        ProductoResponse producto,
        Integer stockTotalConsolidado,
        Integer umbralMinimo,
        String estado
) {}

