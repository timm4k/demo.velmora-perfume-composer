package velmora.composer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import velmora.composer.model.Composition;

@Repository
public interface CompositionRepository extends JpaRepository<Composition, Long> {
  java.util.List<Composition> findByUserId(Long userId);
  java.util.List<Composition> findByUserIdOrderByUpdatedAtDesc(Long userId);
  java.util.Optional<Composition> findByPerfumeId(Long perfumeId);
}