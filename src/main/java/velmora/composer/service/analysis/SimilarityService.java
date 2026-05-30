package velmora.composer.service.analysis;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import velmora.composer.model.Composition;
import velmora.composer.model.CompositionItem;
import velmora.composer.model.Note;
import velmora.composer.model.NoteType;
import velmora.composer.model.Perfume;
import velmora.composer.repository.CompositionRepository;
import velmora.composer.repository.NoteRepository;

@Service
@RequiredArgsConstructor
public class SimilarityService {

  private final CompositionRepository compositionRepository;
  private final NoteRepository noteRepository;

  public int calculateMatchPercentage(Set<String> composerNotes, Set<String> perfumeNotes) {
    if (composerNotes.isEmpty() || perfumeNotes.isEmpty()) return 0;
    Set<String> intersection = new HashSet<>(composerNotes);
    intersection.retainAll(perfumeNotes);
    Set<String> union = new HashSet<>(composerNotes);
    union.addAll(perfumeNotes);
    return union.isEmpty() ? 0 : (int) ((double) intersection.size() / union.size() * 100);
  }

  public Set<String> collectPerfumeNotes(Perfume perfume) {
    Set<String> result = new HashSet<>();
    String[] compNotes = loadCompositionNotes(perfume.getId());
    boolean hasCompNotes = compNotes[0] != null || compNotes[1] != null || compNotes[2] != null;
    if (hasCompNotes) {
      for (String notes : compNotes) {
        if (notes != null) result.addAll(splitNotes(notes));
      }
    } else {
      if (perfume.getTopNotes()   != null) result.addAll(splitNotes(perfume.getTopNotes()));
      if (perfume.getHeartNotes() != null) result.addAll(splitNotes(perfume.getHeartNotes()));
      if (perfume.getBaseNotes()  != null) result.addAll(splitNotes(perfume.getBaseNotes()));
    }
    return result;
  }

  public String[] loadCompositionNotes(Long perfumeId) {
    String[] result = new String[]{null, null, null};
    try {
      Optional<Composition> compOpt = compositionRepository.findByPerfumeId(perfumeId);
      if (compOpt.isEmpty()) return result;

      Composition comp = compOpt.get();
      List<CompositionItem> items = comp.getItems();
      if (items == null || items.isEmpty()) return result;

      List<Long> noteIds = items.stream()
          .map(CompositionItem::getNoteId)
          .collect(Collectors.toList());

      Map<Long, Note> noteById = noteRepository.findAllById(noteIds).stream()
          .collect(Collectors.toMap(Note::getId, n -> n));

      StringBuilder top   = new StringBuilder();
      StringBuilder heart = new StringBuilder();
      StringBuilder base  = new StringBuilder();

      for (CompositionItem item : items) {
        Note note = noteById.get(item.getNoteId());
        if (note == null || note.getType() == null) continue;
        String name = note.getName();
        switch (note.getType()) {
          case TOP   -> appendWithComma(top,   name);
          case HEART -> appendWithComma(heart, name);
          case BASE  -> appendWithComma(base,  name);
        }
      }

      if (top.length()   > 0) result[0] = top.toString();
      if (heart.length() > 0) result[1] = heart.toString();
      if (base.length()  > 0) result[2] = base.toString();

    } catch (Exception e) {
      System.err.println("[SIMILARITY] Failed to load composition for perfume "
          + perfumeId + ": " + e.getMessage());
    }
    return result;
  }

  public List<String> splitNotes(String text) {
    return Arrays.stream(text.split(","))
        .map(String::trim)
        .map(String::toLowerCase)
        .filter(s -> !s.isEmpty())
        .collect(Collectors.toList());
  }

  private void appendWithComma(StringBuilder sb, String text) {
    if (sb.length() > 0) sb.append(", ");
    sb.append(text);
  }
}