package velmora.composer.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import velmora.composer.model.Composition;
import velmora.composer.model.CompositionItem;
import velmora.composer.repository.NoteRepository;

@Service
@RequiredArgsConstructor
public class SnapshotService {

  private final NoteRepository noteRepository;
  private final ObjectMapper objectMapper;

  public String createSnapshot(Composition composition) {
    try {
      Map<String, Object> snapshot = new HashMap<>();
      snapshot.put("name", composition.getName());
      snapshot.put("description", composition.getDescription() != null ? composition.getDescription() : "");
      snapshot.put("status", composition.getStatus() != null ? composition.getStatus().name() : "DRAFT");
      snapshot.put("isPublic", composition.isPublic());
      snapshot.put("isFavorite", composition.isFavorite());

      List<Map<String, Object>> notesData = composition.getItems().stream()
          .map(item -> {
            Map<String, Object> noteData = new HashMap<>();
            noteData.put("noteId", item.getNoteId());
            noteData.put("percentage", item.getPercentage());
            if (item.getNote() != null) {
              noteData.put("name", item.getNote().getName());
              noteData.put("type", item.getNote().getType() != null ? item.getNote().getType().name() : null);
              noteData.put("category", item.getNote().getCategory());
            }
            return noteData;
          })
          .collect(Collectors.toList());
      snapshot.put("notes", notesData);

      return objectMapper.writeValueAsString(snapshot);
    } catch (Exception e) {
      throw new RuntimeException("Failed to create snapshot", e);
    }
  }

  public Composition restoreFromSnapshot(String snapshotJson) {
    try {
      Map<String, Object> data = objectMapper.readValue(snapshotJson, new TypeReference<Map<String, Object>>() {});

      Composition composition = new Composition();
      composition.setName((String) data.getOrDefault("name", "Untitled"));
      composition.setDescription((String) data.getOrDefault("description", ""));

      @SuppressWarnings("unchecked")
      List<Map<String, Object>> notesData = (List<Map<String, Object>>) data.getOrDefault("notes", List.of());

      for (Map<String, Object> noteData : notesData) {
        Long noteId = ((Number) noteData.get("noteId")).longValue();
        int percentage = ((Number) noteData.getOrDefault("percentage", 0)).intValue();
        CompositionItem item = new CompositionItem();
        item.setNoteId(noteId);
        item.setPercentage(percentage);
        composition.getItems().add(item);
      }

      return composition;
    } catch (Exception e) {
      throw new RuntimeException("Failed to restore from snapshot", e);
    }
  }
}
