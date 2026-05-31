package velmora.composer.ui.renderer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import velmora.composer.model.Note;
import velmora.composer.service.SynergyService;
import velmora.composer.service.analysis.AnalysisResult;
import velmora.composer.service.analysis.CompositionAnalysisService;
import velmora.composer.ui.util.ComposerAnimationHelper;

public class AnalysisPanelRenderer {

  private final Canvas harmonyCircle;
  private final Rectangle balanceBar;
  private final Rectangle longevityBar;
  private final Rectangle projectionBar;
  private final Rectangle complexityBar;
  private final Label balanceScore;
  private final Label longevityValue;
  private final Label projectionLabel;
  private final Label complexityLabel;
  private final Label dominantFamilyValue;
  private final Label openingCharValue;
  private final Label dryDownCharValue;
  private final VBox familyDistributionList;
  private final VBox insightsContainer;
  private final CompositionAnalysisService analysisService;
  private final SynergyService synergyService;

  private String lastInsightKey = "";

  public AnalysisPanelRenderer(Canvas harmonyCircle, Rectangle balanceBar, Rectangle longevityBar,
      Rectangle projectionBar, Rectangle complexityBar,
      Label balanceScore, Label longevityValue, Label projectionLabel,
      Label complexityLabel, Label dominantFamilyValue, Label openingCharValue,
      Label dryDownCharValue, VBox familyDistributionList,
      VBox insightsContainer, CompositionAnalysisService analysisService,
      SynergyService synergyService) {
    this.harmonyCircle = harmonyCircle;
    this.balanceBar = balanceBar;
    this.longevityBar = longevityBar;
    this.projectionBar = projectionBar;
    this.complexityBar = complexityBar;
    this.balanceScore = balanceScore;
    this.longevityValue = longevityValue;
    this.projectionLabel = projectionLabel;
    this.complexityLabel = complexityLabel;
    this.dominantFamilyValue = dominantFamilyValue;
    this.openingCharValue = openingCharValue;
    this.dryDownCharValue = dryDownCharValue;
    this.familyDistributionList = familyDistributionList;
    this.insightsContainer = insightsContainer;
    this.analysisService = analysisService;
    this.synergyService = synergyService;
  }

  public void renderAll(List<Note> all, Map<Long, Integer> notePercentages,
      List<Note> topNotes, List<Note> heartNotes, List<Note> baseNotes) {
    Platform.runLater(() -> {
      if (all.isEmpty()) {
        renderEmptyImpl();
        return;
      }

      AnalysisResult result = analysisService.analyze(topNotes, heartNotes, baseNotes);
      SynergyService.SynergyResult synergy = all.size() >= 2 ? synergyService.calculate(all) : null;

      ComposerAnimationHelper.drawBalanceCircle(harmonyCircle, result.harmony());
      balanceScore.setText(result.harmony() + "%");
      ComposerAnimationHelper.animatePerfBar(balanceBar, result.harmony());

      longevityValue.setText(result.estLongevity());
      ComposerAnimationHelper.animatePerfBar(longevityBar, result.longevityScore());

      projectionLabel.setText(result.sillage());
      ComposerAnimationHelper.animatePerfBar(projectionBar, result.projectionScore());

      String complexityText = complexityLabel(result.complexityScore());
      complexityLabel.setText(complexityText);
      ComposerAnimationHelper.animatePerfBar(complexityBar, result.complexityScore());

      dominantFamilyValue.setText(result.dominantProfile());
      updateOpeningAndDryDown(topNotes, baseNotes);

      double grand = result.topCount() + result.heartCount() + result.baseCount();
      double topR   = grand > 0 ? result.topCount()   / grand : 0;
      double heartR = grand > 0 ? result.heartCount() / grand : 0;
      double baseR  = grand > 0 ? result.baseCount()  / grand : 0;

      updateFamilyList(all);

      String insightKey = buildInsightKey(all, notePercentages);
      if (!insightKey.equals(lastInsightKey)) {
        lastInsightKey = insightKey;
        renderInsights(result, synergy, all, notePercentages, topNotes, heartNotes, baseNotes,
            topR, heartR, baseR);
      }
    });
  }

  public void renderEmpty() {
    Platform.runLater(this::renderEmptyImpl);
  }

  private void renderEmptyImpl() {
    lastInsightKey = "";
    balanceScore.setText("—");
    longevityValue.setText("—");
    projectionLabel.setText("—");
    complexityLabel.setText("—");
    dominantFamilyValue.setText("—");
    openingCharValue.setText("—");
    dryDownCharValue.setText("—");
    ComposerAnimationHelper.animatePerfBar(balanceBar, 0);
    ComposerAnimationHelper.animatePerfBar(longevityBar, 0);
    ComposerAnimationHelper.animatePerfBar(projectionBar, 0);
    ComposerAnimationHelper.animatePerfBar(complexityBar, 0);
    ComposerAnimationHelper.drawBalanceCircle(harmonyCircle, 0);
    familyDistributionList.getChildren().clear();
    insightsContainer.getChildren().clear();
    Label hint = new Label("Add notes to see live analysis");
    hint.setStyle("-fx-font-size: 17; -fx-font-style: italic; -fx-text-fill: #B0ADA8;");
    insightsContainer.getChildren().add(hint);
    insightsContainer.requestLayout();
    familyDistributionList.requestLayout();
  }

