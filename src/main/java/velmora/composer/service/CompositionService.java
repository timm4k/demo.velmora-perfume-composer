package velmora.composer.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import velmora.composer.model.Composition;
import velmora.composer.model.CompositionItem;
import velmora.composer.model.CompositionStatus;
import velmora.composer.model.Note;
import velmora.composer.model.Role;
import velmora.composer.model.User;
import velmora.composer.repository.CompositionRepository;
import velmora.composer.repository.UserRepository;
import velmora.composer.ui.UserSession;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompositionService {

  private final CompositionRepository compositionRepository;
  private final UserRepository userRepository;
  private final VersionService versionService;
  private final UserSession userSession;

  @PersistenceContext
  private EntityManager entityManager;

  @Transactional(readOnly = true)
  public Composition getAccessibleComposition(Long compositionId) {
    Composition composition = compositionRepository.findWithItemsById(compositionId)
        .orElseThrow(() -> new IllegalStateException("Composition not found: " + compositionId));

    if (composition.isPublic()) return composition;

    Long currentUserId = userSession.getUserId();
    if (currentUserId != null) {
      if (composition.getUser() != null
          && composition.getUser().getId().equals(currentUserId)) {
        return composition;
      }
      if (userSession.getRole() == Role.ADMIN) {
        return composition;
      }
    }
    throw new IllegalStateException("You do not have access to this composition");
  }

  @Transactional
  public Composition saveComposition(Composition composition) {
    return saveComposition(composition, null);
  }

  @Transactional
  public Composition saveComposition(Composition composition, String changeDescription) {
    validate(composition);

    Composition saved;
    boolean isNew = composition.getId() == null;
    log.info("[SAVE] Starting saveComposition for composition id={} (isNew={})",
        composition.getId(), isNew);

    if (!isNew) {
      saved = updateExisting(composition);
    } else {
      saved = createNew(composition);
    }

    log.info("[SAVE] Composition saved successfully id={}, now creating version...", saved.getId());

    String description = versionService.generateChangeDescription(saved.getId(), saved);
    versionService.createVersion(saved, description);
    log.info("[SAVE] Version CREATED for composition id={}", saved.getId());

    return saved;
  }

  private void validate(Composition composition) {
    if (composition.getName() == null || composition.getName().isBlank()) {
      throw new IllegalStateException("Composition name must not be empty");
    }
    List<CompositionItem> items = composition.getItems();
    if (items == null || items.isEmpty()) {
      throw new IllegalStateException("Composition must have at least one note");
    }
    if (items.size() > 50) {
      throw new IllegalStateException("Composition cannot exceed 50 notes");
    }
    int totalPct = items.stream()
        .mapToInt(i -> i.getPercentage() != null ? i.getPercentage() : 0)
        .sum();
    if (totalPct > 100) {
      throw new IllegalStateException(
          "Total percentage exceeds 100% (current: " + totalPct + "%)");
    }
    for (CompositionItem item : items) {
      if (item.getPercentage() != null
          && (item.getPercentage() < 0 || item.getPercentage() > 100)) {
        throw new IllegalStateException("Each percentage must be between 0 and 100");
      }
    }
  }

  private Composition updateExisting(Composition incoming) {
    Composition managed = compositionRepository.findWithItemsById(incoming.getId())
        .orElseThrow(() -> new IllegalStateException(
            "Composition not found: " + incoming.getId()));

    Long currentUserId = userSession.getUserId();
    if (currentUserId != null
        && managed.getUser() != null
        && !managed.getUser().getId().equals(currentUserId)
        && userSession.getRole() != Role.ADMIN) {
      throw new IllegalStateException("You do not have permission to edit this composition");
    }

    if (incoming.getUser() != null && incoming.getUser().getId() != null) {
      managed.setUser(userRepository.getReferenceById(incoming.getUser().getId()));
    }

    managed.setName(incoming.getName());
    if (incoming.getDescription() != null) {
      managed.setDescription(incoming.getDescription());
    }
    if (incoming.getStatus() != null) {
      managed.setStatus(incoming.getStatus());
    }
    managed.setPublic(incoming.isPublic());
    managed.setUpdatedAt(LocalDateTime.now());

    managed.getItems().clear();
    entityManager.flush();

    for (CompositionItem item : incoming.getItems()) {
      item.setCompositionId(managed.getId());
      resolveNoteReference(item);
      managed.getItems().add(item);
    }

    Composition result = compositionRepository.save(managed);
    entityManager.flush();
    log.info("[COMPOSITION] Updated id={} name='{}'",
        result.getId(), result.getName());
    return result;
  }

  private Composition createNew(Composition composition) {
    if (composition.getUser() != null && composition.getUser().getId() != null) {
      composition.setUser(userRepository.getReferenceById(
          composition.getUser().getId()));
    }
    if (composition.getStatus() == null) {
      composition.setStatus(CompositionStatus.DRAFT);
    }
    composition.setUpdatedAt(LocalDateTime.now());

    for (CompositionItem item : composition.getItems()) {
      resolveNoteReference(item);
    }

    Composition result = compositionRepository.save(composition);
    entityManager.flush();
    log.info("[COMPOSITION] Created id={} name='{}'",
        result.getId(), result.getName());
    return result;
  }

  private void resolveNoteReference(CompositionItem item) {
    if (item.getNote() == null && item.getNoteId() != null) {
      Note note = entityManager.find(Note.class, item.getNoteId());
      if (note != null) {
        item.setNote(note);
      }
    }
  }
}
