package api.inventario.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "metrica_rentabilidad")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MetricaRentabilidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    // Relacionamos la métrica con un producto usando su SKU
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku", referencedColumnName = "sku", nullable = false)
    @Setter(AccessLevel.NONE)
    private Producto producto;

    @Column(name = "margen_ganancia")
    private Double margenGanancia;

    @Column(name = "costo_operativo")
    private Double costoOperativo;

    @Column(name = "roi")
    private Double roi;

    @Column(name = "fecha_calculo")
    private LocalDate fechaCalculo;

    // 1. Constructor privado
    private MetricaRentabilidad(Producto producto, Double costoOperativo, Double margenGanancia, Double roi) {
        this.producto = producto;
        this.costoOperativo = costoOperativo;
        this.margenGanancia = margenGanancia;
        this.roi = roi;
        this.fechaCalculo = LocalDate.now(); // Se sella la fecha automáticamente
    }

    // 2. Factory Method que encapsula la LÓGICA DE NEGOCIO (Las fórmulas matemáticas)
    public static MetricaRentabilidad calcularPara(Producto producto, Double precioVenta, Double costoOperativo) {

        // Evitar división por cero si el costo o precio es 0
        if (costoOperativo == null || costoOperativo <= 0) {
            throw new IllegalArgumentException("El costo operativo debe ser mayor a cero para calcular métricas");
        }
        if (precioVenta == null || precioVenta <= 0) {
            throw new IllegalArgumentException("El precio de venta debe ser mayor a cero");
        }

        // Fórmulas financieras estándar
        // Beneficio Neto = Precio de Venta - Costo
        Double beneficioNeto = precioVenta - costoOperativo;

        // Margen de Ganancia (%) = (Beneficio Neto / Precio Venta) * 100
        Double margenCalculado = (beneficioNeto / precioVenta) * 100.0;

        // ROI (Retorno de Inversión) (%) = (Beneficio Neto / Costo) * 100
        Double roiCalculado = (beneficioNeto / costoOperativo) * 100.0;

        // Redondear a 2 decimales (opcional, pero buena práctica)
        margenCalculado = Math.round(margenCalculado * 100.0) / 100.0;
        roiCalculado = Math.round(roiCalculado * 100.0) / 100.0;

        return new MetricaRentabilidad(producto, costoOperativo, margenCalculado, roiCalculado);
    }
}