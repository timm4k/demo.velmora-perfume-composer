package velmora.composer.ui.renderer;

import java.util.List;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import velmora.composer.model.Composition;
import velmora.composer.model.CompositionVersion;
import velmora.composer.repository.NoteRepository;
import velmora.composer.service.VersionService;

@Slf4j
public class HistoryCompareRenderer {

  private final StackPane compareOverlay;
  private final VBox comparePanel;
  private final VersionService versionService;
  private final NoteRepository noteRepository;
  private final Runnable onClose;

  public HistoryCompareRenderer(
      StackPane compareOverlay, VBox comparePanel,
      VersionService versionService, NoteRepository noteRepository,
      Runnable onClose) {
    this.compareOverlay = compareOverlay;
    this.comparePanel = comparePanel;
    this.versionService = versionService;
    this.noteRepository = noteRepository;
    this.onClose = onClose;
  }

  public void show(Composition comp) {
    List<CompositionVersion> versions = versionService.getVersions(comp.getId());
    if (versions.size() < 2) {
      new Alert(Alert.AlertType.INFORMATION, "Need at least 2 versions to compare").show();
      return;
    }

    comparePanel.getChildren().clear();
    compareOverlay.setVisible(true);
    compareOverlay.setManaged(true);

    VBox content = new VBox(0);

    Label title = new Label("Compare Versions");
    title.getStyleClass().add("compare-title");

    HBox header = new HBox(10);
    header.getStyleClass().add("compare-header");
    header.setAlignment(Pos.CENTER_LEFT);
    header.setPadding(new Insets(0, 20, 12, 20));

    ComboBox<String> verA = new ComboBox<>();
    verA.getStyleClass().add("detail-status-combo");
    ComboBox<String> verB = new ComboBox<>();
    verB.getStyleClass().add("detail-status-combo");

    for (CompositionVersion v : versions) {
      String label = "v" + v.getVersionNumber() + " \u2014 " +
          (v.getChangeDescription() != null ? v.getChangeDescription() : "Saved");
      verA.getItems().add(label);
      verB.getItems().add(label);
    }
    verA.getSelectionModel().select(Math.min(1, versions.size() - 1));
    verB.getSelectionModel().select(0);

    Label vs = new Label("VS");
    vs.getStyleClass().add("compare-vs");

    Button compareBtn = new Button("Compare");
    compareBtn.getStyleClass().addAll("detail-action-btn", "detail-action-btn-primary");

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    Label closeBtn = new Label("\u2715");
    closeBtn.getStyleClass().add("detail-close");
    closeBtn.setOnMouseClicked(e -> close());

    header.getChildren().addAll(verA, vs, verB, compareBtn, spacer, closeBtn);
    content.getChildren().addAll(title, header);

    VBox results = new VBox(6);
    results.setPadding(new Insets(0, 20, 20, 20));
    content.getChildren().add(results);

    compareBtn.setOnAction(e -> {
      results.getChildren().clear();
      int idxA = verA.getSelectionModel().getSelectedIndex();
      int idxB = verB.getSelectionModel().getSelectedIndex();
      if (idxA < 0 || idxB < 0 || idxA == idxB) {
        results.getChildren().add(new Label("Select two different versions"));
        return;
      }
      int vNumA = versions.get(idxA).getVersionNumber();
      int vNumB = versions.get(idxB).getVersionNumber();

      Task<VersionService.VersionDiff> diffTask = new Task<>() {
        @Override protected VersionService.VersionDiff call() {
          return versionService.diff(comp.getId(),
              Math.min(vNumA, vNumB), Math.max(vNumA, vNumB));
        }
      };
      diffTask.setOnSucceeded(de -> renderDiff(diffTask.getValue(), results));
      diffTask.setOnFailed(de ->
          results.getChildren().add(new Label("Failed to compute diff")));
      new Thread(diffTask).start();
    });

    ScrollPane scroll = new ScrollPane(content);
    scroll.setFitToWidth(true);
    scroll.setStyle("-fx-background-color: transparent;");
    scroll.getStyleClass().add("thin-scroll");
    comparePanel.getChildren().add(scroll);

    compareOverlay.setOnMouseClicked(e -> {
      if (e.getTarget() == compareOverlay) close();
    });
  }

  public void close() {
    compareOverlay.setVisible(false);
    compareOverlay.setManaged(false);
    onClose.run();
  }

  private void renderDiff(VersionService.VersionDiff diff, VBox container) {
    if (diff == null || diff.isEmpty()) {
      Label l = new Label("No differences found between these versions");
      l.setStyle("-fx-font-size: 14; -fx-text-fill: #B0ADA8; -fx-font-style: italic;");
      container.getChildren().add(l);
      return;
    }

    if (!diff.addedNoteIds.isEmpty()) {
      container.getChildren().add(sectionTitle("ADDED NOTES"));
      diff.addedNoteIds.forEach(id -> noteRepository.findById(id).ifPresent(n -> {
        Label l = new Label("+ " + n.getName());
        l.getStyleClass().add("compare-added");
        container.getChildren().add(l);
      }));
    }

    if (!diff.removedNoteIds.isEmpty()) {
      container.getChildren().add(sectionTitle("REMOVED NOTES"));
      diff.removedNoteIds.forEach(id -> noteRepository.findById(id).ifPresent(n -> {
        Label l = new Label("\u2212 " + n.getName());
        l.getStyleClass().add("compare-removed");
        container.getChildren().add(l);
      }));
    }

    if (!diff.changedPercentages.isEmpty()) {
      container.getChildren().add(sectionTitle("CHANGED PERCENTAGES"));
      diff.changedPercentages.forEach((id, vals) ->
          noteRepository.findById(id).ifPresent(n -> {
            Label l = new Label(n.getName() + "  " + vals[0] + "% \u2192 " + vals[1] + "%");
            l.getStyleClass().add("compare-changed");
            container.getChildren().add(l);
          }));
    }

    if (diff.nameChanged) {
      container.getChildren().add(sectionTitle("NAME CHANGED"));
      Label l = new Label(
          "\"" + diff.oldName + "\" \u2192 \"" + diff.newName + "\"");
      l.getStyleClass().add("compare-desc-change");
      l.setWrapText(true);
      container.getChildren().add(l);
    }

    if (diff.descriptionChanged) {
      container.getChildren().add(sectionTitle("DESCRIPTION CHANGED"));
      Label l = new Label(
          "\"" + diff.oldDescription + "\" \u2192 \"" + diff.newDescription + "\"");
      l.getStyleClass().add("compare-desc-change");
      l.setWrapText(true);
      container.getChildren().add(l);
    }
  }

  private Label sectionTitle(String text) {
    Label l = new Label(text);
    l.getStyleClass().add("compare-section-title");
    return l;
  }
}
