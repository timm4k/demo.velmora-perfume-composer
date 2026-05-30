package velmora.composer.ui.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import velmora.composer.model.Note;
import velmora.composer.model.NoteType;
import velmora.composer.model.Perfume;
import velmora.composer.repository.NoteRepository;
import velmora.composer.repository.PerfumeRepository;
import velmora.composer.service.analysis.AlternativeNoteService;
import velmora.composer.service.analysis.FormulaRecommendationService;
import velmora.composer.service.analysis.PerfumeSimilarityService;
import velmora.composer.service.analysis.PerfumeStyleService;
import velmora.composer.service.analysis.SimilarityService;
import velmora.composer.state.CompositionState;
import velmora.composer.ui.util.UiUtils;
import velmora.composer.ui.ViewManager;

@Component
@RequiredArgsConstructor
public class CatalogController {

  private final PerfumeRepository perfumeRepository;
  private final NoteRepository noteRepository;
  private final CompositionState compositionState;
  private final ViewManager viewManager;
  private final SimilarityService similarityService;
  private final PerfumeSimilarityService perfumeSimilarityService;
  private final PerfumeStyleService perfumeStyleService;
  private final AlternativeNoteService alternativeNoteService;
  private final FormulaRecommendationService formulaRecommendationService;

  @FXML private FlowPane catalogGrid;
  @FXML private Label catalogCount;
  @FXML private Label headerTitle;
  @FXML private TextField searchField;
  @FXML private ComboBox<String> brandFilter;
  @FXML private ComboBox<String> familyFilter;
  @FXML private ComboBox<String> styleFilter;
  @FXML private ComboBox<String> longevityFilter;
  @FXML private Slider similaritySlider;
  @FXML private Label similarityLabel;
  @FXML private StackPane modalOverlay;
  @FXML private VBox modalContent;
  @FXML private ScrollPane gridScroll;
  @FXML private VBox emptyState;
  @FXML private VBox mainContent;
  @FXML private Label emptyTitle;
  @FXML private Label emptySubtitle;

  private List<Perfume> allPerfumes;
  private List<Note> cachedComposerNotes;
  private Set<String> cachedComposerNoteNames;
  private String cachedDetectedStyle;
  private List<PerfumeSimilarityService.PerfumeMatch> lastSimilarityResults;

  @FXML
  public void initialize() {
    loadPerfumes();
    setupFilters();
    searchField.textProperty().addListener((obs, o, n) -> applyFilters());
    similaritySlider.valueProperty().addListener((obs, o, n) -> {
      similarityLabel.setText(String.format("%.0f%%+", n));
      applyFilters();
    });
  }

  private void setupFilters() {
    brandFilter.valueProperty().addListener((obs, o, n) -> applyFilters());
    familyFilter.valueProperty().addListener((obs, o, n) -> applyFilters());
    styleFilter.valueProperty().addListener((obs, o, n) -> applyFilters());
    longevityFilter.valueProperty().addListener((obs, o, n) -> applyFilters());
  }

  private void refreshComposerContext() {
    List<Long> ids = compositionState.getCurrentNoteIds();
    Set<String> names = new HashSet<>(compositionState.getCurrentNoteNames());
    if (ids == null || ids.isEmpty() || names.isEmpty()) {
      cachedComposerNotes = List.of();
      cachedComposerNoteNames = Set.of();
      cachedDetectedStyle = null;
      lastSimilarityResults = null;
      return;
    }
    cachedComposerNoteNames = names;
    cachedComposerNotes = ids.stream()
        .map(id -> noteRepository.findById(id).orElse(null))
        .filter(n -> n != null)
        .collect(Collectors.toList());
    cachedDetectedStyle = perfumeStyleService.detectStyle(cachedComposerNotes,
        cachedComposerNotes.stream().filter(n -> n.getType() == NoteType.TOP).collect(Collectors.toList()),
        cachedComposerNotes.stream().filter(n -> n.getType() == NoteType.HEART).collect(Collectors.toList()),
        cachedComposerNotes.stream().filter(n -> n.getType() == NoteType.BASE).collect(Collectors.toList()));
    if (allPerfumes != null) {
      recalculateSimilarity();
    }
  }

  private void recalculateSimilarity() {
    if (cachedComposerNoteNames == null || cachedComposerNoteNames.isEmpty()) return;
    Set<String> families = cachedComposerNotes.stream()
        .map(Note::getCategory).filter(c -> c != null).map(String::toLowerCase).collect(Collectors.toSet());
    lastSimilarityResults = perfumeSimilarityService.findSimilar(
        cachedComposerNoteNames, families, 25, 40, 35, 70);
  }

  @FXML
  private void handleBack() {
    viewManager.showMain();
  }

