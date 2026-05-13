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
import velmora.composer.service.SynergyService;
import velmora.composer.ui.UserSession;
import velmora.composer.ui.ViewManager;

@Component
@RequiredArgsConstructor
public class ComposerController {

  private final NoteRepository noteRepository;
  private final CompositionService compositionService;
  private final SynergyService synergyService;
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

  @FXML private VBox insightsContainer;
  @FXML private HBox adminLink;

  private final ObservableList<Note> allNotes = FXCollections.observableArrayList();
  private final ObservableList<Note> currentTopNotes = FXCollections.observableArrayList();
  private final ObservableList<Note> currentHeartNotes = FXCollections.observableArrayList();
  private final ObservableList<Note> currentBaseNotes = FXCollections.observableArrayList();
  private FilteredList<Note> filteredNotes;

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
    List<Note> all = allNotes();
    int total = all.size();
    long top = countByType(NoteType.TOP);
    long heart = countByType(NoteType.HEART);
    long base = countByType(NoteType.BASE);

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
      formulaStatus.setText("");
      return;
    }

    double topR = (double) top / total;
    double heartR = (double) heart / total;
    double baseR = (double) base / total;

    int balance = calcBalance(topR, heartR, baseR);
    harmonyScore.setText(String.valueOf(balance));

    int families = (int) all.stream().map(Note::getCategory).filter(Objects::nonNull).distinct().count();
    int cplx = Math.min(40 + families * 10 + (total > 6 ? 10 : 0), 100);
    complexityScore.setText(String.valueOf(cplx));

    int avgIntensity = (int) all.stream()
        .mapToInt(n -> n.getIntensity() != null ? n.getIntensity() : 5)
        .average().orElse(0);
    int lo = (int) (base > 0 ? 40 + base * 12 : 15);
    longevityScore.setText(String.valueOf(Math.min(lo, 100)));

    noteCount.setText(String.valueOf(total));
    int totalInt = all.stream().mapToInt(n -> n.getIntensity() != null ? n.getIntensity() : 5).sum();
    totalIntensity.setText(String.valueOf(totalInt));
    moleculeCount.setText(String.valueOf(total));

    if (base == 0) {
      estLongevity.setText("1-2h");
    } else if (base <= 2) {
      estLongevity.setText("4-6h");
    } else {
      estLongevity.setText("8-12h");
    }
    sillage.setText(avgIntensity >= 6 ? "Strong" : avgIntensity >= 4 ? "Moderate" : "Soft");

    updateFamilyDistribution();

    if (balance < 40) {
      formulaStatus.setText("⚠ Unbalanced — add missing pyramid layers");
      formulaStatus.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #E2A998;");
    } else if (topR > 0.6) {
      formulaStatus.setText("⚠ Too top-heavy — needs more heart/base");
      formulaStatus.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #E2A998;");
    } else if (baseR < 0.1 && total > 3) {
      formulaStatus.setText("⚠ Weak base — composition may lack longevity");
      formulaStatus.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #E2A998;");
    } else {
      formulaStatus.setText("✓ Good structure");
      formulaStatus.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #5A9E8F;");
    }

    renderSynergyInsights(all);
  }

  private void renderSynergyInsights(List<Note> notes) {
    insightsContainer.getChildren().clear();
    if (notes.size() < 2) {
      Label hint = new Label("Add at least 2 notes to see synergy insights");
      hint.setStyle("-fx-font-size: 12; -fx-font-style: italic; -fx-text-fill: #A8A29E;");
      insightsContainer.getChildren().add(hint);
      return;
    }

    var result = synergyService.calculate(notes);
    Label scoreLabel = new Label("Synergy: " + result.score() + "/100");
    scoreLabel.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: " +
        (result.score() >= 60 ? "#5A9E8F;" : result.score() >= 40 ? "#E2A998;" : "#D9534F;") +
        " -fx-padding: 0 0 6 0;");
    insightsContainer.getChildren().add(scoreLabel);

    for (var ins : result.conflicts()) {
      Label l = new Label("✗ " + ins.explanation());
      l.setWrapText(true);
      l.setStyle("-fx-font-size: 12; -fx-text-fill: #D9534F; -fx-padding: 0 0 4 4;");
      insightsContainer.getChildren().add(l);
    }

    int shown = 0;
    for (var ins : result.good()) {
      if (shown >= 5) break;
      Label l = new Label("✓ " + ins.explanation());
      l.setWrapText(true);
      l.setStyle("-fx-font-size: 12; -fx-text-fill: #5A9E8F; -fx-padding: 0 0 4 4;");
      insightsContainer.getChildren().add(l);
      shown++;
    }

    for (String w : result.warnings()) {
      Label l = new Label(w);
      l.setStyle("-fx-font-size: 12; -fx-text-fill: #E2A998; -fx-padding: 0 0 2 4;");
      insightsContainer.getChildren().add(l);
    }
  }

  private int calcBalance(double topR, double heartR, double baseR) {
    if (topR == 0 || heartR == 0 || baseR == 0) return 30;
    double score = 100;
    score -= Math.abs(topR - 0.25) * 80;
    score -= Math.abs(heartR - 0.50) * 60;
    score -= Math.abs(baseR - 0.25) * 70;
    return Math.max(0, Math.min(100, (int) score));
  }

  private long countByType(NoteType type) {
    return switch (type) {
      case TOP -> currentTopNotes.size();
      case HEART -> currentHeartNotes.size();
      case BASE -> currentBaseNotes.size();
    };
  }

  private List<Note> allNotes() {
    List<Note> all = new ArrayList<>();
    all.addAll(currentTopNotes);
    all.addAll(currentHeartNotes);
    all.addAll(currentBaseNotes);
    return all;
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
      formulaStatus.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #D9534F;");
      return;
    }
    List<Note> all = allNotes();
    if (all.isEmpty()) {
      formulaStatus.setText("NO NOTES");
      formulaStatus.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #D9534F;");
      return;
    }

    User userRef = new User();
    userRef.setId(userSession.getUserId());
    Composition composition = new Composition();
    composition.setName(name);
    composition.setDescription("Created in Velmora Olfactory Lab");
    composition.setPublic(false);
    composition.setUser(userRef);

    for (Note note : all) {
      CompositionItem item = new CompositionItem();
      item.setNoteId(note.getId());
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
