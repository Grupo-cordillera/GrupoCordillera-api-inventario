package api.inventario.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ItemInventarioTest {

    @Test
    void registrarEntrada_creaItemConFecha() {
        Producto producto = Producto.crearNuevo("Disco", "SSD");

        ItemInventario item = ItemInventario.registrarEntrada(producto, "Proveedor", 12);

        assertEquals(producto, item.getProducto());
        assertEquals("Proveedor", item.getOrigen());
        assertEquals(12, item.getCantidad());
        assertNotNull(item.getUltimaActualizacion());
    }
}

