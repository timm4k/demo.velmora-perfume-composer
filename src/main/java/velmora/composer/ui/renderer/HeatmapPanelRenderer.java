package velmora.composer.ui.renderer;

import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import velmora.composer.model.Note;
import velmora.composer.service.analysis.FragranceTimelineService;

public class HeatmapPanelRenderer {

  private final VBox heatMapContainer;
  private final FragranceTimelineService timelineService;

  public HeatmapPanelRenderer(VBox heatMapContainer, FragranceTimelineService timelineService) {
    this.heatMapContainer = heatMapContainer;
    this.timelineService = timelineService;
  }

  public void update(List<Note> all, List<Note> topNotes, List<Note> heartNotes, List<Note> baseNotes,
                     Map<Long, Integer> notePercentages) {
    Platform.runLater(() -> {
      if (all.isEmpty()) {
        renderEmpty();
        return;
      }
      var data = timelineService.calculate(topNotes, heartNotes, baseNotes, notePercentages, 0);
      renderHeatMap(data);
    });
  }

  public void renderEmpty() {
    heatMapContainer.getChildren().clear();
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
}
