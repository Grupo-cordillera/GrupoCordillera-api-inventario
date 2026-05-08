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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private final InventarioService inventarioService;

    @PostMapping("/productos")
    public ResponseEntity<ProductoResponse> crearProducto(@Valid @RequestBody ProductoRequest request) {

        Producto p = inventarioService.crearNuevoProducto(
                request.nombre(),
                request.descripcion(),
                request.umbralMinimo()
        );

        ProductoResponse response = new ProductoResponse(
                p.getSku(),
                p.getNombre(),
                p.getDescripcion(),
                "SIN_STOCK_INICIAL"
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/productos")
    public ResponseEntity<List<ProductoResponse>> listarTodosLosProductos() {
        List<ProductoResponse> productos = inventarioService.obtenerTodosLosProductos().stream()
                .map(p -> new ProductoResponse(p.getSku(), p.getNombre(), p.getDescripcion(), "VER_DETALLE"))
                .toList();

        return ResponseEntity.ok(productos);
    }

    @PostMapping("/stock/entrada")
    public ResponseEntity<ItemInventario> registrarEntrada(@RequestBody Map<String, Object> body) {
        ItemInventario item = inventarioService.agregarStock(
                (String) body.get("sku"),
                (String) body.get("origen"), // Ej: "Proveedor Tech Limitada"
                (Integer) body.get("cantidad")
        );
        return ResponseEntity.ok(item);
    }

    @PostMapping("/stock/salida")
    public ResponseEntity<ItemInventario> registrarSalida(@RequestBody Map<String, Object> body) {
        ItemInventario item = inventarioService.registrarSalida(
                (String) body.get("sku"),
                (String) body.get("destino"), // Ej: "Boleta #12345"
                (Integer) body.get("cantidad")
        );
        return ResponseEntity.ok(item);
    }

    @GetMapping("/stock/{sku}")
    public ResponseEntity<IndicadorStock> consultarStock(@PathVariable String sku) {
        IndicadorStock stock = inventarioService.consultarStock(sku);
        return ResponseEntity.ok(stock);
    }

    @GetMapping("/movimientos/{sku}")
    public ResponseEntity<List<ItemInventario>> historialMovimientos(@PathVariable String sku) {
        List<ItemInventario> historial = inventarioService.obtenerHistorial(sku);
        return ResponseEntity.ok(historial);
    }

    @DeleteMapping("/productos/{sku}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable String sku) {
        inventarioService.eliminarProducto(sku);
        return ResponseEntity.noContent().build(); // Devuelve un 204 No Content
    }

}