package velmora.composer.ui.renderer;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.collections.FXCollections;
import lombok.extern.slf4j.Slf4j;
import velmora.composer.model.Composition;
import velmora.composer.model.CompositionStatus;
import velmora.composer.model.CompositionVersion;
import velmora.composer.repository.NoteRepository;
import velmora.composer.service.HistoryService;
import velmora.composer.service.VersionService;

@Slf4j
public class HistoryDetailRenderer {

  private final StackPane detailOverlay;
  private final AnchorPane detailPanel;
  private final HistoryService historyService;
  private final VersionService versionService;
  private final NoteRepository noteRepository;
  private final Runnable onRefresh;
  private final Consumer<Composition> onOpen;
  private final Consumer<Composition> onDuplicate;
  private final BiConsumer<Composition, Integer> onRestore;
  private final Consumer<Composition> onDelete;

  private static final DateTimeFormatter SHORT_DATE =
      DateTimeFormatter.ofPattern("MMM dd, HH:mm", Locale.ENGLISH);

  public HistoryDetailRenderer(
      StackPane detailOverlay, AnchorPane detailPanel,
      HistoryService historyService, VersionService versionService,
      NoteRepository noteRepository,
      Runnable onRefresh,
      Consumer<Composition> onOpen,
      Consumer<Composition> onDuplicate,
      BiConsumer<Composition, Integer> onRestore,
      Consumer<Composition> onDelete) {
    this.detailOverlay = detailOverlay;
    this.detailPanel = detailPanel;
    this.historyService = historyService;
    this.versionService = versionService;
    this.noteRepository = noteRepository;
    this.onRefresh = onRefresh;
    this.onOpen = onOpen;
    this.onDuplicate = onDuplicate;
    this.onRestore = onRestore;
    this.onDelete = onDelete;
  }

  public void show(Composition comp) {
    detailPanel.getChildren().clear();
    detailOverlay.setVisible(true);
    detailOverlay.setManaged(true);

    VBox content = new VBox(0);
    content.setStyle("-fx-background-color: #FFFDFB;");

    HBox header = new HBox(12);
    header.getStyleClass().add("detail-header");
    header.setAlignment(Pos.CENTER_LEFT);

    Label title = new Label(comp.getName() != null ? comp.getName() : "Untitled");
    title.getStyleClass().add("detail-title");
    title.setWrapText(true);

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    Label closeBtn = new Label("\u2715");
    closeBtn.getStyleClass().add("detail-close");
    closeBtn.setOnMouseClicked(e -> close());

    header.getChildren().addAll(title, spacer, closeBtn);
    content.getChildren().add(header);

    FlowPane statsGrid = new FlowPane(8, 8);
    statsGrid.getStyleClass().add("stats-grid");
    try {
      var stats = historyService.getStats(comp.getId());
      if (stats != null) {
        statsGrid.getChildren().add(statBox("Created",
            stats.createdAt != null ? stats.createdAt.format(SHORT_DATE) : "\u2014"));
        statsGrid.getChildren().add(statBox("Modified",
            stats.lastModified != null ? stats.lastModified.format(SHORT_DATE) : "\u2014"));
        statsGrid.getChildren().add(statBox("Versions", String.valueOf(stats.versionCount)));
        statsGrid.getChildren().add(statBox("Notes", String.valueOf(stats.noteCount)));
        if (stats.dominantFamily != null)
          statsGrid.getChildren().add(statBox("Top Family", stats.dominantFamily));
      }
    } catch (Exception ex) {
      log.error("Failed to load stats for composition {}", comp.getId(), ex);
    }
    content.getChildren().add(statsGrid);

    HBox actions = new HBox(8);
    actions.getStyleClass().add("detail-actions");
    actions.setPadding(new Insets(0, 20, 12, 20));

    Button openBtn = new Button("Open in Composer");
    openBtn.getStyleClass().addAll("detail-action-btn", "detail-action-btn-primary");
    openBtn.setOnAction(e -> { close(); onOpen.accept(comp); });

    Button dupBtn = new Button("Clone Formula");
    dupBtn.getStyleClass().add("detail-action-btn");
    dupBtn.setOnAction(e -> onDuplicate.accept(comp));

    Button deleteBtn = new Button("Delete");
    deleteBtn.getStyleClass().addAll("detail-action-btn", "detail-action-btn-danger");
    deleteBtn.setOnAction(e -> { close(); onDelete.accept(comp); });

    ComboBox<String> statusCombo = new ComboBox<>(
        FXCollections.observableArrayList("DRAFT", "EXPERIMENTAL", "PUBLISHED", "ARCHIVED"));
    statusCombo.getStyleClass().add("detail-status-combo");
    statusCombo.getSelectionModel().select(
        comp.getStatus() != null ? comp.getStatus().name() : "DRAFT");
    statusCombo.setOnAction(e ->
        historyService.setStatus(comp.getId(),
            CompositionStatus.valueOf(statusCombo.getSelectionModel().getSelectedItem())));

    actions.getChildren().addAll(openBtn, dupBtn, statusCombo, deleteBtn);
    content.getChildren().add(actions);

    Label timelineTitle = new Label("Version Timeline");
    timelineTitle.getStyleClass().add("timeline-title");
    VBox.setMargin(timelineTitle, new Insets(8, 20, 6, 20));
    content.getChildren().add(timelineTitle);

    VBox timeline = new VBox(0);
    timeline.getStyleClass().add("timeline-container");
    loadTimeline(comp, timeline);
    content.getChildren().add(timeline);

    ScrollPane scroll = new ScrollPane(content);
    scroll.setFitToWidth(true);
    scroll.setStyle("-fx-background-color: transparent;");
    scroll.getStyleClass().add("thin-scroll");
    AnchorPane.setTopAnchor(scroll, 0.0);
    AnchorPane.setBottomAnchor(scroll, 0.0);
    AnchorPane.setLeftAnchor(scroll, 0.0);
    AnchorPane.setRightAnchor(scroll, 0.0);

    detailPanel.getChildren().add(scroll);
    detailOverlay.setOnMouseClicked(e -> {
      if (e.getTarget() == detailOverlay) close();
    });
  }