  private String complexityLabel(int score) {
    if (score >= 85) return "Masterpiece";
    if (score >= 70) return "Complex";
    if (score >= 50) return "Moderate";
    if (score >= 30) return "Simple";
    return "Very Simple";
  }

  private void updateOpeningAndDryDown(List<Note> topNotes, List<Note> baseNotes) {
    if (topNotes.isEmpty()) {
      openingCharValue.setText("—");
    } else {
      String topCats = topNotes.stream()
          .map(n -> n.getCategory() != null ? n.getCategory() : "")
          .filter(c -> !c.isEmpty())
          .distinct()
          .collect(Collectors.joining(", "));
      openingCharValue.setText(topCats.isEmpty() ? "—" : topCats);
    }
    if (baseNotes.isEmpty()) {
      dryDownCharValue.setText("—");
    } else {
      String baseCats = baseNotes.stream()
          .map(n -> n.getCategory() != null ? n.getCategory() : "")
          .filter(c -> !c.isEmpty())
          .distinct()
          .collect(Collectors.joining(", "));
      dryDownCharValue.setText(baseCats.isEmpty() ? "—" : baseCats);
    }
  }

  private void updateFamilyList(List<Note> all) {
    familyDistributionList.getChildren().clear();
    if (all.isEmpty()) return;

    Map<String, Long> famCount = all.stream()
        .map(n -> n.getCategory() != null ? n.getCategory() : "Other")
        .collect(Collectors.groupingBy(c -> c, Collectors.counting()));
    long total = all.size();

    Map<String, String> colorMap = Map.ofEntries(
        Map.entry("Citrus",   "#D69478"),
        Map.entry("Floral",   "#B18DB8"),
        Map.entry("Woody",    "#6DA89E"),
        Map.entry("Earthy",   "#8B9E6D"),
        Map.entry("Oriental", "#C8954A"),
        Map.entry("Spicy",    "#D9534F"),
        Map.entry("Fresh",    "#5A9E8F"),
        Map.entry("Aquatic",  "#4A9EB8"),
        Map.entry("Leather",  "#8B4513"),
        Map.entry("Gourmand", "#D4A89A"),
        Map.entry("Green",    "#7D9E6D"),
        Map.entry("Aromatic", "#8DA89E"),
        Map.entry("Fruity",   "#E2A998")
    );

    List<Map.Entry<String, Long>> sorted = famCount.entrySet().stream()
        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
        .collect(Collectors.toList());

    for (var entry : sorted) {
      String name = entry.getKey();
      double pct = (double) entry.getValue() / total * 100;
      String color = colorMap.getOrDefault(name, "#C4BFB9");

      HBox row = new HBox(6);
      row.setAlignment(Pos.CENTER_LEFT);

      Circle dot = new Circle(4);
      dot.setStyle("-fx-fill: " + color + ";");

      Label famLabel = new Label(name);
      famLabel.getStyleClass().add("fam-row-name");
      famLabel.setWrapText(true);
      famLabel.setMaxWidth(Double.MAX_VALUE);

      Rectangle bar = new Rectangle();
      bar.setHeight(6);
      bar.setStyle("-fx-arc-width: 3; -fx-arc-height: 3; -fx-fill: " + color + ";");
      double barPct = Math.max(4, pct * 1.2);
      bar.setWidth(barPct);
      HBox.setHgrow(bar, Priority.NEVER);

      Region spacer = new Region();
      HBox.setHgrow(spacer, Priority.ALWAYS);

      Label pctLabel = new Label(String.format("%.0f%%", pct));
      pctLabel.getStyleClass().add("fam-row-pct");

      row.getChildren().addAll(dot, famLabel, spacer, bar, pctLabel);
      familyDistributionList.getChildren().add(row);
    }

    familyDistributionList.requestLayout();
  }

