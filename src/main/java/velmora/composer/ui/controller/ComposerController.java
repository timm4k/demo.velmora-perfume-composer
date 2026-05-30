package velmora.composer.ui.controller;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
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
import javafx.scene.chart.PieChart;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Circle;
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
import velmora.composer.service.AutoSaveService;
import velmora.composer.service.CompositionService;
import velmora.composer.service.SynergyService;
import velmora.composer.service.analysis.AnalysisResult;
import velmora.composer.service.analysis.CompositionAnalysisService;
import velmora.composer.service.analysis.FragranceTimelineService;
import velmora.composer.service.export.PdfExportService;
import velmora.composer.state.CompositionState;
import velmora.composer.ui.UserSession;
import velmora.composer.ui.ViewManager;
import velmora.composer.ui.component.MaterialCardCell;
import velmora.composer.ui.renderer.AnalysisPanelRenderer;
import velmora.composer.ui.renderer.ConcentrationPanelRenderer;
import velmora.composer.ui.renderer.TimelinePanelRenderer;
import velmora.composer.ui.util.ComposerAnimationHelper;

@Component
@RequiredArgsConstructor
public class ComposerController {

  private final NoteRepository noteRepository;
  private final CompositionService compositionService;
  private final SynergyService synergyService;
  private final CompositionAnalysisService analysisService;
  private final FragranceTimelineService timelineService;
  private final PdfExportService pdfExportService;
  private final AutoSaveService autoSaveService;
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
  @FXML private VBox insightsContainer;

  @FXML private Canvas harmonyCircle;
  @FXML private Rectangle balanceBar;
  @FXML private Rectangle longevityBar;
  @FXML private Rectangle projectionBar;
  @FXML private Rectangle complexityBar;
  @FXML private Label balanceScore;
  @FXML private Label longevityValue;
  @FXML private Label projectionLabel;
  @FXML private Label complexityLabel;

  @FXML private Label formulaTotalLabel;
  @FXML private Label validationLabel;
  @FXML private VBox concentrationRows;

  @FXML private Label dominantFamilyValue;
  @FXML private Label openingCharValue;
  @FXML private Label dryDownCharValue;

  @FXML private PieChart familyChart;

  @FXML private StackPane pyramidTopContainer;
  @FXML private StackPane pyramidHeartContainer;
  @FXML private StackPane pyramidBaseContainer;
  @FXML private Rectangle pyramidTop;
  @FXML private Rectangle pyramidHeart;
  @FXML private Rectangle pyramidBase;
  @FXML private Label pyramidTopPct;
  @FXML private Label pyramidHeartPct;
  @FXML private Label pyramidBasePct;

  @FXML private Label totalConcentration;
  @FXML private Label concentrationValidation;
  @FXML private Circle statusDot;
  @FXML private HBox statusIndicator;

  @FXML private Slider timelineSlider;
  @FXML private Label currentStageValue;
  @FXML private Label currentCharValue;
  @FXML private Label currentNotesValue;
  @FXML private Label currentIntensityValue;
  @FXML private VBox noteActivityContainer;
  @FXML private VBox heatMapContainer;
  @FXML private Label fragranceStory;

  private final ObservableList<Note> allNotes = FXCollections.observableArrayList();
  private final ObservableList<Note> currentTopNotes = FXCollections.observableArrayList();
  private final ObservableList<Note> currentHeartNotes = FXCollections.observableArrayList();
  private final ObservableList<Note> currentBaseNotes = FXCollections.observableArrayList();
  private final Map<Long, Integer> notePercentages = new HashMap<>();
  private final Set<Long> previousNoteIds = new HashSet<>();
  private FilteredList<Note> filteredNotes;
  @FXML private Button closeDraftBtn;
  private Long editingCompositionId;

  private ConcentrationPanelRenderer concentrationPanelRenderer;
  private AnalysisPanelRenderer analysisPanelRenderer;
  private TimelinePanelRenderer timelinePanelRenderer;

