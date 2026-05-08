package api.inventario.service;

import api.inventario.model.MetricaRentabilidad;
import api.inventario.model.Producto;
import api.inventario.repository.MetricaRepository;
import api.inventario.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MetricaService {

    private final MetricaRepository metricaRepository;
    private final ProductoRepository productoRepository;

    public MetricaService(MetricaRepository metricaRepository, ProductoRepository productoRepository) {
        this.metricaRepository = metricaRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional
    public MetricaRentabilidad generarMetrica(String skuProducto, Double precioVenta, Double costoOperativo) {

        // 1. Buscamos el producto en la BD
        Producto producto = productoRepository.findBySku(skuProducto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con SKU: " + skuProducto));

        // 2. Usamos tu Factory Method para delegar el cálculo matemático a la Entidad
        MetricaRentabilidad nuevaMetrica = MetricaRentabilidad.calcularPara(producto, precioVenta, costoOperativo);

        // 3. Guardamos el resultado histórico
        return metricaRepository.save(nuevaMetrica);
    }

    public List<MetricaRentabilidad> obtenerHistorialMetricas(String sku) {
        return metricaRepository.findByProducto_Sku(sku);
    }
}