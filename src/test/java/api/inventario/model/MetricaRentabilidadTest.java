package api.inventario.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MetricaRentabilidadTest {

    @Test
    void calcularPara_calculaMetricasYFecha() {
        Producto producto = Producto.crearNuevo("Tablet", "128GB");

        MetricaRentabilidad metrica = MetricaRentabilidad.calcularPara(producto, 100.0, 40.0);

        assertEquals(60.0, metrica.getMargenGanancia());
        assertEquals(150.0, metrica.getRoi());
        assertEquals(40.0, metrica.getCostoOperativo());
        assertNotNull(metrica.getFechaCalculo());
        assertEquals(producto, metrica.getProducto());
    }

    @Test
    void calcularPara_costoOperativoInvalido_lanzaExcepcion() {
        Producto producto = Producto.crearNuevo("Tablet", "128GB");

        assertThrows(IllegalArgumentException.class,
                () -> MetricaRentabilidad.calcularPara(producto, 100.0, 0.0));
    }

    @Test
    void calcularPara_precioVentaInvalido_lanzaExcepcion() {
        Producto producto = Producto.crearNuevo("Tablet", "128GB");

        assertThrows(IllegalArgumentException.class,
                () -> MetricaRentabilidad.calcularPara(producto, 0.0, 40.0));
    }
}

