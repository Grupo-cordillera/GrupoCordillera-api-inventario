package api.inventario.repository;

import api.inventario.model.MetricaRentabilidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetricaRepository extends JpaRepository<MetricaRentabilidad, Long> {
    List<MetricaRentabilidad> findByProducto_Sku(String sku);
    void deleteByProducto_Sku(String sku);
}