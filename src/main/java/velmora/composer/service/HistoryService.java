package velmora.composer.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import velmora.composer.model.Composition;
import velmora.composer.model.CompositionItem;
import velmora.composer.model.CompositionStatus;
import velmora.composer.repository.CompositionRepository;
import velmora.composer.repository.CompositionVersionRepository;

@Service
@RequiredArgsConstructor
public class HistoryService {

  private final CompositionRepository compositionRepository;
  private final CompositionVersionRepository versionRepository;
  private final VersionService versionService;

  public List<Composition> getUserCompositions(Long userId) {
    return compositionRepository.findByUserIdOrderByUpdatedAtDesc(userId);
  }

  public List<Composition> getFilteredCompositions(Long userId, String search, CompositionStatus status,
      Boolean favoritesOnly, Boolean publicOnly,
      String sortBy) {
    List<Composition> compositions = compositionRepository.findByUserIdOrderByUpdatedAtDesc(userId);

    if (status != null) {
      compositions = compositions.stream()
          .filter(c -> status.equals(c.getStatus()))
          .collect(Collectors.toList());
    }

    if (Boolean.TRUE.equals(favoritesOnly)) {
      compositions = compositions.stream()
          .filter(Composition::isFavorite)
          .collect(Collectors.toList());
    }

    if (Boolean.TRUE.equals(publicOnly)) {
      compositions = compositions.stream()
          .filter(Composition::isPublic)
          .collect(Collectors.toList());
    }

    if (search != null && !search.isBlank()) {
      String lowerSearch = search.toLowerCase();
      compositions = compositions.stream()
          .filter(c -> c.getName() != null && c.getName().toLowerCase().contains(lowerSearch))
          .collect(Collectors.toList());
    }

    if (sortBy != null) {
      compositions = switch (sortBy) {
        case "oldest" -> compositions.stream()
            .sorted(Comparator.comparing(Composition::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
            .collect(Collectors.toList());
        case "mostNotes" -> compositions.stream()
            .sorted(Comparator.<Composition, Integer>comparing(
                c -> c.getItems() != null ? c.getItems().size() : 0, Comparator.reverseOrder()))
            .collect(Collectors.toList());
        case "highestBalance" -> compositions.stream()
            .sorted(Comparator.<Composition, Integer>comparing(
                c -> balanceScore(c), Comparator.reverseOrder()))
            .collect(Collectors.toList());
        default -> compositions;
      };
    }

    return compositions;
  }

  public void toggleFavorite(Long compositionId) {
    compositionRepository.findById(compositionId).ifPresent(comp -> {
      comp.setFavorite(!comp.isFavorite());
      compositionRepository.save(comp);
    });
  }

  public void setStatus(Long compositionId, CompositionStatus status) {
    compositionRepository.findById(compositionId).ifPresent(comp -> {
      comp.setStatus(status);
      compositionRepository.save(comp);
    });
  }

  public void setPublic(Long compositionId, boolean isPublic) {
    compositionRepository.findById(compositionId).ifPresent(comp -> {
      comp.setPublic(isPublic);
      compositionRepository.save(comp);
    });
  }

  public Composition duplicateComposition(Long compositionId) {
    Composition original = compositionRepository.findById(compositionId)
        .orElseThrow(() -> new IllegalStateException("Composition not found"));

    Composition copy = new Composition();
    copy.setName((original.getName() != null ? original.getName() : "Untitled") + " (Copy)");
    copy.setDescription(original.getDescription());
    copy.setUser(original.getUser());
    copy.setStatus(CompositionStatus.DRAFT);
    copy.setPublic(false);
    copy.setFavorite(false);
    copy.setCreatedAt(LocalDateTime.now());
    copy.setUpdatedAt(LocalDateTime.now());

    if (original.getItems() != null) {
      for (var item : original.getItems()) {
        CompositionItem newItem = new CompositionItem();
        newItem.setNoteId(item.getNoteId());
        newItem.setPercentage(item.getPercentage());
        copy.getItems().add(newItem);
      }
    }

    return compositionRepository.save(copy);
  }

  public void deleteComposition(Long compositionId) {
    compositionRepository.deleteById(compositionId);
  }

  public CompositionStats getStats(Long compositionId) {
    Composition comp = compositionRepository.findById(compositionId).orElse(null);
    if (comp == null) return null;

    CompositionStats stats = new CompositionStats();
    stats.createdAt = comp.getCreatedAt();
    stats.lastModified = comp.getUpdatedAt();
    stats.versionCount = versionService.getVersionCount(compositionId);
    stats.noteCount = comp.getItems() != null ? comp.getItems().size() : 0;

    if (comp.getItems() != null && !comp.getItems().isEmpty()) {
      stats.balanceScore = comp.getItems().stream()
          .filter(i -> i.getPercentage() != null)
          .mapToInt(CompositionItem::getPercentage)
          .sum();
      stats.dominantFamily = comp.getItems().stream()
          .filter(i -> i.getNote() != null && i.getNote().getCategory() != null)
          .collect(Collectors.groupingBy(i -> i.getNote().getCategory(), Collectors.counting()))
          .entrySet().stream()
          .max(Comparator.comparingLong(Map.Entry::getValue))
          .map(Map.Entry::getKey)
          .orElse(null);
    }

    return stats;
  }

  private int balanceScore(Composition c) {
    if (c.getItems() == null || c.getItems().isEmpty()) return 0;
    int total = c.getItems().stream()
        .filter(i -> i.getPercentage() != null)
        .mapToInt(CompositionItem::getPercentage)
        .sum();
    return Math.max(0, 100 - Math.abs(total - 100));
  }

  public static class CompositionStats {
    public LocalDateTime createdAt;
    public LocalDateTime lastModified;
    public int versionCount;
    public int noteCount;
    public int balanceScore;
    public String dominantFamily;
  }
}