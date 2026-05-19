package velmora.composer.ui.controller;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import velmora.composer.model.Composition;
import velmora.composer.repository.CompositionRepository;
import velmora.composer.state.CompositionState;
import velmora.composer.ui.UserSession;
import velmora.composer.ui.ViewManager;

@Component
@RequiredArgsConstructor
public class HistoryController {

  private final CompositionRepository compositionRepository;
  private final UserSession userSession;
  private final ViewManager viewManager;
  private final CompositionState compositionState;

  @FXML private VBox historyContainer;
  @FXML private Label historyCount;

  @FXML
  public void initialize() {
    loadHistory();
  }

  @FXML
  private void handleBack() {
    viewManager.showMain();
  }

  private void loadHistory() {
    if (userSession.getUserId() == null) {
      historyCount.setText("0 sessions");
      historyContainer.getChildren().clear();
      Label empty = new Label("Log in to see your saved compositions");
      empty.setStyle("-fx-font-size: 19; -fx-text-fill: #B0ADA8; -fx-padding: 40 0 0 0; -fx-font-style: italic;");
      historyContainer.getChildren().add(empty);
      return;
    }
    Task<List<Composition>> task = new Task<>() {
      @Override protected List<Composition> call() {
        return compositionRepository.findByUserIdOrderByUpdatedAtDesc(userSession.getUserId());
      }
    };
    task.setOnSucceeded(e -> renderCards(task.getValue()));
    task.setOnFailed(e -> System.err.println("[HISTORY] Load failed: " + task.getException().getMessage()));
    new Thread(task).start();
  }

  private void renderCards(List<Composition> compositions) {
    historyContainer.getChildren().clear();
    historyCount.setText(compositions.size() + " sessions");
    if (compositions.isEmpty()) {
      Label empty = new Label("No saved compositions yet. Create one in the Composer");
      empty.setStyle("-fx-font-size: 19; -fx-text-fill: #B0ADA8; -fx-padding: 40 0 0 0; -fx-font-style: italic;");
      historyContainer.getChildren().add(empty);
      return;
    }
    for (int i = 0; i < compositions.size(); i++) {
      HBox card = createHistoryCard(compositions.get(i), i == compositions.size() - 1);
      historyContainer.getChildren().add(card);
    }
  }

  private HBox createHistoryCard(Composition comp, boolean isLast) {
    HBox card = new HBox(16);
    card.setAlignment(Pos.TOP_LEFT);
    card.getStyleClass().add("history-card");

    VBox timeline = new VBox(0);
    timeline.setAlignment(Pos.TOP_CENTER);
    timeline.setPrefWidth(24);
    Circle dot = new Circle(5);
    dot.getStyleClass().add("history-dot");
    timeline.getChildren().add(dot);
    if (!isLast) {
      VBox line = new VBox();
      line.setPrefWidth(2);
      line.setMaxWidth(2);
      line.getStyleClass().add("history-line");
      VBox.setVgrow(line, Priority.ALWAYS);
      timeline.getChildren().add(line);
    }

    VBox content = new VBox(6);
    content.setAlignment(Pos.TOP_LEFT);
    HBox.setHgrow(content, Priority.ALWAYS);

    HBox header = new HBox(12);
    header.setAlignment(Pos.CENTER_LEFT);
    Label name = new Label(comp.getName());
    name.getStyleClass().add("history-name");
    Label status = new Label(deriveStatus(comp));
    status.getStyleClass().addAll("history-status", "status-" + deriveStatus(comp).toLowerCase());
    header.getChildren().addAll(name, status);

    HBox meta = new HBox(16);
    meta.setAlignment(Pos.CENTER_LEFT);
    meta.getStyleClass().add("history-meta");
    Label date = new Label(comp.getUpdatedAt() != null
        ? comp.getUpdatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy  HH:mm"))
        : "");
    Label notesCount = new Label((comp.getItems() != null ? comp.getItems().size() : 0) + " notes");
    date.getStyleClass().add("history-date");
    notesCount.getStyleClass().add("history-notes");
    meta.getChildren().addAll(date, notesCount);

    content.getChildren().addAll(header, meta);
    card.getChildren().addAll(timeline, content);

    ContextMenu cm = new ContextMenu();
    MenuItem open = new MenuItem("Open in Composer");
    MenuItem duplicate = new MenuItem("Duplicate");
    MenuItem rename = new MenuItem("Rename");
    MenuItem delete = new MenuItem("Delete");
    MenuItem export = new MenuItem("Export PDF");

    open.setOnAction(e -> handleOpen(comp));
    duplicate.setOnAction(e -> handleDuplicate(comp));
    rename.setOnAction(e -> showRenameDialog(comp));
    delete.setOnAction(e -> confirmDelete(comp));
    export.setOnAction(e -> handleExport(comp));

    cm.getItems().addAll(open, duplicate, new SeparatorMenuItem(), rename, delete, export);

    card.setOnMouseClicked(e -> {
      if (e.getButton() == MouseButton.PRIMARY) {
        handleOpen(comp);
      } else if (e.getButton() == MouseButton.SECONDARY) {
        cm.show(card, e.getScreenX(), e.getScreenY());
      }
    });

    return card;
  }

  private String deriveStatus(Composition comp) {
    if (comp.getDescription() != null && comp.getDescription().contains("EXPERIMENTAL"))
      return "EXPERIMENTAL";
    if (comp.getCreatedAt() == null || comp.getUpdatedAt() == null) return "DRAFT";
    long days = Duration.between(comp.getCreatedAt(), comp.getUpdatedAt()).toDays();
    if (days <= 1) return "DRAFT";
    if (days <= 7) return "REFINED";
    return "FINAL";
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
              .collect(java.util.stream.Collectors.toList())
      );
      compositionState.setCurrentNoteNames(
          items.stream()
              .filter(i -> i.getNote() != null)
              .map(i -> i.getNote().getName() != null ? i.getNote().getName() : "")
              .collect(java.util.stream.Collectors.toList())
      );
    }
    viewManager.showMain();
  }

  private void handleDuplicate(Composition comp) {
    Task<Void> task = new Task<>() {
      @Override protected Void call() {
        Composition copy = new Composition();
        copy.setName((comp.getName() != null ? comp.getName() : "Untitled") + " (copy)");
        copy.setDescription(comp.getDescription());
        copy.setUser(comp.getUser());
        copy.setItems(comp.getItems());
        compositionRepository.save(copy);
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
          @Override protected Void call() {
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
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete \"" + comp.getName() + "\"?");
    alert.showAndWait().ifPresent(r -> {
      if (r == ButtonType.OK) {
        Task<Void> task = new Task<>() {
          @Override protected Void call() {
            compositionRepository.delete(comp);
            return null;
          }
        };
        task.setOnSucceeded(e -> loadHistory());
        new Thread(task).start();
      }
    });
  }

  private void handleExport(Composition comp) {
    System.out.println("[HISTORY] Export: " + comp.getName());
  }
}