  public void close() {
    detailOverlay.setVisible(false);
    detailOverlay.setManaged(false);
    onRefresh.run();
  }

  private void loadTimeline(Composition comp, VBox container) {
    Task<List<CompositionVersion>> task = new Task<>() {
      @Override protected List<CompositionVersion> call() {
        return versionService.getVersions(comp.getId());
      }
    };
    task.setOnSucceeded(e -> {
      List<CompositionVersion> versions = task.getValue();
      if (versions.isEmpty()) {
        Label empty = new Label("No versions saved yet");
        empty.setStyle("-fx-font-size: 14; -fx-text-fill: #B0ADA8; -fx-font-style: italic; -fx-padding: 8 0;");
        container.getChildren().add(empty);
        return;
      }

      int latestNum = versionService.getLatestVersionNumber(comp.getId());

      for (int i = 0; i < versions.size(); i++) {
        CompositionVersion v = versions.get(i);
        boolean isLatest = v.getVersionNumber() == latestNum;
        boolean isLast = i == versions.size() - 1;
        container.getChildren().add(buildTimelineItem(comp, v, isLatest, isLast, versions));
      }

      if (versions.size() >= 2) {
        Label compareLink = new Label("Compare Versions \u2192");
        compareLink.setStyle(
            "-fx-font-size: 13; -fx-font-weight: 600; -fx-text-fill: #B18DB8;" +
            " -fx-cursor: hand; -fx-padding: 10 0 4 36;");
        container.getChildren().add(compareLink);
      }
    });
    task.setOnFailed(e -> {
      log.error("Failed to load versions for composition {}", comp.getId(), e.getSource().getException());
      container.getChildren().add(new Label("Could not load versions"));
    });
    new Thread(task).start();
  }

  private HBox buildTimelineItem(Composition comp, CompositionVersion v,
                                  boolean isLatest, boolean isLast,
                                  List<CompositionVersion> allVersions) {
    HBox item = new HBox(10);
    item.getStyleClass().add("timeline-item");
    item.setAlignment(Pos.TOP_LEFT);
    item.setPadding(new Insets(0, 20, 0, 20));

    VBox dotCol = new VBox(0);
    dotCol.setAlignment(Pos.TOP_CENTER);
    dotCol.setPrefWidth(20);

    Circle dot = new Circle(5);
    dot.getStyleClass().add(isLatest ? "timeline-dot-active" : "timeline-dot");
    dotCol.getChildren().add(dot);

    if (!isLast) {
      Region line = new Region();
      line.getStyleClass().add("timeline-line");
      line.setPrefWidth(2);
      line.setMaxWidth(2);
      line.setPrefHeight(Double.MAX_VALUE);
      VBox.setVgrow(line, Priority.ALWAYS);
      dotCol.getChildren().add(line);
    }

    VBox content = new VBox(4);
    content.setAlignment(Pos.TOP_LEFT);
    HBox.setHgrow(content, Priority.ALWAYS);
    content.setPadding(new Insets(0, 0, 14, 0));

    HBox topRow = new HBox(8);
    topRow.setAlignment(Pos.CENTER_LEFT);

    Label vNum = new Label("v" + v.getVersionNumber());
    vNum.getStyleClass().add(isLatest ? "timeline-version-num-active" : "timeline-version-num");

    String descText = v.getChangeDescription() != null
        ? v.getChangeDescription() : "Saved";
    Label desc = new Label(descText);
    desc.getStyleClass().add("timeline-version-desc");
    desc.setWrapText(true);

    if (isLatest) {
      Label currentTag = new Label("CURRENT");
      currentTag.setStyle(
          "-fx-font-size: 10; -fx-font-weight: 700; -fx-text-fill: #5A9E8F;" +
          " -fx-background-color: rgba(90,158,143,0.12); -fx-background-radius: 6;" +
          " -fx-padding: 1 6;");
      topRow.getChildren().addAll(vNum, desc, currentTag);
    } else {
      topRow.getChildren().addAll(vNum, desc);
    }
    content.getChildren().add(topRow);

    Label date = new Label(v.getCreatedAt() != null
        ? v.getCreatedAt().format(SHORT_DATE) : "");
    date.getStyleClass().add("timeline-version-date");
    content.getChildren().add(date);

    if (v.getVersionNumber() > 1) {
      int prevNum = v.getVersionNumber() - 1;
      allVersions.stream()
          .filter(prev -> prev.getVersionNumber() == prevNum)
          .findFirst()
          .ifPresent(prevV -> {
            VBox diffBox = buildInlineDiff(comp.getId(), prevV.getVersionNumber(),
                v.getVersionNumber());
            content.getChildren().add(diffBox);
          });
    }

    HBox btnRow = new HBox(6);
    btnRow.setAlignment(Pos.CENTER_LEFT);
    btnRow.setPadding(new Insets(2, 0, 0, 0));

    if (!isLatest) {
      Button restoreBtn = new Button("Restore this version");
      restoreBtn.getStyleClass().add("timeline-restore-btn");
      restoreBtn.setOnAction(e -> onRestore.accept(comp, v.getVersionNumber()));
      btnRow.getChildren().add(restoreBtn);
    }

    if (!btnRow.getChildren().isEmpty()) content.getChildren().add(btnRow);

    item.getChildren().addAll(dotCol, content);
    return item;
  }

