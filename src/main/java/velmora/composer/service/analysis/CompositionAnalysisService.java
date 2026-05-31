package velmora.composer.service.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import velmora.composer.model.Note;

@Component
@RequiredArgsConstructor
public class CompositionAnalysisService {

  public AnalysisResult analyze(List<Note> topNotes, List<Note> heartNotes, List<Note> baseNotes) {
    List<Note> all = new ArrayList<>();
    all.addAll(topNotes);
    all.addAll(heartNotes);
    all.addAll(baseNotes);

    int total = all.size();
    if (total == 0) {
      return AnalysisResult.empty();
    }

    long topCount = topNotes.size();
    long heartCount = heartNotes.size();
    long baseCount = baseNotes.size();

    double topRatio = (double) topCount / total;
    double heartRatio = (double) heartCount / total;
    double baseRatio = (double) baseCount / total;

    double avgIntensity = all.stream()
        .filter(n -> n.getIntensity() != null)
        .mapToInt(Note::getIntensity)
        .average()
        .orElse(5.0);

    long families = all.stream()
        .map(Note::getCategory)
        .filter(Objects::nonNull)
        .distinct()
        .count();

    double topHours = topNotes.stream()
        .filter(n -> n.getIntensity() != null)
        .mapToInt(Note::getIntensity)
        .average()
        .orElse(5) * 0.5;

    double heartHours = heartNotes.stream()
        .filter(n -> n.getIntensity() != null)
        .mapToInt(Note::getIntensity)
        .average()
        .orElse(5) * 1.2;

    double baseHours = baseNotes.stream()
        .filter(n -> n.getIntensity() != null)
        .mapToInt(Note::getIntensity)
        .average()
        .orElse(5) * 2.0;

    double totalLongevity = topHours * 0.2 + heartHours * 0.35 + baseHours * 0.45;

    int harmony = calcBalance(topRatio, heartRatio, baseRatio);
    int longevityScore = Math.min((int) (totalLongevity * 15), 100);
    int complexityScore = Math.min(30 + (int) families * 14 + total * 4, 100);
    int projectionScore = Math.min((int) avgIntensity * 15, 100);
    int overall = (int) (harmony * 0.25 + longevityScore * 0.30 + complexityScore * 0.20 + projectionScore * 0.25);

    String sillage = avgIntensity >= 8 ? "Heavy" : avgIntensity >= 6 ? "Strong" : avgIntensity >= 4 ? "Moderate" : "Soft";
    String estLongevity = fmtLongevity(totalLongevity);
    String dominantProfile = detectDominantProfile(all);
    String topInsight = generateTopAnalysis(topNotes);
    String heartInsight = generateHeartAnalysis(heartNotes);
    String baseInsight = generateBaseAnalysis(baseNotes);

    long citrus = countByCategory(all, "Citrus");
    long floral = countByCategory(all, "Floral");
    long woody = countByCategory(all, "Woody");
    long earthy = countByCategory(all, "Earthy");

    String topDisp = topHours > 0 ? String.format("%.1fh", topHours) : "—";
    String heartDisp = heartHours > 0 ? String.format("%.1fh", heartHours) : "—";
    String baseDisp = baseHours > 0 ? String.format("%.1fh", baseHours) : "—";

    return new AnalysisResult(total, topCount, heartCount, baseCount,
        avgIntensity, families, topDisp, heartDisp, baseDisp,
        totalLongevity, estLongevity, harmony, longevityScore,
        complexityScore, projectionScore, overall, sillage,
        dominantProfile, topInsight, heartInsight, baseInsight,
        citrus, floral, woody, earthy, all);
  }

  public int calcBalanceScore(List<Note> topNotes, List<Note> heartNotes, List<Note> baseNotes) {
    int total = topNotes.size() + heartNotes.size() + baseNotes.size();
    if (total == 0) return 0;
    double topR = (double) topNotes.size() / total;
    double heartR = (double) heartNotes.size() / total;
    double baseR = (double) baseNotes.size() / total;
    return calcBalance(topR, heartR, baseR);
  }

