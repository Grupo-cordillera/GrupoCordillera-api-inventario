package api.inventario.repository;

import api.inventario.model.ItemInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemInventarioRepository extends JpaRepository<ItemInventario, Long> {
    List<ItemInventario> findByProducto_Sku(String sku);
}