  private VBox buildInlineDiff(Long compositionId, int fromVersion, int toVersion) {
    VBox box = new VBox(3);
    box.getStyleClass().add("version-diff-inline");
    box.setPadding(new Insets(6, 10, 6, 10));

    Task<VersionService.VersionDiff> task = new Task<>() {
      @Override protected VersionService.VersionDiff call() {
        return versionService.diff(compositionId, fromVersion, toVersion);
      }
    };

    task.setOnSucceeded(e -> {
      VersionService.VersionDiff diff = task.getValue();
      if (diff == null || diff.isEmpty()) {
        Label noChange = new Label("No changes from v" + fromVersion);
        noChange.setStyle("-fx-font-size: 11; -fx-text-fill: #B0ADA8;");
        box.getChildren().add(noChange);
        return;
      }

      if (!diff.addedNoteIds.isEmpty()) {
        box.getChildren().add(diffSectionTitle("ADDED", "#5A9E8F"));
        diff.addedNoteIds.forEach(id ->
            noteRepository.findById(id).ifPresent(n ->
                box.getChildren().add(diffRow("+ " + n.getName(), "#5A9E8F"))));
      }

      if (!diff.removedNoteIds.isEmpty()) {
        box.getChildren().add(diffSectionTitle("REMOVED", "#D9534F"));
        diff.removedNoteIds.forEach(id ->
            noteRepository.findById(id).ifPresent(n ->
                box.getChildren().add(diffRow("\u2212 " + n.getName(), "#D9534F"))));
      }

      if (!diff.changedPercentages.isEmpty()) {
        box.getChildren().add(diffSectionTitle("CHANGED %", "#C8954A"));
        diff.changedPercentages.forEach((id, vals) ->
            noteRepository.findById(id).ifPresent(n ->
                box.getChildren().add(diffRow(
                    n.getName() + "  " + vals[0] + "% \u2192 " + vals[1] + "%",
                    "#C8954A"))));
      }

      if (diff.nameChanged) {
        box.getChildren().add(diffSectionTitle("RENAMED", "#B18DB8"));
        box.getChildren().add(diffRow(
            "\"" + diff.oldName + "\" \u2192 \"" + diff.newName + "\"", "#B18DB8"));
      }
    });

    task.setOnFailed(e -> {
      log.error("Failed to load diff for composition {} v{}-v{}: {}", compositionId, fromVersion, toVersion, e.getSource().getException().getMessage());
      Label err = new Label("Could not load diff");
      err.setStyle("-fx-font-size: 11; -fx-text-fill: #B0ADA8;");
      box.getChildren().add(err);
    });

    new Thread(task).start();
    return box;
  }

  private Label diffSectionTitle(String text, String color) {
    Label l = new Label(text);
    l.setStyle("-fx-font-size: 10; -fx-font-weight: 700; -fx-text-fill: " + color +
        "; -fx-letter-spacing: 1; -fx-padding: 3 0 1 0;");
    return l;
  }

  private Label diffRow(String text, String color) {
    Label l = new Label(text);
    l.setStyle("-fx-font-size: 12; -fx-text-fill: " + color + ";");
    l.setWrapText(true);
    return l;
  }

  private VBox statBox(String label, String value) {
    VBox box = new VBox(2);
    box.getStyleClass().add("stat-box");
    Label val = new Label(value);
    val.getStyleClass().add("stat-value");
    Label lbl = new Label(label.toUpperCase());
    lbl.getStyleClass().add("stat-label");
    box.getChildren().addAll(val, lbl);
    return box;
  }
}