  private void renderInsights(AnalysisResult result, SynergyService.SynergyResult synergy,
      List<Note> all, Map<Long, Integer> notePercentages,
      List<Note> topNotes, List<Note> heartNotes, List<Note> baseNotes,
      double topR, double heartR, double baseR) {
    insightsContainer.getChildren().clear();
    if (all.isEmpty()) {
      addHint("Add notes to see live analysis");
      insightsContainer.requestLayout();
      return;
    }

    int totalPct = notePercentages.values().stream().mapToInt(Integer::intValue).sum();
    double avgInt = all.stream()
        .filter(n -> n.getIntensity() != null)
        .mapToInt(Note::getIntensity)
        .average().orElse(5);
    long citrusCount = all.stream()
        .filter(n -> "Citrus".equalsIgnoreCase(n.getCategory())).count();
    long woodyCount = all.stream()
        .filter(n -> "Woody".equalsIgnoreCase(n.getCategory())).count();
    long totalNotes = all.size();

    if (result.topInsight() != null && !result.topInsight().isBlank()) {
      addInsightCard("TOP NOTES", result.topInsight(), "rgba(214,148,120,0.12)", "#B5704A");
    }
    if (result.heartInsight() != null && !result.heartInsight().isBlank()) {
      addInsightCard("HEART NOTES", result.heartInsight(), "rgba(177,141,184,0.12)", "#6B67A8");
    }
    if (result.baseInsight() != null && !result.baseInsight().isBlank()) {
      addInsightCard("BASE NOTES", result.baseInsight(), "rgba(109,168,158,0.12)", "#3D7070");
    }

    if (synergy != null) {
      synergy.good().stream().limit(3).forEach(ins ->
          addInsightCard("SYNERGY", ins.explanation(), "rgba(90,158,143,0.12)", "#5A9E8F"));
      synergy.conflicts().stream().limit(2).forEach(ins ->
          addInsightCard("CONFLICT", ins.explanation(), "rgba(217,83,79,0.12)", "#D9534F"));
      synergy.warnings().stream().limit(2).forEach(w ->
          addInsightCard("WARNING", w, "rgba(200,149,74,0.12)", "#C8954A"));
    }

    List<String> suggestions = new ArrayList<>();
    if (woodyCount == 0 && !baseNotes.isEmpty())
      suggestions.add("Add woody notes for better longevity.");
    if (heartR < 0.3 && !heartNotes.isEmpty())
      suggestions.add("Increase heart phase by 10-15% for better depth.");
    if (citrusCount > totalNotes * 0.4)
      suggestions.add("Reduce citrus concentration slightly.");
    if (avgInt < 4 && totalNotes > 1)
      suggestions.add("Consider higher intensity notes for better projection.");
    if (result.families() < 3 && totalNotes >= 3)
      suggestions.add("Add notes from different families for more complexity.");
    if (totalPct != 100)
      suggestions.add("Balance the formula to 100% for optimal performance.");
    if (topR > 0.5)
      suggestions.add(String.format("Top notes dominate (%.0f%%). Consider reducing.", topR * 100));
    if (baseR < 0.15 && !baseNotes.isEmpty())
      suggestions.add(String.format("Base notes are low (%.0f%%). Longevity may suffer.", baseR * 100));
    if (avgInt < 3 && totalNotes > 2)
      suggestions.add("Low average intensity -- the scent may be faint.");

    suggestions.forEach(s ->
        addInsightCard("SUGGESTION", s, "rgba(177,141,184,0.12)", "#6B67A8"));

    if (insightsContainer.getChildren().isEmpty()) {
      addHint("Add at least 2 notes for detailed insights");
    }

    insightsContainer.requestLayout();
  }

  private void addHint(String text) {
    Label hint = new Label(text);
    hint.setStyle("-fx-font-size: 15; -fx-font-style: italic; -fx-text-fill: #B0ADA8; -fx-wrap-text: true;");
    insightsContainer.getChildren().add(hint);
  }

  private void addInsightCard(String tagText, String body, String bgColor, String textColor) {
    VBox card = new VBox(3);
    card.getStyleClass().add("insight-glass-card");
    card.setOpacity(0);
    card.setTranslateY(8);

    Label tag = new Label(tagText);
    tag.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 10; " +
        "-fx-padding: 2 10; -fx-font-size: 12; -fx-font-weight: 700; " +
        "-fx-text-fill: " + textColor + "; -fx-letter-spacing: 1;");

    Label text = new Label(body);
    text.setWrapText(true);
    text.setStyle("-fx-font-size: 14; -fx-text-fill: #5F5B57; -fx-wrap-text: true;");

    card.getChildren().addAll(tag, text);
    insightsContainer.getChildren().add(card);

    FadeTransition ft = new FadeTransition(Duration.millis(200), card);
    ft.setToValue(1.0);
    TranslateTransition tt = new TranslateTransition(Duration.millis(200), card);
    tt.setToY(0);
    tt.setInterpolator(Interpolator.EASE_OUT);
    ft.play();
    tt.play();
  }

  private String buildInsightKey(List<Note> all, Map<Long, Integer> notePercentages) {
    return all.stream()
        .map(n -> n.getId() + ":" + notePercentages.getOrDefault(n.getId(), 0))
        .sorted()
        .collect(Collectors.joining(","));
  }
}