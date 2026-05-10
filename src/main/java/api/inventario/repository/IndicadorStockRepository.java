package api.inventario.repository;

import api.inventario.model.IndicadorStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IndicadorStockRepository extends JpaRepository<IndicadorStock, Long> {
    Optional<IndicadorStock> findByProducto_Sku(String sku);
    void deleteByProducto_Sku(String sku);
}