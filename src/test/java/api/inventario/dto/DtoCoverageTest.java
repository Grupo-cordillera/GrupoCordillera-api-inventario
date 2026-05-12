package api.inventario.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DtoCoverageTest {

    @Test
    void indicadorStockResponse_exponeCampos() {
        ProductoResponse producto = new ProductoResponse("SKU-1", "Producto", "Desc", 5, "STOCK_OK");
        IndicadorStockResponse response = new IndicadorStockResponse(1L, producto, 10, 3, "STOCK_OK");

        assertEquals(1L, response.id());
        assertEquals(producto, response.producto());
        assertEquals(10, response.stockTotalConsolidado());
        assertEquals(3, response.umbralMinimo());
        assertEquals("STOCK_OK", response.estado());
    }

    @Test
    void itemInventarioResponse_exponeCampos() {
        ProductoResponse producto = new ProductoResponse("SKU-2", "Producto 2", "Desc 2", 7, "STOCK_OK");
        LocalDateTime fecha = LocalDateTime.of(2026, 5, 12, 10, 30);
        ItemInventarioResponse response = new ItemInventarioResponse(2L, producto, "Proveedor", 4, fecha);

        assertEquals(2L, response.id());
        assertEquals(producto, response.producto());
        assertEquals("Proveedor", response.origen());
        assertEquals(4, response.cantidad());
        assertEquals(fecha, response.ultimaActualizacion());
    }

    @Test
    void metricaRentabilidadResponse_exponeCampos() {
        ProductoResponse producto = new ProductoResponse("SKU-3", "Producto 3", "Desc 3", 12, "STOCK_OK");
        LocalDate fecha = LocalDate.of(2026, 5, 12);
        MetricaRentabilidadResponse response = new MetricaRentabilidadResponse(3L, producto, 20.5, 10.0, 2.05, fecha);

        assertEquals(3L, response.id());
        assertEquals(producto, response.producto());
        assertEquals(20.5, response.margenGanancia());
        assertEquals(10.0, response.costoOperativo());
        assertEquals(2.05, response.roi());
        assertEquals(fecha, response.fechaCalculo());
    }
}

