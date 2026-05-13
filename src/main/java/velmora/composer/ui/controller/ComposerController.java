package velmora.composer.ui.controller;

import java.util.*;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.geometry.Pos;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import velmora.composer.model.Composition;
import velmora.composer.model.CompositionItem;
import velmora.composer.model.Note;
import velmora.composer.model.NoteType;
import velmora.composer.model.Role;
import velmora.composer.model.User;
import velmora.composer.repository.NoteRepository;
import velmora.composer.service.CompositionService;
import velmora.composer.ui.UserSession;
import velmora.composer.ui.ViewManager;

@Component
@RequiredArgsConstructor
public class ComposerController {

  private final NoteRepository noteRepository;
  private final CompositionService compositionService;
  private final UserSession userSession;
  private final ViewManager viewManager;

  @FXML private Label userInitials;
  @FXML private ListView<Note> materialsList;
  @FXML private Label materialCount;
  @FXML private Label selectedCount;

  @FXML private Button filterAll;
  @FXML private Button filterTop;
  @FXML private Button filterHeart;
  @FXML private Button filterBase;

  @FXML private TextField formulaName;
  @FXML private Label formulaStatus;

  @FXML private FlowPane topChips;
  @FXML private FlowPane heartChips;
  @FXML private FlowPane baseChips;
  @FXML private Label topCount;
  @FXML private Label heartCount;
  @FXML private Label baseCount;
  @FXML private Label topCountBar;
  @FXML private Label heartCountBar;
  @FXML private Label baseCountBar;

  @FXML private Label harmonyScore;
  @FXML private Label complexityScore;
  @FXML private Label longevityScore;

  @FXML private Rectangle citrusBar;
  @FXML private Rectangle floralBar;
  @FXML private Rectangle woodyBar;
  @FXML private Rectangle earthyBar;
  @FXML private Label citrusPct;
  @FXML private Label floralPct;
  @FXML private Label woodyPct;
  @FXML private Label earthyPct;

  @FXML private Label noteCount;
  @FXML private Label totalIntensity;
  @FXML private Label estLongevity;
  @FXML private Label sillage;
  @FXML private Label familyCount;
  @FXML private Label moleculeCount;

  @FXML private StackPane evaporationChart;
  @FXML private HBox adminLink;

  private final ObservableList<Note> allNotes = FXCollections.observableArrayList();
  private final ObservableList<Note> currentTopNotes = FXCollections.observableArrayList();
  private final ObservableList<Note> currentHeartNotes = FXCollections.observableArrayList();
  private final ObservableList<Note> currentBaseNotes = FXCollections.observableArrayList();
  private FilteredList<Note> filteredNotes;

  private final Map<Long, Integer> percentages = new HashMap<>();
  private Button activeFilter;

  @FXML
  public void initialize() {
    userInitials.setText(userSession.getInitials() != null ? userSession.getInitials() : "?");
    adminLink.setVisible(userSession.getRole() == Role.ADMIN);
    adminLink.setManaged(userSession.getRole() == Role.ADMIN);
    loadNotes();
    setupFilterButtons();
    setupMaterialClick();
    updateAll();
  }

  private void loadNotes() {
    allNotes.setAll(noteRepository.findAll());
    filteredNotes = new FilteredList<>(allNotes, p -> true);
    materialsList.setItems(filteredNotes);
    materialsList.setCellFactory(lv -> new MaterialCell());
    updateMaterialCount();
  }

  private void setupFilterButtons() {
    activeFilter = filterAll;
    filterAll.setOnAction(e -> setFilter(null));
    filterTop.setOnAction(e -> setFilter(NoteType.TOP));
    filterHeart.setOnAction(e -> setFilter(NoteType.HEART));
    filterBase.setOnAction(e -> setFilter(NoteType.BASE));
  }

  private void setFilter(NoteType type) {
    if (type == null) {
      filteredNotes.setPredicate(p -> true);
    } else {
      filteredNotes.setPredicate(n -> n.getType() == type);
    }
    for (var btn : new Button[]{filterAll, filterTop, filterHeart, filterBase}) {
      btn.setStyle(null);
    }
    Button target = type == null ? filterAll
        : type == NoteType.TOP ? filterTop
        : type == NoteType.HEART ? filterHeart : filterBase;
    target.setStyle("-fx-background-color: #1C1917; -fx-text-fill: #FAFAF8; -fx-border-color: #1C1917;");
    activeFilter = target;
    updateMaterialCount();
  }

  private void setupMaterialClick() {
    materialsList.setOnMouseClicked(event -> {
      if (event.getButton() == MouseButton.PRIMARY) {
        Note selected = materialsList.getSelectionModel().getSelectedItem();
        if (selected != null) {
          addNoteToPyramid(selected);
        }
      }
    });
  }

  private void addNoteToPyramid(Note note) {
    List<Note> targetList = switch (note.getType()) {
      case TOP -> currentTopNotes;
      case HEART -> currentHeartNotes;
      case BASE -> currentBaseNotes;
    };
    if (!targetList.contains(note)) {
      targetList.add(note);
      percentages.put(note.getId(), 10);
    }
    updateAll();
  }

