package velmora.composer.ui.renderer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.application.Platform;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import velmora.composer.model.Note;
import velmora.composer.model.NoteType;
import velmora.composer.service.AutoSaveService;

public class ConcentrationPanelRenderer {

  private final VBox concentrationRows;
  private final Label validationLabel;
  private final Circle balanceIndicator;
  private final Label totalConcentration;
  private final Label concentrationValidation;
  private final Label topPct;
  private final Label heartPct;
  private final Label basePct;
  private final AutoSaveService autoSaveService;
  private final Map<Long, Label> pctLabelById = new HashMap<>();
  private final PauseTransition analysisDebounce = new PauseTransition(Duration.millis(150));
  private boolean concentrationUpdating = false;

  public ConcentrationPanelRenderer(VBox concentrationRows, Label validationLabel,
                                    Circle balanceIndicator,
                                    Label totalConcentration, Label concentrationValidation,
                                    Label topPct, Label heartPct, Label basePct,
                                    AutoSaveService autoSaveService) {
    this.concentrationRows = concentrationRows;
    this.validationLabel = validationLabel;
    this.balanceIndicator = balanceIndicator;
    this.totalConcentration = totalConcentration;
    this.concentrationValidation = concentrationValidation;
    this.topPct = topPct;
    this.heartPct = heartPct;
    this.basePct = basePct;
    this.autoSaveService = autoSaveService;
  }

  public void renderRows(List<Note> all, Map<Long, Integer> notePercentages,
                          List<Note> topNotes, List<Note> heartNotes, List<Note> baseNotes,
                          Runnable onUpdate) {
    Platform.runLater(() -> {
      concentrationUpdating = true;
      concentrationRows.getChildren().clear();
      pctLabelById.clear();

      if (!all.isEmpty()) {
        for (Note note : all) {
          HBox row = buildRow(note, notePercentages, onUpdate);
          concentrationRows.getChildren().add(row);

          FadeTransition ft = new FadeTransition(Duration.millis(200), row);
          ft.setToValue(1.0);
          ScaleTransition st = new ScaleTransition(Duration.millis(250), row);
          st.setToX(1.0);
          st.setToY(1.0);
          st.setInterpolator(Interpolator.EASE_OUT);
          ft.play();
          st.play();
        }
        updateConcentrationLabels(notePercentages);
        updatePhaseLabels(notePercentages, topNotes, heartNotes, baseNotes);
      }
      concentrationUpdating = false;
    });
  }

  public void renderEmpty() {
    Platform.runLater(this::renderEmptyImpl);
  }

  private void renderEmptyImpl() {
    concentrationRows.getChildren().clear();
    pctLabelById.clear();
    validationLabel.setText("");
    balanceIndicator.setStyle("-fx-fill: #C4BFB9;");
    totalConcentration.setText("Total: 0%");
    concentrationValidation.getStyleClass().removeAll("conc-display-ok", "conc-display-warn", "conc-display-error");
    concentrationValidation.setText("");
    topPct.setText("0%");
    heartPct.setText("0%");
    basePct.setText("0%");
  }

  public void updateLabels(List<Note> all, Map<Long, Integer> notePercentages,
                            List<Note> topNotes, List<Note> heartNotes, List<Note> baseNotes) {
    Platform.runLater(() -> {
      if (all.isEmpty()) {
        renderEmptyImpl();
        return;
      }
      for (Note note : all) {
        Label pctLbl = pctLabelById.get(note.getId());
        if (pctLbl != null) {
          pctLbl.setText(notePercentages.getOrDefault(note.getId(), 0) + "%");
        }
      }
      updateConcentrationLabels(notePercentages);
      updatePhaseLabels(notePercentages, topNotes, heartNotes, baseNotes);
    });
  }

