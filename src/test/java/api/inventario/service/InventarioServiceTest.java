package api.inventario.service;

import api.inventario.model.IndicadorStock;
import api.inventario.model.ItemInventario;
import api.inventario.model.Producto;
import api.inventario.repository.IndicadorStockRepository;
import api.inventario.repository.ItemInventarioRepository;
import api.inventario.repository.MetricaRepository;
import api.inventario.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private IndicadorStockRepository indicadorRepository;

    @Mock
    private ItemInventarioRepository itemRepository;

    @Mock
    private MetricaRepository metricaRepository;

    @InjectMocks
    private InventarioService inventarioService;

    @Test
    void crearNuevoProducto_creaProductoEIndicador() {
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(indicadorRepository.save(any(IndicadorStock.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Producto producto = inventarioService.crearNuevoProducto("Teclado", "Mecanico", 5);

        assertNotNull(producto.getSku());
        ArgumentCaptor<IndicadorStock> captor = ArgumentCaptor.forClass(IndicadorStock.class);
        verify(indicadorRepository).save(captor.capture());

        IndicadorStock indicador = captor.getValue();
        assertEquals(producto, indicador.getProducto());
        assertEquals(0, indicador.getStockTotalConsolidado());
        assertEquals(5, indicador.getUmbralMinimo());
        assertEquals("SIN_STOCK", indicador.getEstado());
    }

    @Test
    void agregarStock_actualizaIndicadorYEstado() {
        Producto producto = Producto.crearNuevo("Mouse", "Inalambrico");
        String sku = producto.getSku();
        IndicadorStock indicador = IndicadorStock.inicializarPara(producto, 10);
        indicador.setStockTotalConsolidado(5);

        when(productoRepository.findBySku(sku)).thenReturn(Optional.of(producto));
        when(indicadorRepository.findByProducto_Sku(sku)).thenReturn(Optional.of(indicador));
        when(itemRepository.save(any(ItemInventario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemInventario item = inventarioService.agregarStock(sku, "Proveedor", 10);

        assertEquals(15, indicador.getStockTotalConsolidado());
        assertEquals("STOCK_OK", indicador.getEstado());
        assertEquals(10, item.getCantidad());
        verify(itemRepository).save(any(ItemInventario.class));
    }

    @Test
    void registrarSalida_conStockInsuficiente_lanzaExcepcion() {
        Producto producto = Producto.crearNuevo("Cable", "USB-C");
        String sku = producto.getSku();
        IndicadorStock indicador = IndicadorStock.inicializarPara(producto, 10);
        indicador.setStockTotalConsolidado(3);

        when(productoRepository.findBySku(sku)).thenReturn(Optional.of(producto));
        when(indicadorRepository.findByProducto_Sku(sku)).thenReturn(Optional.of(indicador));

        assertThrows(RuntimeException.class, () -> inventarioService.registrarSalida(sku, "Venta", 5));
        assertEquals(3, indicador.getStockTotalConsolidado());
        verify(itemRepository, never()).save(any(ItemInventario.class));
    }

    @Test
    void registrarSalida_actualizaIndicadorYRegistraSalida() {
        Producto producto = Producto.crearNuevo("Monitor", "27 pulgadas");
        String sku = producto.getSku();
        IndicadorStock indicador = IndicadorStock.inicializarPara(producto, 10);
        indicador.setStockTotalConsolidado(20);

        when(productoRepository.findBySku(sku)).thenReturn(Optional.of(producto));
        when(indicadorRepository.findByProducto_Sku(sku)).thenReturn(Optional.of(indicador));
        when(itemRepository.save(any(ItemInventario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemInventario item = inventarioService.registrarSalida(sku, "Venta", 5);

        assertEquals(15, indicador.getStockTotalConsolidado());
        assertEquals("STOCK_OK", indicador.getEstado());

        ArgumentCaptor<ItemInventario> captor = ArgumentCaptor.forClass(ItemInventario.class);
        verify(itemRepository).save(captor.capture());
        assertEquals(-5, captor.getValue().getCantidad());
        assertEquals(item.getCantidad(), captor.getValue().getCantidad());
    }

    @Test
    void consultarStock_sinIndicador_lanzaExcepcion() {
        when(indicadorRepository.findByProducto_Sku("SKU-TEST")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> inventarioService.consultarStock("SKU-TEST"));
    }

    @Test
    void obtenerHistorial_devuelveListaDelRepositorio() {
        when(itemRepository.findByProducto_Sku("SKU-TEST")).thenReturn(List.of());

        assertEquals(0, inventarioService.obtenerHistorial("SKU-TEST").size());
    }

    @Test
    void eliminarProducto_eliminaDependenciasYProducto() {
        Producto producto = Producto.crearNuevo("Notebook", "16GB");
        String sku = producto.getSku();

        when(productoRepository.findBySku(sku)).thenReturn(Optional.of(producto));

        inventarioService.eliminarProducto(sku);

        verify(itemRepository).deleteByProducto_Sku(sku);
        verify(metricaRepository).deleteByProducto_Sku(sku);
        verify(indicadorRepository).deleteByProducto_Sku(sku);
        verify(productoRepository).delete(producto);
    }

    @Test
    void eliminarProducto_sinProducto_lanzaExcepcion() {
        when(productoRepository.findBySku("SKU-TEST")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> inventarioService.eliminarProducto("SKU-TEST"));
    }
}