  private void removeNoteFromPyramid(Note note) {
    List<Note> targetList = switch (note.getType()) {
      case TOP -> currentTopNotes;
      case HEART -> currentHeartNotes;
      case BASE -> currentBaseNotes;
    };
    targetList.remove(note);
    percentages.remove(note.getId());
    updateAll();
  }

  private void updateAll() {
    renderChips();
    updateCounts();
    updateAnalysis();
  }

  private void renderChips() {
    renderChipPane(topChips, currentTopNotes);
    renderChipPane(heartChips, currentHeartNotes);
    renderChipPane(baseChips, currentBaseNotes);
  }

  private void renderChipPane(FlowPane pane, ObservableList<Note> notes) {
    pane.getChildren().clear();
    for (Note note : notes) {
      pane.getChildren().add(createChip(note, pane == topChips ? NoteType.TOP
          : pane == heartChips ? NoteType.HEART : NoteType.BASE, notes));
    }
  }

  private Label createChip(Note note, NoteType type, ObservableList<Note> source) {
    Label chip = new Label(note.getName());
    chip.getStyleClass().add("note-chip");
    chip.getStyleClass().add(
        type == NoteType.TOP ? "top-note"
        : type == NoteType.HEART ? "heart-note"
        : "base-note"
    );
    chip.setOnMouseClicked(e -> {
      source.remove(note);
      percentages.remove(note.getId());
      updateAll();
    });
    return chip;
  }

  private void updateCounts() {
    int top = currentTopNotes.size();
    int heart = currentHeartNotes.size();
    int base = currentBaseNotes.size();
    int total = top + heart + base;
    topCount.setText(String.valueOf(top));
    heartCount.setText(String.valueOf(heart));
    baseCount.setText(String.valueOf(base));
    topCountBar.setText(String.valueOf(top));
    heartCountBar.setText(String.valueOf(heart));
    baseCountBar.setText(String.valueOf(base));
    selectedCount.setText(String.valueOf(total));
  }

  private void updateAnalysis() {
    int top = currentTopNotes.size();
    int heart = currentHeartNotes.size();
    int base = currentBaseNotes.size();
    int total = top + heart + base;

    if (total == 0) {
      harmonyScore.setText("—");
      complexityScore.setText("—");
      longevityScore.setText("—");
      noteCount.setText("0");
      totalIntensity.setText("0");
      estLongevity.setText("—");
      sillage.setText("—");
      familyCount.setText("0");
      moleculeCount.setText("0");
      resetBars();
      return;
    }

    int harmony = calcHarmony(top, heart, base);
    harmonyScore.setText(String.valueOf(harmony));

    int complexity = Math.min(total * 14, 100);
    complexityScore.setText(String.valueOf(complexity));

    int longevity = base > 0 ? 40 + base * 15 : 20;
    longevity = Math.min(longevity, 100);
    longevityScore.setText(String.valueOf(longevity));

    noteCount.setText(String.valueOf(total));

    int intensity = currentTopNotes.stream().mapToInt(n -> n.getIntensity() != null ? n.getIntensity() : 5).sum()
        + currentHeartNotes.stream().mapToInt(n -> n.getIntensity() != null ? n.getIntensity() : 5).sum()
        + currentBaseNotes.stream().mapToInt(n -> n.getIntensity() != null ? n.getIntensity() : 5).sum();
    totalIntensity.setText(String.valueOf(intensity));

    if (base > 0) {
      estLongevity.setText(base >= 3 ? "8-12h" : base >= 2 ? "6-8h" : "4-6h");
    } else {
      estLongevity.setText("1-2h");
    }
    sillage.setText(total >= 5 ? "Moderate" : "Soft");

    updateFamilyDistribution();
    moleculeCount.setText(String.valueOf(total));
  }

  private int calcHarmony(int top, int heart, int base) {
    if (top == 0 || heart == 0 || base == 0) return 30;
    int total = top + heart + base;
    double topPct = (double) top / total * 100;
    double heartPct = (double) heart / total * 100;
    double basePct = (double) base / total * 100;
    int score = 100;
    score -= Math.abs(topPct - 25) * 0.8;
    score -= Math.abs(heartPct - 45) * 0.6;
    score -= Math.abs(basePct - 30) * 0.7;
    return Math.max(0, Math.min(100, (int) score));
  }

  private void updateFamilyDistribution() {
    List<Note> allSelected = new ArrayList<>();
    allSelected.addAll(currentTopNotes);
    allSelected.addAll(currentHeartNotes);
    allSelected.addAll(currentBaseNotes);
    long citrus = countByCategory(allSelected, "Citrus");
    long floral = countByCategory(allSelected, "Floral");
    long woody = countByCategory(allSelected, "Woody");
    long earthy = countByCategory(allSelected, "Earthy");
    long total = allSelected.size();
    Set<String> presentFamilies = allSelected.stream()
        .map(n -> n.getCategory() != null ? n.getCategory() : "Other")
        .collect(Collectors.toSet());
    familyCount.setText(String.valueOf(presentFamilies.size()));
    if (total == 0) {
      resetBars();
      return;
    }
    setBar(citrusBar, citrusPct, citrus, total);
    setBar(floralBar, floralPct, floral, total);
    setBar(woodyBar, woodyPct, woody, total);
    setBar(earthyBar, earthyPct, earthy, total);
  }