  @FXML
  public void initialize() {
    concentrationPanelRenderer = new ConcentrationPanelRenderer(
        concentrationRows, formulaTotalLabel, validationLabel,
        totalConcentration, concentrationValidation,
        topPct, heartPct, basePct,
        autoSaveService
    );
    analysisPanelRenderer = new AnalysisPanelRenderer(
        harmonyCircle, balanceBar, longevityBar, projectionBar, complexityBar,
        balanceScore, longevityValue, projectionLabel, complexityLabel,
        dominantFamilyValue, openingCharValue, dryDownCharValue,
        familyChart, pyramidTop, pyramidHeart, pyramidBase,
        pyramidTopPct, pyramidHeartPct, pyramidBasePct, insightsContainer,
        analysisService, synergyService
    );
    timelinePanelRenderer = new TimelinePanelRenderer(
        timelineSlider, currentStageValue, currentCharValue, currentNotesValue, currentIntensityValue,
        noteActivityContainer, heatMapContainer, fragranceStory,
        timelineService
    );

    viewManager.setBeforeCatalogNavigate(() -> {
      compositionState.setCurrentNoteIds(allNotes().stream().map(Note::getId).collect(Collectors.toList()));
      compositionState.setCurrentNoteNames(allNotes().stream().map(Note::getName).collect(Collectors.toList()));
    });
    viewManager.setBeforeVaultNavigate(() -> {
      compositionState.setCurrentNoteIds(allNotes().stream().map(Note::getId).collect(Collectors.toList()));
      compositionState.setCurrentNoteNames(allNotes().stream().map(Note::getName).collect(Collectors.toList()));
    });
    loadNotes();
    clearPyramid();
    restoreFromState();
    setupFilterButtons();
    setupMaterialClick();
    setupDragDrop();
    ComposerAnimationHelper.initParticles(particleLayer);
    ComposerAnimationHelper.initFloatingGradients(floatingGradients);
    timelinePanelRenderer.init(this::runAnalysis);
    updateAll();
    Platform.runLater(this::runAnalysis);

    autoSaveService.setListener(status -> {
      formulaStatus.setText(switch (status) {
        case SAVED -> "Saved";
        case SAVING -> "Saving...";
        case UNSAVED -> "Unsaved Changes";
      });
      statusDot.getStyleClass().removeAll("status-dot-saved", "status-dot-saving", "status-dot-warn");
      statusDot.getStyleClass().add(switch (status) {
        case SAVED -> "status-dot-saved";
        case SAVING -> "status-dot-saving";
        case UNSAVED -> "status-dot-warn";
      });
    });

    formulaName.textProperty().addListener((obs, old, val) -> {
      autoSaveService.setCurrentName(val);
      autoSaveService.markDirty();
    });

    if (editingCompositionId != null) {
      autoSaveService.setCurrentComposition(editingCompositionId, formulaName.getText(), "");
    }
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
      autoSaveService.markDirty();
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
    autoSaveService.markDirty();
    updateAll();
  }

  private void redistributePercentages() {
    List<Note> all = allNotes();
    notePercentages.clear();
    if (all.isEmpty()) return;

    boolean hasTop = !currentTopNotes.isEmpty();
    boolean hasHeart = !currentHeartNotes.isEmpty();
    boolean hasBase = !currentBaseNotes.isEmpty();

    double topTarget, heartTarget, baseTarget;
    if (hasTop && hasHeart && hasBase) {
      topTarget = 25; heartTarget = 40; baseTarget = 35;
    } else if (hasTop && hasHeart) {
      topTarget = 35; heartTarget = 65; baseTarget = 0;
    } else if (hasTop && hasBase) {
      topTarget = 40; baseTarget = 60; heartTarget = 0;
    } else if (hasHeart && hasBase) {
      heartTarget = 55; baseTarget = 45; topTarget = 0;
    } else {
      redistributeEvenly(all);
      return;
    }

    Map<NoteType, List<Note>> byType = new HashMap<>();
    for (NoteType t : new NoteType[]{NoteType.TOP, NoteType.HEART, NoteType.BASE}) {
      byType.put(t, new ArrayList<>());
    }
    for (Note n : all) {
      if (n.getType() != null) byType.get(n.getType()).add(n);
    }

    double[][] targets = {
        {NoteType.TOP.ordinal(), topTarget},
        {NoteType.HEART.ordinal(), heartTarget},
        {NoteType.BASE.ordinal(), baseTarget}
    };

    for (double[] t : targets) {
      NoteType type = NoteType.values()[(int) t[0]];
      double phasePct = t[1];
      List<Note> notes = byType.get(type);
      if (notes.isEmpty()) continue;
      int phaseTotal = (int) Math.round(phasePct);
      int perNote = phaseTotal / notes.size();
      int rem = phaseTotal - perNote * notes.size();
      for (int i = 0; i < notes.size(); i++) {
        notePercentages.put(notes.get(i).getId(), perNote + (i < rem ? 1 : 0));
      }
    }

    int missing = 100 - getTotalPercentage();
    if (missing != 0) {
      Note first = all.get(0);
      notePercentages.merge(first.getId(), missing, Integer::sum);
    }
  }

