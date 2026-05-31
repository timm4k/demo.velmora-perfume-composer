package velmora.composer.ui.controller;

import java.util.List;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
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
  @FXML private AnchorPane detailPanel;
  @FXML private VBox comparePanel;

  private HistoryDetailRenderer detailRenderer;
  private HistoryCompareRenderer compareRenderer;
  private List<Composition> allCompositions;
  private boolean favoritesFilterActive;

  @FXML
  public void initialize() {
    detailRenderer = new HistoryDetailRenderer(
        detailOverlay, detailPanel,
        historyService, versionService, noteRepository,
        this::loadHistory,
        this::handleOpen,
        this::handleDuplicate,
        this::confirmRestore,
        this::confirmDelete
    );
    compareRenderer = new HistoryCompareRenderer(
        compareOverlay, comparePanel,
        versionService, noteRepository,
        () -> {}
    );
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
    favoritesBtn.setText(favoritesFilterActive ? "♥ Favorites" : "♡ Favorites");
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
    header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

    Label favBtn = new Label(comp.isFavorite() ? "♥" : "♡");
    favBtn.getStyleClass().add("history-card-fav");
    favBtn.setStyle(comp.isFavorite() ? "-fx-text-fill: #D69478;" : "-fx-text-fill: #C4BFB9;");
    favBtn.setOnMouseClicked(e -> {
      e.consume();
      historyService.toggleFavorite(comp.getId());
      loadHistory();
    });

    Label visBtn = new Label(comp.isPublic() ? "◉ Public" : "○ Private");
    visBtn.getStyleClass().add("history-card-vis");
    visBtn.setOnMouseClicked(e -> {
      e.consume();
      historyService.setPublic(comp.getId(), !comp.isPublic());
      loadHistory();
    });

    Label name = new Label(comp.getName() != null ? comp.getName() : "Untitled");
    name.getStyleClass().add("history-card-name");
    name.setWrapText(true);

    Label statusBadge = new Label(comp.getStatus() != null ? comp.getStatus().name() : "DRAFT");
    statusBadge.getStyleClass().addAll("history-status-badge",
        "history-status-" + (comp.getStatus() != null ? comp.getStatus().name() : "DRAFT"));

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
    meta.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

    Label date = new Label(comp.getUpdatedAt() != null
        ? comp.getUpdatedAt().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy  HH:mm")) : "");
    date.getStyleClass().add("history-card-date");

    int noteCount = comp.getItems() != null ? comp.getItems().size() : 0;
    Label notesLabel = new Label(noteCount + " note" + (noteCount != 1 ? "s" : ""));
    notesLabel.getStyleClass().add("history-card-notes");

    String verText = latestVer + " version" + (latestVer != 1 ? "s" : "");
    Label versionsLabel = new Label(verText);
    versionsLabel.getStyleClass().add("history-card-versions");
    versionsLabel.setCursor(javafx.scene.Cursor.HAND);
    versionsLabel.setOnMouseClicked(e -> detailRenderer.show(comp));

    Button detailBtn = new Button("▼ Versions");
    detailBtn.getStyleClass().add("history-card-changes-btn");
    detailBtn.setOnAction(e -> detailRenderer.show(comp));

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
      if (e.getButton() == javafx.scene.input.MouseButton.PRIMARY) handleOpen(comp);
      else if (e.getButton() == javafx.scene.input.MouseButton.SECONDARY)
        cm.show(card, e.getScreenX(), e.getScreenY());
    });

    return card;
  }

  private ContextMenu buildContextMenu(Composition comp) {
    ContextMenu cm = new ContextMenu();
    MenuItem open      = new MenuItem("Open in Composer");
    MenuItem details   = new MenuItem("View Versions");
    MenuItem compare   = new MenuItem("Compare Versions…");
    MenuItem duplicate = new MenuItem("Duplicate");
    MenuItem rename    = new MenuItem("Rename");
    MenuItem delete    = new MenuItem("Delete");

    open.setOnAction(e      -> handleOpen(comp));
    details.setOnAction(e   -> detailRenderer.show(comp));
    compare.setOnAction(e   -> compareRenderer.show(comp));
    duplicate.setOnAction(e -> handleDuplicate(comp));
    rename.setOnAction(e    -> showRenameDialog(comp));
    delete.setOnAction(e    -> confirmDelete(comp));

    cm.getItems().addAll(open, details, compare,
        new SeparatorMenuItem(), duplicate, rename, delete);
    return cm;
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
          detailRenderer.show(comp);
        });
        new Thread(t).start();
      }
    });
  }
}