package velmora.composer.repository;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import velmora.composer.model.Perfume;

@Repository
public interface PerfumeRepository extends JpaRepository<Perfume, Long> {
  List<List<Perfume>> findByBrand(String brand);

  List<Perfume> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
}