  private void loadPerfumes() {
    Task<List<Perfume>> task = new Task<>() {
      @Override protected List<Perfume> call() {
        return perfumeRepository.findAll();
      }
    };
    task.setOnSucceeded(e -> {
      allPerfumes = task.getValue();
      Set<String> brands = allPerfumes.stream()
          .map(Perfume::getBrand).filter(b -> b != null && !b.isBlank())
          .collect(Collectors.toSet());
      Set<String> families = allPerfumes.stream()
          .map(Perfume::getOlfactoryFamily).filter(f -> f != null && !f.isBlank())
          .collect(Collectors.toSet());
      Set<String> styles = allPerfumes.stream()
          .map(Perfume::getStyle).filter(s -> s != null && !s.isBlank())
          .collect(Collectors.toSet());

      brandFilter.setItems(FXCollections.observableArrayList(
          brands.stream().sorted().collect(Collectors.toList())));
      familyFilter.setItems(FXCollections.observableArrayList(
          families.stream().sorted().collect(Collectors.toList())));
      styleFilter.setItems(FXCollections.observableArrayList(
          styles.stream().sorted().collect(Collectors.toList())));
      longevityFilter.setItems(FXCollections.observableArrayList(
          "All", "Short (1-3h)", "Moderate (4-6h)", "Long (7-9h)", "Very Long (10h+)"));

      refreshComposerContext();
      updateHeader();
      renderGrid(allPerfumes);
    });
    task.setOnFailed(e -> System.err.println("[CATALOG] Load failed: " + task.getException().getMessage()));
    new Thread(task).start();
  }

  private void updateHeader() {
    if (cachedComposerNoteNames != null && !cachedComposerNoteNames.isEmpty()) {
      String style = cachedDetectedStyle != null ? cachedDetectedStyle : "Custom";
      headerTitle.setText("Recommendations for your formula");
      catalogCount.setText(lastSimilarityResults != null
          ? lastSimilarityResults.size() + " fragrances matched"
          : (allPerfumes != null ? allPerfumes.size() : 0) + " fragrances");
    } else {
      headerTitle.setText("Perfume Catalog");
      catalogCount.setText((allPerfumes != null ? allPerfumes.size() : 0) + " fragrances");
    }
  }

  @FXML
  private void refreshRecommendations() {
    refreshComposerContext();
    updateHeader();
    applyFilters();
  }

  private void applyFilters() {
    if (allPerfumes == null) return;
    List<Perfume> filtered = allPerfumes.stream()
        .filter(p -> matchesSearch(p))
        .filter(p -> matchesBrand(p))
        .filter(p -> matchesFamily(p))
        .filter(p -> matchesStyle(p))
        .filter(p -> matchesLongevity(p))
        .filter(p -> matchesSimilarity(p))
        .collect(Collectors.toList());

    if (lastSimilarityResults != null && !lastSimilarityResults.isEmpty()) {
      Map<Long, Integer> scoreMap = lastSimilarityResults.stream()
          .collect(Collectors.toMap(m -> m.perfume().getId(), m -> m.overallScore()));
      filtered.sort((a, b) -> Integer.compare(
          scoreMap.getOrDefault(b.getId(), 0),
          scoreMap.getOrDefault(a.getId(), 0)));
    }

    catalogCount.setText(filtered.size() + " fragrances");
    renderGrid(filtered);
  }

  private boolean matchesSearch(Perfume p) {
    String q = searchField.getText();
    if (q == null || q.isBlank()) return true;
    String lq = q.toLowerCase();
    if (p.getName() != null && p.getName().toLowerCase().contains(lq)) return true;
    if (p.getBrand() != null && p.getBrand().toLowerCase().contains(lq)) return true;
    if (p.getOlfactoryFamily() != null && p.getOlfactoryFamily().toLowerCase().contains(lq)) return true;
    if (p.getStyle() != null && p.getStyle().toLowerCase().contains(lq)) return true;
    if (p.getTopNotes() != null && p.getTopNotes().toLowerCase().contains(lq)) return true;
    if (p.getHeartNotes() != null && p.getHeartNotes().toLowerCase().contains(lq)) return true;
    if (p.getBaseNotes() != null && p.getBaseNotes().toLowerCase().contains(lq)) return true;
    return false;
  }

  private boolean matchesBrand(Perfume p) {
    String val = brandFilter.getValue();
    return val == null || val.equals("All") || val.isBlank() || val.equals(p.getBrand());
  }

  private boolean matchesFamily(Perfume p) {
    String val = familyFilter.getValue();
    return val == null || val.equals("All") || val.isBlank() || val.equals(p.getOlfactoryFamily());
  }

  private boolean matchesStyle(Perfume p) {
    String val = styleFilter.getValue();
    return val == null || val.equals("All") || val.isBlank() || val.equals(p.getStyle());
  }

  private boolean matchesLongevity(Perfume p) {
    String val = longevityFilter.getValue();
    if (val == null || val.equals("All") || val.isBlank()) return true;
    int score = p.getLongevityScore() != null ? p.getLongevityScore() : 0;
    return switch (val) {
      case "Short (1-3h)" -> score <= 3;
      case "Moderate (4-6h)" -> score >= 4 && score <= 6;
      case "Long (7-9h)" -> score >= 7 && score <= 9;
      case "Very Long (10h+)" -> score >= 10;
      default -> true;
    };
  }

