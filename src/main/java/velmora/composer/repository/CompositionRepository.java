package velmora.composer.repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import velmora.composer.model.Composition;
import velmora.composer.model.CompositionStatus;

@Repository
public interface CompositionRepository extends JpaRepository<Composition, Long> {

  @EntityGraph(attributePaths = {"items", "items.note"})
  @Query("SELECT c FROM Composition c WHERE c.id = :id")
  Optional<Composition> findWithItemsById(@Param("id") Long id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @EntityGraph(attributePaths = {"items", "items.note"})
  @Query("SELECT c FROM Composition c WHERE c.id = :id")
  Optional<Composition> findByIdForUpdate(@Param("id") Long id);

  @EntityGraph(attributePaths = {"items", "items.note"})
  List<Composition> findByUserIdOrderByUpdatedAtDesc(Long userId);

  @EntityGraph(attributePaths = {"items", "items.note"})
  List<Composition> findByUserIdAndStatusOrderByUpdatedAtDesc(Long userId, CompositionStatus status);

  @EntityGraph(attributePaths = {"items", "items.note"})
  List<Composition> findByUserIdAndFavoriteTrueOrderByUpdatedAtDesc(Long userId);

  @EntityGraph(attributePaths = {"items", "items.note"})
  List<Composition> findByUserIdAndIsPublicTrueOrderByUpdatedAtDesc(Long userId);

  @EntityGraph(attributePaths = {"items", "items.note"})
  List<Composition> findByUserIdAndNameContainingIgnoreCaseOrderByUpdatedAtDesc(Long userId, String search);

  @EntityGraph(attributePaths = {"items", "items.note", "user"})
  List<Composition> findByIsPublicTrueOrderByUpdatedAtDesc();

  Optional<Composition> findByPerfumeId(Long perfumeId);

  default List<Composition> findByUserId(Long userId) {
    return findByUserIdOrderByUpdatedAtDesc(userId);
  }
}
