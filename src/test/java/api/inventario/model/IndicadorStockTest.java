package api.inventario.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IndicadorStockTest {

    @Test
    void inicializarPara_configuraEstadoInicial() {
        Producto producto = Producto.crearNuevo("Router", "WiFi 6");

        IndicadorStock indicador = IndicadorStock.inicializarPara(producto, 8);

        assertEquals(producto, indicador.getProducto());
        assertEquals(0, indicador.getStockTotalConsolidado());
        assertEquals(8, indicador.getUmbralMinimo());
        assertEquals("SIN_STOCK", indicador.getEstado());
    }
}

