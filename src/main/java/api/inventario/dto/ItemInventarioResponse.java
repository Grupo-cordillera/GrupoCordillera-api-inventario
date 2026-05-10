package api.inventario.dto;

import java.time.LocalDateTime;

public record ItemInventarioResponse(
        Long id,
        ProductoResponse producto,
        String origen,
        Integer cantidad,
        LocalDateTime ultimaActualizacion
) {}

