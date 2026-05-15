package velmora.composer.ui.controller;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import velmora.composer.model.Note;
import velmora.composer.model.NoteType;
import velmora.composer.repository.NoteRepository;
import velmora.composer.state.CompositionState;
import velmora.composer.ui.UserSession;
import velmora.composer.ui.ViewManager;

@Component
@RequiredArgsConstructor
public class VaultController {

  private final NoteRepository noteRepository;
  private final UserSession userSession;
  private final ViewManager viewManager;
  private final CompositionState compositionState;

  @FXML private TextField searchField;
  @FXML private Button filterAll;
  @FXML private Button filterTop;
  @FXML private Button filterHeart;
  @FXML private Button filterBase;
  @FXML private Slider intensitySlider;
  @FXML private Label intensityLabel;
  @FXML private ComboBox<String> categoryFilter;
  @FXML private TilePane cardGrid;
  @FXML private ScrollPane scrollPane;

  @FXML private Region panelGap;
  @FXML private VBox detailPanel;
  @FXML private Label detailName;
  @FXML private Label detailCategory;
  @FXML private Label detailDescription;
  @FXML private Label detailPhase;
  @FXML private Label detailOrigin;
  @FXML private Label detailLongevity;
  @FXML private HBox detailIntensityDots;
  @FXML private StackPane detailImageContainer;
  @FXML private Label detailImagePlaceholder;
  @FXML private Rectangle tlTop;
  @FXML private Rectangle tlHeart;
  @FXML private Rectangle tlBase;

  private final ObservableList<Note> allNotes = FXCollections.observableArrayList();
  private FilteredList<Note> filteredNotes;
  private Note selectedNote;
  private NoteType activeFilter = null;

  @FXML
  public void initialize() {
    loadNotes();
    setupFilters();
    setupSearch();
    setupIntensitySlider();
    setupCategoryFilter();
  }

  private void loadNotes() {
    allNotes.setAll(noteRepository.findAll());
    filteredNotes = new FilteredList<>(allNotes, p -> true);
    renderCards();
  }

  private void setupFilters() {
    filterAll.setOnAction(e -> setPhaseFilter(null));
    filterTop.setOnAction(e -> setPhaseFilter(NoteType.TOP));
    filterHeart.setOnAction(e -> setPhaseFilter(NoteType.HEART));
    filterBase.setOnAction(e -> setPhaseFilter(NoteType.BASE));
  }

  private void setPhaseFilter(NoteType type) {
    activeFilter = type;
    updateActiveFilterButton();
    applyFilters();
  }

  private void updateActiveFilterButton() {
    for (var btn : new Button[]{filterAll, filterTop, filterHeart, filterBase}) {
      btn.getStyleClass().remove("filter-btn-active");
      btn.getStyleClass().add("filter-btn");
    }
    Button target = activeFilter == null ? filterAll
        : activeFilter == NoteType.TOP ? filterTop
        : activeFilter == NoteType.HEART ? filterHeart
        : filterBase;
    target.getStyleClass().remove("filter-btn");
    target.getStyleClass().add("filter-btn-active");
  }

  private void setupSearch() {
    searchField.textProperty().addListener((obs, old, val) -> applyFilters());
  }

  private void setupIntensitySlider() {
    intensitySlider.valueProperty().addListener((obs, old, val) -> {
      intensityLabel.setText(String.format("%.0f+", val));
      applyFilters();
    });
  }

  private void setupCategoryFilter() {
    List<String> categories = allNotes.stream()
        .map(Note::getCategory)
        .filter(Objects::nonNull)
        .distinct()
        .sorted()
        .collect(Collectors.toList());
    categories.add(0, "All");
    categoryFilter.setItems(FXCollections.observableArrayList(categories));
    categoryFilter.getSelectionModel().select("All");
    categoryFilter.getSelectionModel().selectedItemProperty()
        .addListener((obs, old, val) -> applyFilters());
  }

  private void applyFilters() {
    String query = searchField.getText().toLowerCase().trim();
    int minIntensity = (int) intensitySlider.getValue();
    String category = categoryFilter.getValue();

    filteredNotes.setPredicate(note -> {
      if (!query.isEmpty() && !note.getName().toLowerCase().contains(query)) {
        return false;
      }
      if (activeFilter != null && note.getType() != activeFilter) {
        return false;
      }
      if (note.getIntensity() == null || note.getIntensity() < minIntensity) {
        return false;
      }
      if (category != null && !category.equals("All")
          && !category.equalsIgnoreCase(note.getCategory())) {
        return false;
      }
      return true;
    });
    renderCards();
  }

