package velmora.composer.ui.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
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
import velmora.composer.model.Composition;
import velmora.composer.model.CompositionItem;
import velmora.composer.model.Note;
import velmora.composer.model.NoteType;
import velmora.composer.repository.CompositionRepository;
import velmora.composer.repository.NoteRepository;
import velmora.composer.state.CompositionState;
import velmora.composer.ui.UserSession;
import velmora.composer.ui.ViewManager;

@Component
@RequiredArgsConstructor
public class CommunityController {

  private final CompositionRepository compositionRepository;
  private final NoteRepository noteRepository;
  private final CompositionState compositionState;
  private final UserSession userSession;
  private final ViewManager viewManager;

  @FXML private FlowPane communityGrid;
  @FXML private Label communityCount;
  @FXML private TextField searchField;
  @FXML private ComboBox<String> familyFilter;
  @FXML private TextField authorField;
  @FXML private Slider harmonySlider;
  @FXML private Label harmonyLabel;
  @FXML private ComboBox<String> dateFilter;
  @FXML private StackPane detailOverlay;
  @FXML private VBox detailContent;

  private List<Composition> allCompositions;

  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH);
  private static final DateTimeFormatter SHORT_DATE = DateTimeFormatter.ofPattern("MMM dd, HH:mm", Locale.ENGLISH);

  @FXML
  public void initialize() {
    harmonySlider.valueProperty().addListener((obs, o, n) -> {
      harmonyLabel.setText(String.format("%.0f%%+", n));
      applyFilters();
    });
    searchField.textProperty().addListener((obs, o, n) -> applyFilters());
    authorField.textProperty().addListener((obs, o, n) -> applyFilters());
    familyFilter.valueProperty().addListener((obs, o, n) -> applyFilters());
    dateFilter.valueProperty().addListener((obs, o, n) -> applyFilters());
    dateFilter.setItems(FXCollections.observableArrayList(
        "All Time", "Today", "This Week", "This Month"));
    dateFilter.getSelectionModel().select("All Time");
    loadCommunity();
  }

  @FXML
  private void handleBack() {
    viewManager.showCatalog();
  }

  @FXML
  private void handleRefresh() {
    loadCommunity();
  }

  private void loadCommunity() {
    Task<List<Composition>> task = new Task<>() {
      @Override
      protected List<Composition> call() {
        return compositionRepository.findByIsPublicTrueOrderByUpdatedAtDesc();
      }
    };
    task.setOnSucceeded(e -> {
      allCompositions = task.getValue();
      populateFamilyFilter();
      applyFilters();
    });
    task.setOnFailed(e -> System.err.println("[COMMUNITY] Load failed: " + task.getException().getMessage()));
    new Thread(task).start();
  }

  private void populateFamilyFilter() {
    Set<String> families = allCompositions.stream()
        .flatMap(c -> c.getItems() != null ? c.getItems().stream() : java.util.stream.Stream.empty())
        .filter(i -> i.getNote() != null && i.getNote().getCategory() != null)
        .map(i -> i.getNote().getCategory())
        .collect(Collectors.toSet());
    familyFilter.setItems(FXCollections.observableArrayList(
        families.stream().sorted().collect(Collectors.toList())));
  }

  private void applyFilters() {
    if (allCompositions == null) return;
    String search = searchField.getText().trim().toLowerCase();
    String family = familyFilter.getValue();
    String author = authorField.getText().trim().toLowerCase();
    double minHarmony = harmonySlider.getValue();
    String date = dateFilter.getValue();

    List<Composition> filtered = allCompositions.stream()
        .filter(c -> search.isEmpty() || (c.getName() != null && c.getName().toLowerCase().contains(search)))
        .filter(c -> family == null || family.isBlank() || hasFamily(c, family))
        .filter(c -> author.isEmpty() || (c.getUser() != null && c.getUser().getNickname() != null
            && c.getUser().getNickname().toLowerCase().contains(author)))
        .filter(c -> minHarmony <= 1 || computeHarmonyScore(c) >= minHarmony)
        .filter(c -> matchesDate(c, date))
        .sorted(Comparator.comparing(Composition::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
        .collect(Collectors.toList());

    communityCount.setText(filtered.size() + " formulas");
    renderCards(filtered);
  }

  private boolean hasFamily(Composition c, String family) {
    if (c.getItems() == null) return false;
    return c.getItems().stream()
        .filter(i -> i.getNote() != null && i.getNote().getCategory() != null)
        .anyMatch(i -> i.getNote().getCategory().equalsIgnoreCase(family));
  }

  private boolean matchesDate(Composition c, String date) {
    if (date == null || "All Time".equals(date)) return true;
    if (c.getUpdatedAt() == null) return false;
    LocalDate updated = c.getUpdatedAt().toLocalDate();
    LocalDate today = LocalDate.now();
    return switch (date) {
      case "Today" -> updated.equals(today);
      case "This Week" -> updated.isAfter(today.minusDays(7));
      case "This Month" -> updated.isAfter(today.minusDays(30));
      default -> true;
    };
  }

  private double computeHarmonyScore(Composition c) {
    if (c.getItems() == null || c.getItems().isEmpty()) return 0;
    int totalPct = c.getItems().stream()
        .filter(i -> i.getPercentage() != null)
        .mapToInt(CompositionItem::getPercentage)
        .sum();
    double balancePenalty = Math.abs(totalPct - 100);
    long topCount = c.getItems().stream()
        .filter(i -> i.getNote() != null && i.getNote().getType() == NoteType.TOP)
        .count();
    long heartCount = c.getItems().stream()
        .filter(i -> i.getNote() != null && i.getNote().getType() == NoteType.HEART)
        .count();
    long baseCount = c.getItems().stream()
        .filter(i -> i.getNote() != null && i.getNote().getType() == NoteType.BASE)
        .count();
    long total = topCount + heartCount + baseCount;
    if (total == 0) return 0;
    boolean hasAllLevels = topCount > 0 && heartCount > 0 && baseCount > 0;
    double pyramidBonus = hasAllLevels ? 15 : 0;
    return Math.max(0, Math.min(100, 100 - balancePenalty * 2 + pyramidBonus));
  }

  private String getDominantFamily(Composition c) {
    if (c.getItems() == null) return null;
    return c.getItems().stream()
        .filter(i -> i.getNote() != null && i.getNote().getCategory() != null)
        .collect(Collectors.groupingBy(i -> i.getNote().getCategory(), Collectors.counting()))
        .entrySet().stream()
        .max(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey)
        .orElse(null);
  }

  private void renderCards(List<Composition> compositions) {
    communityGrid.getChildren().clear();
    if (compositions.isEmpty()) {
      VBox empty = new VBox(4);
      empty.setAlignment(Pos.CENTER);
      empty.setPadding(new Insets(80, 0, 0, 0));
      Label title = new Label("No community formulas found");
      title.setStyle("-fx-font-family: 'Cormorant Garamond', 'Georgia', serif; -fx-font-size: 26; -fx-text-fill: #B0ADA8; -fx-font-weight: 600;");
      Label sub = new Label("Adjust filters or check back later");
      sub.setStyle("-fx-font-size: 15; -fx-text-fill: #C4BFB9; -fx-font-style: italic; -fx-padding: 8 0 0 0;");
      empty.getChildren().addAll(title, sub);
      communityGrid.getChildren().add(empty);
      return;
    }
    for (Composition comp : compositions) {
      communityGrid.getChildren().add(createCommunityCard(comp));
    }
  }

  private Node createCommunityCard(Composition comp) {
    VBox card = new VBox(10);
    card.setPrefWidth(320);
    card.getStyleClass().add("community-card");

    HBox header = new HBox(10);
    header.setAlignment(Pos.CENTER_LEFT);

    Label name = new Label(comp.getName() != null ? comp.getName() : "Untitled");
    name.getStyleClass().add("community-card-name");

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    int noteCount = comp.getItems() != null ? comp.getItems().size() : 0;
    Label notesLabel = new Label(noteCount + " notes");
    notesLabel.getStyleClass().add("community-card-notes");

    header.getChildren().addAll(name, spacer, notesLabel);

    HBox meta = new HBox(12);
    meta.setAlignment(Pos.CENTER_LEFT);
    meta.setSpacing(16);

    String userText = comp.getUser() != null && comp.getUser().getNickname() != null
        ? "by " + comp.getUser().getNickname() : "";
    Label userLabel = new Label(userText);
    userLabel.getStyleClass().add("community-card-user");

    String dateText = comp.getUpdatedAt() != null ? comp.getUpdatedAt().format(DATE_FMT) : "";
    Label date = new Label(dateText);
    date.getStyleClass().add("community-card-date");

    String family = getDominantFamily(comp);
    Label familyLabel = new Label(family != null ? family : "");
    familyLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #B18DB8; -fx-font-weight: 600;");

    double harmony = computeHarmonyScore(comp);
    Label harmonyLabel = new Label(String.format("%.0f%%", harmony));
    String hColor = harmony >= 80 ? "#5A9E8F" : harmony >= 60 ? "#C8954A" : "#D9534F";
    harmonyLabel.setStyle("-fx-font-size: 14; -fx-font-weight: 700; -fx-text-fill: " + hColor + ";");

    meta.getChildren().addAll(userLabel, date, familyLabel, harmonyLabel);

    HBox actions = new HBox(8);
    actions.setAlignment(Pos.CENTER_RIGHT);

    Button openBtn = new Button("Open in Composer");
    openBtn.getStyleClass().add("community-card-open-btn");
    openBtn.setOnAction(e -> openInComposer(comp));

    Button viewBtn = new Button("View Details");
    viewBtn.setStyle("-fx-font-size: 13; -fx-font-weight: 600; -fx-text-fill: #6B6560; "
        + "-fx-background-color: transparent; -fx-background-radius: 6; -fx-padding: 6 14; "
        + "-fx-border-color: #D8D0C6; -fx-border-radius: 6; -fx-border-width: 1; -fx-cursor: hand;");
    viewBtn.setOnAction(e -> showDetail(comp));

    actions.getChildren().addAll(viewBtn, openBtn);
    meta.getChildren().add(actions);

    card.getChildren().addAll(header, meta);

    card.setOnMouseClicked(e -> {
      if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
        showDetail(comp);
      }
    });

    return card;
  }

  private void openInComposer(Composition comp) {
    compositionState.setFormulaName(comp.getName());
    compositionState.setEditCompositionId(comp.getId());
    if (comp.getItems() != null) {
      compositionState.setCurrentNoteIds(
          comp.getItems().stream()
              .filter(i -> i.getNote() != null)
              .map(i -> i.getNote().getId())
              .collect(Collectors.toList())
      );
      compositionState.setCurrentNoteNames(
          comp.getItems().stream()
              .filter(i -> i.getNote() != null)
              .map(i -> i.getNote().getName() != null ? i.getNote().getName() : "")
              .collect(Collectors.toList())
      );
    }
    compositionState.setReadOnly(
        userSession.getUserId() == null
            || comp.getUser() == null
            || !comp.getUser().getId().equals(userSession.getUserId())
    );
    viewManager.showMain();
  }

  private void showDetail(Composition comp) {
    detailContent.getChildren().clear();
    detailOverlay.setVisible(true);
    detailOverlay.setManaged(true);

    VBox content = new VBox(0);

    HBox header = new HBox(12);
    header.getStyleClass().add("detail-header");
    Label title = new Label(comp.getName() != null ? comp.getName() : "Untitled");
    title.setStyle("-fx-font-family: 'Cormorant Garamond', 'Georgia', serif; -fx-font-size: 22; -fx-font-weight: 700; -fx-text-fill: #2D2A24;");
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    Label closeBtn = new Label("\u2715");
    closeBtn.setStyle("-fx-font-size: 20; -fx-text-fill: #B0ADA8; -fx-cursor: hand; -fx-padding: 4 8;");
    closeBtn.setOnMouseClicked(e -> closeDetail());
    header.getChildren().addAll(title, spacer, closeBtn);
    content.getChildren().add(header);

    String userText = comp.getUser() != null && comp.getUser().getNickname() != null
        ? "by " + comp.getUser().getNickname() : "";
    if (!userText.isEmpty()) {
      Label author = new Label(userText);
      author.setStyle("-fx-font-size: 16; -fx-text-fill: #B18DB8; -fx-font-weight: 600; -fx-font-style: italic; -fx-padding: 0 0 8 0;");
      content.getChildren().add(author);
    }

    FlowPane statsGrid = new FlowPane(8, 8);
    statsGrid.setPadding(new Insets(4, 0, 8, 0));

    String dateText = comp.getUpdatedAt() != null ? comp.getUpdatedAt().format(SHORT_DATE) : "\u2014";
    statsGrid.getChildren().add(createStatBox("Created", dateText));

    int noteCount = comp.getItems() != null ? comp.getItems().size() : 0;
    statsGrid.getChildren().add(createStatBox("Notes", String.valueOf(noteCount)));

    String family = getDominantFamily(comp);
    if (family != null) {
      statsGrid.getChildren().add(createStatBox("Top Family", family));
    }

    double harmony = computeHarmonyScore(comp);
    statsGrid.getChildren().add(createStatBox("Harmony", String.format("%.0f%%", harmony)));

    content.getChildren().add(statsGrid);

    Rectangle div1 = new Rectangle();
    div1.setHeight(1);
    div1.setFill(Color.web("rgba(177,141,184,0.15)"));
    content.getChildren().add(div1);

    if (comp.getItems() != null && !comp.getItems().isEmpty()) {
      Label notesTitle = new Label("OLFACTORY PYRAMID");
      notesTitle.setStyle("-fx-font-size: 14; -fx-font-weight: 700; -fx-text-fill: #A8A29E; -fx-letter-spacing: 1.5; -fx-padding: 8 0 4 0;");
      content.getChildren().add(notesTitle);

      List<CompositionItem> topItems = comp.getItems().stream()
          .filter(i -> i.getNote() != null && i.getNote().getType() == NoteType.TOP)
          .collect(Collectors.toList());
      List<CompositionItem> heartItems = comp.getItems().stream()
          .filter(i -> i.getNote() != null && i.getNote().getType() == NoteType.HEART)
          .collect(Collectors.toList());
      List<CompositionItem> baseItems = comp.getItems().stream()
          .filter(i -> i.getNote() != null && i.getNote().getType() == NoteType.BASE)
          .collect(Collectors.toList());

      if (!topItems.isEmpty()) {
        content.getChildren().add(createNotePyramidRow("TOP", topItems, "#E2A998"));
      }
      if (!heartItems.isEmpty()) {
        content.getChildren().add(createNotePyramidRow("HEART", heartItems, "#b18db8"));
      }
      if (!baseItems.isEmpty()) {
        content.getChildren().add(createNotePyramidRow("BASE", baseItems, "#78A0A0"));
      }

      Rectangle div2 = new Rectangle();
      div2.setHeight(1);
      div2.setFill(Color.web("rgba(177,141,184,0.15)"));
      content.getChildren().add(div2);

      Label timelineTitle = new Label("COMPOSITION BREAKDOWN");
      timelineTitle.setStyle("-fx-font-size: 14; -fx-font-weight: 700; -fx-text-fill: #A8A29E; -fx-letter-spacing: 1.5; -fx-padding: 8 0 4 0;");
      content.getChildren().add(timelineTitle);

      int totalPct = comp.getItems().stream()
          .filter(i -> i.getPercentage() != null)
          .mapToInt(CompositionItem::getPercentage)
          .sum();
      content.getChildren().add(createBreakdownBar("Concentration", totalPct + "%", Math.min(totalPct, 100) / 100.0, "#B18DB8"));

      long families = comp.getItems().stream()
          .filter(i -> i.getNote() != null && i.getNote().getCategory() != null)
          .map(i -> i.getNote().getCategory())
          .distinct()
          .count();
      content.getChildren().add(createBreakdownBar("Families", String.valueOf(families), Math.min(families, 8) / 8.0, "#5A9E8F"));

      double topPct = topItems.stream().filter(i -> i.getPercentage() != null).mapToInt(CompositionItem::getPercentage).sum() / (double) Math.max(1, totalPct);
      content.getChildren().add(createBreakdownBar("Top Notes", String.format("%.0f%%", topPct * 100), topPct, "#E2A998"));

      double heartPct2 = heartItems.stream().filter(i -> i.getPercentage() != null).mapToInt(CompositionItem::getPercentage).sum() / (double) Math.max(1, totalPct);
      content.getChildren().add(createBreakdownBar("Heart Notes", String.format("%.0f%%", heartPct2 * 100), heartPct2, "#b18db8"));

      double basePct2 = baseItems.stream().filter(i -> i.getPercentage() != null).mapToInt(CompositionItem::getPercentage).sum() / (double) Math.max(1, totalPct);
      content.getChildren().add(createBreakdownBar("Base Notes", String.format("%.0f%%", basePct2 * 100), basePct2, "#78A0A0"));
    }

    HBox detailActions = new HBox(8);
    detailActions.setPadding(new Insets(12, 0, 0, 0));
    detailActions.setAlignment(Pos.CENTER_RIGHT);

    Button closeDetailBtn = new Button("Close");
    closeDetailBtn.setStyle("-fx-font-size: 14; -fx-font-weight: 600; -fx-text-fill: #6B6560; "
        + "-fx-background-color: transparent; -fx-background-radius: 6; -fx-padding: 8 16; "
        + "-fx-border-color: #D8D0C6; -fx-border-radius: 6; -fx-border-width: 1; -fx-cursor: hand;");
    closeDetailBtn.setOnAction(e -> closeDetail());

    Button openFromDetailBtn = new Button("Open in Composer");
    openFromDetailBtn.setStyle("-fx-font-size: 14; -fx-font-weight: 700; -fx-text-fill: white; "
        + "-fx-background-color: #B18DB8; -fx-background-radius: 6; -fx-padding: 8 16; -fx-cursor: hand;");
    openFromDetailBtn.setOnAction(e -> { closeDetail(); openInComposer(comp); });

    detailActions.getChildren().addAll(closeDetailBtn, openFromDetailBtn);
    content.getChildren().add(detailActions);

    ScrollPane scroll = new ScrollPane(content);
    scroll.setFitToWidth(true);
    scroll.setStyle("-fx-background-color: transparent; -fx-background-radius: 12;");
    scroll.getStyleClass().add("thin-scroll");

    detailContent.getChildren().add(scroll);
    detailOverlay.setOnMouseClicked(e -> {
      if (e.getTarget() == detailOverlay) closeDetail();
    });
  }

  private VBox createStatBox(String label, String value) {
    VBox box = new VBox(2);
    box.setPadding(new Insets(6, 12, 6, 12));
    box.setStyle("-fx-background-color: rgba(240,237,230,0.6); -fx-background-radius: 8;");
    Label val = new Label(value);
    val.setStyle("-fx-font-size: 16; -fx-font-weight: 700; -fx-text-fill: #2D2A24;");
    Label lbl = new Label(label.toUpperCase());
    lbl.setStyle("-fx-font-size: 11; -fx-font-weight: 700; -fx-text-fill: #A8A29E; -fx-letter-spacing: 1;");
    box.getChildren().addAll(val, lbl);
    return box;
  }

  private HBox createNotePyramidRow(String phase, List<CompositionItem> items, String color) {
    HBox row = new HBox(8);
    row.setAlignment(Pos.CENTER_LEFT);
    row.setPadding(new Insets(3, 0, 3, 0));
    Rectangle dot = new Rectangle(4, 14);
    dot.setFill(Color.web(color));
    dot.setArcWidth(2);
    dot.setArcHeight(2);
    Label phaseLabel = new Label(phase);
    phaseLabel.setStyle("-fx-font-size: 13; -fx-font-weight: 700; -fx-text-fill: #A8A29E; -fx-letter-spacing: 1; -fx-min-width: 50;");
    HBox chips = new HBox(6);
    chips.setAlignment(Pos.CENTER_LEFT);
    for (CompositionItem item : items) {
      if (item.getNote() != null) {
        String text = item.getNote().getName();
        if (item.getPercentage() != null) {
          text += " (" + item.getPercentage() + "%)";
        }
        Label chip = new Label(text);
        chip.setStyle("-fx-background-color: rgba(255,255,255,0.6); -fx-background-radius: 6; -fx-padding: 2 8;"
            + "-fx-font-size: 13; -fx-font-weight: 600; -fx-text-fill: #4B4B4B;"
            + "-fx-border-color: " + color + "40; -fx-border-radius: 6; -fx-border-width: 1;");
        chips.getChildren().add(chip);
      }
    }
    row.getChildren().addAll(dot, phaseLabel, chips);
    return row;
  }

  private HBox createBreakdownBar(String label, String value, double ratio, String color) {
    HBox row = new HBox(8);
    row.setAlignment(Pos.CENTER_LEFT);
    row.setPadding(new Insets(2, 0, 2, 0));
    Label lbl = new Label(label);
    lbl.setMinWidth(100);
    lbl.setStyle("-fx-font-size: 14; -fx-font-weight: 600; -fx-text-fill: #6B6560;");
    StackPane track = new StackPane();
    track.setPrefWidth(160);
    track.setPrefHeight(10);
    track.setStyle("-fx-background-color: rgba(234,230,223,0.5); -fx-background-radius: 5;");
    Rectangle fill = new Rectangle();
    fill.setHeight(10);
    fill.setArcWidth(5);
    fill.setArcHeight(5);
    fill.setWidth(160 * Math.min(1, Math.max(0, ratio)));
    fill.setFill(Color.web(color));
    StackPane.setAlignment(fill, Pos.CENTER_LEFT);
    track.getChildren().add(fill);
    Label val = new Label(value);
    val.setMinWidth(40);
    val.setStyle("-fx-font-size: 14; -fx-font-weight: 700; -fx-text-fill: " + color + "; -fx-alignment: center-right;");
    row.getChildren().addAll(lbl, track, val);
    return row;
  }

  private void closeDetail() {
    detailOverlay.setVisible(false);
    detailOverlay.setManaged(false);
  }
}