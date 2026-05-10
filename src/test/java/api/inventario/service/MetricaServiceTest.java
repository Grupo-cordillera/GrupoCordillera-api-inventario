package api.inventario.service;

import api.inventario.model.MetricaRentabilidad;
import api.inventario.model.Producto;
import api.inventario.repository.MetricaRepository;
import api.inventario.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetricaServiceTest {

    @Mock
    private MetricaRepository metricaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private MetricaService metricaService;

    @Test
    void generarMetrica_guardaResultado() {
        Producto producto = Producto.crearNuevo("Camara", "HD");
        String sku = producto.getSku();

        when(productoRepository.findBySku(sku)).thenReturn(Optional.of(producto));
        when(metricaRepository.save(any(MetricaRentabilidad.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MetricaRentabilidad metrica = metricaService.generarMetrica(sku, 100.0, 40.0);

        assertEquals(60.0, metrica.getMargenGanancia());
        assertEquals(150.0, metrica.getRoi());
        assertEquals(40.0, metrica.getCostoOperativo());
        assertNotNull(metrica.getFechaCalculo());
    }

    @Test
    void generarMetrica_productoNoExiste_lanzaExcepcion() {
        when(productoRepository.findBySku("SKU-TEST")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> metricaService.generarMetrica("SKU-TEST", 100.0, 50.0));
    }

    @Test
    void obtenerHistorialMetricas_devuelveLista() {
        when(metricaRepository.findByProducto_Sku("SKU-TEST")).thenReturn(List.of());

        assertEquals(0, metricaService.obtenerHistorialMetricas("SKU-TEST").size());
    }
}

