package api.inventario.service;

import api.inventario.exception.IndicadorStockNotFoundException;
import api.inventario.exception.ProductoNotFoundException;
import api.inventario.exception.StockInsuficienteException;
import api.inventario.model.IndicadorStock;
import api.inventario.model.ItemInventario;
import api.inventario.model.Producto;
import api.inventario.repository.IndicadorStockRepository;
import api.inventario.repository.ItemInventarioRepository;
import api.inventario.repository.ProductoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor // Genera el constructor para la inyección de dependencias (Lombok)
public class InventarioService {

    private final ProductoRepository productoRepository;
    private final IndicadorStockRepository indicadorRepository;
    private final ItemInventarioRepository itemRepository;

    @Transactional
    public Producto crearNuevoProducto(String nombre, String descripcion, Integer umbralMinimo) {
        // 1. Usar Factory Method del modelo Producto
        Producto producto = Producto.crearNuevo(nombre, descripcion);
        producto = productoRepository.save(producto);

        // 2. Inicializar automáticamente el indicador de stock para ese producto
        IndicadorStock stockInicial = IndicadorStock.inicializarPara(producto, umbralMinimo);
        indicadorRepository.save(stockInicial);

        return producto;
    }

    @Transactional
    public ItemInventario agregarStock(String sku, String origen, Integer cantidad) {
        Producto producto = productoRepository.findBySku(sku)
                .orElseThrow(() -> new ProductoNotFoundException("Producto no encontrado"));

        // 1. Registrar el movimiento individual
        ItemInventario nuevoItem = ItemInventario.registrarEntrada(producto, origen, cantidad);
        itemRepository.save(nuevoItem);

        // 2. Actualizar el consolidado (IndicadorStock)
        IndicadorStock indicador = indicadorRepository.findByProducto_Sku(sku)
                .orElseThrow(() -> new IndicadorStockNotFoundException("Error: El producto no tiene indicador de stock"));

        indicador.setStockTotalConsolidado(indicador.getStockTotalConsolidado() + cantidad);

        // Lógica simple de estado
        if (indicador.getStockTotalConsolidado() > indicador.getUmbralMinimo()) {
            indicador.setEstado("STOCK_OK");
        } else {
            indicador.setEstado("STOCK_BAJO");
        }

        return nuevoItem;
    }

    @Transactional
    public ItemInventario registrarSalida(String sku, String destinoOVenta, Integer cantidad) {
        // 1. Buscamos el producto y su indicador
        Producto producto = productoRepository.findBySku(sku)
                .orElseThrow(() -> new ProductoNotFoundException("Producto no encontrado"));

        IndicadorStock indicador = indicadorRepository.findByProducto_Sku(sku)
                .orElseThrow(() -> new IndicadorStockNotFoundException("Error: El producto no tiene indicador de stock"));

        // 2. VALIDACIÓN CRÍTICA: ¿Tenemos stock suficiente?
        if (indicador.getStockTotalConsolidado() < cantidad) {
            throw new StockInsuficienteException("Stock insuficiente. Solo tienes " + indicador.getStockTotalConsolidado() + " unidades disponibles.");
        }

        // 3. Registramos el movimiento.
        // Nota: Como es salida, guardamos la cantidad en negativo para mantener el historial claro.
        ItemInventario salida = ItemInventario.registrarEntrada(producto, destinoOVenta, -cantidad);
        itemRepository.save(salida);

        // 4. Actualizamos el consolidado restando la cantidad
        indicador.setStockTotalConsolidado(indicador.getStockTotalConsolidado() - cantidad);

        // 5. Verificamos si al sacar stock caímos por debajo del umbral mínimo
        if (indicador.getStockTotalConsolidado() <= indicador.getUmbralMinimo()) {
            indicador.setEstado("STOCK_BAJO");
        } else {
            indicador.setEstado("STOCK_OK");
        }

        return salida;
    }

    public List<Producto> obtenerTodosLosProductos() {
        return productoRepository.findAll();
    }

    public IndicadorStock consultarStock(String sku) {
        return indicadorRepository.findByProducto_Sku(sku)
                .orElseThrow(() -> new IndicadorStockNotFoundException("No hay información de stock para el SKU: " + sku));
    }

    public List<ItemInventario> obtenerHistorial(String sku) {
        return itemRepository.findByProducto_Sku(sku);
    }

    @Transactional
    public void eliminarProducto(String sku) {
        Producto producto = productoRepository.findBySku(sku)
                .orElseThrow(() -> new ProductoNotFoundException("No se puede eliminar: Producto no encontrado"));

        itemRepository.deleteByProducto_Sku(sku);
        indicadorRepository.deleteByProducto_Sku(sku);

        productoRepository.delete(producto);
    }
}