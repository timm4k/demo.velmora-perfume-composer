package velmora.composer.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import velmora.composer.model.FamilySynergy;

@Repository
public interface FamilySynergyRepository extends JpaRepository<FamilySynergy, Long> {

  List<FamilySynergy> findByFamilyAOrFamilyB(String familyA, String familyB);
}
