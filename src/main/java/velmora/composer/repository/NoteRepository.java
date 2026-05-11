package velmora.composer.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import velmora.composer.model.Note;
import velmora.composer.model.NoteCategory;
import velmora.composer.model.NoteType;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
  List<Note> findByType(NoteType type);

  List<Note> findByCategory(NoteCategory category);
}
