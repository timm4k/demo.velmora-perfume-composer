package velmora.composer.ui.controller;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import velmora.composer.model.Composition;
import velmora.composer.model.CompositionItem;
import velmora.composer.model.Note;
import velmora.composer.model.NoteType;
import velmora.composer.model.User;
import velmora.composer.repository.NoteRepository;
import velmora.composer.service.CompositionService;
import velmora.composer.service.SynergyService;
import velmora.composer.service.analysis.AnalysisResult;
import velmora.composer.service.analysis.CompositionAnalysisService;
import velmora.composer.service.export.PdfExportService;
import velmora.composer.state.CompositionState;
import velmora.composer.ui.UserSession;
import velmora.composer.ui.ViewManager;

@Component
@RequiredArgsConstructor
public class ComposerController {

  private final NoteRepository noteRepository;
  private final CompositionService compositionService;
  private final SynergyService synergyService;
  private final CompositionAnalysisService analysisService;
  private final PdfExportService pdfExportService;
  private final UserSession userSession;
  private final ViewManager viewManager;
  private final CompositionState compositionState;

  @FXML private ListView<Note> materialsList;
  @FXML private Label materialCount;
  @FXML private Label selectedCount;
  @FXML private Button filterAll;
  @FXML private Button filterTop;
  @FXML private Button filterHeart;
  @FXML private Button filterBase;

  @FXML private TextField formulaName;
  @FXML private Label formulaStatus;

  @FXML private TilePane topChips;
  @FXML private TilePane heartChips;
  @FXML private TilePane baseChips;
  @FXML private Label topCount;
  @FXML private Label heartCount;
  @FXML private Label baseCount;
  @FXML private Label topPct;
  @FXML private Label heartPct;
  @FXML private Label basePct;
  @FXML private VBox topZone;
  @FXML private VBox heartZone;
  @FXML private VBox baseZone;

  @FXML private Pane particleLayer;
  @FXML private Pane floatingGradients;
  @FXML private Circle cursorGlow;
  @FXML private VBox insightsContainer;
  @FXML private VBox profileContainer;

  @FXML private Canvas harmonyCircle;
  @FXML private Rectangle longevityBar;
  @FXML private Rectangle complexityBar;
  @FXML private Rectangle projectionBar;
  @FXML private Label harmonyScore;
  @FXML private Label longevityScore;
  @FXML private Label complexityScore;
  @FXML private Label projectionScore;
  @FXML private Label harmonyStatus;
  @FXML private Label longevityStatus;
  @FXML private Label projectionStatus;
  @FXML private Label complexityStatus;

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
  @FXML private Label topDuration;
  @FXML private Label heartDuration;
  @FXML private Label baseDuration;
  @FXML private Label overallScore;

  @FXML private Label totalConcentration;
  @FXML private Label concentrationValidation;
  @FXML private VBox noteConcentrationContainer;

