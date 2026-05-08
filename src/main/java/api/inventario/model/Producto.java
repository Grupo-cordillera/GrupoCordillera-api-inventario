package api.inventario.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "producto")
@Getter
@Setter
// Genera el constructor vacío requerido por JPA, pero lo oculta (protected)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE) // Evita que se genere un setter para el ID
    private Long id;

    @Column(name = "sku", nullable = false, unique = true)
    @Setter(AccessLevel.NONE) // Evita que se genere un setter para el SKU (no debería cambiar)
    private String sku;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    // 1. Constructor privado usado EXCLUSIVAMENTE por el Factory Method
    private Producto(String sku, String nombre, String descripcion) {
        this.sku = sku;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public static Producto crearNuevo(String nombre, String descripcion) {
        // Lógica de negocio: generamos un SKU único automáticamente
        String skuGenerado = "PROD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        return new Producto(skuGenerado, nombre, descripcion);
    }
}