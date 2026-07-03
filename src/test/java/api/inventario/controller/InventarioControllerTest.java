package api.inventario.controller;

import api.inventario.model.IndicadorStock;
import api.inventario.model.ItemInventario;
import api.inventario.model.Producto;
import api.inventario.service.InventarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class InventarioControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private InventarioService inventarioService;

    @BeforeEach
    void setup() {
        InventarioController controller = new InventarioController(inventarioService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void crearProducto_devuelveProductoConStock() throws Exception {
        Producto producto = Producto.crearNuevo("Teclado", "Mecanico");
        IndicadorStock indicador = indicadorConStock(producto, 0, 5, "SIN_STOCK");

        when(inventarioService.crearNuevoProducto("Teclado", "Mecanico", 5)).thenReturn(producto);
        when(inventarioService.consultarStock(producto.getSku())).thenReturn(indicador);

        Map<String, Object> request = Map.of(
                "nombre", "Teclado",
                "descripcion", "Mecanico",
                "umbralMinimo", 5
        );

        mockMvc.perform(post("/api/inventario/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value(producto.getSku()))
                .andExpect(jsonPath("$.nombre").value("Teclado"))
                .andExpect(jsonPath("$.descripcion").value("Mecanico"))
                .andExpect(jsonPath("$.stockTotalConsolidado").value(0))
                .andExpect(jsonPath("$.estadoStock").value("SIN_STOCK"));
    }

    @Test
    void listarProductos_incluyeStockPorProducto() throws Exception {
        Producto producto = Producto.crearNuevo("Mouse", "Inalambrico");
        IndicadorStock indicador = indicadorConStock(producto, 12, 5, "STOCK_OK");

        when(inventarioService.obtenerTodosLosProductos()).thenReturn(List.of(producto));
        when(inventarioService.consultarStock(producto.getSku())).thenReturn(indicador);

        mockMvc.perform(get("/api/inventario/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sku").value(producto.getSku()))
                .andExpect(jsonPath("$[0].stockTotalConsolidado").value(12))
                .andExpect(jsonPath("$[0].estadoStock").value("STOCK_OK"));
    }

    @Test
    void registrarEntrada_devuelveItemConProductoCompleto() throws Exception {
        Producto producto = Producto.crearNuevo("SSD", "1TB");
        IndicadorStock indicador = indicadorConStock(producto, 50, 10, "STOCK_OK");
        ItemInventario item = ItemInventario.registrarEntrada(producto, "Proveedor", 5);

        when(inventarioService.agregarStock(producto.getSku(), "Proveedor", 5)).thenReturn(item);
        when(inventarioService.consultarStock(producto.getSku())).thenReturn(indicador);

        Map<String, Object> request = Map.of(
                "sku", producto.getSku(),
                "origen", "Proveedor",
                "cantidad", 5
        );

        mockMvc.perform(post("/api/inventario/stock/entrada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.producto.sku").value(producto.getSku()))
                .andExpect(jsonPath("$.producto.stockTotalConsolidado").value(50))
                .andExpect(jsonPath("$.cantidad").value(5));
    }

    @Test
    void registrarSalida_devuelveItemConProductoCompleto() throws Exception {
        Producto producto = Producto.crearNuevo("Monitor", "4K");
        IndicadorStock indicador = indicadorConStock(producto, 10, 5, "STOCK_OK");
        ItemInventario item = ItemInventario.registrarEntrada(producto, "Boleta #123", -2);

        when(inventarioService.registrarSalida(producto.getSku(), "Boleta #123", 2)).thenReturn(item);
        when(inventarioService.consultarStock(producto.getSku())).thenReturn(indicador);

        Map<String, Object> request = Map.of(
                "sku", producto.getSku(),
                "destino", "Boleta #123",
                "cantidad", 2
        );

        mockMvc.perform(post("/api/inventario/stock/salida")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.producto.sku").value(producto.getSku()))
                .andExpect(jsonPath("$.producto.stockTotalConsolidado").value(10))
                .andExpect(jsonPath("$.cantidad").value(-2));
    }

    @Test
    void historialMovimientos_devuelveListaDeItems() throws Exception {
        Producto producto = Producto.crearNuevo("Webcam", "1080p");
        IndicadorStock indicador = indicadorConStock(producto, 10, 2, "STOCK_OK");
        ItemInventario item = ItemInventario.registrarEntrada(producto, "Compra", 10);

        when(inventarioService.obtenerHistorial(producto.getSku())).thenReturn(List.of(item));
        when(inventarioService.consultarStock(producto.getSku())).thenReturn(indicador);

        mockMvc.perform(get("/api/inventario/movimientos/{sku}", producto.getSku()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].producto.sku").value(producto.getSku()))
                .andExpect(jsonPath("$[0].producto.estadoStock").value("STOCK_OK"))
                .andExpect(jsonPath("$[0].cantidad").value(10));
    }

    private IndicadorStock indicadorConStock(Producto producto, int stock, int umbral, String estado) {
        IndicadorStock indicador = IndicadorStock.inicializarPara(producto, umbral);
        indicador.setStockTotalConsolidado(stock);
        indicador.setEstado(estado);
        return indicador;
    }
}