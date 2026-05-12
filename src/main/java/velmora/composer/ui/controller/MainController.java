package velmora.composer.ui.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import velmora.composer.model.Composition;
import velmora.composer.model.CompositionItem;
import velmora.composer.model.Note;
import velmora.composer.model.NoteType;
import velmora.composer.model.User;
import velmora.composer.repository.NoteRepository;
import velmora.composer.service.CompositionService;
import velmora.composer.ui.UserSession;
import velmora.composer.ui.ViewManager;

@Component
@RequiredArgsConstructor
public class MainController {

  private final NoteRepository noteRepository;
  private final CompositionService compositionService;
  private final ViewManager viewManager;
  private final UserSession userSession;

  @FXML private ListView<Note> notesListView;
  @FXML private TextField searchNotes;
  @FXML private TextField compositionName;
  @FXML private Label analysisLabel;
  @FXML private Label compatibilityScore;
  @FXML private Label analysisStatus;
  @FXML private Label topPctLabel;
  @FXML private Label heartPctLabel;
  @FXML private Label basePctLabel;
  @FXML private Label userInitials;
  @FXML private VBox topNotesZone;
  @FXML private VBox heartNotesZone;
  @FXML private VBox baseNotesZone;

  private final ObservableList<Note> allNotes = FXCollections.observableArrayList();
  private final ObservableList<Note> topNotes = FXCollections.observableArrayList();
  private final ObservableList<Note> heartNotes = FXCollections.observableArrayList();
  private final ObservableList<Note> baseNotes = FXCollections.observableArrayList();
  private FilteredList<Note> filteredNotes;

  @FXML
  public void initialize() {
    loadNotes();
    setupSearch();
    setupNoteClick();
    userInitials.setText(userSession.getInitials() != null ? userSession.getInitials() : "?");
    updateAnalysis();
  }

  private void loadNotes() {
    allNotes.setAll(noteRepository.findAll());
    filteredNotes = new FilteredList<>(allNotes, p -> true);
    notesListView.setItems(filteredNotes);
    notesListView.setCellFactory(lv -> new NoteListCell());
  }

  private void setupSearch() {
    searchNotes.textProperty().addListener((obs, old, val) -> {
      String query = val.toLowerCase().trim();
      filteredNotes.setPredicate(n ->
          query.isEmpty() || n.getName().toLowerCase().contains(query)
      );
    });
  }

