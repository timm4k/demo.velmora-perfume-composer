package velmora.composer.ui.controller;

import java.util.List;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import velmora.composer.model.Composition;
import velmora.composer.model.CompositionStatus;
import velmora.composer.repository.CompositionRepository;
import velmora.composer.repository.NoteRepository;
import velmora.composer.service.HistoryService;
import velmora.composer.service.VersionService;
import velmora.composer.state.CompositionState;
import velmora.composer.ui.UserSession;
import velmora.composer.ui.ViewManager;
import velmora.composer.ui.renderer.HistoryCompareRenderer;
import velmora.composer.ui.renderer.HistoryDetailRenderer;

@Component
@Slf4j
@RequiredArgsConstructor
public class HistoryController {

  private final CompositionRepository compositionRepository;
  private final NoteRepository noteRepository;
  private final UserSession userSession;
  private final ViewManager viewManager;
  private final CompositionState compositionState;
  private final HistoryService historyService;
  private final VersionService versionService;

  @FXML private VBox historyContainer;
  @FXML private Label historyCount;
  @FXML private Label historyEnd;
  @FXML private TextField searchField;
  @FXML private ComboBox<String> statusFilter;
  @FXML private ComboBox<String> sortFilter;
  @FXML private Button favoritesBtn;
  @FXML private StackPane detailOverlay;
  @FXML private StackPane compareOverlay;
  @FXML private VBox detailPanel;
  @FXML private VBox comparePanel;

  private HistoryDetailRenderer detailRenderer;
  private HistoryCompareRenderer compareRenderer;
  private List<Composition> allCompositions;
  private boolean favoritesFilterActive;

  @FXML
  public void initialize() {
    detailRenderer = new HistoryDetailRenderer(
        detailOverlay, detailPanel, historyService, versionService, noteRepository,
        this::loadHistory, this::handleOpen, this::handleDuplicate,
        this::confirmRestore, this::confirmDelete);
    compareRenderer = new HistoryCompareRenderer(
        compareOverlay, comparePanel, versionService, noteRepository,
        () -> {});
    setupFilters();
    setupSearch();
    loadHistory();
  }

  private void setupFilters() {
    statusFilter.setItems(FXCollections.observableArrayList(
        "All", "Draft", "Experimental", "Published", "Archived"));
    statusFilter.getSelectionModel().select("All");
    statusFilter.setOnAction(e -> applyFilters());

    sortFilter.setItems(FXCollections.observableArrayList(
        "Latest Edited", "Oldest", "Most Notes", "Highest Balance"));
    sortFilter.getSelectionModel().select("Latest Edited");
    sortFilter.setOnAction(e -> applyFilters());
  }

  private void setupSearch() {
    searchField.textProperty().addListener((obs, old, val) -> applyFilters());
  }

  @FXML
  private void toggleFavoritesFilter() {
    favoritesFilterActive = !favoritesFilterActive;
    favoritesBtn.setText(favoritesFilterActive ? "\u2665 Favorites" : "\u2661 Favorites");
    if (favoritesFilterActive) {
      favoritesBtn.getStyleClass().add("filter-btn-active");
    } else {
      favoritesBtn.getStyleClass().remove("filter-btn-active");
    }
    applyFilters();
  }

  @FXML
  private void handleBack() {
    viewManager.showMain();
  }

  private void loadHistory() {
    if (userSession.getUserId() == null) {
      historyCount.setText("0 formulas");
      showEmptyState("Log in to see your saved compositions");
      return;
    }
    Task<List<Composition>> task = new Task<>() {
      @Override protected List<Composition> call() {
        return historyService.getUserCompositions(userSession.getUserId());
      }
    };
    task.setOnSucceeded(e -> {
      allCompositions = task.getValue();
      applyFilters();
    });
    task.setOnFailed(e ->
        showEmptyState("Failed to load compositions: " + task.getException().getMessage()));
    new Thread(task).start();
  }

  private void applyFilters() {
    if (allCompositions == null) return;
    String search = searchField.getText().trim();
    CompositionStatus status = parseStatusFilter();
    String sort = parseSortFilter();

    Task<List<Composition>> task = new Task<>() {
      @Override protected List<Composition> call() {
        return historyService.getFilteredCompositions(
            userSession.getUserId(),
            search.isEmpty() ? null : search,
            status,
            favoritesFilterActive ? true : null,
            null,
            sort);
      }
    };
    task.setOnSucceeded(e -> renderCards(task.getValue()));
    new Thread(task).start();
  }

  private CompositionStatus parseStatusFilter() {
    String sel = statusFilter.getSelectionModel().getSelectedItem();
    if (sel == null || "All".equals(sel)) return null;
    return CompositionStatus.valueOf(sel.toUpperCase());
  }