  private boolean matchesSimilarity(Perfume p) {
    double minSim = similaritySlider.getValue();
    if (minSim <= 1) return true;
    if (lastSimilarityResults == null) return false;
    return lastSimilarityResults.stream()
        .filter(m -> m.perfume().getId().equals(p.getId()))
        .anyMatch(m -> m.overallScore() >= minSim);
  }

  private void renderGrid(List<Perfume> perfumes) {
    catalogGrid.getChildren().clear();
    boolean hasComposition = cachedComposerNoteNames != null && !cachedComposerNoteNames.isEmpty();
    boolean empty = perfumes.isEmpty();

    if (empty && hasComposition) {
      mainContent.setVisible(false);
      mainContent.setManaged(false);
      emptyState.setVisible(true);
      emptyState.setManaged(true);
      emptyTitle.setText("No matching fragrances");
      emptySubtitle.setText("Try adjusting filters or adding different notes to your composition.");
      return;
    }
    if (empty && !hasComposition) {
      mainContent.setVisible(false);
      mainContent.setManaged(false);
      emptyState.setVisible(true);
      emptyState.setManaged(true);
      emptyTitle.setText("Create a composition first");
      emptySubtitle.setText("Build your formula in the Composer to discover similar perfumes.");
      return;
    }

    mainContent.setVisible(true);
    mainContent.setManaged(true);
    emptyState.setVisible(false);
    emptyState.setManaged(false);

    for (Perfume p : perfumes) {
      Node card = createPerfumeCard(p, hasComposition);
      catalogGrid.getChildren().add(card);

      FadeTransition ft = new FadeTransition(Duration.millis(300), card);
      ft.setFromValue(0);
      ft.setToValue(1);
      ft.setDelay(Duration.millis(catalogGrid.getChildren().size() * 15));
      ft.play();

      TranslateTransition tt = new TranslateTransition(Duration.millis(300), card);
      tt.setFromY(20);
      tt.setToY(0);
      tt.setDelay(Duration.millis(catalogGrid.getChildren().size() * 15));
      tt.play();
    }
  }

  private Node createPerfumeCard(Perfume p, boolean hasComposition) {
    VBox card = new VBox(0);
    card.setPrefWidth(220);
    card.getStyleClass().add("perfume-card");
    card.setCursor(Cursor.HAND);

    int matchScore = 0;
    String scoreColor = "#B0ADA8";
    String glowClass = "";
    if (hasComposition && lastSimilarityResults != null) {
      var match = lastSimilarityResults.stream()
          .filter(m -> m.perfume().getId().equals(p.getId()))
          .findFirst().orElse(null);
      if (match != null) {
        matchScore = match.overallScore();
        if (matchScore >= 80) { scoreColor = "#B18DB8"; glowClass = "card-glow-lavender"; }
        else if (matchScore >= 60) { scoreColor = "#D69478"; glowClass = "card-glow-peach"; }
        else { scoreColor = "#6DA89E"; glowClass = "card-glow-teal"; }
      }
    }
    if (!glowClass.isEmpty()) card.getStyleClass().add(glowClass);

    StackPane imageArea = new StackPane();
    imageArea.setPrefHeight(170);
    imageArea.getStyleClass().add("perfume-card-image");

    if (p.getImageUrl() != null && !p.getImageUrl().isBlank()) {
      try {
        Image img = new Image(p.getImageUrl(), 220, 170, true, true, true);
        if (!img.isError()) {
          ImageView iv = new ImageView(img);
          iv.setFitWidth(220);
          iv.setFitHeight(170);
          iv.setPreserveRatio(false);
          imageArea.getChildren().add(iv);
        }
      } catch (Exception ignored) {}
    }

    if (imageArea.getChildren().isEmpty()) {
      String initial = p.getName() != null ? p.getName().substring(0, 1).toUpperCase() : "?";
      Label placeholder = new Label(initial);
      placeholder.getStyleClass().add("perfume-card-placeholder");
      imageArea.getChildren().add(placeholder);
    }

    if (hasComposition && matchScore > 0) {
      VBox scoreBadge = createScoreBadge(matchScore, scoreColor);
      StackPane.setAlignment(scoreBadge, Pos.TOP_RIGHT);
      StackPane.setMargin(scoreBadge, new Insets(8));
      imageArea.getChildren().add(scoreBadge);
    }

    Label brandBadge = new Label(p.getBrand() != null ? p.getBrand() : "");
    brandBadge.getStyleClass().add("perfume-card-brand-badge");
    StackPane.setAlignment(brandBadge, Pos.BOTTOM_LEFT);
    StackPane.setMargin(brandBadge, new Insets(0, 0, 8, 8));
    imageArea.getChildren().add(brandBadge);

    VBox body = new VBox(4);
    body.setPadding(new Insets(10, 12, 12, 12));

    Label name = new Label(p.getName());
    name.getStyleClass().add("perfume-card-name");
    name.setWrapText(true);

    HBox infoRow = new HBox(8);
    infoRow.setAlignment(Pos.CENTER_LEFT);
    Label family = new Label(p.getOlfactoryFamily() != null ? p.getOlfactoryFamily() : "");
    family.getStyleClass().add("perfume-card-family");
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    Label price = new Label(p.getPrice() != null ? "$" + String.format("%.0f", p.getPrice()) : "");
    price.getStyleClass().add("perfume-card-price");
    infoRow.getChildren().addAll(family, spacer, price);

    body.getChildren().addAll(name, infoRow);
    card.getChildren().addAll(imageArea, body);

    card.setOnMouseClicked(e -> {
      if (e.getButton() == MouseButton.PRIMARY) {
        showModal(p);
      }
    });

    return card;
  }