  private void renderCards() {
    cardGrid.getChildren().clear();
    for (Note note : filteredNotes) {
      cardGrid.getChildren().add(createCard(note));
    }
  }

  private VBox createCard(Note note) {
    VBox card = new VBox();
    card.getStyleClass().add("note-card");
    card.setPrefWidth(250);
    card.setPrefHeight(285);

    StackPane imageArea = new StackPane();
    imageArea.setPrefHeight(148);
    imageArea.getStyleClass().add("card-image-container");

    Rectangle imageAreaClip = new Rectangle();
    imageAreaClip.setArcWidth(18);
    imageAreaClip.setArcHeight(18);
    imageAreaClip.widthProperty().bind(imageArea.widthProperty());
    imageAreaClip.heightProperty().bind(imageArea.heightProperty());
    imageArea.setClip(imageAreaClip);

    String typeClass = note.getType() == NoteType.TOP ? "card-image-top"
        : note.getType() == NoteType.HEART ? "card-image-heart"
        : "card-image-base";
    imageArea.getStyleClass().add(typeClass);

    Label emoji = new Label(getNoteEmoji(note));
    emoji.setStyle("-fx-font-size: 40; -fx-text-fill: " + getNoteColor(note) + ";");

    if (note.getImagePath() != null && !note.getImagePath().isBlank()) {
      try {
        var imgStream = getClass().getResourceAsStream("/images/notes/" + note.getImagePath());
        if (imgStream != null) {
          ImageView iv = new ImageView(new Image(imgStream));
          iv.setFitWidth(250);
          iv.setFitHeight(148);
          iv.setPreserveRatio(false);
          iv.setSmooth(true);
          iv.setCache(true);
          Rectangle cardClip = new Rectangle(250, 148);
          cardClip.setArcWidth(18);
          cardClip.setArcHeight(18);
          iv.setClip(cardClip);
          imageArea.getChildren().add(iv);
        } else {
          imageArea.getChildren().add(emoji);
        }
      } catch (Exception e) {
        imageArea.getChildren().add(emoji);
      }
    } else {
      imageArea.getChildren().add(emoji);
    }

    VBox overlay = new VBox();
    overlay.getStyleClass().add("card-image-overlay");
    VBox.setVgrow(overlay, Priority.ALWAYS);
    Region spacer = new Region();
    VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
    HBox badges = new HBox(6);
    badges.setPadding(new Insets(0, 12, 10, 12));
    Label phaseBadge = new Label(note.getType() != null ? note.getType().name() : "—");
    phaseBadge.getStyleClass().addAll("card-badge",
        note.getType() == NoteType.TOP ? "badge-top"
            : note.getType() == NoteType.HEART ? "badge-heart" : "badge-base");
    Label catBadge = new Label(note.getCategory() != null ? note.getCategory().toUpperCase() : "");
    catBadge.getStyleClass().add("badge-category");
    badges.getChildren().addAll(phaseBadge, catBadge);
    overlay.getChildren().addAll(spacer, badges);
    imageArea.getChildren().add(overlay);

    VBox body = new VBox();
    body.getStyleClass().add("card-body");
    VBox.setVgrow(body, Priority.ALWAYS);

    Label name = new Label(note.getName());
    name.getStyleClass().add("card-name");

    Region bodySpacer1 = new Region();
    VBox.setVgrow(bodySpacer1, javafx.scene.layout.Priority.ALWAYS);

    Label desc = new Label(note.getDescription() != null && note.getDescription().length() > 60
        ? note.getDescription().substring(0, 57) + "..."
        : (note.getDescription() != null ? note.getDescription() : ""));
    desc.getStyleClass().add("card-description");
    desc.setWrapText(true);
    desc.setMaxHeight(36);

    Region bodySpacer2 = new Region();
    VBox.setVgrow(bodySpacer2, javafx.scene.layout.Priority.ALWAYS);

    HBox dots = new HBox(4);
    dots.getStyleClass().add("card-intensity");
    int intensity = note.getIntensity() != null ? note.getIntensity() : 0;
    for (int i = 0; i < 10; i++) {
      Label dot = new Label();
      dot.setStyle(String.format(
          "-fx-background-radius: 50; -fx-min-width: 7; -fx-min-height: 7; "
              + "-fx-max-width: 7; -fx-max-height: 7; -fx-background-color: %s;",
          i < intensity ? "#E2A998" : "#E8E4DE"));
      dots.getChildren().add(dot);
    }

    body.getChildren().addAll(name, bodySpacer1, desc, bodySpacer2, dots);
    card.getChildren().addAll(imageArea, body);

    card.setOnMouseClicked(e -> {
      if (e.getButton() == MouseButton.PRIMARY) {
        showDetail(note);
      }
    });

    return card;
  }

