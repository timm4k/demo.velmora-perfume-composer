package velmora.composer.service.analysis;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import velmora.composer.model.Perfume;
import velmora.composer.repository.NoteRepository;
import velmora.composer.repository.PerfumeRepository;

@Service
@RequiredArgsConstructor
public class PerfumeSimilarityService {

  private final PerfumeRepository perfumeRepository;
  private final NoteRepository noteRepository;
  private final SimilarityService similarityService;

  public record PerfumeMatch(
      Perfume perfume,
      int overallScore,
      int notesScore,
      int familyScore,
      int balanceScore,
      int longevityScore,
      List<String> sharedNotes,
      List<String> sharedFamilies,
      String mainDifference
  ) {}

  public List<PerfumeMatch> findSimilar(Set<String> composerNoteNames, Set<String> composerFamilies,
                                         double topPct, double heartPct, double basePct,
                                         double composerLongevity) {
    List<Perfume> all = perfumeRepository.findAll();
    List<PerfumeMatch> results = new ArrayList<>();
    for (Perfume p : all) {
      results.add(calculateMatch(p, composerNoteNames, composerFamilies, topPct, heartPct, basePct, composerLongevity));
    }
    results.sort((a, b) -> b.overallScore - a.overallScore);
    return results;
  }

  public PerfumeMatch calculateMatch(Perfume perfume, Set<String> composerNoteNames, Set<String> composerFamilies,
                                      double topPct, double heartPct, double basePct,
                                      double composerLongevity) {
    Set<String> perfumeNotes = similarityService.collectPerfumeNotes(perfume);
    Set<String> shared = new HashSet<>(composerNoteNames);
    shared.retainAll(perfumeNotes);
    Set<String> allNotes = new HashSet<>(composerNoteNames);
    allNotes.addAll(perfumeNotes);
    int notesScore = allNotes.isEmpty() ? 0 : (int) ((double) shared.size() / allNotes.size() * 100);

    Set<String> perfumeFamilies = new HashSet<>();
    if (perfume.getOlfactoryFamily() != null) {
      perfumeFamilies.add(perfume.getOlfactoryFamily().toLowerCase());
    }
    Set<String> famShared = new HashSet<>(composerFamilies);
    famShared.retainAll(perfumeFamilies);
    Set<String> famAll = new HashSet<>(composerFamilies);
    famAll.addAll(perfumeFamilies);
    int familyScore = famAll.isEmpty() ? 0 : (int) ((double) famShared.size() / famAll.size() * 100);

    double pTop = parsePct(perfume.getTopNotes());
    double pHeart = parsePct(perfume.getHeartNotes());
    double pBase = parsePct(perfume.getBaseNotes());
    double compTotal = topPct + heartPct + basePct;
    double perfTotal = pTop + pHeart + pBase;
    double topDiff = compTotal > 0 && perfTotal > 0
        ? 100 - Math.abs(topPct / compTotal - pTop / perfTotal) * 100
        : 50;
    topDiff = Math.max(0, Math.min(100, topDiff));
    int balanceScore = (int) topDiff;

    int perfLongevity = perfume.getLongevityScore() != null ? perfume.getLongevityScore() * 10 : 50;
    int longevityScore = Math.max(0, 100 - Math.abs((int) composerLongevity - perfLongevity));

    int overall = (int) (notesScore * 0.40 + familyScore * 0.20 + balanceScore * 0.20 + longevityScore * 0.20);

    List<String> sharedNotes = new ArrayList<>(shared);
    List<String> sharedFamilies = new ArrayList<>(famShared);

    String diff = buildDifference(shared, allNotes, composerNoteNames, perfumeNotes);

    return new PerfumeMatch(perfume, overall, notesScore, familyScore, balanceScore, longevityScore,
        sharedNotes, sharedFamilies, diff);
  }

  private double parsePct(String notes) {
    if (notes == null || notes.isBlank()) return 0;
    return notes.split(",").length * 10.0;
  }

  private String buildDifference(Set<String> shared, Set<String> all,
                                  Set<String> composerSet, Set<String> perfumeSet) {
    Set<String> composerOnly = new HashSet<>(composerSet);
    composerOnly.removeAll(shared);
    Set<String> perfumeOnly = new HashSet<>(perfumeSet);
    perfumeOnly.removeAll(shared);
    if (perfumeOnly.isEmpty() && composerOnly.isEmpty()) return "Very similar composition";
    if (composerOnly.size() > perfumeOnly.size()) {
      return "Your formula has more " + String.join(", ", composerOnly);
    }
    return "Contains " + String.join(", ", perfumeOnly);
  }
}
