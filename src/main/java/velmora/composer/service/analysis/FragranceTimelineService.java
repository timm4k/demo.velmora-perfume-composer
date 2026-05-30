package velmora.composer.service.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import velmora.composer.model.Note;
import velmora.composer.model.NoteType;

@Component
public class FragranceTimelineService {

  public record NoteCurve(String noteName, NoteType type, String category, String color,
                          double[] activity, double currentActivity, boolean isActive) {}

  public record PhaseInfo(String name, double start, double end, String character,
                          String intensity, List<String> dominantNotes) {}

  public TimelineData calculate(List<Note> topNotes, List<Note> heartNotes, List<Note> baseNotes,
                                Map<Long, Integer> percentages, double currentTimeHours) {
    List<PhaseInfo> phases = buildPhases(topNotes, heartNotes, baseNotes);
    PhaseInfo current = findCurrentPhase(phases, currentTimeHours);
    List<NoteCurve> curves = buildCurves(topNotes, heartNotes, baseNotes, percentages, currentTimeHours);
    String story = generateStory(phases, topNotes, heartNotes, baseNotes);
    return new TimelineData(phases, current, curves, story, currentTimeHours);
  }

  private PhaseInfo currentPhase(List<PhaseInfo> phases, double time) {
    for (PhaseInfo p : phases) {
      if (time >= p.start && time < p.end) return p;
    }
    return phases.get(phases.size() - 1);
  }

  public PhaseInfo findCurrentPhase(List<PhaseInfo> phases, double time) {
    return currentPhase(phases, time);
  }

  private List<PhaseInfo> buildPhases(List<Note> topNotes, List<Note> heartNotes, List<Note> baseNotes) {
    List<PhaseInfo> phases = new ArrayList<>();

    String openingChar = "Bright Opening";
    String heartChar = "Balanced Heart";
    String baseChar = "Gentle Dry Down";

    if (!topNotes.isEmpty()) {
      List<String> cats = topNotes.stream()
          .map(n -> n.getCategory() != null ? n.getCategory().toLowerCase() : "")
          .filter(c -> !c.isEmpty()).distinct().collect(Collectors.toList());
      if (cats.contains("citrus")) openingChar = "Fresh Citrus";
      else if (cats.contains("green")) openingChar = "Green Aromatic";
      else if (cats.contains("aromatic")) openingChar = "Herbal Fresh";
      else if (cats.contains("fruity")) openingChar = "Sweet Fruity";
      else if (cats.contains("spicy")) openingChar = "Warm Spicy";
      else if (cats.contains("floral")) openingChar = "Floral Fresh";
    }

    if (!heartNotes.isEmpty()) {
      List<String> cats = heartNotes.stream()
          .map(n -> n.getCategory() != null ? n.getCategory().toLowerCase() : "")
          .filter(c -> !c.isEmpty()).distinct().collect(Collectors.toList());
      if (cats.contains("floral")) heartChar = "Warm Floral";
      else if (cats.contains("spicy")) heartChar = "Spicy Heart";
      else if (cats.contains("gourmand")) heartChar = "Sweet Gourmand";
      else if (cats.contains("oriental")) heartChar = "Rich Oriental";
      else if (cats.contains("woody")) heartChar = "Woody Transition";
      else if (cats.contains("green")) heartChar = "Green Floral";
    }

    if (!baseNotes.isEmpty()) {
      List<String> cats = baseNotes.stream()
          .map(n -> n.getCategory() != null ? n.getCategory().toLowerCase() : "")
          .filter(c -> !c.isEmpty()).distinct().collect(Collectors.toList());
      if (cats.contains("woody") && cats.contains("amber")) baseChar = "Warm Woody Amber";
      else if (cats.contains("woody")) baseChar = "Warm Woody";
      else if (cats.contains("earthy")) baseChar = "Dark Resinous";
      else if (cats.contains("musk")) baseChar = "Soft Musky";
      else if (cats.contains("gourmand")) baseChar = "Sweet Gourmand";
      else if (cats.contains("amber")) baseChar = "Warm Amber";
      else if (cats.contains("leather")) baseChar = "Leather Tobacco";
    }

    double topAvgInt = topNotes.stream().filter(n -> n.getIntensity() != null)
        .mapToInt(Note::getIntensity).average().orElse(5);
    double heartAvgInt = heartNotes.stream().filter(n -> n.getIntensity() != null)
        .mapToInt(Note::getIntensity).average().orElse(5);
    double baseAvgInt = baseNotes.stream().filter(n -> n.getIntensity() != null)
        .mapToInt(Note::getIntensity).average().orElse(5);

    List<String> topNames = topNotes.stream().map(Note::getName).collect(Collectors.toList());
    List<String> heartNames = heartNotes.stream().map(Note::getName).collect(Collectors.toList());
    List<String> baseNames = baseNotes.stream().map(Note::getName).collect(Collectors.toList());

    if (!topNotes.isEmpty()) {
      phases.add(new PhaseInfo("Opening", 0, 0.25, openingChar,
          fmtIntensity(topAvgInt), topNames));
    }
    if (!heartNotes.isEmpty()) {
      phases.add(new PhaseInfo("Heart", 0.25, 3, heartChar,
          fmtIntensity(heartAvgInt), heartNames));
    }
    if (!baseNotes.isEmpty()) {
      phases.add(new PhaseInfo("Drydown", 3, 8, baseChar,
          fmtIntensity(baseAvgInt), baseNames));
    }
    return phases;
  }