  private void redistributeEvenly(List<Note> all) {
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

  private int getTotalPercentage() {
    return notePercentages.values().stream().mapToInt(Integer::intValue).sum();
  }

  private void updateAll() {
    renderChips();
    adjustZoneWidths();
    updateCounts();
    List<Note> all = allNotes();
    if (all.isEmpty()) {
      concentrationPanelRenderer.renderEmpty();
      analysisPanelRenderer.renderEmpty();
      timelinePanelRenderer.renderEmpty();
      return;
    }
    concentrationPanelRenderer.renderRows(all, notePercentages, currentTopNotes, currentHeartNotes, currentBaseNotes, this::runAnalysis);
    analysisPanelRenderer.renderAll(all, notePercentages, currentTopNotes, currentHeartNotes, currentBaseNotes);
    timelinePanelRenderer.update(all, currentTopNotes, currentHeartNotes, currentBaseNotes, notePercentages);
  }

  private void runAnalysis() {
    List<Note> all = allNotes();
    if (all.isEmpty()) {
      concentrationPanelRenderer.renderEmpty();
      analysisPanelRenderer.renderEmpty();
      timelinePanelRenderer.renderEmpty();
      return;
    }
    concentrationPanelRenderer.updateLabels(all, notePercentages, currentTopNotes, currentHeartNotes, currentBaseNotes);
    analysisPanelRenderer.renderAll(all, notePercentages, currentTopNotes, currentHeartNotes, currentBaseNotes);
    timelinePanelRenderer.update(all, currentTopNotes, currentHeartNotes, currentBaseNotes, notePercentages);
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
    name.getStyleClass().add("chip-note-name");

    Label close = new Label("\u2715");
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
  }

  private List<Note> allNotes() {
    List<Note> all = new ArrayList<>();
    all.addAll(currentTopNotes);
    all.addAll(currentHeartNotes);
    all.addAll(currentBaseNotes);
    return all;
  }

  private void updateMaterialCount() {
    materialCount.setText(filteredNotes.size() + " materials");
  }

  @FXML
  public void handleCloseDraft() {
    editingCompositionId = null;
    formulaName.setText("");
    formulaStatus.setText("");
    autoSaveService.clear();
    clearPyramid();
    updateAll();
    closeDraftBtn.setVisible(false);
    closeDraftBtn.setManaged(false);
  }

  @FXML
  public void handleSave() {
    if (userSession.getUserId() == null) {
      formulaStatus.setText("LOG IN TO SAVE");
      statusDot.getStyleClass().removeAll("status-dot-saved", "status-dot-saving", "status-dot-warn");
      statusDot.getStyleClass().add("status-dot-warn");
      return;
    }
    String name = formulaName.getText().trim();
    if (name.isEmpty()) {
      formulaStatus.setText("NAME REQUIRED");
      statusDot.getStyleClass().removeAll("status-dot-saved", "status-dot-saving", "status-dot-warn");
      statusDot.getStyleClass().add("status-dot-warn");
      return;
    }
    List<Note> all = allNotes();
    if (all.isEmpty()) {
      formulaStatus.setText("NO NOTES");
      statusDot.getStyleClass().removeAll("status-dot-saved", "status-dot-saving", "status-dot-warn");
      statusDot.getStyleClass().add("status-dot-warn");
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
    autoSaveService.forceSave();
    javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<>() {
      @Override protected Void call() {
        compositionService.saveComposition(composition, "Manual save");
        return null;
      }
    };
    formulaStatus.setText("SAVING...");
    task.setOnSucceeded(e -> {
      formulaStatus.setText("SAVED \u2713");
      statusDot.getStyleClass().removeAll("status-dot-saved", "status-dot-saving", "status-dot-warn");
      statusDot.getStyleClass().add("status-dot-saved");
      editingCompositionId = null;
      autoSaveService.clear();
    });
    task.setOnFailed(e -> {
      formulaStatus.setText("ERROR: " + task.getException().getMessage());
      statusDot.getStyleClass().removeAll("status-dot-saved", "status-dot-saving", "status-dot-warn");
      statusDot.getStyleClass().add("status-dot-warn");
    });
    new Thread(task).start();
  }

  @FXML
  public void handleExport() {
    List<Note> all = allNotes();
    if (all.isEmpty()) {
      formulaStatus.setText("NOTHING TO EXPORT");
      statusDot.getStyleClass().removeAll("status-dot-saved", "status-dot-saving", "status-dot-warn");
      statusDot.getStyleClass().add("status-dot-warn");
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

    String name = baseName;
    AnalysisResult exportResult = analysisService.analyze(currentTopNotes, currentHeartNotes, currentBaseNotes);
    javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<>() {
      @Override protected Void call() throws Exception {
        pdfExportService.export(file, name, all, exportResult);
        return null;
      }
    };
    task.setOnSucceeded(e -> {
      formulaStatus.setText("EXPORTED \u2713");
      statusDot.getStyleClass().removeAll("status-dot-saved", "status-dot-saving", "status-dot-warn");
      statusDot.getStyleClass().add("status-dot-saved");
    });
    task.setOnFailed(e -> {
      formulaStatus.setText("EXPORT FAILED");
      statusDot.getStyleClass().removeAll("status-dot-saved", "status-dot-saving", "status-dot-warn");
      statusDot.getStyleClass().add("status-dot-warn");
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
  public void handleHistory() { viewManager.showHistory(); }
}
