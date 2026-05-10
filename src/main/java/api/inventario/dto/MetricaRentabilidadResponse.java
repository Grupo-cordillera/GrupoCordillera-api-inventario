package api.inventario.dto;

import java.time.LocalDate;

public record MetricaRentabilidadResponse(
        Long id,
        ProductoResponse producto,
        Double margenGanancia,
        Double costoOperativo,
        Double roi,
        LocalDate fechaCalculo
) {}