  private String fmtIntensity(double avg) {
    return avg >= 7 ? "High" : avg >= 4 ? "Moderate" : "Soft";
  }

  private List<NoteCurve> buildCurves(List<Note> topNotes, List<Note> heartNotes, List<Note> baseNotes,
                                       Map<Long, Integer> percentages, double currentTimeHours) {
    List<NoteCurve> curves = new ArrayList<>();
    double[] times = {0, 0.25, 0.5, 1, 2, 3, 4, 6, 8};
    for (Note n : topNotes) curves.add(makeCurve(n, "TOP", times, currentTimeHours));
    for (Note n : heartNotes) curves.add(makeCurve(n, "HEART", times, currentTimeHours));
    for (Note n : baseNotes) curves.add(makeCurve(n, "BASE", times, currentTimeHours));
    return curves;
  }

  private NoteCurve makeCurve(Note note, String type, double[] times, double currentTime) {
    double[] activity = new double[times.length];
    for (int i = 0; i < times.length; i++) {
      activity[i] = switch (type) {
        case "TOP" -> topCurve(times[i], note.getIntensity() != null ? note.getIntensity() : 5);
        case "HEART" -> heartCurve(times[i]);
        default -> baseCurve(times[i]);
      };
    }
    double cur = calcActivity(type, currentTime, note.getIntensity() != null ? note.getIntensity() : 5);
    String color = note.getColorCode() != null ? note.getColorCode() : "#C4BFB9";
    return new NoteCurve(note.getName(), note.getType(), note.getCategory() != null ? note.getCategory() : "Other",
        color, activity, cur, cur > 0.15);
  }

  private double calcActivity(String type, double t, int intensity) {
    return switch (type) {
      case "TOP" -> topCurve(t, intensity);
      case "HEART" -> heartCurve(t);
      default -> baseCurve(t);
    };
  }

  private double topCurve(double t, int intensity) {
    double duration = 0.5 + intensity * 0.15;
    return Math.max(0, 1 - t / duration);
  }

  private double heartCurve(double t) {
    if (t < 0.25) return t / 0.25;
    if (t <= 3) return 1.0;
    return Math.max(0, 1 - (t - 3) / 3);
  }

  private double baseCurve(double t) {
    if (t < 3) return Math.min(1, t / 3);
    return Math.max(0, 1 - (t - 8) / 4);
  }

  private String generateStory(List<PhaseInfo> phases, List<Note> topNotes, List<Note> heartNotes, List<Note> baseNotes) {
    StringBuilder sb = new StringBuilder();
    if (!topNotes.isEmpty()) {
      String topNames = topNotes.stream().map(Note::getName).collect(Collectors.joining(", "));
      PhaseInfo p = phases.stream().filter(ph -> ph.name().equals("Opening")).findFirst().orElse(null);
      String opening = p != null ? p.character().toLowerCase() : "bright";
      sb.append("Opens with a ").append(opening).append(" accord led by ").append(topNames).append(".");
    }
    if (!heartNotes.isEmpty()) {
      String heartNames = heartNotes.stream().map(Note::getName).collect(Collectors.joining(", "));
      PhaseInfo p = phases.stream().filter(ph -> ph.name().equals("Heart")).findFirst().orElse(null);
      String heart = p != null ? p.character().toLowerCase() : "floral";
      sb.append(" Transitions into a ").append(heart).append(" where ").append(heartNames).append(" become dominant.");
    }
    if (!baseNotes.isEmpty()) {
      String baseNames = baseNotes.stream().map(Note::getName).collect(Collectors.joining(", "));
      PhaseInfo p = phases.stream().filter(ph -> ph.name().equals("Drydown")).findFirst().orElse(null);
      String base = p != null ? p.character().toLowerCase() : "warm";
      sb.append(" Settles into a ").append(base).append(" base with ").append(baseNames).append(".");
    }
    return sb.toString();
  }

  public record TimelineData(List<PhaseInfo> phases, PhaseInfo currentPhase,
                             List<NoteCurve> curves, String story, double currentTime) {}
}