  private VBox createScoreBadge(int score, String color) {
    VBox badge = new VBox(0);
    badge.setAlignment(Pos.CENTER);
    badge.setPadding(new Insets(4, 10, 4, 10));
    badge.setStyle("-fx-background-color: rgba(45,42,36,0.75); -fx-background-radius: 10;");
    Label pct = new Label(score + "%");
    pct.setStyle("-fx-font-size: 18; -fx-font-weight: 700; -fx-text-fill: " + color + "; -fx-font-family: 'Libre Baskerville',serif;");
    badge.getChildren().add(pct);
    return badge;
  }

  private void showModal(Perfume p) {
    modalContent.getChildren().clear();
    modalContent.getStyleClass().add("modal-glass");
    modalContent.setMaxWidth(580);
    modalContent.setMaxHeight(760);

    HBox headerRow = new HBox();
    headerRow.setAlignment(Pos.CENTER_RIGHT);
    Label closeBtn = new Label("\u2715");
    closeBtn.getStyleClass().add("modal-close");
    closeBtn.setOnMouseClicked(e -> hideModal());
    headerRow.getChildren().add(closeBtn);

    Label name = new Label(p.getName());
    name.getStyleClass().add("modal-title");
    Label brand = new Label(p.getBrand());
    brand.getStyleClass().add("modal-brand");

    HBox styleRow = new HBox(8);
    styleRow.setAlignment(Pos.CENTER_LEFT);
    styleRow.setPadding(new Insets(4, 0, 0, 0));
    if (p.getOlfactoryFamily() != null) {
      Label famChip = createChip(p.getOlfactoryFamily(), "#b18db8");
      styleRow.getChildren().add(famChip);
    }
    if (p.getStyle() != null) {
      Label styleChip = createChip(p.getStyle(), "#6DA89E");
      styleRow.getChildren().add(styleChip);
    }
    if (p.getReleaseYear() != null) {
      Label year = new Label("Released: " + p.getReleaseYear());
      year.setStyle("-fx-font-size: 14; -fx-text-fill: #B0ADA8; -fx-font-weight: 600;");
      styleRow.getChildren().add(year);
    }

    Rectangle divider = new Rectangle();
    divider.setHeight(1);
    divider.setFill(Color.web("rgba(177,141,184,0.2)"));

    VBox profileSection = new VBox(8);
    profileSection.setPadding(new Insets(8, 0, 0, 0));
    Label profileTitle = new Label("PROFILE");
    profileTitle.getStyleClass().add("modal-section-title");
    FlowPane profileGrid = new FlowPane(8, 8);
    profileGrid.setAlignment(Pos.CENTER_LEFT);

    if (p.getStyle() != null) profileGrid.getChildren().add(createMetaBadge("Style", p.getStyle()));
    if (p.getSeason() != null) profileGrid.getChildren().add(createMetaBadge("Season", capitalize(p.getSeason())));
    if (p.getGenderProfile() != null) profileGrid.getChildren().add(createMetaBadge("Gender", capitalize(p.getGenderProfile())));
    if (p.getProjection() != null) profileGrid.getChildren().add(createMetaBadge("Projection", capitalize(p.getProjection())));
    if (p.getLongevityScore() != null) profileGrid.getChildren().add(createMetaBadge("Longevity", p.getLongevityScore() + "/10"));
    if (p.getComplexity() != null) profileGrid.getChildren().add(createMetaBadge("Complexity", p.getComplexity()));
    if (p.getPrice() != null) profileGrid.getChildren().add(createMetaBadge("Price", "$" + String.format("%.0f", p.getPrice())));
    profileSection.getChildren().addAll(profileTitle, profileGrid);

    Rectangle divider2 = new Rectangle();
    divider2.setHeight(1);
    divider2.setFill(Color.web("rgba(177,141,184,0.2)"));

    VBox pyramidSection = new VBox(8);
    Label pyrTitle = new Label("OLFACTORY PYRAMID");
    pyrTitle.getStyleClass().add("modal-section-title");
    pyramidSection.getChildren().add(pyrTitle);

    String[][] pyramidData = getPyramidData(p);
    for (String[] row : pyramidData) {
      if (row[1] != null && !row[1].isBlank()) {
        pyramidSection.getChildren().add(createChipRow(row[0], row[1], row[2]));
      }
    }

    if (p.getDescription() != null && !p.getDescription().isBlank()) {
      Rectangle divider3 = new Rectangle();
      divider3.setHeight(1);
      divider3.setFill(Color.web("rgba(177,141,184,0.2)"));

      Label aboutTitle = new Label("ABOUT THIS FRAGRANCE");
      aboutTitle.getStyleClass().add("modal-section-title");
      Label desc = new Label(p.getDescription());
      desc.getStyleClass().add("modal-desc");
      desc.setWrapText(true);

      VBox aboutSection = new VBox(6);
      aboutSection.getChildren().addAll(divider3, aboutTitle, desc);
      pyramidSection.getChildren().add(aboutSection);
    }

    if (p.getAccords() != null && !p.getAccords().isBlank()) {
      Rectangle divider4 = new Rectangle();
      divider4.setHeight(1);
      divider4.setFill(Color.web("rgba(177,141,184,0.2)"));
      Label accordTitle = new Label("MAIN ACCORDS");
      accordTitle.getStyleClass().add("modal-section-title");
      FlowPane accordChips = new FlowPane(6, 6);
      accordChips.setAlignment(Pos.CENTER_LEFT);
      for (String a : p.getAccords().split(",")) {
        String trimmed = a.trim();
        if (!trimmed.isEmpty()) {
          Label chip = createAccordChip(trimmed);
          accordChips.getChildren().add(chip);
        }
      }
      VBox accordSection = new VBox(6);
      accordSection.getChildren().addAll(divider4, accordTitle, accordChips);
      pyramidSection.getChildren().add(accordSection);
    }

    if (p.getSeason() != null && !p.getSeason().isBlank()) {
      Rectangle divider5 = new Rectangle();
      divider5.setHeight(1);
      divider5.setFill(Color.web("rgba(177,141,184,0.2)"));
      Label seasonTitle = new Label("BEST SEASONS");
      seasonTitle.getStyleClass().add("modal-section-title");
      VBox seasonViz = createSeasonVisualization(p.getSeason());
      VBox seasonSection = new VBox(6);
      seasonSection.getChildren().addAll(divider5, seasonTitle, seasonViz);
      pyramidSection.getChildren().add(seasonSection);
    }

    if (p.getOccasions() != null && !p.getOccasions().isBlank()) {
      Rectangle divider6 = new Rectangle();
      divider6.setHeight(1);
      divider6.setFill(Color.web("rgba(177,141,184,0.2)"));
      Label occTitle = new Label("BEST FOR");
      occTitle.getStyleClass().add("modal-section-title");
      FlowPane occChips = new FlowPane(6, 6);
      occChips.setAlignment(Pos.CENTER_LEFT);
      for (String o : p.getOccasions().split(",")) {
        String trimmed = o.trim();
        if (!trimmed.isEmpty()) {
          occChips.getChildren().add(createOccasionBadge(trimmed));
        }
      }
      VBox occSection = new VBox(6);
      occSection.getChildren().addAll(divider6, occTitle, occChips);
      pyramidSection.getChildren().add(occSection);
    }

    Rectangle divider7 = new Rectangle();
    divider7.setHeight(1);
    divider7.setFill(Color.web("rgba(177,141,184,0.2)"));

    Button similarBtn = new Button("Compare With My Formula");
    similarBtn.getStyleClass().addAll("btn-similar", "btn-similar-full");
    similarBtn.setOnAction(e -> showSimilarityAnalysis(p));

    VBox content = new VBox(10);
    content.getChildren().addAll(headerRow, name, brand, styleRow, divider,
        profileSection, divider2, pyramidSection, divider7, similarBtn);

    ScrollPane scrollContent = new ScrollPane(content);
    scrollContent.setFitToWidth(true);
    scrollContent.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
    scrollContent.getStyleClass().add("thin-scroll");

    modalContent.getChildren().add(scrollContent);
    modalOverlay.setVisible(true);
    modalOverlay.setManaged(true);
  }