  private void setupNoteClick() {
    notesListView.setOnMouseClicked(event -> {
      if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
        Note selected = notesListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
          toggleNoteInPyramid(selected);
        }
      }
    });
  }

  private void toggleNoteInPyramid(Note note) {
    switch (note.getType()) {
      case TOP -> toggleInList(note, topNotes);
      case HEART -> toggleInList(note, heartNotes);
      case BASE -> toggleInList(note, baseNotes);
    }
    updateZoneViews();
    updateAnalysis();
  }

  private void toggleInList(Note note, ObservableList<Note> list) {
    if (list.contains(note)) {
      list.remove(note);
    } else {
      list.add(note);
    }
  }

  private void removeFromPyramid(Note note) {
    switch (note.getType()) {
      case TOP -> topNotes.remove(note);
      case HEART -> heartNotes.remove(note);
      case BASE -> baseNotes.remove(note);
    }
    updateZoneViews();
    updateAnalysis();
  }

  private void updateZoneViews() {
    populateZone(topNotesZone, topNotes, "TOP NOTES", "\u2191 First impression");
    populateZone(heartNotesZone, heartNotes, "HEART NOTES", "\u2666 The soul of the fragrance");
    populateZone(baseNotesZone, baseNotes, "BASE NOTES", "\u23F3 Long-lasting foundation");

    double topPct = calculatePercentage(topNotes);
    double heartPct = calculatePercentage(heartNotes);
    double basePct = calculatePercentage(baseNotes);

    topPctLabel.setText(String.format("%.0f%%", topPct));
    heartPctLabel.setText(String.format("%.0f%%", heartPct));
    basePctLabel.setText(String.format("%.0f%%", basePct));
  }

  private void populateZone(VBox zone, ObservableList<Note> notes, String title, String subtitle) {
    zone.getChildren().clear();

    Label titleLabel = new Label(title);
    titleLabel.getStyleClass().add("pyramid-zone-title");
    zone.getChildren().add(titleLabel);

    if (notes.isEmpty()) {
      Label emptyLabel = new Label(subtitle);
      emptyLabel.getStyleClass().add("pyramid-zone-empty");
      zone.getChildren().add(emptyLabel);
    } else {
      for (Note note : notes) {
        HBox chip = createNoteChip(note);
        chip.setOnMouseClicked(e -> removeFromPyramid(note));
        zone.getChildren().add(chip);
      }
    }
  }

  private HBox createNoteChip(Note note) {
    HBox chip = new HBox(6);
    chip.setAlignment(Pos.CENTER_LEFT);
    chip.getStyleClass().add("note-chip");

    String typeClass = switch (note.getType()) {
      case TOP -> "top-note";
      case HEART -> "heart-note";
      case BASE -> "base-note";
    };
    chip.getStyleClass().add(typeClass);

    Rectangle dot = new Rectangle(6, 6, parseColor(note.getColorCode()));
    dot.setArcWidth(2);
    dot.setArcHeight(2);

    Label name = new Label(note.getName());
    name.setStyle("-fx-font-size: 12; -fx-font-weight: 600;");

    Label pct = new Label();
    pct.setStyle("-fx-font-size: 10; -fx-text-fill: #888;");

    chip.getChildren().addAll(dot, name);
    chip.setPickOnBounds(true);

    return chip;
  }

  private double calculatePercentage(ObservableList<Note> notes) {
    int total = topNotes.size() + heartNotes.size() + baseNotes.size();
    if (total == 0) return 0;
    return Math.round((double) notes.size() / total * 100);
  }

  private void updateAnalysis() {
    int total = topNotes.size() + heartNotes.size() + baseNotes.size();
    if (total == 0) {
      analysisLabel.setText("Waiting for ingredients...");
      compatibilityScore.setText("--");
      analysisStatus.setManaged(false);
      analysisStatus.setVisible(false);
    } else {
      analysisLabel.setText("Ready to analyze. Click ANALYZE.");
      int score = calculateScore();
      compatibilityScore.setText(String.valueOf(score));
      analysisStatus.setManaged(false);
      analysisStatus.setVisible(false);
    }
  }

  @FXML
  public void handleAnalyze() {
    int total = topNotes.size() + heartNotes.size() + baseNotes.size();

    if (total == 0) {
      analysisLabel.setText("Add some notes first");
      compatibilityScore.setText("--");
      analysisStatus.setManaged(false);
      analysisStatus.setVisible(false);
      return;
    }

    StringBuilder sb = new StringBuilder();
    sb.append("\u2022 ").append(topNotes.size()).append(" top notes\n");
    sb.append("\u2022 ").append(heartNotes.size()).append(" heart notes\n");
    sb.append("\u2022 ").append(baseNotes.size()).append(" base notes\n");

    int score = calculateScore();
    compatibilityScore.setText(String.valueOf(score));

    if (topNotes.isEmpty()) {
      sb.append("\n\u26A0 Missing top notes.\nScent may feel too heavy.");
      setStatus("warning", "Missing Top Notes");
    } else if (baseNotes.isEmpty()) {
      sb.append("\n\u26A0 No base notes.\nFragrance will lack longevity.");
      setStatus("warning", "Missing Base Notes");
    } else if (heartNotes.isEmpty()) {
      sb.append("\n\u26A0 No heart notes.\nThe fragrance has no core.");
      setStatus("warning", "Missing Heart Notes");
    } else if (score >= 70) {
      sb.append("\n\u2714 Good balance across all layers.");
      setStatus("good", "Balanced Composition");
    } else {
      sb.append("\n\u2022 Composition registered.\nConsider more variety.");
      setStatus("good", "Composition Ready");
    }

    analysisLabel.setText(sb.toString());
  }

  private int calculateScore() {
    int score = 0;
    if (!topNotes.isEmpty()) score += 30;
    if (!heartNotes.isEmpty()) score += 40;
    if (!baseNotes.isEmpty()) score += 30;
    return score;
  }

  private void setStatus(String type, String text) {
    analysisStatus.getStyleClass().removeAll("good", "warning", "error");
    analysisStatus.getStyleClass().add(type);
    analysisStatus.setText(text);
    analysisStatus.setManaged(true);
    analysisStatus.setVisible(true);
  }

  @FXML
  public void handleClear() {
    topNotes.clear();
    heartNotes.clear();
    baseNotes.clear();
    updateZoneViews();
    analysisLabel.setText("Waiting for ingredients...");
    compatibilityScore.setText("--");
    analysisStatus.setManaged(false);
    analysisStatus.setVisible(false);
  }

  @FXML
  public void handleSave() {
    String name = compositionName.getText().trim();
    if (name.isEmpty()) {
      analysisLabel.setText("Please name your composition first");
      return;
    }

    int total = topNotes.size() + heartNotes.size() + baseNotes.size();
    if (total == 0) {
      analysisLabel.setText("Add at least one note before saving");
      return;
    }

    List<Note> allSelected = new ArrayList<>();
    allSelected.addAll(topNotes);
    allSelected.addAll(heartNotes);
    allSelected.addAll(baseNotes);

    int basePct = 100 / total;
    int remainder = 100 - basePct * total;

    User currentUser = new User();
    currentUser.setId(userSession.getUserId());

    Composition composition = new Composition();
    composition.setName(name);
    composition.setDescription("Created in Velmora Olfactory Lab");
    composition.setPublic(false);
    composition.setUser(currentUser);

    for (int i = 0; i < total; i++) {
      CompositionItem item = new CompositionItem();
      item.setNoteId(allSelected.get(i).getId());
      item.setPercentage(basePct + (i < remainder ? 1 : 0));
      composition.getItems().add(item);
    }

    try {
      compositionService.saveComposition(composition);
      analysisLabel.setText("Saved: \"" + name + "\"");
      compositionName.clear();
      handleClear();
    } catch (Exception e) {
      analysisLabel.setText("Save failed: " + e.getMessage());
    }
  }

  @FXML
  public void handleSettings() {
    viewManager.showSettings();
  }

  @FXML
  public void handleLogout() {
    userSession.clear();
    viewManager.showAuth();
  }

  private static class NoteListCell extends ListCell<Note> {
    @Override
    protected void updateItem(Note note, boolean empty) {
      super.updateItem(note, empty);
      if (empty || note == null) {
        setText(null);
        setGraphic(null);
      } else {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER_LEFT);

        Rectangle dot = new Rectangle(8, 8);
        dot.setArcWidth(2);
        dot.setArcHeight(2);
        dot.setFill(parseColor(note.getColorCode()));

        Label nameLabel = new Label(note.getName());
        nameLabel.setStyle("-fx-font-size: 13; -fx-font-weight: 500;");

        Label typeLabel = new Label(note.getType().name());
        typeLabel.setStyle("-fx-font-size: 9; -fx-text-fill: #B0ADA8; -fx-font-weight: bold;");

        box.getChildren().addAll(dot, nameLabel, typeLabel);
        setGraphic(box);
      }
    }
  }

  private static Color parseColor(String colorCode) {
    if (colorCode == null || colorCode.isBlank()) return Color.GRAY;
    try {
      return Color.web(colorCode);
    } catch (Exception e) {
      return Color.GRAY;
    }
  }
}
