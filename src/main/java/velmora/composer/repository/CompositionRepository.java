package velmora.composer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import velmora.composer.model.Composition;

@Repository
public interface CompositionRepository extends JpaRepository<Composition, Long> {
  java.util.List<Composition> findByUserId(Long userId);
}