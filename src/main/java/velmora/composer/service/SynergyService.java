package velmora.composer.service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import velmora.composer.model.Note;
import velmora.composer.repository.FamilySynergyRepository;

@Service
@RequiredArgsConstructor
public class SynergyService {

  private final FamilySynergyRepository synergyRepository;

  private final Map<String, Map<String, Integer>> synergyMap = new HashMap<>();

  @PostConstruct
  void preload() {
    var rows = synergyRepository.findAll();
    for (var row : rows) {
      String a = row.getFamilyA().toLowerCase();
      String b = row.getFamilyB().toLowerCase();
      int score = row.getScore() != null ? row.getScore() : 0;
      synergyMap.computeIfAbsent(a, k -> new HashMap<>()).put(b, score);
      synergyMap.computeIfAbsent(b, k -> new HashMap<>()).put(a, score);
    }
  }

  public void reload() {
    synergyMap.clear();
    preload();
  }

  public SynergyResult calculate(List<Note> notes) {
    if (notes == null || notes.size() < 2) {
      return new SynergyResult(0, List.of(), List.of(), List.of());
    }

    List<NoteInsight> good = new ArrayList<>();
    List<NoteInsight> conflicts = new ArrayList<>();
    List<String> warnings = new ArrayList<>();
    double rawSum = 0;
    int pairCount = 0;

    Map<String, Integer> familyCount = countFamilies(notes);

    for (int i = 0; i < notes.size(); i++) {
      for (int j = i + 1; j < notes.size(); j++) {
        Note a = notes.get(i);
        Note b = notes.get(j);
        if (a.getCategory() == null || b.getCategory() == null) continue;

        String fa = a.getCategory();
        String fb = b.getCategory();
        if (fa.equalsIgnoreCase(fb)) continue;

        int score = getSynergy(fa, fb);
        pairCount++;

        rawSum += switch (score) {
          case 2 -> 1.0;
          case 1 -> 0.75;
          case 0 -> 0.40;
          default -> 0.0;
        };

        NoteInsight ins = new NoteInsight(
            a.getName(), fa, b.getName(), fb, score, explain(a, b, score)
        );
        if (score >= 1) {
          if (good.size() < 20) good.add(ins);
        } else if (score == -1) {
          conflicts.add(ins);
        }
      }
    }

    if (pairCount == 0) return new SynergyResult(50, List.of(), List.of(), List.of());

    int baseScore = (int) Math.round(rawSum / pairCount * 100);
    int diversityBonus = calcDiversityBonus(familyCount, notes.size());
    int filledBonus = Math.min(25, pairCount);
    int finalScore = Math.min(100, baseScore + diversityBonus + filledBonus);

    checkFamilyBalance(familyCount, notes.size(), warnings);

    return new SynergyResult(finalScore, good, conflicts, warnings);
  }

  private int getSynergy(String fa, String fb) {
    Map<String, Integer> inner = synergyMap.get(fa.toLowerCase());
    if (inner != null) {
      Integer score = inner.get(fb.toLowerCase());
      if (score != null) return score;
    }
    return 0;
  }

  private int calcDiversityBonus(Map<String, Integer> familyCount, int total) {
    int families = familyCount.size();
    if (total < 3) return 0;
    if (families >= 5) return 15;
    if (families >= 3) return 10;
    return 0;
  }

  private String explain(Note a, Note b, int score) {
    String na = a.getName();
    String nb = b.getName();

    if (score >= 2) {
      return pick(
        na + " + " + nb + " — excellent synergy: they amplify each other, creating a rich, coherent accord",
        na + " + " + nb + " — perfect pair: together they build a harmonious blend where both shine",
        na + " + " + nb + " — great combination: the two notes fuse naturally into a unified scent"
      );
    }
    if (score == 1) {
      return pick(
        na + " + " + nb + " — good match: one softens the other for a balanced transition",
        na + " + " + nb + " — works well: their contrast adds depth without clashing",
        na + " + " + nb + " — compatible: they create a pleasant bridge between two families"
      );
    }
    if (score == -1) {
      return pick(
        na + " + " + nb + " — may clash: bright volatiles compete with heavy base, creating a disjointed profile",
        na + " + " + nb + " — poor synergy: their characters fight rather than blend, the scent may feel fragmented",
        na + " + " + nb + " — conflict: one overpowers the other, losing the nuance of both"
      );
    }
    return na + " + " + nb + " — neutral: neither helps nor hurts, they coexist without interaction";
  }

  private String pick(String... options) {
    return options[(int) (Math.random() * options.length)];
  }

  private Map<String, Integer> countFamilies(List<Note> notes) {
    Map<String, Integer> map = new LinkedHashMap<>();
    for (Note n : notes) {
      map.merge(n.getCategory() != null ? n.getCategory() : "Other", 1, Integer::sum);
    }
    return map;
  }

  private void checkFamilyBalance(Map<String, Integer> familyCount, int total, List<String> warnings) {
    if (total < 3) return;
    familyCount.entrySet().stream()
        .filter(e -> e.getValue() > total / 2)
        .findFirst().ifPresent(dom ->
            warnings.add("\u26A0 " + dom.getKey() + " dominates (" + dom.getValue() + "/" + total + " notes) — consider diversifying")
        );
  }

  public record NoteInsight(
      String noteA, String categoryA, String noteB, String categoryB,
      int score, String explanation
  ) {}

  public record SynergyResult(int score, List<NoteInsight> good, List<NoteInsight> conflicts, List<String> warnings) {}
}
