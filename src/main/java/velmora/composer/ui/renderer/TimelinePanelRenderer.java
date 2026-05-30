package velmora.composer.ui.renderer;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.animation.FadeTransition;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import velmora.composer.model.Note;
import velmora.composer.service.analysis.FragranceTimelineService;

public class TimelinePanelRenderer {

  private final Slider timelineSlider;
  private final Label currentStageValue;
  private final Label currentCharValue;
  private final Label currentNotesValue;
  private final Label currentIntensityValue;
  private final VBox noteActivityContainer;
  private final VBox heatMapContainer;
  private final Label fragranceStory;
  private final FragranceTimelineService timelineService;
  private double currentTimeHours = 0;
  private boolean timelineUpdating = false;

  public TimelinePanelRenderer(Slider timelineSlider, Label currentStageValue, Label currentCharValue,
                               Label currentNotesValue, Label currentIntensityValue,
                               VBox noteActivityContainer, VBox heatMapContainer, Label fragranceStory,
                               FragranceTimelineService timelineService) {
    this.timelineSlider = timelineSlider;
    this.currentStageValue = currentStageValue;
    this.currentCharValue = currentCharValue;
    this.currentNotesValue = currentNotesValue;
    this.currentIntensityValue = currentIntensityValue;
    this.noteActivityContainer = noteActivityContainer;
    this.heatMapContainer = heatMapContainer;
    this.fragranceStory = fragranceStory;
    this.timelineService = timelineService;
  }

  public void init(Runnable onSliderChange) {
    timelineSlider.valueProperty().addListener((obs, old, val) -> {
      if (timelineUpdating) return;
      currentTimeHours = val.doubleValue();
      onSliderChange.run();
    });
  }

  public void update(List<Note> all, List<Note> topNotes, List<Note> heartNotes, List<Note> baseNotes,
                     Map<Long, Integer> notePercentages) {
    if (all.isEmpty()) {
      renderEmpty();
      return;
    }

    timelineUpdating = true;
    if (timelineSlider != null) {
      timelineSlider.setValue(currentTimeHours);
    }
    timelineUpdating = false;

    var data = timelineService.calculate(topNotes, heartNotes, baseNotes, notePercentages, currentTimeHours);

    var phase = data.currentPhase();
    if (phase != null) {
      currentStageValue.setText(phase.name());
      currentCharValue.setText(phase.character());
      currentNotesValue.setText(String.join(", ", phase.dominantNotes()));
      currentIntensityValue.setText(phase.intensity());
    }

    renderNoteActivity(data);
    renderHeatMap(data);
    fragranceStory.setText(data.story());

    animateTimelineNodes();
  }

  public void renderEmpty() {
    currentStageValue.setText("—");
    currentCharValue.setText("—");
    currentNotesValue.setText("—");
    currentIntensityValue.setText("—");
    noteActivityContainer.getChildren().clear();
    heatMapContainer.getChildren().clear();
    fragranceStory.setText("Add notes to generate a fragrance story.");
  }

  public double getCurrentTimeHours() {
    return currentTimeHours;
  }

  public void setCurrentTimeHours(double hours) {
    this.currentTimeHours = hours;
  }

  private void renderNoteActivity(FragranceTimelineService.TimelineData data) {
    noteActivityContainer.getChildren().clear();
    var active = data.curves().stream().filter(c -> c.isActive()).collect(Collectors.toList());
    if (active.isEmpty()) {
      Label none = new Label("No active notes at this time");
      none.setStyle("-fx-font-size: 13; -fx-font-style: italic; -fx-text-fill: #B0ADA8;");
      noteActivityContainer.getChildren().add(none);
      return;
    }
    for (var curve : active) {
      HBox row = new HBox(6);
      row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
      row.getStyleClass().add("activity-note-row");
      Circle dot = new Circle(3);
      dot.setStyle("-fx-fill: " + curve.color() + ";");
      Label name = new Label(curve.noteName());
      name.getStyleClass().add("activity-label");
      Label pct = new Label(String.format("%.0f%%", curve.currentActivity() * 100));
      pct.setStyle("-fx-font-size: 12; -fx-font-weight: 600; -fx-text-fill: #B0ADA8; -fx-min-width: 32; -fx-alignment: center-right;");
      Region spacer = new Region();
      HBox.setHgrow(spacer, Priority.ALWAYS);
      row.getChildren().addAll(dot, name, spacer, pct);
      noteActivityContainer.getChildren().add(row);
    }
  }

  private void renderHeatMap(FragranceTimelineService.TimelineData data) {
    heatMapContainer.getChildren().clear();
    if (data.curves().isEmpty()) {
      Label none = new Label("No notes to display");
      none.setStyle("-fx-font-size: 13; -fx-font-style: italic; -fx-text-fill: #B0ADA8;");
      heatMapContainer.getChildren().add(none);
      return;
    }
    for (var curve : data.curves()) {
      HBox row = new HBox(6);
      row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
      row.getStyleClass().add("heat-row");

      Label name = new Label(curve.noteName());
      name.getStyleClass().add("heat-label");

      HBox bars = new HBox(2);
      bars.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
      HBox.setHgrow(bars, Priority.ALWAYS);

      double[] activity = curve.activity();
      for (int i = 0; i < activity.length; i++) {
        double act = activity[i];
        StackPane seg = new StackPane();
        seg.setPrefWidth(16);
        seg.setPrefHeight(10);
        seg.setMaxWidth(16);
        seg.setMinWidth(8);
        String fill = act > 0.7 ? "rgba(177,141,184,0.7)"
            : act > 0.4 ? "rgba(177,141,184,0.4)"
                : act > 0.15 ? "rgba(177,141,184,0.2)"
                    : "rgba(234,230,223,0.3)";
        seg.setStyle("-fx-background-color: " + fill + "; -fx-background-radius: 2;");
        bars.getChildren().add(seg);
      }

      row.getChildren().addAll(name, bars);
      heatMapContainer.getChildren().add(row);
    }
  }

  private void animateTimelineNodes() {
    for (Node node : new Node[]{currentStageValue, currentCharValue, currentNotesValue, currentIntensityValue, fragranceStory}) {
      if (node instanceof Label lbl) {
        FadeTransition ft = new FadeTransition(Duration.millis(200), lbl);
        ft.setFromValue(0.6);
        ft.setToValue(1.0);
        ft.play();
      }
    }
  }
}
