package velmora.composer.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import velmora.composer.model.Composition;
import velmora.composer.model.CompositionItem;
import velmora.composer.model.NoteType;
import velmora.composer.model.User;
import velmora.composer.repository.CompositionRepository;
import velmora.composer.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CompositionService {

  private final CompositionRepository compositionRepository;
  private final UserRepository userRepository;

  @Transactional
  public Composition saveComposition(Composition composition) {
    List<CompositionItem> items = composition.getItems();

    if (composition.getName() == null || composition.getName().trim().isEmpty()) {
      throw new IllegalStateException("Composition name must not be empty");
    }
    if (items == null || items.isEmpty()) {
      throw new IllegalStateException("Composition must have at least one note");
    }
    if (items.size() > 50) {
      throw new IllegalStateException("Composition cannot exceed 50 notes");
    }

    int totalPercentage = 0;
    for (CompositionItem item : items) {
      if (item.getPercentage() != null) {
        if (item.getPercentage() < 0 || item.getPercentage() > 100) {
          throw new IllegalStateException("Percentage must be between 0 and 100");
        }
        totalPercentage += item.getPercentage();
      }
    }
    if (totalPercentage > 100) {
      throw new IllegalStateException("Total percentage exceeds 100%");
    }

    if (composition.getUser() != null && composition.getUser().getId() != null) {
      User managed = userRepository.getReferenceById(composition.getUser().getId());
      composition.setUser(managed);
    }

    composition.setUpdatedAt(LocalDateTime.now());

    List<CompositionItem> detached = new ArrayList<>(items);
    composition.getItems().clear();

    Composition saved = compositionRepository.save(composition);

    for (CompositionItem item : detached) {
      item.setCompositionId(saved.getId());
      saved.getItems().add(item);
    }

    return compositionRepository.save(saved);
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

    if (topNotesCount == 0) {
      return "Warning: Missing top notes. The scent may feel too heavy initially";
    }
    if (baseNotesCount == 0) {
      return "Warning: No base notes detected. The fragrance will lack longevity";
    }

    return "Composition is balanced. Pyramid structure is correct";
  }
}
