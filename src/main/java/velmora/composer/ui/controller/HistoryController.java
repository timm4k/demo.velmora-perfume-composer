package velmora.composer.ui.controller;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import velmora.composer.model.Composition;
import velmora.composer.model.CompositionStatus;
import velmora.composer.model.CompositionVersion;
import velmora.composer.repository.CompositionRepository;
import velmora.composer.repository.NoteRepository;
import velmora.composer.service.HistoryService;
import velmora.composer.service.VersionService;
import velmora.composer.state.CompositionState;
import velmora.composer.ui.UserSession;
import velmora.composer.ui.ViewManager;

@Component
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
  @FXML private VBox detailPanel;
  @FXML private StackPane compareOverlay;
  @FXML private VBox comparePanel;

  private List<Composition> allCompositions;
  private boolean favoritesFilterActive;

  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy  HH:mm");
  private static final DateTimeFormatter SHORT_DATE = DateTimeFormatter.ofPattern("MMM dd, HH:mm");

  @FXML
  public void initialize() {
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
    favoritesBtn.getStyleClass().removeAll("filter-btn-active");
    if (favoritesFilterActive) {
      favoritesBtn.getStyleClass().add("filter-btn-active");
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
      @Override
      protected List<Composition> call() {
        return historyService.getUserCompositions(userSession.getUserId());
      }
    };
    task.setOnSucceeded(e -> {
      allCompositions = task.getValue();
      applyFilters();
    });
    task.setOnFailed(e -> {
      System.err.println("[HISTORY] Load failed: " + task.getException().getMessage());
      showEmptyState("Failed to load compositions");
    });
    new Thread(task).start();
  }

  private void applyFilters() {
    if (allCompositions == null) return;

    String search = searchField.getText().trim();
    CompositionStatus status = parseStatusFilter();
    String sort = parseSortFilter();

    Task<List<Composition>> task = new Task<>() {
      @Override
      protected List<Composition> call() {
        return historyService.getFilteredCompositions(
            userSession.getUserId(),
            search.isEmpty() ? null : search,
            status,
            favoritesFilterActive ? true : null,
            null,
            sort
        );
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
      case "Oldest" -> "oldest";
      case "Most Notes" -> "mostNotes";
      case "Highest Balance" -> "highestBalance";
      default -> null;
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
    empty.getChildren().addAll(title, sub);
    historyContainer.getChildren().add(empty);
    historyEnd.setVisible(false);
  }

  private Node createCard(Composition comp) {
    VBox card = new VBox(8);
    card.getStyleClass().add("history-card");

    HBox header = new HBox(12);
    header.getStyleClass().add("history-card-header");

    Label name = new Label(comp.getName() != null ? comp.getName() : "Untitled");
    name.getStyleClass().add("history-card-name");

    Label favBtn = new Label(comp.isFavorite() ? "\u2665" : "\u2661");
    favBtn.getStyleClass().add("history-card-fav");
    favBtn.setStyle(comp.isFavorite() ? "-fx-text-fill: #D69478;" : "-fx-text-fill: #C4BFB9;");
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

    Label statusBadge = new Label(comp.getStatus() != null ? comp.getStatus().name() : "DRAFT");
    statusBadge.getStyleClass().addAll("history-status-badge", "history-status-" + (comp.getStatus() != null ? comp.getStatus().name() : "DRAFT"));

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    HBox actions = new HBox(6);
    actions.getStyleClass().add("history-card-actions");

    Button statusCombo = new Button(comp.getStatus() != null ? comp.getStatus().name() : "DRAFT");
    statusCombo.getStyleClass().add("status-combo-box");
    statusCombo.setOnMouseClicked(e -> showStatusMenu(statusCombo, comp));

    Button cloneBtn = new Button("Clone");
    cloneBtn.getStyleClass().add("detail-action-btn");
    cloneBtn.setOnAction(e -> handleDuplicate(comp));

    actions.getChildren().addAll(statusCombo, cloneBtn);
    header.getChildren().addAll(favBtn, visBtn, name, statusBadge, spacer, actions);

    HBox meta = new HBox(16);
    meta.getStyleClass().add("history-card-meta");

    String dateText = comp.getUpdatedAt() != null ? comp.getUpdatedAt().format(DATE_FMT) : "";
    Label date = new Label(dateText);
    date.getStyleClass().add("history-card-date");

    int noteCount = comp.getItems() != null ? comp.getItems().size() : 0;
    Label notes = new Label(noteCount + " notes");
    notes.getStyleClass().add("history-card-notes");

    int versionCount = versionService.getVersionCount(comp.getId());
    Label versions = new Label(versionCount + (versionCount == 1 ? " version" : " versions"));
    versions.getStyleClass().add("history-card-versions");

    meta.getChildren().addAll(date, notes, versions);

    HBox statsRow = new HBox(8);
    statsRow.getStyleClass().add("history-card-stats");

    if (comp.getItems() != null && !comp.getItems().isEmpty()) {
      long families = comp.getItems().stream()
          .filter(i -> i.getNote() != null && i.getNote().getCategory() != null)
          .map(i -> i.getNote().getCategory())
          .distinct()
          .count();
      if (families > 0) {
        Label familyChip = new Label(families + " families");
        familyChip.getStyleClass().add("history-stat-chip");
        statsRow.getChildren().add(familyChip);
      }

      int totalPct = comp.getItems().stream()
          .filter(i -> i.getPercentage() != null)
          .mapToInt(i -> i.getPercentage()).sum();
      Label pctChip = new Label(totalPct + "%");
      pctChip.getStyleClass().add("history-stat-chip");
      statsRow.getChildren().add(pctChip);
    }

    card.getChildren().addAll(header, meta);
    if (!statsRow.getChildren().isEmpty()) {
      card.getChildren().add(statsRow);
    }

    ContextMenu cm = buildContextMenu(comp);

    card.setOnMouseClicked(e -> {
      if (e.getButton() == MouseButton.PRIMARY) {
        handleOpen(comp);
      } else if (e.getButton() == MouseButton.SECONDARY) {
        cm.show(card, e.getScreenX(), e.getScreenY());
      }
    });

    return card;
  }

  private ContextMenu buildContextMenu(Composition comp) {
    ContextMenu cm = new ContextMenu();

    MenuItem open = new MenuItem("Open in Composer");
    MenuItem details = new MenuItem("View Details & Versions");
    MenuItem compare = new MenuItem("Compare Versions...");
    MenuItem duplicate = new MenuItem("Duplicate");
    MenuItem rename = new MenuItem("Rename");
    MenuItem delete = new MenuItem("Delete");

    open.setOnAction(e -> handleOpen(comp));
    details.setOnAction(e -> showDetailOverlay(comp));
    compare.setOnAction(e -> showCompareDialog(comp));
    duplicate.setOnAction(e -> handleDuplicate(comp));
    rename.setOnAction(e -> showRenameDialog(comp));
    delete.setOnAction(e -> confirmDelete(comp));

    cm.getItems().addAll(open, details, compare, new SeparatorMenuItem(), duplicate, rename, delete);
    return cm;
  }

  private void showStatusMenu(Button anchor, Composition comp) {
    ContextMenu menu = new ContextMenu();
    for (CompositionStatus s : CompositionStatus.values()) {
      MenuItem item = new MenuItem(s.name());
      item.setOnAction(e -> {
        historyService.setStatus(comp.getId(), s);
        loadHistory();
      });
      menu.getItems().add(item);
    }
    menu.show(anchor, anchor.getScene().getWindow().getX() + anchor.getLayoutX(),
        anchor.getScene().getWindow().getY() + anchor.getLayoutY() + anchor.getHeight());
  }

  private void handleOpen(Composition comp) {
    compositionState.setFormulaName(comp.getName());
    compositionState.setEditCompositionId(comp.getId());
    var items = comp.getItems();
    if (items != null) {
      compositionState.setCurrentNoteIds(
          items.stream()
              .filter(i -> i.getNote() != null)
              .map(i -> i.getNote().getId())
              .collect(Collectors.toList())
      );
      compositionState.setCurrentNoteNames(
          items.stream()
              .filter(i -> i.getNote() != null)
              .map(i -> i.getNote().getName() != null ? i.getNote().getName() : "")
              .collect(Collectors.toList())
      );
    }
    viewManager.showMain();
  }

  private void handleDuplicate(Composition comp) {
    Task<Void> task = new Task<>() {
      @Override
      protected Void call() {
        historyService.duplicateComposition(comp.getId());
        return null;
      }
    };
    task.setOnSucceeded(e -> loadHistory());
    new Thread(task).start();
  }

  private void showRenameDialog(Composition comp) {
    TextInputDialog dialog = new TextInputDialog(comp.getName());
    dialog.setTitle("Rename Composition");
    dialog.setHeaderText("Enter a new name:");
    dialog.showAndWait().ifPresent(name -> {
      if (!name.isBlank()) {
        Task<Void> task = new Task<>() {
          @Override
          protected Void call() {
            comp.setName(name);
            compositionRepository.save(comp);
            return null;
          }
        };
        task.setOnSucceeded(e -> loadHistory());
        new Thread(task).start();
      }
    });
  }

  private void confirmDelete(Composition comp) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
        "Delete \"" + comp.getName() + "\"?\nVersions will also be deleted.");
    alert.showAndWait().ifPresent(r -> {
      if (r == ButtonType.OK) {
        Task<Void> task = new Task<>() {
          @Override
          protected Void call() {
            historyService.deleteComposition(comp.getId());
            return null;
          }
        };
        task.setOnSucceeded(e -> loadHistory());
        new Thread(task).start();
      }
    });
  }

  private void showDetailOverlay(Composition comp) {
    detailPanel.getChildren().clear();
    detailOverlay.setVisible(true);
    detailOverlay.setManaged(true);

    VBox content = new VBox(0);

    HBox header = new HBox(12);
    header.getStyleClass().add("detail-header");
    Label title = new Label(comp.getName() != null ? comp.getName() : "Untitled");
    title.getStyleClass().add("detail-title");
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    Label closeBtn = new Label("\u2715");
    closeBtn.getStyleClass().add("detail-close");
    closeBtn.setOnMouseClicked(e -> closeDetail());
    header.getChildren().addAll(title, spacer, closeBtn);
    content.getChildren().add(header);

    FlowPane statsGrid = new FlowPane();
    statsGrid.getStyleClass().add("stats-grid");

    var stats = historyService.getStats(comp.getId());
    if (stats != null) {
      statsGrid.getChildren().add(createStatBox("Created",
          stats.createdAt != null ? stats.createdAt.format(SHORT_DATE) : "\u2014"));
      statsGrid.getChildren().add(createStatBox("Modified",
          stats.lastModified != null ? stats.lastModified.format(SHORT_DATE) : "\u2014"));
      statsGrid.getChildren().add(createStatBox("Versions", String.valueOf(stats.versionCount)));
      statsGrid.getChildren().add(createStatBox("Notes", String.valueOf(stats.noteCount)));
      if (stats.dominantFamily != null) {
        statsGrid.getChildren().add(createStatBox("Top Family", stats.dominantFamily));
      }
    }
    content.getChildren().add(statsGrid);

    HBox actions = new HBox(8);
    actions.getStyleClass().add("detail-actions");

    Button openBtn = new Button("Open in Composer");
    openBtn.getStyleClass().addAll("detail-action-btn", "detail-action-btn-primary");
    openBtn.setOnAction(e -> { closeDetail(); handleOpen(comp); });

    Button duplicateBtn = new Button("Clone Formula");
    duplicateBtn.getStyleClass().add("detail-action-btn");
    duplicateBtn.setOnAction(e -> handleDuplicate(comp));

    Button deleteBtn = new Button("Delete");
    deleteBtn.getStyleClass().addAll("detail-action-btn", "detail-action-btn-danger");
    deleteBtn.setOnAction(e -> { closeDetail(); confirmDelete(comp); });

    ComboBox<String> statusCombo = new ComboBox<>();
    statusCombo.getStyleClass().add("detail-status-combo");
    statusCombo.setItems(FXCollections.observableArrayList(
        "DRAFT", "EXPERIMENTAL", "PUBLISHED", "ARCHIVED"));
    statusCombo.getSelectionModel().select(comp.getStatus() != null ? comp.getStatus().name() : "DRAFT");
    statusCombo.setOnAction(e -> {
      historyService.setStatus(comp.getId(),
          CompositionStatus.valueOf(statusCombo.getSelectionModel().getSelectedItem()));
    });

    actions.getChildren().addAll(openBtn, duplicateBtn, statusCombo, deleteBtn);
    content.getChildren().add(actions);

    Label timelineTitle = new Label("Version Timeline");
    timelineTitle.getStyleClass().add("timeline-title");
    VBox.setMargin(timelineTitle, new Insets(8, 20, 0, 20));
    content.getChildren().add(timelineTitle);

    VBox timeline = buildTimeline(comp);
    content.getChildren().add(timeline);

    ScrollPane scroll = new ScrollPane(content);
    scroll.setFitToWidth(true);
    scroll.setStyle("-fx-background-color: transparent; -fx-background-radius: 12;");
    scroll.getStyleClass().add("thin-scroll");

    detailPanel.getChildren().add(scroll);

    detailOverlay.setOnMouseClicked(e -> {
      if (e.getTarget() == detailOverlay) closeDetail();
    });
  }

  private VBox createStatBox(String label, String value) {
    VBox box = new VBox(2);
    box.getStyleClass().add("stat-box");
    Label val = new Label(value);
    val.getStyleClass().add("stat-value");
    Label lbl = new Label(label.toUpperCase());
    lbl.getStyleClass().add("stat-label");
    box.getChildren().addAll(val, lbl);
    return box;
  }

  private VBox buildTimeline(Composition comp) {
    VBox container = new VBox(0);
    container.getStyleClass().add("timeline-container");

    Task<List<CompositionVersion>> task = new Task<>() {
      @Override
      protected List<CompositionVersion> call() {
        return versionService.getVersions(comp.getId());
      }
    };
    task.setOnSucceeded(e -> {
      List<CompositionVersion> versions = task.getValue();
      if (versions.isEmpty()) {
        Label empty = new Label("No versions saved yet");
        empty.setStyle("-fx-font-size: 15; -fx-text-fill: #B0ADA8; -fx-font-style: italic; -fx-padding: 8 0;");
        container.getChildren().add(empty);
        return;
      }
      for (int i = 0; i < versions.size(); i++) {
        CompositionVersion v = versions.get(i);
        boolean isLast = i == versions.size() - 1;
        container.getChildren().add(createTimelineItem(v, isLast, comp));
      }

      if (versions.size() >= 2) {
        Label compareLabel = new Label("Compare Versions \u2192");
        compareLabel.setStyle("-fx-font-size: 14; -fx-font-weight: 600; -fx-text-fill: #B18DB8; -fx-cursor: hand; -fx-padding: 8 0 0 36;");
        compareLabel.setOnMouseClicked(ev -> showCompareDialog(comp));
        container.getChildren().add(compareLabel);
      }
    });
    new Thread(task).start();

    return container;
  }

  private HBox createTimelineItem(CompositionVersion v, boolean isLast, Composition comp) {
    HBox item = new HBox(10);
    item.getStyleClass().add("timeline-item");
    item.setAlignment(Pos.TOP_LEFT);

    VBox timelineCol = new VBox(0);
    timelineCol.setAlignment(Pos.TOP_CENTER);
    timelineCol.setPrefWidth(24);
    Circle dot = new Circle(5);
    dot.getStyleClass().add(isLast || v.getVersionNumber() == versionService.getVersionCount(comp.getId()) ? "timeline-dot-active" : "timeline-dot");
    timelineCol.getChildren().add(dot);
    if (!isLast) {
      VBox line = new VBox();
      line.setPrefWidth(2);
      line.setMaxWidth(2);
      line.getStyleClass().add("timeline-line");
      VBox.setVgrow(line, Priority.ALWAYS);
      timelineCol.getChildren().add(line);
    }

    VBox content = new VBox(2);
    content.setAlignment(Pos.TOP_LEFT);
    HBox.setHgrow(content, Priority.ALWAYS);

    HBox topRow = new HBox(8);
    topRow.setAlignment(Pos.CENTER_LEFT);
    Label versionNum = new Label("v" + v.getVersionNumber());
    versionNum.getStyleClass().add("timeline-version-num");
    Label desc = new Label(v.getChangeDescription() != null ? v.getChangeDescription() : "Saved");
    desc.getStyleClass().add("timeline-version-desc");
    topRow.getChildren().addAll(versionNum, desc);

    HBox bottomRow = new HBox(8);
    bottomRow.setAlignment(Pos.CENTER_LEFT);
    Label date = new Label(v.getCreatedAt() != null ? v.getCreatedAt().format(SHORT_DATE) : "");
    date.getStyleClass().add("timeline-version-date");
    bottomRow.getChildren().add(date);

    if (!isLast) {
      Button restoreBtn = new Button("Restore");
      restoreBtn.getStyleClass().add("timeline-restore-btn");
      restoreBtn.setOnAction(e -> confirmRestore(comp, v.getVersionNumber()));
      bottomRow.getChildren().add(restoreBtn);
    }

    content.getChildren().addAll(topRow, bottomRow);

    item.getChildren().addAll(timelineCol, content);
    return item;
  }

  private void confirmRestore(Composition comp, int versionNumber) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
        "Restore Version " + versionNumber + " of \"" + comp.getName() + "\"?\nA new version will be created.");
    alert.showAndWait().ifPresent(r -> {
      if (r == ButtonType.OK) {
        Task<Void> task = new Task<>() {
          @Override
          protected Void call() {
            versionService.restoreVersion(comp.getId(), versionNumber);
            return null;
          }
        };
        task.setOnSucceeded(e -> {
          loadHistory();
          showDetailOverlay(comp);
        });
        new Thread(task).start();
      }
    });
  }

  private void showCompareDialog(Composition comp) {
    List<CompositionVersion> versions = versionService.getVersions(comp.getId());
    if (versions.size() < 2) {
      Alert alert = new Alert(Alert.AlertType.INFORMATION, "Need at least 2 versions to compare");
      alert.show();
      return;
    }

    comparePanel.getChildren().clear();
    compareOverlay.setVisible(true);
    compareOverlay.setManaged(true);

    VBox content = new VBox(0);

    Label title = new Label("Compare Versions");
    title.getStyleClass().add("compare-title");

    HBox header = new HBox(12);
    header.getStyleClass().add("compare-header");

    ComboBox<String> verA = new ComboBox<>();
    verA.getStyleClass().add("detail-status-combo");
    for (CompositionVersion v : versions) {
      verA.getItems().add("v" + v.getVersionNumber() + " - " + (v.getChangeDescription() != null ? v.getChangeDescription() : "Saved"));
    }
    verA.getSelectionModel().select(Math.min(1, versions.size() - 1));

    Label vs = new Label("VS");
    vs.getStyleClass().add("compare-vs");

    ComboBox<String> verB = new ComboBox<>();
    verB.getStyleClass().add("detail-status-combo");
    for (CompositionVersion v : versions) {
      verB.getItems().add("v" + v.getVersionNumber() + " - " + (v.getChangeDescription() != null ? v.getChangeDescription() : "Saved"));
    }
    verB.getSelectionModel().select(0);

    Button compareBtn = new Button("Compare");
    compareBtn.getStyleClass().addAll("detail-action-btn", "detail-action-btn-primary");

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    Label closeBtn = new Label("\u2715");
    closeBtn.getStyleClass().add("detail-close");
    closeBtn.setOnMouseClicked(e -> closeCompare());

    header.getChildren().addAll(verA, vs, verB, compareBtn, spacer, closeBtn);
    content.getChildren().addAll(title, header);

    VBox results = new VBox(0);
    results.setPadding(new Insets(0, 20, 20, 20));

    compareBtn.setOnAction(e -> {
      results.getChildren().clear();
      int idxA = verA.getSelectionModel().getSelectedIndex();
      int idxB = verB.getSelectionModel().getSelectedIndex();
      if (idxA < 0 || idxB < 0 || idxA == idxB) return;

      int vNumA = versions.get(idxA).getVersionNumber();
      int vNumB = versions.get(idxB).getVersionNumber();

      var diff = versionService.diff(comp.getId(), Math.min(vNumA, vNumB), Math.max(vNumA, vNumB));
      if (diff == null) {
        results.getChildren().add(new Label("Could not compute diff"));
        return;
      }

      Label sectionTitle;

      if (!diff.addedNotes.isEmpty()) {
        sectionTitle = new Label("ADDED NOTES");
        sectionTitle.getStyleClass().add("compare-section-title");
        results.getChildren().add(sectionTitle);
        for (Long noteId : diff.addedNotes) {
          noteRepository.findById(noteId).ifPresent(n -> {
            Label l = new Label("+ " + n.getName());
            l.getStyleClass().add("compare-added");
            results.getChildren().add(l);
          });
        }
      }

      if (!diff.removedNotes.isEmpty()) {
        sectionTitle = new Label("REMOVED NOTES");
        sectionTitle.getStyleClass().add("compare-section-title");
        results.getChildren().add(sectionTitle);
        for (Long noteId : diff.removedNotes) {
          noteRepository.findById(noteId).ifPresent(n -> {
            Label l = new Label("\u2212 " + n.getName());
            l.getStyleClass().add("compare-removed");
            results.getChildren().add(l);
          });
        }
      }

      if (!diff.changedPercentages.isEmpty()) {
        sectionTitle = new Label("CHANGED PERCENTAGES");
        sectionTitle.getStyleClass().add("compare-section-title");
        results.getChildren().add(sectionTitle);
        for (Map.Entry<Long, int[]> entry : diff.changedPercentages.entrySet()) {
          noteRepository.findById(entry.getKey()).ifPresent(n -> {
            int[] vals = entry.getValue();
            Label l = new Label(n.getName() + "  " + vals[0] + "% \u2192 " + vals[1] + "%");
            l.getStyleClass().add("compare-changed");
            results.getChildren().add(l);
          });
        }
      }

      if (diff.descriptionChanged) {
        sectionTitle = new Label("DESCRIPTION CHANGED");
        sectionTitle.getStyleClass().add("compare-section-title");
        results.getChildren().add(sectionTitle);
        Label l = new Label("\"" + (diff.oldDescription != null ? diff.oldDescription : "") + "\" \u2192 \"" + (diff.newDescription != null ? diff.newDescription : "") + "\"");
        l.getStyleClass().add("compare-desc-change");
        results.getChildren().add(l);
      }

      if (diff.addedNotes.isEmpty() && diff.removedNotes.isEmpty()
          && diff.changedPercentages.isEmpty() && !diff.descriptionChanged) {
        Label l = new Label("No differences found between these versions");
        l.setStyle("-fx-font-size: 15; -fx-text-fill: #B0ADA8; -fx-font-style: italic;");
        results.getChildren().add(l);
      }
    });

    content.getChildren().add(results);

    ScrollPane scroll = new ScrollPane(content);
    scroll.setFitToWidth(true);
    scroll.setStyle("-fx-background-color: transparent; -fx-background-radius: 12;");
    scroll.getStyleClass().add("thin-scroll");

    comparePanel.getChildren().add(scroll);
    compareOverlay.setOnMouseClicked(e -> {
      if (e.getTarget() == compareOverlay) closeCompare();
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
}
