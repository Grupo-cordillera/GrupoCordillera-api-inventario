package api.inventario.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "item_inventario")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    // Relación vinculada por SKU
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku", referencedColumnName = "sku", nullable = false)
    @Setter(AccessLevel.NONE)
    private Producto producto;

    private String origen;

    private Integer cantidad;

    @Column(name = "ultima_actualizacion")
    private LocalDateTime ultimaActualizacion;

    // Constructor privado para el Factory
    private ItemInventario(Producto producto, String origen, Integer cantidad) {
        this.producto = producto;
        this.origen = origen;
        this.cantidad = cantidad;
        this.ultimaActualizacion = LocalDateTime.now();
    }

    // Static Factory Method
    public static ItemInventario registrarEntrada(Producto producto, String origen, Integer cantidad) {
        return new ItemInventario(producto, origen, cantidad);
    }
}