package api.inventario.controller;

import api.inventario.dto.ProductoRequest;
import api.inventario.dto.ProductoResponse;
import api.inventario.model.IndicadorStock;
import api.inventario.model.ItemInventario;
import api.inventario.model.Producto;
import api.inventario.service.InventarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PostMapping("/productos")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
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
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<List<ProductoResponse>> listarTodosLosProductos() {
        List<ProductoResponse> productos = inventarioService.obtenerTodosLosProductos().stream()
                .map(this::mapearProductoConStock)
                .toList();
        return ResponseEntity.ok(productos);
    }

    @PostMapping("/stock/entrada")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<Map<String, Object>> registrarEntrada(@RequestBody Map<String, Object> body) {
        ItemInventario item = inventarioService.agregarStock(
                (String) body.get("sku"),
                (String) body.get("origen"),
                (Integer) body.get(CANTIDAD)
        );
        return ResponseEntity.ok(mapearItem(item));
    }

    @PostMapping("/stock/salida")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<Map<String, Object>> registrarSalida(@RequestBody Map<String, Object> body) {
        ItemInventario item = inventarioService.registrarSalida(
                (String) body.get("sku"),
                (String) body.get("destino"),
                (Integer) body.get(CANTIDAD)
        );
        return ResponseEntity.ok(mapearItem(item));
    }

    @GetMapping("/stock/{sku}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<Map<String, Object>> consultarStock(@PathVariable String sku) {
        IndicadorStock stock = inventarioService.consultarStock(sku);
        return ResponseEntity.ok(mapearIndicadorStock(stock));
    }

    @GetMapping("/movimientos/{sku}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<List<Map<String, Object>>> historialMovimientos(@PathVariable String sku) {
        List<Map<String, Object>> historial = inventarioService.obtenerHistorial(sku).stream()
                .map(this::mapearItem)
                .toList();
        return ResponseEntity.ok(historial);
    }

    @DeleteMapping("/productos/{sku}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarProducto(@PathVariable String sku) {
        inventarioService.eliminarProducto(sku);
        return ResponseEntity.noContent().build();
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
}