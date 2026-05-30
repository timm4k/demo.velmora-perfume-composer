package velmora.composer.service.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import velmora.composer.model.Note;
import velmora.composer.model.NoteType;

@Service
public class FormulaRecommendationService {

  public record Recommendation(String category, String message, int priority) {}

  public List<Recommendation> analyze(List<Note> allNotes, List<Note> topNotes, List<Note> heartNotes, List<Note> baseNotes,
                                       Map<Long, Integer> percentages, int balanceScore) {
    List<Recommendation> recs = new ArrayList<>();

    if (allNotes == null || allNotes.isEmpty()) {
      recs.add(new Recommendation("general", "Add notes to start building your formula", 0));
      return recs;
    }

    if (balanceScore < 50) {
      recs.add(new Recommendation("balance", "Balance score is low. Adjust percentages for better harmony between phases.", 5));
    }

    double topPct = topNotes.stream().mapToInt(n -> percentages.getOrDefault(n.getId(), 0)).sum();
    double heartPct = heartNotes.stream().mapToInt(n -> percentages.getOrDefault(n.getId(), 0)).sum();
    double basePct = baseNotes.stream().mapToInt(n -> percentages.getOrDefault(n.getId(), 0)).sum();
    double total = topPct + heartPct + basePct;

    if (total > 0) {
      double topRatio = topPct / total;
      double heartRatio = heartPct / total;
      double baseRatio = basePct / total;

      if (!topNotes.isEmpty() && topRatio > 0.50) {
        recs.add(new Recommendation("balance", "Reduce citrus dominance — top notes are over 50% of your formula.", 4));
      }
      if (!baseNotes.isEmpty() && baseRatio < 0.20) {
        recs.add(new Recommendation("longevity", "Increase base concentration to improve longevity.", 4));
      }
      if (!heartNotes.isEmpty() && heartRatio < 0.20) {
        recs.add(new Recommendation("balance", "Increase heart phase concentration for better transition.", 3));
      }
      if (topRatio > 0 && heartRatio > 0 && baseRatio > 0) {
        double idealTop = 0.25, idealHeart = 0.40, idealBase = 0.35;
        if (Math.abs(topRatio - idealTop) > 0.15) {
          recs.add(new Recommendation("balance", "Adjust top notes closer to 25% for classic perfume balance.", 2));
        }
      }
    }

    var families = allNotes.stream()
        .filter(n -> n.getCategory() != null)
        .collect(Collectors.groupingBy(n -> n.getCategory().toLowerCase(), Collectors.counting()));
    long familyCount = families.size();

    if (familyCount < 2 && allNotes.size() >= 3) {
      recs.add(new Recommendation("depth", "Composition lacks depth. Add notes from different olfactory families.", 4));
    }

    boolean hasWoody = families.containsKey("woody");
    boolean hasAmber = families.containsKey("amber");
    boolean hasMusk = families.containsKey("musk");
    if (!hasWoody && !hasAmber && !hasMusk && !baseNotes.isEmpty()) {
      recs.add(new Recommendation("longevity", "Add woody or amber notes to improve longevity and projection.", 4));
    }

    boolean hasCitrus = families.containsKey("citrus");
    boolean hasFloral = families.containsKey("floral");
    boolean hasSpicy = families.containsKey("spicy");
    if (hasCitrus && hasFloral && !hasSpicy) {
      recs.add(new Recommendation("complexity", "Add a spicy note for more complexity.", 2));
    }

    if (allNotes.size() <= 2) {
      recs.add(new Recommendation("complexity", "Consider adding 1-2 more notes for a more complete fragrance.", 3));
    }

    if (allNotes.size() >= 6) {
      recs.add(new Recommendation("clarity", "You have many notes — ensure each has a clear role in the composition.", 2));
    }

    recs.sort((a, b) -> b.priority - a.priority);
    if (recs.size() > 5) recs = recs.subList(0, 5);
    return recs;
  }
}
