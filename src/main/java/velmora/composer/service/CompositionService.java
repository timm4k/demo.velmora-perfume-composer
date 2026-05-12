package velmora.composer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import velmora.composer.model.Composition;
import velmora.composer.model.CompositionItem;
import velmora.composer.model.NoteType;
import velmora.composer.repository.CompositionRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompositionService {

  private final CompositionRepository compositionRepository;

  @Transactional
  public Composition saveComposition(Composition composition) {
    validateTotalPercentage(composition.getItems());
    composition.setUpdatedAt(LocalDateTime.now());
    return compositionRepository.save(composition);
  }

  private void validateTotalPercentage(List<CompositionItem> items) {
    if (items == null || items.isEmpty()) {
      throw new IllegalStateException("Composition must have at least one note");
    }

    double total = items.stream()
        .mapToDouble(CompositionItem::getPercentage)
        .sum();

    if (Math.abs(total - 100.0) > 0.001) {
      throw new IllegalStateException("Composition balance error: Total percentage must be 100%");
    }
  }

  public Map<NoteType, List<CompositionItem>> getFragrancePyramid(Composition composition) {
    return composition.getItems().stream()
        .collect(Collectors.groupingBy(item -> item.getNote().getType()));
  }

  public String analyzeCompatibility(Composition composition) {
    List<CompositionItem> items = composition.getItems();

    long topNotesCount = items.stream()
        .filter(i -> i.getNote().getType() == NoteType.TOP).count();
    long baseNotesCount = items.stream()
        .filter(i -> i.getNote().getType() == NoteType.BASE).count();

    if (topNotesCount == 0) return "Warning: Missing top notes. The scent may feel too heavy initially.";
    if (baseNotesCount == 0) return "Warning: No base notes detected. The fragrance will lack longevity.";

    return "Composition is balanced. Pyramid structure is correct.";
  }
}