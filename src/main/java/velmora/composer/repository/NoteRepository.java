package velmora.composer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import velmora.composer.model.Note;
import velmora.composer.model.NoteType;
import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
  List<Note> findByType(NoteType type);
}