  private int calcBalance(double topR, double heartR, double baseR) {
    if (topR == 0 || heartR == 0 || baseR == 0) return 25;
    double score = 100;
    score -= Math.abs(topR - 0.30) * 50;
    score -= Math.abs(heartR - 0.50) * 40;
    score -= Math.abs(baseR - 0.20) * 50;
    return Math.max(0, Math.min(100, (int) score));
  }

  private String detectDominantProfile(List<Note> notes) {
    Map<String, Long> famCount = notes.stream()
        .map(n -> n.getCategory() != null ? n.getCategory() : "Other")
        .collect(Collectors.groupingBy(c -> c, Collectors.counting()));
    String topFam = famCount.entrySet().stream()
        .max(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey)
        .orElse("Balanced");

    long spicy = notes.stream().filter(n -> {
      String c = n.getCategory(); return c != null && (c.equalsIgnoreCase("Spicy") || c.equalsIgnoreCase("Woody"));
    }).count();
    long fresh = notes.stream().filter(n -> {
      String c = n.getCategory(); return c != null && (c.equalsIgnoreCase("Citrus") || c.equalsIgnoreCase("Green") || c.equalsIgnoreCase("Aromatic"));
    }).count();
    long sweet = notes.stream().filter(n -> {
      String c = n.getCategory(); return c != null && (c.equalsIgnoreCase("Floral") || c.equalsIgnoreCase("Gourmand"));
    }).count();

    StringBuilder sb = new StringBuilder();
    if (spicy > fresh && spicy > sweet) sb.append("Smoky ");
    if (sweet > spicy && sweet > fresh) sb.append("Sweet ");
    if (fresh > spicy && fresh > sweet) sb.append("Fresh ");
    sb.append(topFam).append(" Amber");
    return sb.toString().trim();
  }

  private String generateTopAnalysis(List<Note> tops) {
    List<String> parts = new ArrayList<>();
    for (Note n : tops) {
      String cat = n.getCategory() != null ? n.getCategory() : "";
      parts.add(switch (cat.toLowerCase()) {
        case "citrus" -> n.getName() + " — bright citrus spark, adds freshness and lift";
        case "green" -> n.getName() + " — crisp green vibrancy, brings a natural dewy feel";
        case "aromatic" -> n.getName() + " — herbal aromatic clarity, gives an airy freshness";
        case "spicy" -> n.getName() + " — warm spicy kick, adds energy and bite";
        case "floral" -> n.getName() + " — soft floral opening, lends elegance and delicacy";
        case "fruity" -> n.getName() + " — juicy fruity sweetness, creates a playful top";
        case "aquatic" -> n.getName() + " — fresh aquatic accord, brings a clean sea breeze";
        case "aldehydic" -> n.getName() + " — sparkling aldehydic effect, adds soapy sophistication";
        default -> n.getName() + " — bright opening note, gives the first impression";
      });
    }
    return String.join("; ", parts) + ".";
  }

  private String generateHeartAnalysis(List<Note> hearts) {
    List<String> parts = new ArrayList<>();
    for (Note n : hearts) {
      String cat = n.getCategory() != null ? n.getCategory() : "";
      parts.add(switch (cat.toLowerCase()) {
        case "floral" -> n.getName() + " — lush floral heart, delivers richness and body";
        case "spicy" -> n.getName() + " — warm spice infusion, adds depth and sensuality";
        case "woody" -> n.getName() + " — smooth woody transition, bridges top and base";
        case "gourmand" -> n.getName() + " — sweet gourmand accord, brings edible warmth";
        case "oriental" -> n.getName() + " — rich oriental depth, adds resinous complexity";
        case "green" -> n.getName() + " — green floral heart, keeps freshness alive";
        case "earthy" -> n.getName() + " — earthy heart note, introduces grounded character";
        case "balsamic" -> n.getName() + " — balsamic warmth, gives a smooth rounded feel";
        default -> n.getName() + " — anchors the heart, defines the fragrance character";
      });
    }
    return String.join("; ", parts) + ".";
  }

