package api.inventario.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductoTest {

    @Test
    void crearNuevo_generaSkuYAsignaCampos() {
        Producto producto = Producto.crearNuevo("Laptop", "16GB RAM");

        assertNotNull(producto.getSku());
        assertTrue(producto.getSku().matches("PROD-[0-9A-F]{8}"));
        assertEquals("Laptop", producto.getNombre());
        assertEquals("16GB RAM", producto.getDescripcion());
    }
}

