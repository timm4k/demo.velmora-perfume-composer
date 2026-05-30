package velmora.composer.ui.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ScrollPane;
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
import javafx.scene.shape.Rectangle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import velmora.composer.model.Perfume;
import velmora.composer.repository.PerfumeRepository;
import velmora.composer.service.analysis.SimilarityService;
import velmora.composer.state.CompositionState;
import velmora.composer.ui.ViewManager;

@Component
@RequiredArgsConstructor
public class CatalogController {

  private final PerfumeRepository perfumeRepository;
  private final CompositionState compositionState;
  private final ViewManager viewManager;
  private final SimilarityService similarityService;

  @FXML private FlowPane catalogGrid;
  @FXML private Label catalogCount;
  @FXML private TextField searchField;
  @FXML private ComboBox<String> brandFilter;
  @FXML private ComboBox<String> familyFilter;
  @FXML private StackPane modalOverlay;
  @FXML private VBox modalContent;
  @FXML private ScrollPane gridScroll;

  private List<Perfume> allPerfumes;

  @FXML
  public void initialize() {
    loadPerfumes();
    brandFilter.valueProperty().addListener((obs, o, n) -> applyFilters());
    familyFilter.valueProperty().addListener((obs, o, n) -> applyFilters());
    searchField.textProperty().addListener((obs, o, n) -> applyFilters());
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

      brandFilter.setItems(FXCollections.observableArrayList(
          brands.stream().sorted().collect(Collectors.toList())));
      familyFilter.setItems(FXCollections.observableArrayList(
          families.stream().sorted().collect(Collectors.toList())));

      catalogCount.setText(allPerfumes.size() + " fragrances");
      renderGrid(allPerfumes);
    });
    task.setOnFailed(e -> System.err.println("[CATALOG] Load failed: " + task.getException().getMessage()));
    new Thread(task).start();
  }

  private void applyFilters() {
    if (allPerfumes == null) return;
    List<Perfume> filtered = allPerfumes.stream()
        .filter(p -> matchesSearch(p))
        .filter(p -> matchesBrand(p))
        .filter(p -> matchesFamily(p))
        .collect(Collectors.toList());
    catalogCount.setText(filtered.size() + " fragrances");
    renderGrid(filtered);
  }

  private boolean matchesSearch(Perfume p) {
    String q = searchField.getText();
    if (q == null || q.isBlank()) return true;
    String lq = q.toLowerCase();
    String name = p.getName();
    if (name != null && name.toLowerCase().contains(lq)) return true;
    String brand = p.getBrand();
    if (brand != null && brand.toLowerCase().contains(lq)) return true;
    if (p.getOlfactoryFamily() != null && p.getOlfactoryFamily().toLowerCase().contains(lq)) return true;
    if (p.getSeason() != null && p.getSeason().toLowerCase().contains(lq)) return true;
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

  private void renderGrid(List<Perfume> perfumes) {
    catalogGrid.getChildren().clear();
    for (Perfume p : perfumes) {
      catalogGrid.getChildren().add(createPerfumeCard(p));
    }
  }

  private VBox createPerfumeCard(Perfume p) {
    VBox card = new VBox(8);
    card.setPrefWidth(200);
    card.getStyleClass().add("perfume-card");
    card.setCursor(Cursor.HAND);

    StackPane imageArea = new StackPane();
    imageArea.setPrefHeight(180);
    imageArea.getStyleClass().add("perfume-card-image");

    if (p.getImageUrl() != null && !p.getImageUrl().isBlank()) {
      try {
        Image img = new Image(p.getImageUrl(), 200, 180, true, true, true);
        if (!img.isError()) {
          ImageView iv = new ImageView(img);
          iv.setFitWidth(200);
          iv.setFitHeight(180);
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

    Label brandBadge = new Label(p.getBrand() != null ? p.getBrand() : "");
    brandBadge.getStyleClass().add("perfume-card-brand-badge");
    StackPane.setAlignment(brandBadge, Pos.TOP_LEFT);
    StackPane.setMargin(brandBadge, new Insets(6));
    imageArea.getChildren().add(brandBadge);

    if (p.getRating() != null) {
      Label ratingBadge = new Label("★ " + String.format("%.1f", p.getRating()));
      ratingBadge.getStyleClass().add("perfume-card-rating-badge");
      StackPane.setAlignment(ratingBadge, Pos.TOP_RIGHT);
      StackPane.setMargin(ratingBadge, new Insets(6));
      imageArea.getChildren().add(ratingBadge);
    }

    Label name = new Label(p.getName());
    name.getStyleClass().add("perfume-card-name");
    name.setWrapText(true);
    name.setTextOverrun(OverrunStyle.ELLIPSIS);
    name.setMaxWidth(180);

    Label family = new Label(p.getOlfactoryFamily() != null ? p.getOlfactoryFamily() : "");
    family.getStyleClass().add("perfume-card-family");

    card.getChildren().addAll(imageArea, name, family);

    card.setOnMouseClicked(e -> {
      if (e.getButton() == MouseButton.PRIMARY) {
        showModal(p);
      }
    });

    return card;
  }

  private void showModal(Perfume p) {
    modalContent.getChildren().clear();
    modalContent.getStyleClass().add("modal-glass");

    HBox headerRow = new HBox();
    headerRow.setAlignment(Pos.CENTER_RIGHT);
    Label closeBtn = new Label("✕");
    closeBtn.getStyleClass().add("modal-close");
    closeBtn.setOnMouseClicked(e -> hideModal());
    headerRow.getChildren().add(closeBtn);

    Label name = new Label(p.getName());
    name.getStyleClass().add("modal-title");
    Label brand = new Label(p.getBrand());
    brand.getStyleClass().add("modal-brand");

    StackPane imgArea = new StackPane();
    imgArea.setPrefHeight(200);
    imgArea.getStyleClass().add("modal-image");
    if (p.getImageUrl() != null && !p.getImageUrl().isBlank()) {
      try {
        Image img = new Image(p.getImageUrl(), 300, 200, true, true, true);
        if (!img.isError()) {
          ImageView iv = new ImageView(img);
          iv.setFitWidth(300);
          iv.setFitHeight(200);
          iv.setPreserveRatio(false);
          imgArea.getChildren().add(iv);
        }
      } catch (Exception ignored) {}
    }
    if (imgArea.getChildren().isEmpty()) {
      String init = p.getName() != null ? p.getName().substring(0, 1).toUpperCase() : "?";
      Label ph = new Label(init);
      ph.getStyleClass().add("modal-image-placeholder");
      imgArea.getChildren().add(ph);
    }

    VBox pyramidBox = new VBox(8);
    pyramidBox.getStyleClass().add("modal-pyramid");
    Label pyrTitle = new Label("OLFACTORY PYRAMID");
    pyrTitle.getStyleClass().add("modal-section-title");
    pyramidBox.getChildren().add(pyrTitle);

    boolean hasV11Notes = (p.getTopNotes() != null && !p.getTopNotes().isBlank())
        || (p.getHeartNotes() != null && !p.getHeartNotes().isBlank())
        || (p.getBaseNotes() != null && !p.getBaseNotes().isBlank());

    if (hasV11Notes) {
      if (p.getTopNotes() != null && !p.getTopNotes().isBlank())
        pyramidBox.getChildren().add(createNoteRow("TOP", p.getTopNotes(), "#E2A998"));
      if (p.getHeartNotes() != null && !p.getHeartNotes().isBlank())
        pyramidBox.getChildren().add(createNoteRow("HEART", p.getHeartNotes(), "#b18db8"));
      if (p.getBaseNotes() != null && !p.getBaseNotes().isBlank())
        pyramidBox.getChildren().add(createNoteRow("BASE", p.getBaseNotes(), "#78A0A0"));
    } else {
      String[] compNotes = similarityService.loadCompositionNotes(p.getId());
      if (compNotes[0] != null) pyramidBox.getChildren().add(createNoteRow("TOP", compNotes[0], "#E2A998"));
      if (compNotes[1] != null) pyramidBox.getChildren().add(createNoteRow("HEART", compNotes[1], "#b18db8"));
      if (compNotes[2] != null) pyramidBox.getChildren().add(createNoteRow("BASE", compNotes[2], "#78A0A0"));
      if (compNotes[0] == null && compNotes[1] == null && compNotes[2] == null) {
        Label noData = new Label("No pyramid data available");
        noData.setStyle("-fx-font-size: 17; -fx-font-style: italic; -fx-text-fill: #B0ADA8;");
        pyramidBox.getChildren().add(noData);
      }
    }

    HBox metaRow = new HBox(16);
    metaRow.setAlignment(Pos.CENTER_LEFT);
    metaRow.getStyleClass().add("modal-meta");
    if (p.getSeason() != null)      metaRow.getChildren().add(createMetaChip("Season", capitalize(p.getSeason())));
    if (p.getProjection() != null)   metaRow.getChildren().add(createMetaChip("Projection", capitalize(p.getProjection())));
    if (p.getLongevityScore() != null) metaRow.getChildren().add(createMetaChip("Longevity", p.getLongevityScore() + "/10"));
    if (p.getGenderProfile() != null)  metaRow.getChildren().add(createMetaChip("Gender", capitalize(p.getGenderProfile())));
    if (p.getPrice() != null)         metaRow.getChildren().add(createMetaChip("Price", "$" + String.format("%.0f", p.getPrice())));

    StringBuilder descText = new StringBuilder();
    if (p.getDescription() != null && !p.getDescription().isBlank()) {
      descText.append(p.getDescription());
    }
    if (p.getOlfactoryFamily() != null && !p.getOlfactoryFamily().isBlank()) {
      if (descText.length() > 0) descText.append("\n\n");
      descText.append("olfactory family: ").append(p.getOlfactoryFamily());
    }
    if (p.getSeason() != null && !p.getSeason().isBlank()) {
      if (descText.length() > 0) descText.append("  ·  ");
      descText.append("best worn in ").append(p.getSeason());
    }
    if (p.getGenderProfile() != null && !p.getGenderProfile().isBlank()) {
      if (descText.length() > 0) descText.append("  ·  ");
      descText.append("gender profile: ").append(p.getGenderProfile());
    }

    modalContent.getChildren().addAll(headerRow, name, brand, imgArea, pyramidBox, metaRow);
    if (descText.length() > 0) {
      Label desc = new Label(descText.toString());
      desc.getStyleClass().add("modal-desc");
      desc.setWrapText(true);
      modalContent.getChildren().add(desc);
    }

    Button similarBtn = new Button("Similar to My Formula?");
    similarBtn.getStyleClass().add("btn-similar");
    similarBtn.setOnAction(e -> findSimilar(p));
    VBox.setMargin(similarBtn, new Insets(8, 0, 0, 0));
    modalContent.getChildren().add(similarBtn);

    modalOverlay.setVisible(true);
    modalOverlay.setManaged(true);
  }

  private void hideModal() {
    modalOverlay.setVisible(false);
    modalOverlay.setManaged(false);
  }

  private HBox createNoteRow(String phase, String notes, String color) {
    HBox row = new HBox(8);
    row.setAlignment(Pos.CENTER_LEFT);
    Rectangle dot = new Rectangle(4, 14);
    dot.setFill(Color.web(color));
    dot.setArcWidth(2);
    dot.setArcHeight(2);
    Label phaseLabel = new Label(phase);
    phaseLabel.getStyleClass().add("modal-phase-label");
    phaseLabel.setPrefWidth(50);
    Label notesLabel = new Label(notes);
    notesLabel.getStyleClass().add("modal-phase-notes");
    notesLabel.setWrapText(true);
    row.getChildren().addAll(dot, phaseLabel, notesLabel);
    return row;
  }

  private VBox createMetaChip(String title, String value) {
    VBox chip = new VBox(2);
    chip.setAlignment(Pos.CENTER);
    chip.getStyleClass().add("modal-meta-chip");
    Label t = new Label(title);
    t.getStyleClass().add("modal-meta-title");
    Label v = new Label(value);
    v.getStyleClass().add("modal-meta-value");
    chip.getChildren().addAll(t, v);
    return chip;
  }

  private void findSimilar(Perfume p) {
    Set<String> composerNotes = new HashSet<>(compositionState.getCurrentNoteNames());
    if (composerNotes.isEmpty()) {
      Label empty = new Label("Add notes to your composition first to compare.");
      empty.getStyleClass().add("modal-similar-result");
      empty.setStyle("-fx-padding: 8 0 0 0; -fx-font-size: 17; -fx-font-style: italic; -fx-text-fill: #B0ADA8;");
      modalContent.getChildren().add(empty);
      return;
    }

    Set<String> perfumeNotes = similarityService.collectPerfumeNotes(p);
    if (perfumeNotes.isEmpty()) {
      Label empty = new Label("No note data available for this perfume.");
      empty.getStyleClass().add("modal-similar-result");
      empty.setStyle("-fx-padding: 8 0 0 0; -fx-font-size: 17; -fx-font-style: italic; -fx-text-fill: #B0ADA8;");
      modalContent.getChildren().add(empty);
      return;
    }

    int matchPct = similarityService.calculateMatchPercentage(composerNotes, perfumeNotes);

    HBox result = new HBox(12);
    result.setAlignment(Pos.CENTER_LEFT);
    result.getStyleClass().add("modal-similar-result");
    result.setStyle("-fx-padding: 8 0 0 0;");

    Label pctLabel = new Label(matchPct + "%");
    pctLabel.setStyle("-fx-font-size: 30; -fx-font-weight: 700; -fx-font-family: 'Cormorant Garamond',serif;"
        + (matchPct >= 60 ? " -fx-text-fill: #5A9E8F;"
        : matchPct >= 30 ? " -fx-text-fill: #C8954A;" : " -fx-text-fill: #D9534F;"));

    Label matchLabel = new Label("match with your formula");
    matchLabel.setStyle("-fx-font-size: 17; -fx-text-fill: #6B6762; -fx-font-style: italic;");

    result.getChildren().addAll(pctLabel, matchLabel);
    modalContent.getChildren().add(result);
  }

  private String capitalize(String s) {
    if (s == null || s.isBlank()) return s;
    return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
  }
}
