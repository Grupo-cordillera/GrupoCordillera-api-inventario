package api.inventario.controller;

import api.inventario.dto.ProductoRequest;
import api.inventario.dto.ProductoResponse;
import api.inventario.model.IndicadorStock;
import api.inventario.model.ItemInventario;
import api.inventario.model.MetricaRentabilidad;
import api.inventario.model.Producto;
import api.inventario.service.InventarioService;
import api.inventario.service.MetricaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private static final String PRODUCTO = "producto";
    private static final String CANTIDAD = "cantidad";
    private static final String ID = "id";

    private final InventarioService inventarioService;
    private final MetricaService metricaService; // ¡Agregamos la inyección del servicio de métricas!

    @PostMapping("/productos")
    public ResponseEntity<ProductoResponse> crearProducto(@Valid @RequestBody ProductoRequest request) {

        Producto p = inventarioService.crearNuevoProducto(
                request.nombre(),
                request.descripcion(),
                request.umbralMinimo()
        );
        IndicadorStock stock = inventarioService.consultarStock(p.getSku());

        return ResponseEntity.ok(new ProductoResponse(
                p.getSku(),
                p.getNombre(),
                p.getDescripcion(),
                stock.getStockTotalConsolidado(),
                stock.getEstado()
        ));
    }

    @GetMapping("/productos")
    public ResponseEntity<List<ProductoResponse>> listarTodosLosProductos() {
        List<ProductoResponse> productos = inventarioService.obtenerTodosLosProductos().stream()
                .map(this::mapearProductoConStock)
                .toList();

        return ResponseEntity.ok(productos);
    }

    @PostMapping("/stock/entrada")
    public ResponseEntity<Map<String, Object>> registrarEntrada(@RequestBody Map<String, Object> body) {
        ItemInventario item = inventarioService.agregarStock(
                (String) body.get("sku"),
                (String) body.get("origen"), // Ej: "Proveedor Tech Limitada"
                (Integer) body.get(CANTIDAD)
        );
        return ResponseEntity.ok(mapearItem(item));
    }

    @PostMapping("/stock/salida")
    public ResponseEntity<Map<String, Object>> registrarSalida(@RequestBody Map<String, Object> body) {
        ItemInventario item = inventarioService.registrarSalida(
                (String) body.get("sku"),
                (String) body.get("destino"), // Ej: "Boleta #12345"
                (Integer) body.get(CANTIDAD)
        );
        return ResponseEntity.ok(mapearItem(item));
    }

    @GetMapping("/stock/{sku}")
    public ResponseEntity<Map<String, Object>> consultarStock(@PathVariable String sku) {
        IndicadorStock stock = inventarioService.consultarStock(sku);
        return ResponseEntity.ok(mapearIndicadorStock(stock));
    }

    @GetMapping("/movimientos/{sku}")
    public ResponseEntity<List<Map<String, Object>>> historialMovimientos(@PathVariable String sku) {
        List<Map<String, Object>> historial = inventarioService.obtenerHistorial(sku).stream()
                .map(this::mapearItem)
                .toList();
        return ResponseEntity.ok(historial);
    }

    @DeleteMapping("/productos/{sku}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable String sku) {
        inventarioService.eliminarProducto(sku);
        return ResponseEntity.noContent().build(); // Devuelve un 204 No Content
    }

    @PostMapping("/metricas/{sku}")
    public ResponseEntity<Map<String, Object>> calcularMetricas(
            @PathVariable String sku,
            @RequestParam Double precioVenta,
            @RequestParam Double costoOperativo) {

        // Usamos el servicio que inyectamos arriba
        MetricaRentabilidad metrica = metricaService.generarMetrica(sku, precioVenta, costoOperativo);
        return ResponseEntity.ok(mapearMetrica(metrica));
    }

    @GetMapping("/metricas/{sku}")
    public ResponseEntity<List<Map<String, Object>>> obtenerMetricas(@PathVariable String sku) {
        List<Map<String, Object>> metricas = metricaService.obtenerHistorialMetricas(sku).stream()
                .map(this::mapearMetrica)
                .toList();
        return ResponseEntity.ok(metricas);
    }

    private ProductoResponse mapearProductoConStock(Producto producto) {
        IndicadorStock stock;
        try {
            stock = inventarioService.consultarStock(producto.getSku());
        } catch (RuntimeException ex) {
            stock = null;
        }

        return new ProductoResponse(
                producto.getSku(),
                producto.getNombre(),
                producto.getDescripcion(),
                stock != null ? stock.getStockTotalConsolidado() : 0,
                stock != null ? stock.getEstado() : "SIN_STOCK"
        );
    }

    private Map<String, Object> mapearItem(ItemInventario item) {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put(ID, item.getId());
        respuesta.put(PRODUCTO, mapearProductoConStock(item.getProducto()));
        respuesta.put("origen", item.getOrigen());
        respuesta.put(CANTIDAD, item.getCantidad());
        respuesta.put("ultimaActualizacion", item.getUltimaActualizacion());
        return respuesta;
    }

    private Map<String, Object> mapearIndicadorStock(IndicadorStock stock) {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put(ID, stock.getId());
        respuesta.put(PRODUCTO, mapearProductoConStock(stock.getProducto()));
        respuesta.put("stockTotalConsolidado", stock.getStockTotalConsolidado());
        respuesta.put("umbralMinimo", stock.getUmbralMinimo());
        respuesta.put("estado", stock.getEstado());
        return respuesta;
    }

    private Map<String, Object> mapearMetrica(MetricaRentabilidad metrica) {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put(ID, metrica.getId());
        respuesta.put(PRODUCTO, mapearProductoConStock(metrica.getProducto()));
        respuesta.put("margenGanancia", metrica.getMargenGanancia());
        respuesta.put("costoOperativo", metrica.getCostoOperativo());
        respuesta.put("roi", metrica.getRoi());
        respuesta.put("fechaCalculo", metrica.getFechaCalculo());
        return respuesta;
    }
}