  private VBox createSeasonVisualization(String season) {
    VBox viz = new VBox(4);
    viz.setPadding(new Insets(4, 0, 0, 0));
    String[][] seasons = {
        {"Spring", season.equalsIgnoreCase("spring") || season.equalsIgnoreCase("all") ? "0.85" : "0.25"},
        {"Summer", season.equalsIgnoreCase("summer") || season.equalsIgnoreCase("all") ? "0.90" : "0.20"},
        {"Autumn", season.equalsIgnoreCase("fall") || season.equalsIgnoreCase("autumn") || season.equalsIgnoreCase("all") ? "0.75" : "0.25"},
        {"Winter", season.equalsIgnoreCase("winter") || season.equalsIgnoreCase("all") ? "0.80" : "0.30"}
    };
    if (season.equalsIgnoreCase("all")) {
      for (String[] s : seasons) s[1] = "0.85";
    }
    for (String[] s : seasons) {
      HBox row = new HBox(8);
      row.setAlignment(Pos.CENTER_LEFT);
      Label name = new Label(s[0]);
      name.setMinWidth(60);
      name.setStyle("-fx-font-size: 13; -fx-font-weight: 600; -fx-text-fill: #6B6560;");
      StackPane barTrack = new StackPane();
      barTrack.setPrefWidth(120);
      barTrack.setPrefHeight(8);
      barTrack.setStyle("-fx-background-color: rgba(234,230,223,0.5); -fx-background-radius: 4;");
      Rectangle barFill = new Rectangle();
      barFill.setHeight(8);
      barFill.setArcWidth(4);
      barFill.setArcHeight(4);
      double pct = Double.parseDouble(s[1]);
      barFill.setWidth(120 * pct);
      String color = switch (s[0]) {
        case "Spring" -> "#B18DB8";
        case "Summer" -> "#D69478";
        case "Autumn" -> "#C8954A";
        default -> "#6DA89E";
      };
      barFill.setFill(Color.web(color));
      StackPane.setAlignment(barFill, Pos.CENTER_LEFT);
      barTrack.getChildren().add(barFill);
      Label pctLbl = new Label(String.format("%.0f%%", pct * 100));
      pctLbl.setStyle("-fx-font-size: 12; -fx-font-weight: 700; -fx-text-fill: " + color + "; -fx-min-width: 30; -fx-alignment: center-right;");
      row.getChildren().addAll(name, barTrack, pctLbl);
      viz.getChildren().add(row);
    }
    return viz;
  }