  private void showDetail(Note note) {
    selectedNote = note;
    detailPanel.setVisible(true);
    detailPanel.setManaged(true);
    panelGap.setVisible(true);
    panelGap.setManaged(true);
    cardGrid.setPrefColumns(3);

    detailName.setText(note.getName());
    detailCategory.setText(note.getCategory() != null ? note.getCategory().toUpperCase() : "");
    detailDescription.setText(note.getDescription() != null ? note.getDescription() : "No description");
    detailPhase.setText(note.getType() != null ? note.getType().name() : "—");
    detailOrigin.setText(note.getOrigin() != null ? note.getOrigin() : "Unknown");
    detailLongevity.setText(note.getLongevityHours() != null ? note.getLongevityHours() + "h" : "—");

    detailIntensityDots.getChildren().clear();
    int intensity = note.getIntensity() != null ? note.getIntensity() : 0;
    for (int i = 0; i < 10; i++) {
      Label dot = new Label();
      dot.setStyle(String.format(
          "-fx-background-radius: 50; -fx-min-width: 10; -fx-min-height: 10; "
              + "-fx-max-width: 10; -fx-max-height: 10; -fx-background-color: %s;",
          i < intensity ? "#E2A998" : "#E8E4DE"));
      detailIntensityDots.getChildren().add(dot);
    }

    double timelineWidth = 200;
    tlTop.setWidth(timelineWidth * 0.15);
    tlHeart.setWidth(timelineWidth * 0.35);
    tlBase.setWidth(timelineWidth * 0.50);

    if (note.getImagePath() != null && !note.getImagePath().isBlank()) {
      try {
        var imgStream = getClass().getResourceAsStream("/images/notes/" + note.getImagePath());
        if (imgStream != null) {
          ImageView iv = new ImageView(new Image(imgStream));
          iv.setFitWidth(336);
          iv.setFitHeight(168);
          iv.setPreserveRatio(false);
          iv.setSmooth(true);
          iv.setCache(true);
          Rectangle detailClip = new Rectangle(336, 168);
          detailClip.setArcWidth(20);
          detailClip.setArcHeight(20);
          iv.setClip(detailClip);
          detailImageContainer.getChildren().clear();
          detailImageContainer.getChildren().add(iv);
          StackPane.setAlignment(iv, Pos.CENTER);
        } else {
          detailImageContainer.getChildren().clear();
          detailImageContainer.getChildren().add(detailImagePlaceholder);
        }
      } catch (Exception e) {
        detailImageContainer.getChildren().clear();
        detailImageContainer.getChildren().add(detailImagePlaceholder);
      }
    } else {
      detailImageContainer.getChildren().clear();
      detailImageContainer.getChildren().add(detailImagePlaceholder);
    }
  }

  @FXML
  public void closeDetail() {
    detailPanel.setVisible(false);
    detailPanel.setManaged(false);
    panelGap.setVisible(false);
    panelGap.setManaged(false);
    cardGrid.setPrefColumns(4);
    selectedNote = null;
  }

  @FXML
  public void handleAddToComposition() {
    if (selectedNote == null) return;
    compositionState.getCurrentNoteIds().add(selectedNote.getId());
    compositionState.getCurrentNoteNames().add(selectedNote.getName());
    viewManager.showMain();
  }

  @FXML
  public void handleBack() {
    viewManager.showMain();
  }

  @FXML
  public void handleSettings() {
    viewManager.showSettings();
  }

  private String getNoteEmoji(Note note) {
    if (note.getType() == NoteType.TOP) return "\u2726";
    if (note.getType() == NoteType.HEART) return "\u2661";
    return "\u2726";
  }

  private String getNoteColor(Note note) {
    if (note.getType() == NoteType.TOP) return "#C4A882";
    if (note.getType() == NoteType.HEART) return "#D4A89A";
    return "#A88878";
  }
}
