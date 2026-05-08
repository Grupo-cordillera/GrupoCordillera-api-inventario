package api.inventario.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "indicador_stock")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IndicadorStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    // Relación OneToOne (o ManyToOne según necesites) vinculada por SKU
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku", referencedColumnName = "sku", nullable = false)
    @Setter(AccessLevel.NONE)
    private Producto producto;

    @Column(name = "stock_total_consolidado")
    private Integer stockTotalConsolidado;

    @Column(name = "umbral_minimo")
    private Integer umbralMinimo;

    private String estado;

    // Constructor privado para el Factory
    private IndicadorStock(Producto producto, Integer umbralMinimo) {
        this.producto = producto;
        this.stockTotalConsolidado = 0; // Empieza en cero
        this.umbralMinimo = umbralMinimo;
        this.estado = "SIN_STOCK";
    }

    // Static Factory Method
    public static IndicadorStock inicializarPara(Producto producto, Integer umbralMinimo) {
        return new IndicadorStock(producto, umbralMinimo);
    }
}