  private void showSimilarityAnalysis(Perfume p) {
    Set<String> composerNames = new HashSet<>(compositionState.getCurrentNoteNames());
    if (composerNames.isEmpty() || cachedComposerNotes == null || cachedComposerNotes.isEmpty()) {
      Label empty = new Label("Create a composition first to compare.");
      empty.setStyle("-fx-padding: 8 0 0 0; -fx-font-size: 17; -fx-font-style: italic; -fx-text-fill: #B0ADA8;");
      modalContent.getChildren().add(empty);
      modalContent.setMaxHeight(800);
      return;
    }

    Set<String> families = cachedComposerNotes.stream()
        .map(Note::getCategory).filter(c -> c != null).map(String::toLowerCase).collect(Collectors.toSet());
    var match = perfumeSimilarityService.calculateMatch(
        p, composerNames, families, 25, 40, 35, 70);

    VBox analysisBox = new VBox(12);
    analysisBox.setPadding(new Insets(12, 0, 0, 0));

    Label analysisTitle = new Label("SIMILARITY ANALYSIS");
    analysisTitle.getStyleClass().add("modal-section-title");
    analysisTitle.setStyle("-fx-font-size: 18; -fx-letter-spacing: 1.5;");
    analysisBox.getChildren().add(analysisTitle);

    int score = match.overallScore();
    String scoreColor = score >= 80 ? "#B18DB8" : score >= 60 ? "#D69478" : score >= 40 ? "#C8954A" : "#D9534F";
    String label = score >= 80 ? "Excellent Match" : score >= 60 ? "Good Match" : score >= 40 ? "Fair Match" : "Distant Match";

    HBox scoreRow = new HBox(12);
    scoreRow.setAlignment(Pos.CENTER_LEFT);
    Label scoreLbl = new Label(score + "%");
    scoreLbl.setStyle("-fx-font-family: 'Libre Baskerville',serif; -fx-font-size: 42; -fx-font-weight: 700; -fx-text-fill: " + scoreColor + ";");
    VBox scoreMeta = new VBox(2);
    Label matchLabel = new Label(label);
    matchLabel.setStyle("-fx-font-size: 18; -fx-font-weight: 700; -fx-text-fill: " + scoreColor + ";");
    Label matchDesc = new Label("based on notes, families, balance, and longevity");
    matchDesc.setStyle("-fx-font-size: 13; -fx-text-fill: #B0ADA8; -fx-font-style: italic;");
    scoreMeta.getChildren().addAll(matchLabel, matchDesc);
    scoreRow.getChildren().addAll(scoreLbl, scoreMeta);
    analysisBox.getChildren().add(scoreRow);

    Rectangle div = new Rectangle();
    div.setHeight(1);
    div.setFill(Color.web("rgba(177,141,184,0.15)"));
    analysisBox.getChildren().add(div);

    Label breakdownTitle = new Label("SCORE BREAKDOWN");
    breakdownTitle.setStyle("-fx-font-size: 14; -fx-font-weight: 700; -fx-text-fill: #A8A29E; -fx-letter-spacing: 1.5;");
    analysisBox.getChildren().add(breakdownTitle);

    analysisBox.getChildren().add(createBreakdownBar("Notes Match", match.notesScore(), "#B18DB8"));
    analysisBox.getChildren().add(createBreakdownBar("Family Match", match.familyScore(), "#D69478"));
    analysisBox.getChildren().add(createBreakdownBar("Balance Match", match.balanceScore(), "#6DA89E"));
    analysisBox.getChildren().add(createBreakdownBar("Longevity Match", match.longevityScore(), "#C8954A"));

    Rectangle div2 = new Rectangle();
    div2.setHeight(1);
    div2.setFill(Color.web("rgba(177,141,184,0.15)"));
    analysisBox.getChildren().add(div2);

    if (!match.sharedNotes().isEmpty()) {
      Label sharedTitle = new Label("SHARED NOTES");
      sharedTitle.setStyle("-fx-font-size: 14; -fx-font-weight: 700; -fx-text-fill: #A8A29E; -fx-letter-spacing: 1.5;");
      analysisBox.getChildren().add(sharedTitle);
      FlowPane sharedChips = new FlowPane(6, 6);
      sharedChips.setAlignment(Pos.CENTER_LEFT);
      for (String n : match.sharedNotes()) {
        sharedChips.getChildren().add(createSharedNoteChip(n, "#5A9E8F"));
      }
      analysisBox.getChildren().add(sharedChips);
    }

    if (match.mainDifference() != null && !match.mainDifference().isBlank()) {
      Label diffTitle = new Label("MAIN DIFFERENCE");
      diffTitle.setStyle("-fx-font-size: 14; -fx-font-weight: 700; -fx-text-fill: #A8A29E; -fx-letter-spacing: 1.5;");
      Label diffText = new Label("\u2022 " + match.mainDifference());
      diffText.setStyle("-fx-font-size: 15; -fx-text-fill: #6B6560; -fx-line-spacing: 2;");
      diffText.setWrapText(true);
      analysisBox.getChildren().addAll(diffTitle, diffText);
    }

    List<FormulaRecommendationService.Recommendation> recs = cachedComposerNotes != null
        ? formulaRecommendationService.analyze(cachedComposerNotes,
            cachedComposerNotes.stream().filter(n -> n.getType() == NoteType.TOP).collect(Collectors.toList()),
            cachedComposerNotes.stream().filter(n -> n.getType() == NoteType.HEART).collect(Collectors.toList()),
            cachedComposerNotes.stream().filter(n -> n.getType() == NoteType.BASE).collect(Collectors.toList()),
            Map.of(), score)
        : List.of();

    if (!recs.isEmpty()) {
      Rectangle div3 = new Rectangle();
      div3.setHeight(1);
      div3.setFill(Color.web("rgba(177,141,184,0.15)"));
      Label recTitle = new Label("RECOMMENDATIONS");
      recTitle.setStyle("-fx-font-size: 14; -fx-font-weight: 700; -fx-text-fill: #A8A29E; -fx-letter-spacing: 1.5;");
      analysisBox.getChildren().addAll(div3, recTitle);
      for (FormulaRecommendationService.Recommendation r : recs) {
        Label rl = new Label("\u2022 " + r.message());
        rl.setStyle("-fx-font-size: 14; -fx-text-fill: #5F5B57; -fx-line-spacing: 2;");
        rl.setWrapText(true);
        analysisBox.getChildren().add(rl);
      }
    }

    List<AlternativeNoteService.NoteAlternative> alts = alternativeNoteService.suggestAlternatives(cachedComposerNotes);
    if (!alts.isEmpty()) {
      Rectangle div4 = new Rectangle();
      div4.setHeight(1);
      div4.setFill(Color.web("rgba(177,141,184,0.15)"));
      Label altTitle = new Label("ALTERNATIVE NOTES");
      altTitle.setStyle("-fx-font-size: 14; -fx-font-weight: 700; -fx-text-fill: #A8A29E; -fx-letter-spacing: 1.5;");
      analysisBox.getChildren().addAll(div4, altTitle);
      for (AlternativeNoteService.NoteAlternative a : alts) {
        HBox altRow = new HBox(6);
        altRow.setAlignment(Pos.CENTER_LEFT);
        Label currentLbl = new Label(a.currentNote() + " \u2192 ");
        currentLbl.setStyle("-fx-font-size: 14; -fx-font-weight: 600; -fx-text-fill: #B0ADA8;");
        Label suggestedLbl = new Label(a.suggestedNote());
        suggestedLbl.setStyle("-fx-font-size: 15; -fx-font-weight: 700; -fx-text-fill: #6DA89E;");
        Label reasonLbl = new Label("\u2014 " + a.reason());
        reasonLbl.setStyle("-fx-font-size: 13; -fx-text-fill: #8A8580; -fx-font-style: italic;");
        altRow.getChildren().addAll(currentLbl, suggestedLbl, reasonLbl);
        analysisBox.getChildren().add(altRow);
      }
    }

    modalContent.getChildren().add(analysisBox);
    modalContent.setMaxHeight(800);
  }