  private String generateBaseAnalysis(List<Note> bases) {
    List<String> parts = new ArrayList<>();
    for (Note n : bases) {
      String cat = n.getCategory() != null ? n.getCategory() : "";
      parts.add(switch (cat.toLowerCase()) {
        case "woody" -> n.getName() + " — warm woody base, gives structure and longevity";
        case "earthy" -> n.getName() + " — earthy depth, adds a dark grounded finish";
        case "musk" -> n.getName() + " — soft musky trail, creates intimate warmth";
        case "amber" -> n.getName() + " — warm amber glow, leaves a rich lingering trail";
        case "gourmand" -> n.getName() + " — sweet gourmand finish, leaves a creamy imprint";
        case "leather" -> n.getName() + " — smoky leather base, adds rugged sophistication";
        case "green" -> n.getName() + " — green woody base, keeps freshness in the dry-down";
        case "resin" -> n.getName() + " — resinous depth, adds balsamic persistence";
        case "animalic" -> n.getName() + " — animalic warmth, gives primal sensuality";
        case "tobacco" -> n.getName() + " — tobacco leaf warmth, adds smoky richness";
        default -> n.getName() + " — anchors the base, provides lasting foundation";
      });
    }
    return String.join("; ", parts) + ".";
  }

  private long countByCategory(List<Note> notes, String category) {
    return notes.stream()
        .filter(n -> category.equalsIgnoreCase(n.getCategory()))
        .count();
  }

  private String fmtLongevity(double value) {
    if (value <= 2) return "1-2h";
    if (value <= 4) return "2-4h";
    if (value <= 6) return "4-6h";
    if (value <= 8) return "6-8h";
    if (value <= 12) return "8-12h";
    return "12h+";
  }

  public String generateDescription(List<Note> topNotes, List<Note> heartNotes, List<Note> baseNotes) {
    List<Note> all = new ArrayList<>();
    all.addAll(topNotes); all.addAll(heartNotes); all.addAll(baseNotes);
    if (all.isEmpty()) return "Empty composition. Add notes to generate a profile.";

    String opening = describePhase(topNotes, "opening", "top");
    String heart = describePhase(heartNotes, "heart", "middle");
    String base = describePhase(baseNotes, "drydown", "base");

    double avgInt = all.stream().filter(n -> n.getIntensity() != null).mapToInt(Note::getIntensity).average().orElse(5);
    String intensity = avgInt >= 7 ? "strong" : avgInt >= 4 ? "moderate" : "soft";

    long families = all.stream().map(Note::getCategory).filter(Objects::nonNull).distinct().count();
    String complexity = families >= 5 ? "complex" : families >= 3 ? "well-balanced" : "focused";

    return opening + " with " + heart + " and " + base + ". A " + intensity + ", " + complexity + " composition.";
  }

  private String describePhase(List<Note> notes, String label, String type) {
    if (notes.isEmpty()) return "no " + label + " notes";
    List<String> categories = notes.stream()
        .map(n -> n.getCategory() != null ? n.getCategory().toLowerCase() : "other")
        .distinct().collect(Collectors.toList());

    if (type.equals("top")) {
      if (categories.contains("citrus")) return "fresh citrus " + label;
      if (categories.contains("green")) return "bright green " + label;
      if (categories.contains("aromatic")) return "herbal aromatic " + label;
      if (categories.contains("spicy")) return "spicy " + label;
      if (categories.contains("fruity")) return "fruity " + label;
      if (categories.contains("floral")) return "floral " + label;
      return "vibrant " + label;
    }
    if (type.equals("middle")) {
      if (categories.contains("floral")) return "floral heart";
      if (categories.contains("spicy")) return "warm spicy heart";
      if (categories.contains("gourmand")) return "sweet gourmand heart";
      if (categories.contains("oriental")) return "rich oriental heart";
      if (categories.contains("woody")) return "woody heart";
      return "balanced heart";
    }
    if (categories.contains("woody")) return "warm woody " + label;
    if (categories.contains("earthy")) return "earthy " + label;
    if (categories.contains("musk")) return "soft musky " + label;
    if (categories.contains("amber")) return "warm amber " + label;
    if (categories.contains("gourmand")) return "sweet gourmand " + label;
    return "lingering " + label;
  }
}
