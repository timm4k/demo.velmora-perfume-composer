package velmora.composer.ui.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import velmora.composer.model.Note;
import velmora.composer.model.NoteType;
import velmora.composer.model.Role;
import velmora.composer.repository.NoteRepository;
import velmora.composer.ui.UserSession;
import velmora.composer.ui.ViewManager;

@Component
@RequiredArgsConstructor
public class AdminController {

  private final NoteRepository noteRepository;
  private final UserSession userSession;
  private final ViewManager viewManager;

  @FXML private Label userInitials;
  @FXML private Label adminMessage;
  @FXML private ListView<Note> notesList;
  @FXML private Label notesCount;

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
    userInitials.setText(userSession.getInitials() != null ? userSession.getInitials() : "?");
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
    var items = noteRepository.findAll();
    notesCount.setText(String.valueOf(items.size()));
    notesList.setItems(FXCollections.observableArrayList(items));
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
          dot.setFill(parseColor(n.getColorCode()));
          Label name = new Label(n.getName());
          name.setStyle("-fx-font-size: 13; -fx-font-weight: 500;");
          Label type = new Label(n.getType() != null ? n.getType().name() : "");
          type.setStyle("-fx-font-size: 9; -fx-text-fill: #A4A0C5; -fx-font-weight: bold;");
          Label cat = new Label(n.getCategory() != null ? n.getCategory() : "");
          cat.setStyle("-fx-font-size: 9; -fx-text-fill: #B0ADA8; -fx-font-style: italic;");
          Label intensity = new Label("★".repeat(n.getIntensity() != null ? n.getIntensity() / 2 : 0));
          intensity.setStyle("-fx-font-size: 9; -fx-text-fill: #E2A998;");
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
    editorPanel.setVisible(true);
    editorPanel.setManaged(true);
  }

  @FXML
  public void handleDelete() {
    if (userSession.getRole() != Role.ADMIN) return;
    Note selected = notesList.getSelectionModel().getSelectedItem();
    if (selected == null) return;
    noteRepository.delete(selected);
    editorPanel.setVisible(false);
    editorPanel.setManaged(false);
    loadNotes();
  }

  @FXML
  public void handleSaveNote() {
    if (userSession.getRole() != Role.ADMIN) return;
    String name = editName.getText().trim();
    if (name.isEmpty()) { adminMessage.setText("Name required"); return; }

    Note note = isNew ? new Note() : editingNote;
    note.setName(name);
    note.setCategory(editCategory.getValue());
    note.setType(editType.getValue() != null ? NoteType.valueOf(editType.getValue()) : null);
    note.setIntensity(editIntensity.getValue());
    note.setColorCode(editColor.getText().trim());
    note.setDescription(editDescription.getText().trim());
    noteRepository.save(note);
    editorPanel.setVisible(false);
    editorPanel.setManaged(false);
    loadNotes();
  }

  @FXML
  public void handleCancel() {
    editorPanel.setVisible(false);
    editorPanel.setManaged(false);
  }

  @FXML
  public void handleBack() {
    viewManager.showMain();
  }

  private static Color parseColor(String colorCode) {
    if (colorCode == null || colorCode.isBlank()) return Color.GRAY;
    try { return Color.web(colorCode); }
    catch (Exception e) { return Color.GRAY; }
  }
}