  private HBox createBreakdownBar(String label, int score, String color) {
    HBox row = new HBox(8);
    row.setAlignment(Pos.CENTER_LEFT);
    Label lbl = new Label(label);
    lbl.setMinWidth(130);
    lbl.setStyle("-fx-font-size: 14; -fx-font-weight: 600; -fx-text-fill: #6B6560;");
    StackPane track = new StackPane();
    track.setPrefWidth(160);
    track.setPrefHeight(10);
    track.setStyle("-fx-background-color: rgba(234,230,223,0.5); -fx-background-radius: 5;");
    Rectangle fill = new Rectangle();
    fill.setHeight(10);
    fill.setArcWidth(5);
    fill.setArcHeight(5);
    fill.setWidth(160 * score / 100.0);
    fill.setFill(Color.web(color));
    StackPane.setAlignment(fill, Pos.CENTER_LEFT);
    track.getChildren().add(fill);
    Label val = new Label(score + "%");
    val.setMinWidth(36);
    val.setStyle("-fx-font-size: 14; -fx-font-weight: 700; -fx-text-fill: " + color + "; -fx-alignment: center-right;");
    row.getChildren().addAll(lbl, track, val);
    return row;
  }

  private Label createSharedNoteChip(String text, String color) {
    Label chip = new Label("\u2713 " + text);
    chip.setStyle("-fx-background-color: " + color + "15; -fx-background-radius: 8; -fx-padding: 3 10;"
        + "-fx-font-size: 13; -fx-font-weight: 600; -fx-text-fill: " + color + ";");
    return chip;
  }

