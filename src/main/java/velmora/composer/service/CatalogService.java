package velmora.composer.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import velmora.composer.model.Perfume;
import velmora.composer.repository.PerfumeRepository;

@Service
@RequiredArgsConstructor
public class CatalogService {

  private final PerfumeRepository perfumeRepository;

  public List<Perfume> getAllPerfumes() {
    return perfumeRepository.findAll();
  }

  public List<Perfume> searchByName(String query) {
    if (query == null || query.isBlank()) {
      return perfumeRepository.findAll();
    }
    String lower = query.toLowerCase();
    return perfumeRepository.findAll().stream()
        .filter(p -> p.getName() != null && p.getName().toLowerCase().contains(lower))
        .collect(Collectors.toList());
  }

  public List<Perfume> search(String text, String brand, String season) {
    return perfumeRepository.findAll().stream()
        .filter(p -> text == null || text.isBlank()
            || (p.getName() != null && p.getName().toLowerCase().contains(text.toLowerCase())))
        .filter(p -> brand == null || brand.isBlank()
            || (p.getBrand() != null && p.getBrand().equalsIgnoreCase(brand)))
        .filter(p -> season == null || season.isBlank()
            || (p.getSeason() != null && p.getSeason().equalsIgnoreCase(season)))
        .collect(Collectors.toList());
  }
}
