package velmora.composer.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import velmora.composer.model.Composition;
import velmora.composer.model.CompositionStatus;

@Repository
public interface CompositionRepository extends JpaRepository<Composition, Long> {
  @EntityGraph(attributePaths = {"items", "items.note"})
  List<Composition> findByUserId(Long userId);

  @EntityGraph(attributePaths = {"items", "items.note"})
  List<Composition> findByUserIdOrderByUpdatedAtDesc(Long userId);

  Optional<Composition> findByPerfumeId(Long perfumeId);

  @EntityGraph(attributePaths = {"items", "items.note"})
  List<Composition> findByUserIdAndStatusOrderByUpdatedAtDesc(Long userId, CompositionStatus status);

  @EntityGraph(attributePaths = {"items", "items.note"})
  List<Composition> findByUserIdAndFavoriteTrueOrderByUpdatedAtDesc(Long userId);

  @EntityGraph(attributePaths = {"items", "items.note"})
  List<Composition> findByUserIdAndIsPublicTrueOrderByUpdatedAtDesc(Long userId);

  @EntityGraph(attributePaths = {"items", "items.note"})
  List<Composition> findByUserIdAndNameContainingIgnoreCaseOrderByUpdatedAtDesc(Long userId, String search);

  @EntityGraph(attributePaths = {"items", "items.note"})
  List<Composition> findByIsPublicTrueOrderByUpdatedAtDesc();
}