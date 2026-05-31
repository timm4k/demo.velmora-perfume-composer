package velmora.composer.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import velmora.composer.model.Composition;
import velmora.composer.model.CompositionItem;
import velmora.composer.model.CompositionVersion;
import velmora.composer.model.Note;
import velmora.composer.repository.CompositionRepository;
import velmora.composer.repository.CompositionVersionRepository;
import velmora.composer.repository.NoteRepository;
import velmora.composer.model.CompositionVersion;

@Slf4j
@Service
@RequiredArgsConstructor
public class VersionService {

  private final CompositionVersionRepository versionRepository;
  private final CompositionRepository compositionRepository;
  private final SnapshotService snapshotService;
  private final NoteRepository noteRepository;

  @PersistenceContext
  private EntityManager entityManager;

  @Transactional
  public CompositionVersion createVersion(Composition composition, String changeDescription) {
    return doCreateVersion(composition, changeDescription);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public CompositionVersion createVersionInNewTransaction(
      Composition composition, String changeDescription) {
    return doCreateVersion(composition, changeDescription);
  }

  @Transactional(readOnly = true)
  public List<CompositionVersion> getVersions(Long compositionId) {
    return versionRepository.findByCompositionIdOrderByVersionNumberDesc(compositionId);
  }

  @Transactional(readOnly = true)
  public CompositionVersion getVersion(Long compositionId, int versionNumber) {
    return versionRepository
        .findByCompositionIdAndVersionNumber(compositionId, versionNumber)
        .orElse(null);
  }

  @Transactional(readOnly = true)
  public int getVersionCount(Long compositionId) {
    return versionRepository.countByCompositionId(compositionId);
  }

  @Transactional(readOnly = true)
  public int getLatestVersionNumber(Long compositionId) {
    return versionRepository.getMaxVersionNumber(compositionId);
  }

  @Transactional
  public CompositionVersion restoreVersion(Long compositionId, int versionNumber) {
    CompositionVersion source = getVersion(compositionId, versionNumber);
    if (source == null) {
      throw new IllegalStateException("Version " + versionNumber + " not found");
    }

    Composition composition = compositionRepository.findWithItemsById(compositionId)
        .orElseThrow(() -> new IllegalStateException("Composition not found: " + compositionId));

    Composition restored = snapshotService.restoreFromSnapshot(source.getSnapshotJson());
    composition.setName(restored.getName());
    composition.setDescription(restored.getDescription());

    composition.getItems().clear();
    entityManager.flush();

    for (CompositionItem item : restored.getItems()) {
      item.setCompositionId(compositionId);
      if (item.getNote() == null && item.getNoteId() != null) {
        item.setNote(entityManager.getReference(Note.class, item.getNoteId()));
      }
      composition.getItems().add(item);
    }

    composition = compositionRepository.save(composition);
    log.info("[VERSION] Restored composition {} to version {}", compositionId, versionNumber);

    return doCreateVersion(composition, "Restored from Version " + versionNumber);
  }

  @Transactional(readOnly = true)
  public VersionDiff diff(Long compositionId, int versionA, int versionB) {
    CompositionVersion vA = getVersion(compositionId, versionA);
    CompositionVersion vB = getVersion(compositionId, versionB);
    if (vA == null || vB == null) return null;

    Composition compA = snapshotService.restoreFromSnapshot(vA.getSnapshotJson());
    Composition compB = snapshotService.restoreFromSnapshot(vB.getSnapshotJson());

    VersionDiff diff = new VersionDiff();

    if (!compA.getName().equals(compB.getName())) {
      diff.nameChanged = true;
      diff.oldName = compA.getName();
      diff.newName = compB.getName();
    }

    String descA = compA.getDescription() != null ? compA.getDescription() : "";
    String descB = compB.getDescription() != null ? compB.getDescription() : "";
    if (!descA.equals(descB)) {
      diff.descriptionChanged = true;
      diff.oldDescription = descA;
      diff.newDescription = descB;
    }

    Map<Long, Integer> mapA = toPercentageMap(compA.getItems());
    Map<Long, Integer> mapB = toPercentageMap(compB.getItems());

    for (var entry : mapB.entrySet()) {
      if (!mapA.containsKey(entry.getKey())) {
        diff.addedNoteIds.add(entry.getKey());
      } else if (!mapA.get(entry.getKey()).equals(entry.getValue())) {
        diff.changedPercentages.put(entry.getKey(),
            new int[]{mapA.get(entry.getKey()), entry.getValue()});
      }
    }
    for (Long id : mapA.keySet()) {
      if (!mapB.containsKey(id)) {
        diff.removedNoteIds.add(id);
      }
    }

    return diff;
  }

  @Transactional(readOnly = true)
  public String generateChangeDescription(Long compositionId, Composition newComposition) {
    int latestVer = getLatestVersionNumber(compositionId);
    if (latestVer == 0) return "Initial creation";

    CompositionVersion prevVersion = getVersion(compositionId, latestVer);
    if (prevVersion == null) return "Saved";

    Composition prevComposition = snapshotService.restoreFromSnapshot(prevVersion.getSnapshotJson());

    StringBuilder sb = new StringBuilder();

    if (!prevComposition.getName().equals(newComposition.getName())) {
      sb.append("Renamed to \"").append(newComposition.getName()).append("\". ");
    }

    Map<Long, Integer> prevMap = toPercentageMap(prevComposition.getItems());
    Map<Long, Integer> newMap = toPercentageMap(newComposition.getItems());

    List<String> added = new ArrayList<>();
    List<String> removed = new ArrayList<>();
    List<String> changed = new ArrayList<>();

    for (var entry : newMap.entrySet()) {
      Long noteId = entry.getKey();
      String noteName = noteName(noteId);
      Integer prevPct = prevMap.get(noteId);
      if (prevPct == null) {
        added.add(noteName);
      } else if (!prevPct.equals(entry.getValue())) {
        changed.add(noteName + " " + prevPct + "%\u2192" + entry.getValue() + "%");
      }
    }
    for (Long id : prevMap.keySet()) {
      if (!newMap.containsKey(id)) {
        removed.add(noteName(id));
      }
    }

    if (!added.isEmpty()) sb.append("Added: ").append(String.join(", ", added)).append(". ");
    if (!removed.isEmpty()) sb.append("Removed: ").append(String.join(", ", removed)).append(". ");
    if (!changed.isEmpty()) sb.append("Changed: ").append(String.join(", ", changed)).append(". ");

    String result = sb.toString().trim();
    return result.isEmpty() ? "No changes" : result;
  }

  private String noteName(Long noteId) {
    return noteRepository.findById(noteId)
        .map(Note::getName)
        .orElse("note#" + noteId);
  }

  private CompositionVersion doCreateVersion(
      Composition composition, String changeDescription) {
    if (composition.getId() == null) {
      log.error("[VERSION] Cannot create version: composition ID is null");
      throw new IllegalArgumentException(
          "Cannot create version for unsaved composition");
    }

    log.info("[VERSION] doCreateVersion: composition id={}, creating snapshot...",
        composition.getId());

    String snapshot;
    try {
      snapshot = snapshotService.createSnapshot(composition);
      log.info("[VERSION] Snapshot created successfully for composition id={}",
          composition.getId());
    } catch (Exception e) {
      log.error("[VERSION] Snapshot creation FAILED for composition {}: {}",
          composition.getId(), e.getMessage(), e);
      throw e;
    }

    int maxVer = versionRepository.getMaxVersionNumber(composition.getId());
    int nextVersion = maxVer + 1;
    log.info("[VERSION] Max version for composition id={} is {}, next will be {}",
        composition.getId(), maxVer, nextVersion);

    CompositionVersion version = CompositionVersion.builder()
        .compositionId(composition.getId())
        .versionNumber(nextVersion)
        .snapshotJson(snapshot)
        .changeDescription(changeDescription != null ? changeDescription : "Auto-saved")
        .createdAt(LocalDateTime.now())
        .build();

    try {
      version = versionRepository.save(version);
      log.info("[VERSION] SUCCESS: Created v{} for composition id={}",
          nextVersion, composition.getId());
    } catch (Exception e) {
      log.error("[VERSION] SAVE FAILED for version of composition {}: {}",
          composition.getId(), e.getMessage(), e);
      throw e;
    }

    return version;
  }

  private Map<Long, Integer> toPercentageMap(List<CompositionItem> items) {
    if (items == null) return Map.of();
    return items.stream()
        .filter(i -> i.getNoteId() != null)
        .collect(Collectors.toMap(
            CompositionItem::getNoteId,
            i -> i.getPercentage() != null ? i.getPercentage() : 0,
            (a, b) -> a
        ));
  }

  public static class VersionDiff {
    public boolean nameChanged;
    public String  oldName;
    public String  newName;
    public boolean descriptionChanged;
    public String  oldDescription;
    public String  newDescription;
    public List<Long>        addedNoteIds       = new ArrayList<>();
    public List<Long>        removedNoteIds     = new ArrayList<>();
    public Map<Long, int[]>  changedPercentages = new HashMap<>();

    public boolean isEmpty() {
      return !nameChanged && !descriptionChanged
          && addedNoteIds.isEmpty()
          && removedNoteIds.isEmpty()
          && changedPercentages.isEmpty();
    }
  }
}
