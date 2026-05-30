package velmora.composer.ui.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import velmora.composer.model.Note;
import velmora.composer.model.Role;
import velmora.composer.service.AdminService;
import velmora.composer.ui.UserSession;
import velmora.composer.ui.ViewManager;
import velmora.composer.ui.util.UiUtils;

@Component
@RequiredArgsConstructor
public class AdminController {

  private final AdminService adminService;
  private final UserSession userSession;
  private final ViewManager viewManager;

  @FXML private Label adminMessage;
  @FXML private ListView<Note> notesList;
  @FXML private Label notesCount;

  private final ObservableList<Note> notesObservable = FXCollections.observableArrayList();

  @FXML private TextField editName;
  @FXML private ComboBox<String> editCategory;
  @FXML private ComboBox<String> editType;
  @FXML private Spinner<Integer> editIntensity;
  @FXML private TextField editColor;
  @FXML private TextArea editDescription;

  @FXML private VBox editorPanel;
  @FXML private Label editorTitle;

  private Note editingNote;
  private boolean isNew;

  @FXML
  public void initialize() {
    if (userSession.getRole() != Role.ADMIN) {
      adminMessage.setText("Admin access required");
      notesList.setDisable(true);
      return;
    }
    editCategory.setItems(FXCollections.observableArrayList(
        "Citrus", "Floral", "Woody", "Spicy", "Gourmand", "Green",
        "Fruit", "Earthy", "Ozonic", "Synthetic", "Smoky", "Tobacco",
        "Musky", "Amber", "Leather"));
    editType.setItems(FXCollections.observableArrayList("TOP", "HEART", "BASE"));
    editIntensity.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 5));
    loadNotes();
    editorPanel.setVisible(false);
    editorPanel.setManaged(false);
  }

  private void loadNotes() {
    var items = adminService.getAllNotes();
    notesCount.setText(String.valueOf(items.size()));
    notesObservable.setAll(items);
    notesList.setItems(notesObservable);
    notesList.setCellFactory(lv -> new ListCell<>() {
      @Override
      protected void updateItem(Note n, boolean empty) {
        super.updateItem(n, empty);
        if (empty || n == null) {
          setText(null); setGraphic(null);
        } else {
          HBox box = new HBox(8);
          box.setAlignment(Pos.CENTER_LEFT);
          Circle dot = new Circle(5);
          dot.setFill(UiUtils.parseColor(n.getColorCode()));
          Label name = new Label(n.getName());
          name.getStyleClass().add("admin-note-name");
          Label type = new Label(n.getType() != null ? n.getType().name() : "");
          type.getStyleClass().add("admin-note-type");
          Label cat = new Label(n.getCategory() != null ? n.getCategory() : "");
          cat.getStyleClass().add("admin-note-category");
          Label intensity = new Label("★".repeat(n.getIntensity() != null ? n.getIntensity() / 2 : 0));
          intensity.getStyleClass().add("admin-note-intensity");
          box.getChildren().addAll(dot, name, type, cat, intensity);
          setGraphic(box);
        }
      }
    });
  }

  @FXML
  public void handleAdd() {
    if (userSession.getRole() != Role.ADMIN) return;
    isNew = true;
    editingNote = null;
    editorTitle.setText("NEW NOTE");
    editName.clear();
    editCategory.getSelectionModel().clearSelection();
    editType.getSelectionModel().clearSelection();
    editIntensity.getValueFactory().setValue(5);
    editColor.clear();
    editDescription.clear();
    adminMessage.setText("");
    editorPanel.setVisible(true);
    editorPanel.setManaged(true);
  }

  @FXML
  public void handleEdit() {
    if (userSession.getRole() != Role.ADMIN) return;
    Note selected = notesList.getSelectionModel().getSelectedItem();
    if (selected == null) return;
    isNew = false;
    editingNote = selected;
    editorTitle.setText("EDIT: " + selected.getName());
    editName.setText(selected.getName());
    editCategory.getSelectionModel().select(selected.getCategory());
    editType.getSelectionModel().select(selected.getType() != null ? selected.getType().name() : null);
    editIntensity.getValueFactory().setValue(selected.getIntensity() != null ? selected.getIntensity() : 5);
    editColor.setText(selected.getColorCode());
    editDescription.setText(selected.getDescription());
    adminMessage.setText("");
    editorPanel.setVisible(true);
    editorPanel.setManaged(true);
  }

  @FXML
  public void handleDelete() {
    if (userSession.getRole() != Role.ADMIN) return;
    Note selected = notesList.getSelectionModel().getSelectedItem();
    if (selected == null) return;

    Task<Void> task = new Task<>() {
      @Override
      protected Void call() {
        adminService.deleteNote(selected);
        return null;
      }
    };
    task.setOnSucceeded(e -> {
      editorPanel.setVisible(false);
      editorPanel.setManaged(false);
      loadNotes();
    });
    task.setOnFailed(e -> adminMessage.setText(
        "Delete failed: " + task.getException().getMessage()));
    new Thread(task).start();
  }

  @FXML
  public void handleSaveNote() {
    if (userSession.getRole() != Role.ADMIN) return;
    String name = editName.getText();
    if (name == null || name.trim().isEmpty()) {
      adminMessage.setText("Name required");
      return;
    }

    Note note = adminService.buildNote(
        isNew ? null : editingNote.getId(),
        name,
        editCategory.getValue(),
        editType.getValue(),
        editIntensity.getValue(),
        editColor.getText(),
        editDescription.getText()
    );

    Task<Void> task = new Task<>() {
      @Override
      protected Void call() {
        adminService.saveNote(note);
        return null;
      }
    };
    task.setOnSucceeded(e -> {
      editorPanel.setVisible(false);
      editorPanel.setManaged(false);
      adminMessage.setText("");
      loadNotes();
    });
    task.setOnFailed(e -> adminMessage.setText(
        "Save failed: " + task.getException().getMessage()));
    new Thread(task).start();
  }

  @FXML
  public void handleCancel() {
    editorPanel.setVisible(false);
    editorPanel.setManaged(false);
    adminMessage.setText("");
  }

  @FXML
  public void handleBack() {
    viewManager.showMain();
  }
}