  private HBox buildRow(Note note, Map<Long, Integer> notePercentages, Runnable onUpdate) {
    HBox row = new HBox(6);
    row.setAlignment(Pos.CENTER_LEFT);
    row.setOpacity(0);
    row.setScaleX(0.96);
    row.setScaleY(0.96);

    Circle typeDot = new Circle(4);
    String phaseColor = note.getType() == NoteType.TOP ? "#D69478"
        : note.getType() == NoteType.HEART ? "#B18DB8" : "#6DA89E";
    typeDot.setStyle("-fx-fill: " + phaseColor + ";");

    Label name = new Label(note.getName());
    name.setStyle("-fx-font-size: 13; -fx-font-weight: 600; -fx-text-fill: #4B4B4B; -fx-min-width: 70; -fx-max-width: 90; -fx-wrap-text: true;");

    int pct = notePercentages.getOrDefault(note.getId(), 0);
    Label pctLbl = new Label(pct + "%");
    pctLbl.setStyle("-fx-font-size: 12; -fx-font-weight: 700; -fx-text-fill: #6B6560; -fx-min-width: 30; -fx-alignment: center-right;");
    pctLabelById.put(note.getId(), pctLbl);

    Slider slider = new Slider(0, 100, pct);
    slider.setShowTickLabels(false);
    slider.setShowTickMarks(false);
    slider.setPrefWidth(80);
    slider.setStyle("-fx-padding: 0; -fx-font-size: 10;");
    slider.valueProperty().addListener((obs, old, val) -> {
      if (concentrationUpdating) return;
      int intVal = (int) Math.round(val.doubleValue());
      if (intVal != notePercentages.getOrDefault(note.getId(), 0)) {
        notePercentages.put(note.getId(), intVal);
        pctLbl.setText(intVal + "%");
        concentrationUpdating = true;
        updateConcentrationLabels(notePercentages);
        concentrationUpdating = false;
        autoSaveService.markDirty();
        analysisDebounce.setOnFinished(e -> onUpdate.run());
        analysisDebounce.playFromStart();
      }
    });

    row.getChildren().addAll(typeDot, name, slider, pctLbl);
    return row;
  }

  private void updateConcentrationLabels(Map<Long, Integer> notePercentages) {
    int total = notePercentages.values().stream().mapToInt(Integer::intValue).sum();
    totalConcentration.setText("Total: " + total + "%");
    concentrationValidation.getStyleClass().removeAll("conc-display-ok", "conc-display-warn", "conc-display-error");
    if (total == 100) {
      concentrationValidation.setText("Formula balanced");
      concentrationValidation.getStyleClass().add("conc-display-ok");
    } else if (total < 100) {
      int remaining = 100 - total;
      concentrationValidation.setText("Incomplete. " + remaining + "% remaining.");
      concentrationValidation.getStyleClass().add("conc-display-warn");
    } else {
      int excess = total - 100;
      concentrationValidation.setText("Exceeds by " + excess + "%.");
      concentrationValidation.getStyleClass().add("conc-display-error");
    }
    updateValidationLabel(total);
  }

  private void updateValidationLabel(int totalPct) {
    if (totalPct == 100) {
      validationLabel.setText("Balanced");
      validationLabel.setStyle("-fx-font-size: 13; -fx-font-weight: 600; -fx-text-fill: #5A9E8F;");
      balanceIndicator.setStyle("-fx-fill: #5A9E8F;");
    } else if (totalPct < 100) {
      int need = 100 - totalPct;
      validationLabel.setText("Need " + need + "% more");
      validationLabel.setStyle("-fx-font-size: 13; -fx-font-weight: 600; -fx-text-fill: #C8954A;");
      balanceIndicator.setStyle("-fx-fill: #C8954A;");
    } else {
      int excess = totalPct - 100;
      validationLabel.setText("Reduce by " + excess + "%");
      validationLabel.setStyle("-fx-font-size: 13; -fx-font-weight: 600; -fx-text-fill: #D9534F;");
      balanceIndicator.setStyle("-fx-fill: #D9534F;");
    }
  }

  private void updatePhaseLabels(Map<Long, Integer> notePercentages,
                                  List<Note> topNotes, List<Note> heartNotes, List<Note> baseNotes) {
    double topSum = topNotes.stream().mapToInt(n -> notePercentages.getOrDefault(n.getId(), 0)).sum();
    double heartSum = heartNotes.stream().mapToInt(n -> notePercentages.getOrDefault(n.getId(), 0)).sum();
    double baseSum = baseNotes.stream().mapToInt(n -> notePercentages.getOrDefault(n.getId(), 0)).sum();
    topPct.setText(String.format("%.0f%%", topSum));
    heartPct.setText(String.format("%.0f%%", heartSum));
    basePct.setText(String.format("%.0f%%", baseSum));
  }
}
