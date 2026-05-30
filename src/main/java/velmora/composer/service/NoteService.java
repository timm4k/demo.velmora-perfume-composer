package velmora.composer.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import velmora.composer.model.Note;
import velmora.composer.model.NoteType;
import velmora.composer.repository.NoteRepository;

@Service
@RequiredArgsConstructor
public class NoteService {

  private final NoteRepository noteRepository;

  public List<Note> getAllNotes() {
    return noteRepository.findAll();
  }

  public List<Note> getByType(NoteType type) {
    return noteRepository.findByType(type);
  }

  public Note getById(Long id) {
    return noteRepository.findById(id).orElse(null);
  }

  public List<Note> getTopNotes() {
    return noteRepository.findByType(NoteType.TOP);
  }

  public List<Note> getHeartNotes() {
    return noteRepository.findByType(NoteType.HEART);
  }

  public List<Note> getBaseNotes() {
    return noteRepository.findByType(NoteType.BASE);
  }
}