  private String parseSortFilter() {
    String sel = sortFilter.getSelectionModel().getSelectedItem();
    if (sel == null) return null;
    return switch (sel) {
      case "Oldest"          -> "oldest";
      case "Most Notes"      -> "mostNotes";
      case "Highest Balance" -> "highestBalance";
      default                -> null;
    };
  }

  private void renderCards(List<Composition> compositions) {
    historyContainer.getChildren().clear();
    historyCount.setText(compositions.size() + " formulas");
    if (compositions.isEmpty()) {
      showEmptyState("No compositions found. Create one in the Composer");
      return;
    }
    for (Composition comp : compositions) {
      historyContainer.getChildren().add(createCard(comp));
    }
    historyEnd.setVisible(true);
  }

  private void showEmptyState(String message) {
    historyContainer.getChildren().clear();
    VBox empty = new VBox(4);
    empty.getStyleClass().add("history-empty");
    Label title = new Label("No compositions yet");
    title.getStyleClass().add("history-empty-title");
    Label sub = new Label(message);
    sub.getStyleClass().add("history-empty-sub");
    sub.setWrapText(true);
    empty.getChildren().addAll(title, sub);
    historyContainer.getChildren().add(empty);
    historyEnd.setVisible(false);
  }

  private Node createCard(Composition comp) {
    VBox card = new VBox(8);
    card.getStyleClass().add("history-card");

    HBox header = new HBox(10);
    header.getStyleClass().add("history-card-header");
    header.setAlignment(Pos.CENTER_LEFT);

    Label favBtn = new Label(comp.isFavorite() ? "\u2665" : "\u2661");
    favBtn.getStyleClass().add("history-card-fav");
    favBtn.setStyle(comp.isFavorite()
        ? "-fx-text-fill: #D69478;" : "-fx-text-fill: #C4BFB9;");
    favBtn.setOnMouseClicked(e -> {
      e.consume();
      historyService.toggleFavorite(comp.getId());
      loadHistory();
    });

    Label visBtn = new Label(comp.isPublic() ? "\u25C9 Public" : "\u25CB Private");
    visBtn.getStyleClass().add("history-card-vis");
    visBtn.setOnMouseClicked(e -> {
      e.consume();
      historyService.setPublic(comp.getId(), !comp.isPublic());
      loadHistory();
    });

    Label name = new Label(comp.getName() != null ? comp.getName() : "Untitled");
    name.getStyleClass().add("history-card-name");
    name.setWrapText(true);

    Label statusBadge = new Label(
        comp.getStatus() != null ? comp.getStatus().name() : "DRAFT");
    statusBadge.getStyleClass().addAll(
        "history-status-badge",
        "history-status-" + (comp.getStatus() != null
            ? comp.getStatus().name() : "DRAFT"));

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    int latestVer = versionService.getLatestVersionNumber(comp.getId());
    Label verBadge = new Label("v" + latestVer);
    verBadge.getStyleClass().add("history-card-version-badge");

    Button cloneBtn = new Button("Clone");
    cloneBtn.getStyleClass().add("detail-action-btn");
    cloneBtn.setOnAction(e -> handleDuplicate(comp));

    header.getChildren().addAll(favBtn, visBtn, name, statusBadge, spacer, verBadge, cloneBtn);

    HBox meta = new HBox(14);
    meta.getStyleClass().add("history-card-meta");
    meta.setAlignment(Pos.CENTER_LEFT);

    Label date = new Label(
        comp.getUpdatedAt() != null ? comp.getUpdatedAt().format(DATE_FMT) : "");
    date.getStyleClass().add("history-card-date");

    int noteCount = comp.getItems() != null ? comp.getItems().size() : 0;
    Label notesLabel = new Label(noteCount + " note" + (noteCount != 1 ? "s" : ""));
    notesLabel.getStyleClass().add("history-card-notes");

    String verText = latestVer + " version" + (latestVer != 1 ? "s" : "");
    Label versionsLabel = new Label(verText);
    versionsLabel.getStyleClass().add("history-card-versions");
    versionsLabel.setCursor(javafx.scene.Cursor.HAND);
    versionsLabel.setOnMouseClicked(e -> showDetailOverlay(comp));

    Button detailBtn = new Button("\u25BC Versions");
    detailBtn.getStyleClass().add("history-card-changes-btn");
    detailBtn.setOnAction(e -> showDetailOverlay(comp));

    meta.getChildren().addAll(date, notesLabel, versionsLabel, detailBtn);

    HBox statsRow = new HBox(6);
    statsRow.getStyleClass().add("history-card-stats");
    if (comp.getItems() != null && !comp.getItems().isEmpty()) {
      long families = comp.getItems().stream()
          .filter(i -> i.getNote() != null && i.getNote().getCategory() != null)
          .map(i -> i.getNote().getCategory())
          .distinct().count();
      if (families > 0) {
        Label fc = new Label(families + " families");
        fc.getStyleClass().add("history-stat-chip");
        statsRow.getChildren().add(fc);
      }
      int totalPct = comp.getItems().stream()
          .filter(i -> i.getPercentage() != null)
          .mapToInt(i -> i.getPercentage()).sum();
      Label pc = new Label(totalPct + "%");
      pc.getStyleClass().add("history-stat-chip");
      statsRow.getChildren().add(pc);
    }

    card.getChildren().addAll(header, meta);
    if (!statsRow.getChildren().isEmpty()) card.getChildren().add(statsRow);

    ContextMenu cm = buildContextMenu(comp);
    card.setOnMouseClicked(e -> {
      if (e.getButton() == MouseButton.PRIMARY) handleOpen(comp);
      else if (e.getButton() == MouseButton.SECONDARY)
        cm.show(card, e.getScreenX(), e.getScreenY());
    });

    return card;
  }

