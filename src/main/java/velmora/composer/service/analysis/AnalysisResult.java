package velmora.composer.service.analysis;

import java.util.List;
import velmora.composer.model.Note;

public record AnalysisResult(
    int total,
    long topCount,
    long heartCount,
    long baseCount,
    double avgIntensity,
    long families,
    String topHoursDisplay,
    String heartHoursDisplay,
    String baseHoursDisplay,
    double totalLongevity,
    String estLongevity,
    int harmony,
    int longevityScore,
    int complexityScore,
    int projectionScore,
    int overallScore,
    String sillage,
    String dominantProfile,
    String topInsight,
    String heartInsight,
    String baseInsight,
    long citrusCount,
    long floralCount,
    long woodyCount,
    long earthyCount,
    List<Note> allNotes
) {
  public boolean isEmpty() {
    return total == 0;
  }

  public static AnalysisResult empty() {
    return new AnalysisResult(0, 0, 0, 0, 0, 0, "—", "—", "—", 0, "—", 0, 0, 0, 0, 0, "—", "—", "—", "—", "—", 0, 0, 0, 0, List.of());
  }
}
