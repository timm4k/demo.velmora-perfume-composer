package velmora.composer.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import velmora.composer.model.Composition;
import velmora.composer.model.CompositionItem;
import velmora.composer.model.CompositionStatus;
import velmora.composer.model.User;
import velmora.composer.repository.CompositionRepository;
import velmora.composer.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CompositionService {

  private final CompositionRepository compositionRepository;
  private final UserRepository userRepository;
  private final VersionService versionService;

  @Transactional
  public Composition saveComposition(Composition composition) {
    return saveComposition(composition, null);
  }

  @Transactional
  public Composition saveComposition(Composition composition, String changeDescription) {
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

    if (composition.getStatus() == null) {
      composition.setStatus(CompositionStatus.DRAFT);
    }

    composition.setUpdatedAt(LocalDateTime.now());

    List<CompositionItem> detached = new ArrayList<>(items);
    composition.getItems().clear();

    Composition saved = compositionRepository.save(composition);

    for (CompositionItem item : detached) {
      item.setCompositionId(saved.getId());
      saved.getItems().add(item);
    }

    Composition result = compositionRepository.save(saved);

    try {
      versionService.createVersion(result, changeDescription != null ? changeDescription : "Saved");
    } catch (Exception e) {
      System.err.println("[COMPOSITION] Version creation failed: " + e.getMessage());
    }

    return result;
  }

}