  private Label createChip(String text, String color) {
    Label chip = new Label(text);
    chip.setStyle("-fx-background-color: " + color + "18; -fx-background-radius: 8; -fx-padding: 3 10;"
        + "-fx-font-size: 13; -fx-font-weight: 600; -fx-text-fill: " + color + ";");
    return chip;
  }

  private Label createAccordChip(String text) {
    String color = switch (text.toLowerCase().trim()) {
      case "fresh", "citrus", "aquatic", "clean" -> "#5A9E8F";
      case "spicy", "warm", "rich" -> "#D69478";
      case "woody", "earthy", "smoky", "mineral" -> "#6DA89E";
      case "sweet", "vanilla", "gourmand" -> "#C8954A";
      case "floral", "powdery", "soft" -> "#B18DB8";
      case "dark", "leather", "tobacco" -> "#8B4513";
      default -> "#6B6560";
    };
    Label chip = new Label(text);
    chip.setStyle("-fx-background-color: " + color + "15; -fx-background-radius: 8; -fx-padding: 3 10;"
        + "-fx-font-size: 13; -fx-font-weight: 600; -fx-text-fill: " + color + ";");
    return chip;
  }

  private Label createMetaBadge(String title, String value) {
    VBox badge = new VBox(1);
    badge.setAlignment(Pos.CENTER);
    badge.setPadding(new Insets(5, 10, 5, 10));
    badge.setStyle("-fx-background-color: rgba(240,237,230,0.6); -fx-background-radius: 8;");
    Label t = new Label(title);
    t.setStyle("-fx-font-size: 11; -fx-font-weight: 700; -fx-text-fill: #A8A29E; -fx-letter-spacing: 1;");
    Label v = new Label(value);
    v.setStyle("-fx-font-size: 15; -fx-font-weight: 700; -fx-text-fill: #2D2A24;");
    badge.getChildren().addAll(t, v);
    Label wrapper = new Label();
    wrapper.setGraphic(badge);
    return wrapper;
  }

  private Label createOccasionBadge(String text) {
    Label badge = new Label(text);
    badge.setStyle("-fx-background-color: rgba(177,141,184,0.12); -fx-background-radius: 10; -fx-padding: 4 12;"
        + "-fx-font-size: 13; -fx-font-weight: 600; -fx-text-fill: #6B67A8;");
    return badge;
  }

  private HBox createChipRow(String phase, String notes, String color) {
    HBox row = new HBox(8);
    row.setAlignment(Pos.CENTER_LEFT);
    Rectangle dot = new Rectangle(4, 14);
    dot.setFill(Color.web(color));
    dot.setArcWidth(2);
    dot.setArcHeight(2);
    Label phaseLabel = new Label(phase);
    phaseLabel.setStyle("-fx-font-size: 13; -fx-font-weight: 700; -fx-text-fill: #A8A29E; -fx-letter-spacing: 1; -fx-min-width: 50;");
    String[] noteParts = notes.split(",");
    HBox chips = new HBox(6);
    chips.setAlignment(Pos.CENTER_LEFT);
    for (String n : noteParts) {
      String trimmed = n.trim();
      if (!trimmed.isEmpty()) {
        Label chip = new Label(trimmed);
        chip.setStyle("-fx-background-color: rgba(255,255,255,0.6); -fx-background-radius: 6; -fx-padding: 2 8;"
            + "-fx-font-size: 13; -fx-font-weight: 600; -fx-text-fill: #4B4B4B;"
            + "-fx-border-color: " + color + "40; -fx-border-radius: 6; -fx-border-width: 1;");
        chips.getChildren().add(chip);
      }
    }
    row.getChildren().addAll(dot, phaseLabel, chips);
    return row;
  }

  private String[][] getPyramidData(Perfume p) {
    String[] compNotes = similarityService.loadCompositionNotes(p.getId());
    boolean hasCompNotes = compNotes[0] != null || compNotes[1] != null || compNotes[2] != null;
    if (hasCompNotes) {
      return new String[][]{
          {"TOP", compNotes[0], "#E2A998"},
          {"HEART", compNotes[1], "#b18db8"},
          {"BASE", compNotes[2], "#78A0A0"}
      };
    }
    return new String[][]{
        {"TOP", p.getTopNotes(), "#E2A998"},
        {"HEART", p.getHeartNotes(), "#b18db8"},
        {"BASE", p.getBaseNotes(), "#78A0A0"}
    };
  }

  private void hideModal() {
    modalOverlay.setVisible(false);
    modalOverlay.setManaged(false);
  }

  private String capitalize(String s) {
    return UiUtils.capitalize(s);
  }
}
