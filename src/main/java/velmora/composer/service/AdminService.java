package velmora.composer.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import velmora.composer.model.Note;
import velmora.composer.model.NoteType;
import velmora.composer.repository.NoteRepository;

@Service
@RequiredArgsConstructor
public class AdminService {

  private final NoteRepository noteRepository;

  public List<Note> getAllNotes() {
    return noteRepository.findAll();
  }

  public Note saveNote(Note note) {
    return noteRepository.save(note);
  }

  public void deleteNote(Note note) {
    noteRepository.delete(note);
  }

  public Note buildNote(Long existingId, String name, String category, String type,
      int intensity, String color, String description) {
    Note note;
    if (existingId != null) {
      note = noteRepository.findById(existingId)
          .orElse(new Note());
    } else {
      note = new Note();
    }
    note.setName(name);
    note.setCategory(category);
    if (type != null) {
      note.setType(NoteType.valueOf(type));
    }
    note.setIntensity(intensity);
    note.setColorCode(color);
    note.setDescription(description);
    return note;
  }
}