  private final ObservableList<Note> allNotes = FXCollections.observableArrayList();
  private final ObservableList<Note> currentTopNotes = FXCollections.observableArrayList();
  private final ObservableList<Note> currentHeartNotes = FXCollections.observableArrayList();
  private final ObservableList<Note> currentBaseNotes = FXCollections.observableArrayList();
  private final Map<Long, Integer> notePercentages = new HashMap<>();
  private final Set<Long> previousNoteIds = new HashSet<>();
  private FilteredList<Note> filteredNotes;
  @FXML private Button closeDraftBtn;
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
    clearPyramid();
    restoreFromState();
    setupFilterButtons();
    setupMaterialClick();
    setupDragDrop();
    initParticles();
    initFloatingGradients();
    initGlowCursor();
    updateAll();
    Platform.runLater(this::updateAnalysis);
  }

  private void loadNotes() {
    allNotes.setAll(noteRepository.findAll());
    filteredNotes = new FilteredList<>(allNotes, p -> true);
    materialsList.setItems(filteredNotes);
    materialsList.setCellFactory(lv -> new MaterialCardCell());
    updateMaterialCount();
  }

  private void clearPyramid() {
    currentTopNotes.clear();
    currentHeartNotes.clear();
    currentBaseNotes.clear();
    notePercentages.clear();
    previousNoteIds.clear();
  }

  private void restoreFromState() {
    List<Long> ids = compositionState.getCurrentNoteIds();
    if (ids != null && !ids.isEmpty()) {
      for (Long id : ids) {
        noteRepository.findById(id).ifPresent(note -> {
          NoteType type = note.getType();
          if (type == null) return;
          List<Note> targetList = switch (type) {
            case TOP -> currentTopNotes;
            case HEART -> currentHeartNotes;
            case BASE -> currentBaseNotes;
          };
          if (!targetList.contains(note)) targetList.add(note);
        });
      }
      redistributePercentages();
    }
    if (compositionState.getFormulaName() != null) {
      formulaName.setText(compositionState.getFormulaName());
    }
    editingCompositionId = compositionState.getEditCompositionId();
    boolean isEditing = editingCompositionId != null;
    closeDraftBtn.setVisible(isEditing);
    closeDraftBtn.setManaged(isEditing);
    compositionState.clear();
  }

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

  private void addNoteToPyramid(Note note) {
    NoteType type = note.getType();
    if (type == null) return;
    List<Note> targetList = switch (type) {
      case TOP -> currentTopNotes;
      case HEART -> currentHeartNotes;
      case BASE -> currentBaseNotes;
    };
    if (!targetList.contains(note)) {
      targetList.add(note);
      redistributePercentages();
    }
    updateAll();
  }

  private void removeNoteFromPyramid(Note note) {
    NoteType type = note.getType();
    if (type == null) return;
    List<Note> targetList = switch (type) {
      case TOP -> currentTopNotes;
      case HEART -> currentHeartNotes;
      case BASE -> currentBaseNotes;
    };
    targetList.remove(note);
    notePercentages.remove(note.getId());
    redistributePercentages();
    updateAll();
  }

  private void redistributePercentages() {
    List<Note> all = allNotes();
    notePercentages.clear();
    if (all.isEmpty()) return;
    int total = all.size();
    int base = 100 / total;
    int remainder = 100 - base * total;
    for (int i = 0; i < all.size(); i++) {
      notePercentages.put(all.get(i).getId(), base + (i < remainder ? 1 : 0));
    }
  }

  @FXML
  private void handleAutoBalance() {
    redistributePercentages();
    updateAll();
  }

  private void adjustPercentage(Note note, int delta) {
    int current = notePercentages.getOrDefault(note.getId(), 0);
    int newVal = Math.max(0, Math.min(100, current + delta));
    notePercentages.put(note.getId(), newVal);
    updateAll();
  }

  private int getTotalPercentage() {
    return notePercentages.values().stream().mapToInt(Integer::intValue).sum();
  }

  private void updateAll() {
    renderChips();
    adjustZoneWidths();
    updateCounts();
    updateConcentrationDisplay();
    updatePhasePercentages();
    renderNoteConcentration();
    updateAnalysis();
  }

  private void renderChips() {
    renderChipPane(topChips, currentTopNotes);
    renderChipPane(heartChips, currentHeartNotes);
    renderChipPane(baseChips, currentBaseNotes);
  }

  private void adjustZoneWidths() {
    adjustZoneWidth(topZone, topChips);
    adjustZoneWidth(heartZone, heartChips);
    adjustZoneWidth(baseZone, baseChips);
  }

  private void adjustZoneWidth(VBox zone, TilePane chips) {
    int count = chips.getChildren().size();
    int cols = count == 0 ? 4 : Math.min(4 + (count - 1) / 4, 8);
    chips.setPrefColumns(cols);

    Region parent = (Region) zone.getParent();
    double parentWidth = parent.getWidth() > 0 ? parent.getWidth() : 700;
    double maxAllowed = parentWidth * 0.7;

    double zoneWidth;
    if (count == 0) {
      zoneWidth = Math.min(310, maxAllowed);
    } else {
      double chipWidth = 150;
      double hgap = 6;
      double zoneHpad = 40;
      zoneWidth = cols * chipWidth + (cols - 1) * hgap + zoneHpad;
      zoneWidth = Math.min(zoneWidth, maxAllowed);
      zoneWidth = Math.max(zoneWidth, 260);
    }
    zone.setPrefWidth(zoneWidth);
    zone.setMaxWidth(zoneWidth);
  }

  private void renderChipPane(TilePane pane, ObservableList<Note> notes) {
    Set<Long> currentIds = notes.stream().map(Note::getId).collect(Collectors.toSet());
    pane.getChildren().clear();
    for (Note note : notes) {
      boolean isNew = !previousNoteIds.contains(note.getId());
      pane.getChildren().add(createChip(note, isNew));
    }
    previousNoteIds.clear();
    previousNoteIds.addAll(currentIds);
    if (pane.getParent() instanceof VBox zone) {
      for (Node child : zone.getChildren()) {
        if (child instanceof Label label && label.getStyleClass().contains("zone-placeholder")) {
          label.setVisible(notes.isEmpty());
          label.setManaged(notes.isEmpty());
          break;
        }
      }
    }
  }

  private HBox createChip(Note note, boolean animate) {
    HBox chip = new HBox(4);
    chip.setAlignment(Pos.CENTER_LEFT);
    chip.getStyleClass().add("note-chip-glass");
    chip.getStyleClass().add(
        note.getType() == NoteType.TOP ? "chip-top"
        : note.getType() == NoteType.HEART ? "chip-heart"
        : "chip-base");

    if (animate) {
      chip.setScaleX(0.85);
      chip.setScaleY(0.85);
      chip.setOpacity(0);
      FadeTransition ft = new FadeTransition(Duration.millis(250), chip);
      ft.setToValue(1.0);
      ScaleTransition st = new ScaleTransition(Duration.millis(300), chip);
      st.setToX(1.0);
      st.setToY(1.0);
      st.setInterpolator(Interpolator.EASE_OUT);
      ft.play();
      st.play();
    }

    Label name = new Label(note.getName());
    name.setStyle("-fx-font-size: 16; -fx-font-weight: 500; -fx-text-fill: #4B4B4B;");

    Label close = new Label("✕");
    close.getStyleClass().add("chip-close");
    close.setOnMouseClicked(e -> removeNoteFromPyramid(note));

    chip.getChildren().addAll(name, close);
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
    selectedCount.setText(String.valueOf(total));

    double topPctVal = total > 0 ? (double) top / total * 100 : 0;
    double heartPctVal = total > 0 ? (double) heart / total * 100 : 0;
    double basePctVal = total > 0 ? (double) base / total * 100 : 0;
    topPct.setText(String.format("%.0f%%", topPctVal));
    heartPct.setText(String.format("%.0f%%", heartPctVal));
    basePct.setText(String.format("%.0f%%", basePctVal));
  }

  private void updatePhasePercentages() {
    int totalInNotes = allNotes().size();
    if (totalInNotes == 0) return;
    double topSum = currentTopNotes.stream().mapToInt(n -> notePercentages.getOrDefault(n.getId(), 0)).sum();
    double heartSum = currentHeartNotes.stream().mapToInt(n -> notePercentages.getOrDefault(n.getId(), 0)).sum();
    double baseSum = currentBaseNotes.stream().mapToInt(n -> notePercentages.getOrDefault(n.getId(), 0)).sum();
    double grand = topSum + heartSum + baseSum;
    if (grand == 0) return;

    topPct.setText(String.format("%.0f%%", topSum));
    heartPct.setText(String.format("%.0f%%", heartSum));
    basePct.setText(String.format("%.0f%%", baseSum));
  }

  private void updateConcentrationDisplay() {
    int total = getTotalPercentage();
    totalConcentration.setText("Total: " + total + "%");
    if (total == 100) {
      concentrationValidation.setText("✓ Formula balanced");
      concentrationValidation.setStyle("-fx-text-fill: #5A9E8F; -fx-font-size: 14; -fx-font-weight: 600;");
    } else if (total < 100) {
      int remaining = 100 - total;
      concentrationValidation.setText("⚠ Incomplete. " + remaining + "% remaining.");
      concentrationValidation.setStyle("-fx-text-fill: #C8954A; -fx-font-size: 14; -fx-font-weight: 600;");
    } else {
      int excess = total - 100;
      concentrationValidation.setText("✗ Exceeds by " + excess + "%.");
      concentrationValidation.setStyle("-fx-text-fill: #D9534F; -fx-font-size: 14; -fx-font-weight: 600;");
    }
  }

  private void updateAnalysis() {
    AnalysisResult result = analysisService.analyze(currentTopNotes, currentHeartNotes, currentBaseNotes);

    if (result.isEmpty()) {
      harmonyScore.setText("—");
      longevityScore.setText("—");
      complexityScore.setText("—");
      projectionScore.setText("—");
      if (harmonyStatus != null) harmonyStatus.setText("");
      if (longevityStatus != null) longevityStatus.setText("");
      if (complexityStatus != null) complexityStatus.setText("");
      if (projectionStatus != null) projectionStatus.setText("");
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
      drawHarmonyCircle(0);
      setPerfBar(longevityBar, 0);
      setPerfBar(complexityBar, 0);
      setPerfBar(projectionBar, 0);
      insightsContainer.getChildren().clear();
      profileContainer.getChildren().clear();
      return;
    }

    noteCount.setText(String.valueOf(result.total()));
    moleculeCount.setText(String.valueOf(result.total()));
    totalIntensity.setText(String.format("%.1f", result.avgIntensity()));

    topDuration.setText(result.topHoursDisplay());
    heartDuration.setText(result.heartHoursDisplay());
    baseDuration.setText(result.baseHoursDisplay());

    estLongevity.setText(result.estLongevity());
    sillage.setText(result.sillage());

    harmonyScore.setText(String.valueOf(result.harmony()));
    drawHarmonyCircle(result.harmony());
    if (harmonyStatus != null) {
      harmonyStatus.setText(result.harmony() >= 80 ? "Excellent" : result.harmony() >= 60 ? "Good" : result.harmony() >= 40 ? "Average" : "Poor");
    }

    longevityScore.setText(String.valueOf(result.longevityScore()));
    setPerfBar(longevityBar, result.longevityScore());
    if (longevityStatus != null) {
      double tl = result.totalLongevity();
      longevityStatus.setText(tl >= 8 ? "Long-lasting" : tl >= 4 ? "Moderate" : "Short");
    }

    complexityScore.setText(String.valueOf(result.complexityScore()));
    setPerfBar(complexityBar, result.complexityScore());
    if (complexityStatus != null) {
      complexityStatus.setText(result.complexityScore() >= 80 ? "Highly Complex" : result.complexityScore() >= 60 ? "Complex" : result.complexityScore() >= 40 ? "Balanced" : "Simple");
    }

    projectionScore.setText(String.valueOf(result.projectionScore()));
    setPerfBar(projectionBar, result.projectionScore());
    if (projectionStatus != null) {
      projectionStatus.setText(result.sillage());
    }

    if (overallScore != null) {
      overallScore.setText(String.valueOf(result.overallScore()));
    }

    updateFamilyDistribution(result);
    renderInsights(result);
    renderLiveProfile(result);
  }

  private void renderNoteConcentration() {
    noteConcentrationContainer.getChildren().clear();
    List<Note> all = allNotes();
    if (all.isEmpty()) return;

    VBox card = new VBox(6);
    card.getStyleClass().add("analysis-glass");

    HBox header = new HBox(6);
    header.setAlignment(Pos.CENTER_LEFT);
    Circle dot = new Circle(3);
    dot.setStyle("-fx-fill: rgba(177,141,184,0.7);");
    Label title = new Label("NOTE CONCENTRATION");
    title.setStyle("-fx-font-size: 13; -fx-font-weight: 600; -fx-letter-spacing: 2; -fx-text-fill: #A8A29E;");
    header.getChildren().addAll(dot, title);
    card.getChildren().add(header);

    for (Note note : all) {
      HBox row = new HBox(6);
      row.setAlignment(Pos.CENTER_LEFT);

      Circle typeDot = new Circle(4);
      String phaseColor = note.getType() == NoteType.TOP ? "#D69478"
          : note.getType() == NoteType.HEART ? "#B18DB8" : "#6DA89E";
      typeDot.setStyle("-fx-fill: " + phaseColor + ";");

      Label name = new Label(note.getName());
      name.setStyle("-fx-font-size: 15; -fx-font-weight: 500; -fx-text-fill: #4B4B4B; -fx-min-width: 70; -fx-max-width: 120; -fx-wrap-text: true;");

      Region spacer = new Region();
      HBox.setHgrow(spacer, Priority.ALWAYS);

      Button minusBtn = new Button("−");
      minusBtn.getStyleClass().add("chip-pct-btn");
      minusBtn.setOnAction(e -> adjustPercentage(note, -1));

      int pct = notePercentages.getOrDefault(note.getId(), 0);
      Label pctLbl = new Label(pct + "%");
      pctLbl.getStyleClass().add("chip-pct");

      Button plusBtn = new Button("+");
      plusBtn.getStyleClass().add("chip-pct-btn");
      plusBtn.setOnAction(e -> adjustPercentage(note, 1));

      row.getChildren().addAll(typeDot, name, spacer, minusBtn, pctLbl, plusBtn);
      card.getChildren().add(row);
    }

    noteConcentrationContainer.getChildren().add(card);
  }

  private void renderLiveProfile(AnalysisResult result) {
    profileContainer.getChildren().clear();
    if (result.total() < 2) return;

    String description = analysisService.generateDescription(currentTopNotes, currentHeartNotes, currentBaseNotes);

    VBox card = new VBox(6);
    card.getStyleClass().add("analysis-glass");
    HBox header = new HBox(8);
    header.setAlignment(Pos.CENTER_LEFT);
    Circle dot = new Circle(3);
    dot.setStyle("-fx-fill: rgba(177,141,184,0.7);");
    Label title = new Label("LIVE PERFUME PROFILE");
    title.setStyle("-fx-font-size: 14; -fx-font-weight: 600; -fx-letter-spacing: 2; -fx-text-fill: #A8A29E;");

    Label desc = new Label(description);
    desc.setWrapText(true);
    desc.setStyle("-fx-font-family: 'Cormorant Garamond','Garamond','Georgia',serif; -fx-font-size: 16; -fx-text-fill: #5F5B57; -fx-line-spacing: 2; -fx-font-style: italic;");

    header.getChildren().addAll(dot, title);
    card.getChildren().addAll(header, desc);
    profileContainer.getChildren().add(card);
  }

  private void drawHarmonyCircle(int value) {
    if (harmonyCircle == null) return;
    renderHarmonyArc(value);
  }

  private void renderHarmonyArc(int value) {
    double w = harmonyCircle.getWidth();
    double h = harmonyCircle.getHeight();
    double cx = w / 2;
    double cy = h / 2;
    double r = Math.min(cx, cy) - 4;
    double angle = 360.0 * Math.min(100, Math.max(0, value)) / 100.0;

    GraphicsContext gc = harmonyCircle.getGraphicsContext2D();
    gc.clearRect(0, 0, w, h);

    gc.setLineWidth(3);
    gc.setStroke(Color.rgb(234, 230, 223));
    gc.strokeArc(cx - r, cy - r, r * 2, r * 2, 0, 360, ArcType.OPEN);

    Color fillColor = value >= 70 ? Color.rgb(90, 158, 143)
        : value >= 40 ? Color.rgb(200, 149, 74)
        : Color.rgb(217, 83, 79);
    gc.setStroke(fillColor);
    gc.setLineCap(StrokeLineCap.ROUND);
    gc.strokeArc(cx - r, cy - r, r * 2, r * 2, 90, -angle, ArcType.OPEN);

    gc.setFill(fillColor);
    gc.setFont(Font.font("Cormorant Garamond", FontWeight.BOLD, 14));
    gc.setTextAlign(TextAlignment.CENTER);
    gc.fillText(value + "%", cx, cy + 5);
  }

  private void setPerfBar(Rectangle bar, int value) {
    double target = PERF_BAR_MAX * value / 100.0;
    if (bar.getWidth() == target) return;
    Timeline anim = new Timeline(
        new KeyFrame(Duration.millis(400),
            new KeyValue(bar.widthProperty(), target, Interpolator.EASE_OUT))
    );
    anim.play();
  }

  private void renderInsights(AnalysisResult result) {
    insightsContainer.getChildren().clear();
    if (result.total() < 2) {
      Label hint = new Label("Add at least 2 notes to see analysis");
      hint.setStyle("-fx-font-size: 17; -fx-font-style: italic; -fx-text-fill: #B0ADA8;");
      insightsContainer.getChildren().add(hint);
      return;
    }

    var synergy = synergyService.calculate(result.allNotes());
    int synScore = synergy.score();

    VBox synCard = new VBox(4);
    synCard.getStyleClass().add("insight-glass-card");
    Label synTag = new Label("SYNERGY");
    synTag.getStyleClass().add("insight-tag-label");
    Label synVal = new Label(synScore + "/100");
    synVal.getStyleClass().add("insight-synergy");
    synVal.setStyle("-fx-font-family: \"Cormorant Garamond\",serif;"
        + (synScore >= 60 ? " -fx-text-fill: #5A9E8F;"
        : synScore >= 40 ? " -fx-text-fill: #C8954A;" : " -fx-text-fill: #D9534F;"));
    synCard.getChildren().addAll(synTag, synVal);
    insightsContainer.getChildren().add(synCard);

    VBox profileCard = new VBox(4);
    profileCard.getStyleClass().add("insight-glass-card");
    Label profTag = new Label("DOMINANT PROFILE");
    profTag.getStyleClass().add("insight-tag-label");
    Label profName = new Label(result.dominantProfile());
    profName.getStyleClass().add("insight-title");
    profileCard.getChildren().addAll(profTag, profName);
    insightsContainer.getChildren().add(profileCard);

    if (!currentTopNotes.isEmpty()) {
      VBox openCard = new VBox(4);
      openCard.getStyleClass().add("insight-glass-card");
      Label openTag = new Label("OPENING");
      openTag.getStyleClass().add("insight-tag-label");
      Label openText = new Label(result.topInsight());
      openText.getStyleClass().add("insight-body");
      openText.setWrapText(true);
      openCard.getChildren().addAll(openTag, openText);
      insightsContainer.getChildren().add(openCard);
    }

    if (!currentHeartNotes.isEmpty()) {
      VBox heartCard = new VBox(4);
      heartCard.getStyleClass().add("insight-glass-card");
      Label heartTag = new Label("HEART TRANSITION");
      heartTag.getStyleClass().add("insight-tag-label");
      Label heartText = new Label(result.heartInsight());
      heartText.getStyleClass().add("insight-body");
      heartText.setWrapText(true);
      heartCard.getChildren().addAll(heartTag, heartText);
      insightsContainer.getChildren().add(heartCard);
    }

    if (!currentBaseNotes.isEmpty()) {
      VBox baseCard = new VBox(4);
      baseCard.getStyleClass().add("insight-glass-card");
      Label baseTag = new Label("BASE BEHAVIOR");
      baseTag.getStyleClass().add("insight-tag-label");
      Label baseText = new Label(result.baseInsight());
      baseText.getStyleClass().add("insight-body");
      baseText.setWrapText(true);
      baseCard.getChildren().addAll(baseTag, baseText);
      insightsContainer.getChildren().add(baseCard);
    }

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

  private List<Note> allNotes() {
    List<Note> all = new ArrayList<>();
    all.addAll(currentTopNotes);
    all.addAll(currentHeartNotes);
    all.addAll(currentBaseNotes);
    return all;
  }

  private void updateFamilyDistribution(AnalysisResult result) {
    familyCount.setText(String.valueOf(result.families()));
    if (result.total() == 0) { resetBars(); return; }
    setBar(citrusBar, citrusPct, result.citrusCount(), result.total());
    setBar(floralBar, floralPct, result.floralCount(), result.total());
    setBar(woodyBar, woodyPct, result.woodyCount(), result.total());
    setBar(earthyBar, earthyPct, result.earthyCount(), result.total());
  }

  private void setBar(Rectangle bar, Label label, long count, long total) {
    double pct = (double) count / total * 100;
    label.setText(String.format("%.0f%%", pct));
    double target = PERF_BAR_MAX * pct / 100.0;
    if (bar.getWidth() == target) return;
    Timeline anim = new Timeline(
        new KeyFrame(Duration.millis(400),
            new KeyValue(bar.widthProperty(), target, Interpolator.EASE_OUT))
    );
    anim.play();
  }

  private void resetBars() {
    setPerfBar(citrusBar, 0); setPerfBar(floralBar, 0);
    setPerfBar(woodyBar, 0); setPerfBar(earthyBar, 0);
    citrusPct.setText("0%"); floralPct.setText("0%");
    woodyPct.setText("0%"); earthyPct.setText("0%");
  }

  private void updateMaterialCount() {
    materialCount.setText(filteredNotes.size() + " materials");
  }

  private void initParticles() {
    Random rand = new Random();
    List<Circle> particles = new ArrayList<>();
    String[] symbols = {"\u2726", "\u2022", "\u25E6", "\u2219"};
    for (int i = 0; i < 35; i++) {
      Circle c = new Circle(1 + rand.nextDouble() * 3);
      double t = rand.nextDouble();
      if (t < 0.33)      c.setFill(Color.rgb(214, 148, 120, 0.06 + rand.nextDouble() * 0.10));
      else if (t < 0.66) c.setFill(Color.rgb(177, 141, 184, 0.06 + rand.nextDouble() * 0.10));
      else                c.setFill(Color.rgb(109, 168, 158, 0.06 + rand.nextDouble() * 0.10));
      c.setCenterX(rand.nextDouble() * 600 + 20);
      c.setCenterY(rand.nextDouble() * 400 + 40);
      c.setOpacity(0);
      particleLayer.getChildren().add(c);
      particles.add(c);
    }

    Timeline tl = new Timeline();
    for (int i = 0; i < particles.size(); i++) {
      Circle p = particles.get(i);
      double delay = rand.nextDouble() * 15;
      double dur = 12 + rand.nextDouble() * 16;
      double dx = (rand.nextDouble() - 0.5) * 80;
      double dy = (rand.nextDouble() - 0.5) * 50;
      tl.getKeyFrames().addAll(
          new KeyFrame(Duration.seconds(delay), new KeyValue(p.opacityProperty(), 0)),
          new KeyFrame(Duration.seconds(delay + 3), new KeyValue(p.opacityProperty(), 0.15 + rand.nextDouble() * 0.12)),
          new KeyFrame(Duration.seconds(delay + 3 + dur),
              new KeyValue(p.translateXProperty(), dx, Interpolator.EASE_BOTH),
              new KeyValue(p.translateYProperty(), dy, Interpolator.EASE_BOTH),
              new KeyValue(p.opacityProperty(), 0.15 + rand.nextDouble() * 0.12)),
          new KeyFrame(Duration.seconds(delay + 3 + dur + 3),
              new KeyValue(p.opacityProperty(), 0, Interpolator.EASE_BOTH)));
    }
    tl.setCycleCount(Timeline.INDEFINITE);
    tl.play();
  }

  private void initFloatingGradients() {
    Random rand = new Random();

    double[][] blobSpecs = {
        {260, 0.14, 214, 148, 120},
        {300, 0.14, 177, 141, 184},
        {240, 0.14, 109, 168, 158}
    };
    String[] classes = {"floating-blob-peach", "floating-blob-lavender", "floating-blob-teal"};

    Circle[] blobs = new Circle[3];

    for (int i = 0; i < 3; i++) {
      Circle blob = new Circle(blobSpecs[i][0]);
      blob.getStyleClass().addAll("floating-blob", classes[i]);
      blob.setCenterX(200 + rand.nextDouble() * 300);
      blob.setCenterY(150 + rand.nextDouble() * 200);
      blob.setOpacity(0);
      floatingGradients.getChildren().add(blob);
      blobs[i] = blob;
    }

    Timeline tl = new Timeline();
    for (int i = 0; i < 3; i++) {
      Circle blob = blobs[i];
      double delay = rand.nextDouble() * 5;
      double startX = blob.getCenterX();
      double startY = blob.getCenterY();
      double dx1 = (rand.nextDouble() - 0.5) * 120;
      double dy1 = (rand.nextDouble() - 0.5) * 80;
      double dx2 = (rand.nextDouble() - 0.5) * 120;
      double dy2 = (rand.nextDouble() - 0.5) * 80;
      double dx3 = (rand.nextDouble() - 0.5) * 120;
      double dy3 = (rand.nextDouble() - 0.5) * 80;

      tl.getKeyFrames().addAll(
          new KeyFrame(Duration.seconds(delay), new KeyValue(blob.opacityProperty(), 0)),
          new KeyFrame(Duration.seconds(delay + 3), new KeyValue(blob.opacityProperty(), 0.8 + rand.nextDouble() * 0.2)),
          new KeyFrame(Duration.seconds(delay + 10),
              new KeyValue(blob.centerXProperty(), startX + dx1, Interpolator.EASE_BOTH),
              new KeyValue(blob.centerYProperty(), startY + dy1, Interpolator.EASE_BOTH)),
          new KeyFrame(Duration.seconds(delay + 20),
              new KeyValue(blob.centerXProperty(), startX + dx1 + dx2, Interpolator.EASE_BOTH),
              new KeyValue(blob.centerYProperty(), startY + dy1 + dy2, Interpolator.EASE_BOTH)),
          new KeyFrame(Duration.seconds(delay + 30),
              new KeyValue(blob.centerXProperty(), startX + dx1 + dx2 + dx3, Interpolator.EASE_BOTH),
              new KeyValue(blob.centerYProperty(), startY + dy1 + dy2 + dy3, Interpolator.EASE_BOTH)),
          new KeyFrame(Duration.seconds(delay + 35),
              new KeyValue(blob.opacityProperty(), 0.6, Interpolator.EASE_BOTH)),
          new KeyFrame(Duration.seconds(delay + 40),
              new KeyValue(blob.centerXProperty(), startX, Interpolator.EASE_BOTH),
              new KeyValue(blob.centerYProperty(), startY, Interpolator.EASE_BOTH)),
          new KeyFrame(Duration.seconds(delay + 43),
              new KeyValue(blob.opacityProperty(), 0, Interpolator.EASE_BOTH)));
    }
    tl.setCycleCount(Timeline.INDEFINITE);
    tl.play();
  }

  private double mouseX = 0;
  private double mouseY = 0;

  private void initGlowCursor() {
    Timeline glowTimeline = new Timeline(
        new KeyFrame(Duration.millis(16), e -> {
          if (cursorGlow != null && cursorGlow.isVisible()) {
            double dx = mouseX - cursorGlow.getCenterX();
            double dy = mouseY - cursorGlow.getCenterY();
            cursorGlow.setCenterX(cursorGlow.getCenterX() + dx * 0.08);
            cursorGlow.setCenterY(cursorGlow.getCenterY() + dy * 0.08);
          }
        })
    );
    glowTimeline.setCycleCount(Timeline.INDEFINITE);
    glowTimeline.play();

    if (cursorGlow != null) {
      cursorGlow.sceneProperty().addListener((obs, old, scene) -> {
        if (scene != null) {
          scene.setOnMouseMoved(e -> {
            mouseX = e.getSceneX();
            mouseY = e.getSceneY();
          });
          scene.setOnMouseEntered(e -> {
            mouseX = e.getSceneX();
            mouseY = e.getSceneY();
            cursorGlow.setVisible(true);
          });
          scene.setOnMouseExited(e -> cursorGlow.setVisible(false));
          cursorGlow.setCenterX(scene.getWidth() / 2);
          cursorGlow.setCenterY(scene.getHeight() / 2);
        }
      });
    }
  }

  @FXML
  public void handleCloseDraft() {
    editingCompositionId = null;
    formulaName.setText("");
    formulaStatus.setText("");
    clearPyramid();
    updateAll();
    closeDraftBtn.setVisible(false);
    closeDraftBtn.setManaged(false);
  }

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
    for (Note note : all) {
      CompositionItem item = new CompositionItem();
      item.setNoteId(note.getId());
      item.setPercentage(notePercentages.getOrDefault(note.getId(), 100 / all.size()));
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
    AnalysisResult exportResult = analysisService.analyze(currentTopNotes, currentHeartNotes, currentBaseNotes);
    javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<>() {
      @Override protected Void call() throws Exception {
        pdfExportService.export(file, name, all, exportResult);
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

        Label name = new Label(note.getName() != null ? note.getName() : "");
        name.getStyleClass().add("mcard-name");

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

        String descText = note.getDescription();
        if (descText != null && descText.length() > 55) descText = descText.substring(0, 52) + "...";
        Label desc = new Label(descText != null ? descText : "");
        desc.getStyleClass().add("mcard-desc");

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
        setStyle("-fx-background-color: transparent; -fx-padding: 3 20 3 4;");
      }
    }
  }
}