  private long countByCategory(List<Note> notes, String category) {
    return notes.stream()
        .filter(n -> category.equalsIgnoreCase(n.getCategory()))
        .count();
  }

  private void setBar(Rectangle bar, Label label, long count, long total) {
    double pct = (double) count / total * 100;
    label.setText(String.format("%.0f%%", pct));
    double maxWidth = 200;
    double w = pct / 100 * maxWidth;
    bar.setWidth(w);
    bar.setStyle(null);
  }

  private void resetBars() {
    citrusBar.setWidth(0); floralBar.setWidth(0);
    woodyBar.setWidth(0); earthyBar.setWidth(0);
    citrusPct.setText("0%"); floralPct.setText("0%");
    woodyPct.setText("0%"); earthyPct.setText("0%");
  }

  private void updateMaterialCount() {
    materialCount.setText(filteredNotes.size() + " materials");
  }

  @FXML
  public void handleSave() {
    String name = formulaName.getText().trim();
    if (name.isEmpty()) {
      formulaStatus.setText("NAME REQUIRED");
      return;
    }
    int total = currentTopNotes.size() + currentHeartNotes.size() + currentBaseNotes.size();
    if (total == 0) {
      formulaStatus.setText("NO NOTES");
      return;
    }
    int topSize = currentTopNotes.size();
    int heartSize = currentHeartNotes.size();
    int baseSize = currentBaseNotes.size();
    int totalSize = topSize + heartSize + baseSize;

    Map<Long, Integer> autoPct = new HashMap<>();
    if (totalSize > 0) {
      for (Note n : currentTopNotes)
        autoPct.put(n.getId(), (int) Math.round(25.0 / topSize));
      for (Note n : currentHeartNotes)
        autoPct.put(n.getId(), (int) Math.round(45.0 / heartSize));
      for (Note n : currentBaseNotes)
        autoPct.put(n.getId(), (int) Math.round(30.0 / baseSize));
    }

    User userRef = new User();
    userRef.setId(userSession.getUserId());
    Composition composition = new Composition();
    composition.setName(name);
    composition.setDescription("Created in Velmora Olfactory Lab");
    composition.setPublic(false);
    composition.setUser(userRef);

    for (Note note : currentTopNotes) {
      CompositionItem item = new CompositionItem();
      item.setNoteId(note.getId());
      item.setPercentage(autoPct.getOrDefault(note.getId(), 0));
      composition.getItems().add(item);
    }
    for (Note note : currentHeartNotes) {
      CompositionItem item = new CompositionItem();
      item.setNoteId(note.getId());
      item.setPercentage(autoPct.getOrDefault(note.getId(), 0));
      composition.getItems().add(item);
    }
    for (Note note : currentBaseNotes) {
      CompositionItem item = new CompositionItem();
      item.setNoteId(note.getId());
      item.setPercentage(autoPct.getOrDefault(note.getId(), 0));
      composition.getItems().add(item);
    }
    try {
      compositionService.saveComposition(composition);
      formulaStatus.setText("SAVED ✓");
      formulaStatus.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #5A9E8F;");
    } catch (Exception e) {
      formulaStatus.setText("ERROR: " + e.getMessage());
      formulaStatus.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #D9534F;");
    }
  }

  @FXML
  public void handleExport() {
    formulaStatus.setText("EXPORT — COMING SOON");
  }

  @FXML
  public void handleSettings() {
    viewManager.showSettings();
  }

  @FXML
  public void handleAdmin() {
    viewManager.showAdmin();
  }

  private static Color parseColor(String colorCode) {
    if (colorCode == null || colorCode.isBlank()) return Color.GRAY;
    try { return Color.web(colorCode); }
    catch (Exception e) { return Color.GRAY; }
  }

  private static class MaterialCell extends ListCell<Note> {
    @Override
    protected void updateItem(Note note, boolean empty) {
      super.updateItem(note, empty);
      if (empty || note == null) {
        setText(null);
        setGraphic(null);
      } else {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER_LEFT);
        Circle dot = new Circle(5);
        dot.setFill(parseColor(note.getColorCode()));
        Label name = new Label(note.getName());
        name.setStyle("-fx-font-size: 13; -fx-font-weight: 500; -fx-text-fill: #1C1917;");
        Label type = new Label(note.getType().name());
        type.setStyle("-fx-font-size: 9; -fx-text-fill: #B0ADA8; -fx-font-weight: bold;");
        Label cat = new Label(note.getCategory() != null ? note.getCategory() : "");
        cat.setStyle("-fx-font-size: 9; -fx-text-fill: #A4A0C5; -fx-font-style: italic;");
        box.getChildren().addAll(dot, name, type, cat);
        setGraphic(box);
      }
    }
  }
}
