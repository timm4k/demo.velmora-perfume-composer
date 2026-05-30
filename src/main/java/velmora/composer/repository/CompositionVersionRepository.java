package velmora.composer.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import velmora.composer.model.CompositionVersion;

@Repository
public interface CompositionVersionRepository extends JpaRepository<CompositionVersion, Long> {
  List<CompositionVersion> findByCompositionIdOrderByVersionNumberDesc(Long compositionId);
  Optional<CompositionVersion> findByCompositionIdAndVersionNumber(Long compositionId, int versionNumber);
  int countByCompositionId(Long compositionId);
}
