package velmora.composer.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import velmora.composer.model.Composition;
import velmora.composer.model.CompositionVersion;
import velmora.composer.repository.CompositionRepository;
import velmora.composer.repository.CompositionVersionRepository;

@Service
@RequiredArgsConstructor
public class VersionService {

  private final CompositionVersionRepository versionRepository;
  private final CompositionRepository compositionRepository;
  private final SnapshotService snapshotService;

  @Transactional
  public CompositionVersion createVersion(Composition composition, String changeDescription) {
    String snapshot = snapshotService.createSnapshot(composition);

    int nextVersion = versionRepository.countByCompositionId(composition.getId()) + 1;

    CompositionVersion version = CompositionVersion.builder()
        .compositionId(composition.getId())
        .versionNumber(nextVersion)
        .snapshotJson(snapshot)
        .changeDescription(changeDescription != null ? changeDescription : "Auto-saved")
        .createdAt(LocalDateTime.now())
        .build();

    version = versionRepository.save(version);

    composition.setVersionNumber(nextVersion);
    compositionRepository.save(composition);

    return version;
  }

  public List<CompositionVersion> getVersions(Long compositionId) {
    return versionRepository.findByCompositionIdOrderByVersionNumberDesc(compositionId);
  }

  public CompositionVersion getVersion(Long compositionId, int versionNumber) {
    return versionRepository.findByCompositionIdAndVersionNumber(compositionId, versionNumber)
        .orElse(null);
  }

  public int getVersionCount(Long compositionId) {
    return versionRepository.countByCompositionId(compositionId);
  }

  @Transactional
  public CompositionVersion restoreVersion(Long compositionId, int versionNumber) {
    CompositionVersion sourceVersion = getVersion(compositionId, versionNumber);
    if (sourceVersion == null) {
      throw new IllegalStateException("Version " + versionNumber + " not found");
    }

    Composition composition = compositionRepository.findById(compositionId)
        .orElseThrow(() -> new IllegalStateException("Composition not found"));

    Composition restored = snapshotService.restoreFromSnapshot(sourceVersion.getSnapshotJson());
    composition.setName(restored.getName());
    composition.setDescription(restored.getDescription());

    composition.getItems().clear();
    for (var item : restored.getItems()) {
      item.setCompositionId(compositionId);
      composition.getItems().add(item);
    }

    composition = compositionRepository.save(composition);
    createVersion(composition, "Restored from Version " + versionNumber);

    return versionRepository.findByCompositionIdAndVersionNumber(compositionId,
            versionRepository.countByCompositionId(compositionId))
        .orElse(null);
  }

  public VersionDiff diff(Long compositionId, int versionA, int versionB) {
    CompositionVersion vA = getVersion(compositionId, versionA);
    CompositionVersion vB = getVersion(compositionId, versionB);
    if (vA == null || vB == null) return null;

    Composition compA = snapshotService.restoreFromSnapshot(vA.getSnapshotJson());
    Composition compB = snapshotService.restoreFromSnapshot(vB.getSnapshotJson());

    VersionDiff diff = new VersionDiff();

    if (!compA.getName().equals(compB.getName())) {
      diff.descriptionChanged = true;
      diff.oldDescription = compA.getName();
      diff.newDescription = compB.getName();
    }

    if (!compA.getDescription().equals(compB.getDescription())) {
      diff.descriptionChanged = true;
    }

    var notesA = compA.getItems();
    var notesB = compB.getItems();
    var mapA = notesA.stream().collect(java.util.stream.Collectors.toMap(
        i -> i.getNoteId(), i -> i.getPercentage() != null ? i.getPercentage() : 0));
    var mapB = notesB.stream().collect(java.util.stream.Collectors.toMap(
        i -> i.getNoteId(), i -> i.getPercentage() != null ? i.getPercentage() : 0));

    for (var entry : mapB.entrySet()) {
      if (!mapA.containsKey(entry.getKey())) {
        diff.addedNotes.add(entry.getKey());
      } else if (!mapA.get(entry.getKey()).equals(entry.getValue())) {
        diff.changedPercentages.put(entry.getKey(),
            new int[]{mapA.get(entry.getKey()), entry.getValue()});
      }
    }
    for (var entry : mapA.entrySet()) {
      if (!mapB.containsKey(entry.getKey())) {
        diff.removedNotes.add(entry.getKey());
      }
    }

    return diff;
  }

  public static class VersionDiff {
    public boolean descriptionChanged;
    public String oldDescription;
    public String newDescription;
    public java.util.List<Long> addedNotes = new java.util.ArrayList<>();
    public java.util.List<Long> removedNotes = new java.util.ArrayList<>();
    public java.util.Map<Long, int[]> changedPercentages = new java.util.HashMap<>();
  }
}
