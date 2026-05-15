package velmora.composer.ui.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.util.Duration;
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
import velmora.composer.state.CompositionState;
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
  private final CompositionState compositionState;

  // Left panel
  @FXML private ListView<Note> materialsList;
  @FXML private Label materialCount;
  @FXML private Label selectedCount;
  @FXML private Button filterAll;
  @FXML private Button filterTop;
  @FXML private Button filterHeart;
  @FXML private Button filterBase;

  // Header
  @FXML private TextField formulaName;
  @FXML private Label formulaStatus;

  // Pyramid zones
  @FXML private FlowPane topChips;
  @FXML private FlowPane heartChips;
  @FXML private FlowPane baseChips;
  @FXML private Label topCount;
  @FXML private Label heartCount;
  @FXML private Label baseCount;
  @FXML private VBox topZone;
  @FXML private VBox heartZone;
  @FXML private VBox baseZone;

  // Center
  @FXML private Pane particleLayer;
  @FXML private VBox insightsContainer;

  // Right panel — performance bars
  @FXML private Rectangle harmonyBar;
  @FXML private Rectangle longevityBar;
  @FXML private Rectangle complexityBar;
  @FXML private Rectangle projectionBar;
  @FXML private Label harmonyScore;
  @FXML private Label longevityScore;
  @FXML private Label complexityScore;
  @FXML private Label projectionScore;

  // Right panel — family distribution
  @FXML private Rectangle citrusBar;
  @FXML private Rectangle floralBar;
  @FXML private Rectangle woodyBar;
  @FXML private Rectangle earthyBar;
  @FXML private Label citrusPct;
  @FXML private Label floralPct;
  @FXML private Label woodyPct;
  @FXML private Label earthyPct;

  // Right panel — formula metrics
  @FXML private Label noteCount;
  @FXML private Label totalIntensity;
  @FXML private Label estLongevity;
  @FXML private Label sillage;
  @FXML private Label familyCount;
  @FXML private Label moleculeCount;
  @FXML private Label topDuration;
  @FXML private Label heartDuration;
  @FXML private Label baseDuration;
  @FXML private Label overallScore;

  private final ObservableList<Note> allNotes = FXCollections.observableArrayList();
  private final ObservableList<Note> currentTopNotes = FXCollections.observableArrayList();
  private final ObservableList<Note> currentHeartNotes = FXCollections.observableArrayList();
  private final ObservableList<Note> currentBaseNotes = FXCollections.observableArrayList();
  private FilteredList<Note> filteredNotes;
  private Button activeFilter;
  private Long editingCompositionId;
  private static final double PERF_BAR_MAX = 160.0;

  @FXML
  public void initialize() {
    viewManager.setBeforeCatalogNavigate(() -> {
      compositionState.setCurrentNoteIds(allNotes().stream().map(Note::getId).collect(Collectors.toList()));
      compositionState.setCurrentNoteNames(allNotes().stream().map(Note::getName).collect(Collectors.toList()));
    });
    loadNotes();
    restoreFromState();
    setupFilterButtons();
    setupMaterialClick();
    setupDragDrop();
    initParticles();
    updateAll();
    Platform.runLater(this::updateAnalysis);
  }

  // ── Data ─────────────────────────────────────────

  private void loadNotes() {
    allNotes.setAll(noteRepository.findAll());
    filteredNotes = new FilteredList<>(allNotes, p -> true);
    materialsList.setItems(filteredNotes);
    materialsList.setCellFactory(lv -> new MaterialCardCell());
    updateMaterialCount();
  }

  private void restoreFromState() {
    List<Long> ids = compositionState.getCurrentNoteIds();
    if (ids != null && !ids.isEmpty()) {
      for (Long id : ids) {
        noteRepository.findById(id).ifPresent(this::addNoteToPyramid);
      }
    }
    if (compositionState.getFormulaName() != null) {
      formulaName.setText(compositionState.getFormulaName());
    }
    editingCompositionId = compositionState.getEditCompositionId();
    compositionState.clear();
  }

  // ── Filter pills ─────────────────────────────────

  private void setupFilterButtons() {
    activeFilter = filterAll;
    filterAll.setOnAction(e -> setFilter(null));
    filterTop.setOnAction(e -> setFilter(NoteType.TOP));
    filterHeart.setOnAction(e -> setFilter(NoteType.HEART));
    filterBase.setOnAction(e -> setFilter(NoteType.BASE));
    setFilter(null);
  }

  private void setFilter(NoteType type) {
    if (type == null) {
      filteredNotes.setPredicate(p -> true);
    } else {
      filteredNotes.setPredicate(n -> n.getType() == type);
    }
    for (var btn : new Button[]{filterAll, filterTop, filterHeart, filterBase}) {
      btn.getStyleClass().remove("active");
    }
    Button target = type == null ? filterAll
        : type == NoteType.TOP ? filterTop
        : type == NoteType.HEART ? filterHeart : filterBase;
    target.getStyleClass().add("active");
    activeFilter = target;
    updateMaterialCount();
  }

  // ── Click + Drag ─────────────────────────────────

  private void setupMaterialClick() {
    materialsList.setOnMouseClicked(event -> {
      if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
        Note selected = materialsList.getSelectionModel().getSelectedItem();
        if (selected != null) {
          addNoteToPyramid(selected);
        }
      }
    });
  }

  private void setupDragDrop() {
    materialsList.setCellFactory(lv -> {
      MaterialCardCell cell = new MaterialCardCell();
      cell.setOnDragDetected(event -> {
        Note note = cell.getItem();
        if (note == null) return;
        Dragboard db = cell.startDragAndDrop(TransferMode.COPY);
        ClipboardContent content = new ClipboardContent();
        content.putString(String.valueOf(note.getId()));
        db.setContent(content);
        cell.getStyleClass().add("dragging");
        event.consume();
      });
      cell.setOnDragDone(event -> {
        cell.getStyleClass().remove("dragging");
        event.consume();
      });
      return cell;
    });

    for (VBox zone : new VBox[]{topZone, heartZone, baseZone}) {
      zone.setOnDragOver(event -> {
        if (event.getGestureSource() != zone && event.getDragboard().hasString()) {
          event.acceptTransferModes(TransferMode.COPY);
          zone.getStyleClass().add("drag-over");
        }
        event.consume();
      });
      zone.setOnDragExited(event -> {
        zone.getStyleClass().remove("drag-over");
        event.consume();
      });
    }

    topZone.setOnDragDropped(event -> handleDrop(event, NoteType.TOP));
    heartZone.setOnDragDropped(event -> handleDrop(event, NoteType.HEART));
    baseZone.setOnDragDropped(event -> handleDrop(event, NoteType.BASE));
  }

  private void handleDrop(DragEvent event, NoteType type) {
    Dragboard db = event.getDragboard();
    if (db.hasString()) {
      long noteId = Long.parseLong(db.getString());
      noteRepository.findById(noteId).ifPresent(note -> {
        if (note.getType() == type) {
          addNoteToPyramid(note);
        }
      });
    }
    event.setDropCompleted(true);
    event.consume();
    ((VBox) event.getSource()).getStyleClass().remove("drag-over");
  }

  // ── Pyramid ──────────────────────────────────────

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

  // ── Chips ────────────────────────────────────────

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
      pane.getChildren().add(createChip(note));
    }
  }

  private HBox createChip(Note note) {
    HBox chip = new HBox(4);
    chip.setAlignment(Pos.CENTER_LEFT);
    chip.getStyleClass().add("note-chip-glass");
    chip.getStyleClass().add(
        note.getType() == NoteType.TOP ? "chip-top"
        : note.getType() == NoteType.HEART ? "chip-heart"
        : "chip-base");
    Label name = new Label(note.getName());
    name.setStyle("-fx-font-size: 12; -fx-font-weight: 500; -fx-text-fill: #4B4B4B;");
    Label close = new Label("✕");
    close.getStyleClass().add("chip-close");
    close.setOnMouseClicked(e -> removeNoteFromPyramid(note));
    chip.getChildren().addAll(name, close);
    return chip;
  }

  // ── Counts ───────────────────────────────────────

  private void updateCounts() {
    int top = currentTopNotes.size();
    int heart = currentHeartNotes.size();
    int base = currentBaseNotes.size();
    int total = top + heart + base;
    topCount.setText(String.valueOf(top));
    heartCount.setText(String.valueOf(heart));
    baseCount.setText(String.valueOf(base));
    selectedCount.setText(String.valueOf(total));
  }

  // ── Analysis ─────────────────────────────────────

  private void updateAnalysis() {
    List<Note> all = allNotes();
    int total = all.size();
    long top = countByType(NoteType.TOP);
    long heart = countByType(NoteType.HEART);
    long base = countByType(NoteType.BASE);

    if (total == 0) {
      harmonyScore.setText("—");
      longevityScore.setText("—");
      complexityScore.setText("—");
      projectionScore.setText("—");
      noteCount.setText("0");
      familyCount.setText("0");
      moleculeCount.setText("0");
      totalIntensity.setText("0");
      overallScore.setText("0");
      estLongevity.setText("—");
      sillage.setText("—");
      topDuration.setText("—");
      heartDuration.setText("—");
      baseDuration.setText("—");
      setPerfBar(harmonyBar, 0);
      setPerfBar(longevityBar, 0);
      setPerfBar(complexityBar, 0);
      setPerfBar(projectionBar, 0);
      insightsContainer.getChildren().clear();
      return;
    }

    double topRatio = (double) top / total;
    double heartRatio = (double) heart / total;
    double baseRatio = (double) base / total;

    double avgIntensity = all.stream()
        .filter(n -> n.getIntensity() != null)
        .mapToInt(Note::getIntensity)
        .average()
        .orElse(5.0);

    long families = all.stream()
        .map(Note::getCategory)
        .filter(Objects::nonNull)
        .distinct()
        .count();

    // FORMULA METRICS
    noteCount.setText(String.valueOf(total));
    familyCount.setText(String.valueOf(families));
    moleculeCount.setText(String.valueOf(total));
    totalIntensity.setText(String.format("%.1f", avgIntensity));

    // PHASE LONGEVITY
    double topHours = currentTopNotes.stream()
        .filter(n -> n.getIntensity() != null)
        .mapToInt(Note::getIntensity)
        .average()
        .orElse(5) * 0.5;

    double heartHours = currentHeartNotes.stream()
        .filter(n -> n.getIntensity() != null)
        .mapToInt(Note::getIntensity)
        .average()
        .orElse(5) * 1.2;

    double baseHours = currentBaseNotes.stream()
        .filter(n -> n.getIntensity() != null)
        .mapToInt(Note::getIntensity)
        .average()
        .orElse(5) * 2.0;

    topDuration.setText(topHours > 0 ? String.format("%.1fh", topHours) : "—");
    heartDuration.setText(heartHours > 0 ? String.format("%.1fh", heartHours) : "—");
    baseDuration.setText(baseHours > 0 ? String.format("%.1fh", baseHours) : "—");

    double totalLongevity = topHours * 0.2 + heartHours * 0.35 + baseHours * 0.45;
    estLongevity.setText(fmtLongevity(totalLongevity));

    // SILLAGE
    if (avgIntensity >= 8) {
      sillage.setText("Heavy");
    } else if (avgIntensity >= 6) {
      sillage.setText("Strong");
    } else if (avgIntensity >= 4) {
      sillage.setText("Moderate");
    } else {
      sillage.setText("Soft");
    }

    // PERFORMANCE
    int harmony = calcBalance(topRatio, heartRatio, baseRatio);
    harmonyScore.setText(String.valueOf(harmony));
    setPerfBar(harmonyBar, harmony);

    int longevity = Math.min((int) (totalLongevity * 15), 100);
    longevityScore.setText(String.valueOf(longevity));
    setPerfBar(longevityBar, longevity);

    int complexity = Math.min(30 + (int) families * 14 + total * 4, 100);
    complexityScore.setText(String.valueOf(complexity));
    setPerfBar(complexityBar, complexity);

    int projection = Math.min((int) avgIntensity * 15, 100);
    projectionScore.setText(String.valueOf(projection));
    setPerfBar(projectionBar, projection);

    // OVERALL COMPOSITE SCORE
    int overall = (int) (harmony * 0.25 + longevity * 0.30 + complexity * 0.20 + projection * 0.25);
    // ensure it shows value even if null label guard
    if (overallScore != null) {
      overallScore.setText(String.valueOf(overall));
    }

    updateFamilyDistribution();
    renderInsights(all);
  }

  private void setPerfBar(Rectangle bar, int value) {
    bar.setWidth(PERF_BAR_MAX * value / 100.0);
  }

  // ── Composition Insights (bottom panel) ──────────

  private void renderInsights(List<Note> notes) {
    insightsContainer.getChildren().clear();
    if (notes.size() < 2) {
      Label hint = new Label("Add at least 2 notes to see analysis");
      hint.setStyle("-fx-font-size: 11; -fx-font-style: italic; -fx-text-fill: #B0ADA8;");
      insightsContainer.getChildren().add(hint);
      return;
    }

    var synergy = synergyService.calculate(notes);
    int synScore = synergy.score();

    // Synergy score card
    VBox synCard = new VBox(4);
    synCard.getStyleClass().add("insight-glass-card");
    Label synTag = new Label("SYNERGY");
    synTag.getStyleClass().add("insight-tag-label");
    Label synVal = new Label(synScore + "/100");
    synVal.getStyleClass().add("insight-synergy");
    synVal.setStyle("-fx-font-size: 18; -fx-font-family: \"Cormorant Garamond\",serif;"
        + (synScore >= 60 ? " -fx-text-fill: #5A9E8F;"
        : synScore >= 40 ? " -fx-text-fill: #C8954A;" : " -fx-text-fill: #D9534F;"));
    synCard.getChildren().addAll(synTag, synVal);
    insightsContainer.getChildren().add(synCard);

    // Dominant profile
    VBox profileCard = new VBox(4);
    profileCard.getStyleClass().add("insight-glass-card");
    Label profTag = new Label("DOMINANT PROFILE");
    profTag.getStyleClass().add("insight-tag-label");
    Label profName = new Label(detectDominantProfile(notes));
    profName.getStyleClass().add("insight-title");
    profileCard.getChildren().addAll(profTag, profName);
    insightsContainer.getChildren().add(profileCard);

    // Opening (top notes analysis)
    if (!currentTopNotes.isEmpty()) {
      VBox openCard = new VBox(4);
      openCard.getStyleClass().add("insight-glass-card");
      Label openTag = new Label("OPENING");
      openTag.getStyleClass().add("insight-tag-label");
      Label openText = new Label(generateTopAnalysis(currentTopNotes));
      openText.getStyleClass().add("insight-body");
      openText.setWrapText(true);
      openCard.getChildren().addAll(openTag, openText);
      insightsContainer.getChildren().add(openCard);
    }

    // Heart transition
    if (!currentHeartNotes.isEmpty()) {
      VBox heartCard = new VBox(4);
      heartCard.getStyleClass().add("insight-glass-card");
      Label heartTag = new Label("HEART TRANSITION");
      heartTag.getStyleClass().add("insight-tag-label");
      Label heartText = new Label(generateHeartAnalysis(currentHeartNotes));
      heartText.getStyleClass().add("insight-body");
      heartText.setWrapText(true);
      heartCard.getChildren().addAll(heartTag, heartText);
      insightsContainer.getChildren().add(heartCard);
    }

    // Base behavior
    if (!currentBaseNotes.isEmpty()) {
      VBox baseCard = new VBox(4);
      baseCard.getStyleClass().add("insight-glass-card");
      Label baseTag = new Label("BASE BEHAVIOR");
      baseTag.getStyleClass().add("insight-tag-label");
      Label baseText = new Label(generateBaseAnalysis(currentBaseNotes));
      baseText.getStyleClass().add("insight-body");
      baseText.setWrapText(true);
      baseCard.getChildren().addAll(baseTag, baseText);
      insightsContainer.getChildren().add(baseCard);
    }

    // Conflicts (how/why scents DON'T combine)
    if (!synergy.conflicts().isEmpty()) {
      VBox conflictCard = new VBox(4);
      conflictCard.getStyleClass().add("insight-glass-card");
      Label confTag = new Label("POTENTIAL CONFLICTS");
      confTag.getStyleClass().add("insight-tag-label");
      conflictCard.getChildren().add(confTag);
      int shown = 0;
      for (var ins : synergy.conflicts()) {
        if (shown >= 2) break;
        Label l = new Label("◈ " + ins.explanation());
        l.getStyleClass().add("insight-conflict");
        l.setWrapText(true);
        conflictCard.getChildren().add(l);
        shown++;
      }
      insightsContainer.getChildren().add(conflictCard);
    }

    // Good combinations (how/why scents DO combine)
    if (!synergy.good().isEmpty()) {
      VBox goodCard = new VBox(4);
      goodCard.getStyleClass().add("insight-glass-card");
      Label goodTag = new Label("GOOD COMBINATIONS");
      goodTag.getStyleClass().add("insight-tag-label");
      goodCard.getChildren().add(goodTag);
      int shown = 0;
      for (var ins : synergy.good()) {
        if (shown >= 2) break;
        Label l = new Label("◈ " + ins.explanation());
        l.getStyleClass().add("insight-synergy");
        l.setWrapText(true);
        goodCard.getChildren().add(l);
        shown++;
      }
      insightsContainer.getChildren().add(goodCard);
    }

    // Warnings
    if (!synergy.warnings().isEmpty()) {
      VBox warnCard = new VBox(4);
      warnCard.getStyleClass().add("insight-glass-card");
      Label warnTag = new Label("OBSERVATIONS");
      warnTag.getStyleClass().add("insight-tag-label");
      warnCard.getChildren().add(warnTag);
      int shown = 0;
      for (String w : synergy.warnings()) {
        if (shown >= 2) break;
        Label l = new Label("◈ " + w);
        l.getStyleClass().add("insight-warning");
        l.setWrapText(true);
        warnCard.getChildren().add(l);
        shown++;
      }
      insightsContainer.getChildren().add(warnCard);
    }
  }

  private String generateTopAnalysis(List<Note> tops) {
    List<String> parts = new ArrayList<>();
    for (Note n : tops) {
      String cat = n.getCategory() != null ? n.getCategory() : "";
      switch (cat.toLowerCase()) {
        case "citrus" -> parts.add(n.getName() + " brings bright citrus lift");
        case "green" -> parts.add(n.getName() + " offers fresh green vibrancy");
        case "aromatic" -> parts.add(n.getName() + " provides herbal aromatic clarity");
        case "spicy" -> parts.add(n.getName() + " adds spicy top notes");
        case "floral" -> parts.add(n.getName() + " opens with floral delicacy");
        case "fruity" -> parts.add(n.getName() + " contributes fruity sweetness");
        default -> parts.add(n.getName() + " opens the composition");
      }
    }
    return String.join("; ", parts) + ".";
  }

  private String generateHeartAnalysis(List<Note> hearts) {
    List<String> parts = new ArrayList<>();
    for (Note n : hearts) {
      String cat = n.getCategory() != null ? n.getCategory() : "";
      switch (cat.toLowerCase()) {
        case "floral" -> parts.add(n.getName() + " forms a floral heart");
        case "spicy" -> parts.add(n.getName() + " introduces warmth and spice");
        case "woody" -> parts.add(n.getName() + " adds a woody transition");
        case "gourmand" -> parts.add(n.getName() + " brings gourmand richness");
        case "oriental" -> parts.add(n.getName() + " deepens with oriental warmth");
        default -> parts.add(n.getName() + " anchors the heart");
      }
    }
    return String.join("; ", parts) + ".";
  }

  private String generateBaseAnalysis(List<Note> bases) {
    List<String> parts = new ArrayList<>();
    for (Note n : bases) {
      String cat = n.getCategory() != null ? n.getCategory() : "";
      switch (cat.toLowerCase()) {
        case "woody" -> parts.add(n.getName() + " provides long woody fixation");
        case "earthy" -> parts.add(n.getName() + " grounds with earthy depth");
        case "musk" -> parts.add(n.getName() + " adds soft musky persistence");
        case "amber" -> parts.add(n.getName() + " creates warm amber trail");
        case "gourmand" -> parts.add(n.getName() + " leaves sweet gourmand imprint");
        default -> parts.add(n.getName() + " extends the dry-down");
      }
    }
    return String.join("; ", parts) + ".";
  }

  private String detectDominantProfile(List<Note> notes) {
    Map<String, Long> famCount = notes.stream()
        .map(n -> n.getCategory() != null ? n.getCategory() : "Other")
        .collect(Collectors.groupingBy(c -> c, Collectors.counting()));
    String topFam = famCount.entrySet().stream()
        .max(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey)
        .orElse("Balanced");

    long spicy = notes.stream().filter(n -> {
      String c = n.getCategory(); return c != null && (c.equalsIgnoreCase("Spicy") || c.equalsIgnoreCase("Woody"));
    }).count();
    long fresh = notes.stream().filter(n -> {
      String c = n.getCategory(); return c != null && (c.equalsIgnoreCase("Citrus") || c.equalsIgnoreCase("Green") || c.equalsIgnoreCase("Aromatic"));
    }).count();
    long sweet = notes.stream().filter(n -> {
      String c = n.getCategory(); return c != null && (c.equalsIgnoreCase("Floral") || c.equalsIgnoreCase("Gourmand"));
    }).count();

    StringBuilder sb = new StringBuilder();
    if (spicy > fresh && spicy > sweet) sb.append("Smoky ");
    if (sweet > spicy && sweet > fresh) sb.append("Sweet ");
    if (fresh > spicy && fresh > sweet) sb.append("Fresh ");
    sb.append(topFam).append(" Amber");
    return sb.toString().trim();
  }

  // ── Balance ──────────────────────────────────────

  private int calcBalance(double topR, double heartR, double baseR) {
    if (topR == 0 || heartR == 0 || baseR == 0) return 25;
    double score = 100;
    score -= Math.abs(topR - 0.30) * 50;
    score -= Math.abs(heartR - 0.50) * 40;
    score -= Math.abs(baseR - 0.20) * 50;
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

  // ── Family distribution ──────────────────────────

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
    if (total == 0) { resetBars(); return; }
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
    bar.setWidth(PERF_BAR_MAX * pct / 100.0);
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

  private String fmtLongevity(double value) {
    if (value <= 2) return "1-2h";
    if (value <= 4) return "2-4h";
    if (value <= 6) return "4-6h";
    if (value <= 8) return "6-8h";
    if (value <= 12) return "8-12h";
    return "12h+";
  }

  // ── Particles ────────────────────────────────────

  private void initParticles() {
    Random rand = new Random();
    List<Circle> particles = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
      Circle c = new Circle(1.5 + rand.nextDouble() * 4);
      double t = rand.nextDouble();
      if (t < 0.33)      c.setFill(Color.rgb(226, 169, 152, 0.08 + rand.nextDouble() * 0.12));
      else if (t < 0.66) c.setFill(Color.rgb(164, 160, 197, 0.08 + rand.nextDouble() * 0.12));
      else                c.setFill(Color.rgb(120, 160, 160, 0.08 + rand.nextDouble() * 0.12));
      c.setCenterX(rand.nextDouble() * 560 + 20);
      c.setCenterY(rand.nextDouble() * 320 + 60);
      c.setOpacity(0);
      particleLayer.getChildren().add(c);
      particles.add(c);
    }

    Timeline tl = new Timeline();
    for (int i = 0; i < particles.size(); i++) {
      Circle p = particles.get(i);
      double delay = rand.nextDouble() * 10;
      double dur = 8 + rand.nextDouble() * 12;
      double dx = (rand.nextDouble() - 0.5) * 50;
      double dy = (rand.nextDouble() - 0.5) * 30;
      tl.getKeyFrames().addAll(
          new KeyFrame(Duration.seconds(delay), new KeyValue(p.opacityProperty(), 0)),
          new KeyFrame(Duration.seconds(delay + 2), new KeyValue(p.opacityProperty(), 0.2 + rand.nextDouble() * 0.15)),
          new KeyFrame(Duration.seconds(delay + 2 + dur),
              new KeyValue(p.translateXProperty(), dx, Interpolator.EASE_BOTH),
              new KeyValue(p.translateYProperty(), dy, Interpolator.EASE_BOTH),
              new KeyValue(p.opacityProperty(), 0.2 + rand.nextDouble() * 0.15)),
          new KeyFrame(Duration.seconds(delay + 2 + dur + 2),
              new KeyValue(p.opacityProperty(), 0, Interpolator.EASE_BOTH)));
    }
    tl.setCycleCount(Timeline.INDEFINITE);
    tl.play();
  }

  // ── Actions ──────────────────────────────────────

  @FXML
  public void handleSave() {
    if (userSession.getUserId() == null) {
      formulaStatus.setText("LOG IN TO SAVE");
      formulaStatus.getStyleClass().add("status-badge-warn");
      return;
    }
    String name = formulaName.getText().trim();
    if (name.isEmpty()) {
      formulaStatus.setText("NAME REQUIRED");
      formulaStatus.getStyleClass().add("status-badge-warn");
      return;
    }
    List<Note> all = allNotes();
    if (all.isEmpty()) {
      formulaStatus.setText("NO NOTES");
      formulaStatus.getStyleClass().add("status-badge-warn");
      return;
    }
    User userRef = new User();
    userRef.setId(userSession.getUserId());
    Composition composition;
    if (editingCompositionId != null) {
      composition = new Composition();
      composition.setId(editingCompositionId);
    } else {
      composition = new Composition();
    }
    composition.setName(name);
    composition.setDescription("Created in Velmora Olfactory Lab");
    composition.setPublic(false);
    composition.setUser(userRef);
    int total = all.size();
    int basePct = 100 / total;
    int remainder = 100 - basePct * total;
    for (int i = 0; i < all.size(); i++) {
      CompositionItem item = new CompositionItem();
      item.setNoteId(all.get(i).getId());
      item.setPercentage(basePct + (i < remainder ? 1 : 0));
      composition.getItems().add(item);
    }
    javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<>() {
      @Override protected Void call() {
        compositionService.saveComposition(composition);
        return null;
      }
    };
    formulaStatus.setText("SAVING...");
    task.setOnSucceeded(e -> {
      formulaStatus.setText("SAVED ✓");
      formulaStatus.getStyleClass().remove("status-badge-warn");
      editingCompositionId = null;
    });
    task.setOnFailed(e -> {
      formulaStatus.setText("ERROR: " + task.getException().getMessage());
      formulaStatus.getStyleClass().add("status-badge-warn");
    });
    new Thread(task).start();
  }

  @FXML
  public void handleExport() {
    List<Note> all = allNotes();
    if (all.isEmpty()) {
      formulaStatus.setText("NOTHING TO EXPORT");
      formulaStatus.getStyleClass().add("status-badge-warn");
      return;
    }

    FileChooser chooser = new FileChooser();
    chooser.setTitle("Export Formula Report");
    chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Report", "*.pdf"));
    String baseName = formulaName.getText().trim();
    if (baseName.isEmpty()) baseName = "formula";
    chooser.setInitialFileName(baseName.replaceAll("\\s+", "_") + ".pdf");

    File file = chooser.showSaveDialog(formulaName.getScene().getWindow());
    if (file == null) return;

    formulaStatus.setText("EXPORTING...");
    formulaStatus.getStyleClass().remove("status-badge-warn");

    String name = baseName;
    javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<>() {
      @Override protected Void call() throws Exception {
        com.lowagie.text.Document doc = new com.lowagie.text.Document();
        com.lowagie.text.pdf.PdfWriter.getInstance(doc, new FileOutputStream(file));
        doc.open();

        com.lowagie.text.Font titleF = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 18, com.lowagie.text.Font.BOLD);
        com.lowagie.text.Font hF = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 14, com.lowagie.text.Font.BOLD);
        com.lowagie.text.Font bF = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 12, com.lowagie.text.Font.BOLD);
        com.lowagie.text.Font nF = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 11);

        doc.add(new com.lowagie.text.Paragraph("VELMORA OLFACTORY LAB", titleF));
        doc.add(new com.lowagie.text.Paragraph("FORMULA REPORT", hF));
        doc.add(new com.lowagie.text.Paragraph(" "));

        doc.add(new com.lowagie.text.Paragraph("Formula: " + name, bF));
        doc.add(new com.lowagie.text.Paragraph("Date: " + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date()), nF));
        doc.add(new com.lowagie.text.Paragraph(" "));

        // Notes table
        com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(5);
        table.setWidthPercentage(100);
        float[] cols = {35, 14, 18, 13, 10};
        table.setWidths(cols);
        String[] headers = {"Note", "Type", "Category", "Int.", "%"};
        for (String h : headers) {
          com.lowagie.text.pdf.PdfPCell cell = new com.lowagie.text.pdf.PdfPCell(
              new com.lowagie.text.Phrase(h, new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 9, com.lowagie.text.Font.BOLD)));
          cell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
          table.addCell(cell);
        }
        for (Note n : all) {
          table.addCell(n.getName() != null ? n.getName() : "—");
          table.addCell(n.getType() != null ? n.getType().name() : "—");
          table.addCell(n.getCategory() != null ? n.getCategory() : "—");
          table.addCell(n.getIntensity() != null ? String.valueOf(n.getIntensity()) : "—");
          table.addCell("—");
        }
        doc.add(table);
        doc.add(new com.lowagie.text.Paragraph(" "));

        doc.add(new com.lowagie.text.Paragraph("PERFORMANCE", bF));
        doc.add(new com.lowagie.text.Paragraph("Harmony: " + harmonyScore.getText(), nF));
        doc.add(new com.lowagie.text.Paragraph("Longevity: " + longevityScore.getText(), nF));
        doc.add(new com.lowagie.text.Paragraph("Complexity: " + complexityScore.getText(), nF));
        doc.add(new com.lowagie.text.Paragraph("Projection: " + projectionScore.getText(), nF));
        if (overallScore != null) {
          doc.add(new com.lowagie.text.Paragraph("Overall Score: " + overallScore.getText(), bF));
        }
        doc.add(new com.lowagie.text.Paragraph(" "));

        doc.add(new com.lowagie.text.Paragraph("PHASE LONGEVITY", bF));
        doc.add(new com.lowagie.text.Paragraph("Top Notes: " + topDuration.getText(), nF));
        doc.add(new com.lowagie.text.Paragraph("Heart Notes: " + heartDuration.getText(), nF));
        doc.add(new com.lowagie.text.Paragraph("Base Notes: " + baseDuration.getText(), nF));
        doc.add(new com.lowagie.text.Paragraph("Est. Total: " + estLongevity.getText(), nF));
        doc.add(new com.lowagie.text.Paragraph("Sillage: " + sillage.getText(), nF));
        doc.add(new com.lowagie.text.Paragraph(" "));

        doc.add(new com.lowagie.text.Paragraph("NOTES: " + noteCount.getText() +
            "  |  FAMILIES: " + familyCount.getText() +
            "  |  INTENSITY: " + totalIntensity.getText(), nF));

        doc.close();
        return null;
      }
    };
    task.setOnSucceeded(e -> {
      formulaStatus.setText("EXPORTED ✓");
      formulaStatus.getStyleClass().remove("status-badge-warn");
    });
    task.setOnFailed(e -> {
      formulaStatus.setText("EXPORT FAILED");
      formulaStatus.getStyleClass().add("status-badge-warn");
      System.out.println("[EXPORT] Error: " + task.getException().getMessage());
    });
    new Thread(task).start();
  }
  @FXML
  public void handleVault() { viewManager.showVault(); }
  @FXML
  public void handleSettings() { viewManager.showSettings(); }
  @FXML
  public void handleAdmin() { viewManager.showAdmin(); }
  @FXML
  public void handleHistory() {
    viewManager.showHistory();
  }

  private static Color parseColor(String code) {
    if (code == null || code.isBlank()) return Color.GRAY;
    try { return Color.web(code); } catch (Exception e) { return Color.GRAY; }
  }

  // ── MaterialCardCell ─────────────────────────────

  private static class MaterialCardCell extends ListCell<Note> {
    @Override
    protected void updateItem(Note note, boolean empty) {
      super.updateItem(note, empty);
      if (empty || note == null) {
        setGraphic(null);
        setStyle("-fx-background-color: transparent; -fx-padding: 3 0;");
      } else {
        HBox root = new HBox(12);
        root.setAlignment(Pos.CENTER_LEFT);
        root.getStyleClass().add("material-mini-card");

        Circle dot = new Circle(6);
        dot.getStyleClass().add("mcard-color-dot");
        dot.setFill(parseColor(note.getColorCode()));

        VBox info = new VBox(3);
        VBox.setVgrow(info, Priority.ALWAYS);

        // Row 1: Name
        Label name = new Label(note.getName());
        name.getStyleClass().add("mcard-name");

        // Row 2: tags row
        HBox tags = new HBox(8);
        tags.setAlignment(Pos.CENTER_LEFT);
        Label cat = new Label(note.getCategory() != null ? note.getCategory().toUpperCase() : "");
        cat.getStyleClass().add("mcard-tags");
        Label phase = new Label(note.getType() != null ? note.getType().name() : "");
        phase.getStyleClass().addAll("mcard-phase-tag",
            note.getType() == NoteType.TOP ? "mcard-tag-top"
            : note.getType() == NoteType.HEART ? "mcard-tag-heart"
            : "mcard-tag-base");
        tags.getChildren().addAll(cat, phase);

        // Row 3: description
        String descText = note.getDescription();
        if (descText != null && descText.length() > 55) descText = descText.substring(0, 52) + "...";
        Label desc = new Label(descText != null ? descText : "");
        desc.getStyleClass().add("mcard-desc");

        // Row 4: origin + intensity dots
        HBox meta = new HBox(8);
        meta.setAlignment(Pos.CENTER_LEFT);
        Label origin = new Label(note.getOrigin() != null ? note.getOrigin() : "");
        origin.getStyleClass().add("mcard-origin");
        HBox dots = new HBox(3);
        dots.setAlignment(Pos.CENTER_LEFT);
        int intensity = note.getIntensity() != null ? note.getIntensity() : 0;
        for (int i = 0; i < 5; i++) {
          Label d = new Label();
          d.getStyleClass().add("mcard-intense-dot");
          d.setStyle(String.format("-fx-background-color: %s;", i < intensity / 2 ? "#D4A89A" : "#EAE6DF"));
          dots.getChildren().add(d);
        }
        meta.getChildren().addAll(origin, dots);

        info.getChildren().addAll(name, tags, desc, meta);
        root.getChildren().addAll(dot, info);
        setGraphic(root);
        setStyle("-fx-background-color: transparent; -fx-padding: 3 4;");
      }
    }
  }
}