  private ContextMenu buildContextMenu(Composition comp) {
    ContextMenu cm = new ContextMenu();
    MenuItem open      = new MenuItem("Open in Composer");
    MenuItem details   = new MenuItem("View Versions");
    MenuItem compare   = new MenuItem("Compare Versions\u2026");
    MenuItem duplicate = new MenuItem("Duplicate");
    MenuItem rename    = new MenuItem("Rename");
    MenuItem delete    = new MenuItem("Delete");

    open.setOnAction(e      -> handleOpen(comp));
    details.setOnAction(e   -> showDetailOverlay(comp));
    compare.setOnAction(e   -> showCompareDialog(comp));
    duplicate.setOnAction(e -> handleDuplicate(comp));
    rename.setOnAction(e    -> showRenameDialog(comp));
    delete.setOnAction(e    -> confirmDelete(comp));

    cm.getItems().addAll(open, details, compare,
        new SeparatorMenuItem(), duplicate, rename, delete);
    return cm;
  }

  private void showDetailOverlay(Composition comp) {
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
    closeBtn.setOnMouseClicked(e -> closeDetail());

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
        statsGrid.getChildren().add(statBox("Notes",    String.valueOf(stats.noteCount)));
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
    openBtn.setOnAction(e -> { closeDetail(); handleOpen(comp); });

    Button dupBtn = new Button("Clone Formula");
    dupBtn.getStyleClass().add("detail-action-btn");
    dupBtn.setOnAction(e -> handleDuplicate(comp));

    Button deleteBtn = new Button("Delete");
    deleteBtn.getStyleClass().addAll("detail-action-btn", "detail-action-btn-danger");
    deleteBtn.setOnAction(e -> { closeDetail(); confirmDelete(comp); });

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
      if (e.getTarget() == detailOverlay) closeDetail();
    });
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
        boolean isLast   = i == versions.size() - 1;
        container.getChildren().add(buildTimelineItem(comp, v, isLatest, isLast, versions));
      }

      if (versions.size() >= 2) {
        Label compareLink = new Label("Compare Versions \u2192");
        compareLink.setStyle(
            "-fx-font-size: 13; -fx-font-weight: 600; -fx-text-fill: #B18DB8;" +
            " -fx-cursor: hand; -fx-padding: 10 0 4 36;");
        compareLink.setOnMouseClicked(ev -> showCompareDialog(comp));
        container.getChildren().add(compareLink);
      }
    });
    task.setOnFailed(e -> {
      log.error("Failed to load versions for composition {}: {}", comp.getId(), e.getSource().getException().getMessage());
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
      restoreBtn.setOnAction(e -> confirmRestore(comp, v.getVersionNumber()));
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

  private void showCompareDialog(Composition comp) {
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
    closeBtn.setOnMouseClicked(e -> closeCompare());

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
      diffTask.setOnSucceeded(de -> renderDiffIntoContainer(diffTask.getValue(), results));
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
      if (e.getTarget() == compareOverlay) closeCompare();
    });
  }

  private void renderDiffIntoContainer(VersionService.VersionDiff diff, VBox container) {
    if (diff == null || diff.isEmpty()) {
      Label l = new Label("No differences found between these versions");
      l.setStyle("-fx-font-size: 14; -fx-text-fill: #B0ADA8; -fx-font-style: italic;");
      container.getChildren().add(l);
      return;
    }

    if (!diff.addedNoteIds.isEmpty()) {
      container.getChildren().add(compareSectionTitle("ADDED NOTES"));
      diff.addedNoteIds.forEach(id -> noteRepository.findById(id).ifPresent(n -> {
        Label l = new Label("+ " + n.getName());
        l.getStyleClass().add("compare-added");
        container.getChildren().add(l);
      }));
    }

    if (!diff.removedNoteIds.isEmpty()) {
      container.getChildren().add(compareSectionTitle("REMOVED NOTES"));
      diff.removedNoteIds.forEach(id -> noteRepository.findById(id).ifPresent(n -> {
        Label l = new Label("\u2212 " + n.getName());
        l.getStyleClass().add("compare-removed");
        container.getChildren().add(l);
      }));
    }

    if (!diff.changedPercentages.isEmpty()) {
      container.getChildren().add(compareSectionTitle("CHANGED PERCENTAGES"));
      diff.changedPercentages.forEach((id, vals) ->
          noteRepository.findById(id).ifPresent(n -> {
            Label l = new Label(n.getName() + "  " + vals[0] + "% \u2192 " + vals[1] + "%");
            l.getStyleClass().add("compare-changed");
            container.getChildren().add(l);
          }));
    }

    if (diff.nameChanged) {
      container.getChildren().add(compareSectionTitle("NAME CHANGED"));
      Label l = new Label(
          "\"" + diff.oldName + "\" \u2192 \"" + diff.newName + "\"");
      l.getStyleClass().add("compare-desc-change");
      l.setWrapText(true);
      container.getChildren().add(l);
    }

    if (diff.descriptionChanged) {
      container.getChildren().add(compareSectionTitle("DESCRIPTION CHANGED"));
      Label l = new Label(
          "\"" + diff.oldDescription + "\" \u2192 \"" + diff.newDescription + "\"");
      l.getStyleClass().add("compare-desc-change");
      l.setWrapText(true);
      container.getChildren().add(l);
    }
  }

  private Label compareSectionTitle(String text) {
    Label l = new Label(text);
    l.getStyleClass().add("compare-section-title");
    return l;
  }

  private void handleOpen(Composition comp) {
    compositionState.setFormulaName(comp.getName());
    compositionState.setEditCompositionId(comp.getId());
    var items = comp.getItems();
    if (items != null) {
      compositionState.setCurrentNoteIds(items.stream()
          .filter(i -> i.getNote() != null)
          .map(i -> i.getNote().getId())
          .collect(Collectors.toList()));
      compositionState.setCurrentNoteNames(items.stream()
          .filter(i -> i.getNote() != null)
          .map(i -> i.getNote().getName() != null ? i.getNote().getName() : "")
          .collect(Collectors.toList()));
    }
    viewManager.showMain();
  }

  private void handleDuplicate(Composition comp) {
    Task<Void> t = new Task<>() {
      @Override protected Void call() {
        historyService.duplicateComposition(comp.getId());
        return null;
      }
    };
    t.setOnSucceeded(e -> loadHistory());
    new Thread(t).start();
  }

  private void showRenameDialog(Composition comp) {
    TextInputDialog dialog = new TextInputDialog(comp.getName());
    dialog.setTitle("Rename Composition");
    dialog.setHeaderText("Enter a new name:");
    dialog.showAndWait().ifPresent(name -> {
      if (!name.isBlank()) {
        Task<Void> t = new Task<>() {
          @Override protected Void call() {
            comp.setName(name);
            compositionRepository.save(comp);
            return null;
          }
        };
        t.setOnSucceeded(e -> loadHistory());
        new Thread(t).start();
      }
    });
  }

  private void confirmDelete(Composition comp) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
        "Delete \"" + comp.getName() + "\"?\nAll versions will also be deleted.");
    alert.showAndWait().ifPresent(r -> {
      if (r == ButtonType.OK) {
        Task<Void> t = new Task<>() {
          @Override protected Void call() {
            historyService.deleteComposition(comp.getId());
            return null;
          }
        };
        t.setOnSucceeded(e -> loadHistory());
        new Thread(t).start();
      }
    });
  }

  private void confirmRestore(Composition comp, int versionNumber) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
        "Restore Version " + versionNumber + " of \"" + comp.getName() + "\"?\n" +
        "A new version will be created with the restored state.");
    alert.showAndWait().ifPresent(r -> {
      if (r == ButtonType.OK) {
        Task<Void> t = new Task<>() {
          @Override protected Void call() {
            versionService.restoreVersion(comp.getId(), versionNumber);
            return null;
          }
        };
        t.setOnSucceeded(e -> {
          loadHistory();
          showDetailOverlay(comp);
        });
        new Thread(t).start();
      }
    });
  }

  private void closeDetail() {
    detailOverlay.setVisible(false);
    detailOverlay.setManaged(false);
    loadHistory();
  }

  private void closeCompare() {
    compareOverlay.setVisible(false);
    compareOverlay.setManaged(false);
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
