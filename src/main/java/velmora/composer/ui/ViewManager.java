package velmora.composer.ui;

import java.util.HashMap;
import java.util.Map;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import velmora.composer.model.Role;
import velmora.composer.state.CompositionState;

@Component
public class ViewManager {

  private final ApplicationContext context;
  private final UserSession userSession;
  private final CompositionState compositionState;

  private BorderPane root;
  private VBox sidebar;
  private boolean ready;
  private final Map<String, HBox> navItems = new HashMap<>();
  private Runnable beforeCatalogNavigate = () -> {};

  public ViewManager(ApplicationContext context, UserSession userSession, CompositionState compositionState) {
    this.context = context;
    this.userSession = userSession;
    this.compositionState = compositionState;
  }

  public void setBeforeCatalogNavigate(Runnable r) {
    this.beforeCatalogNavigate = r;
  }

  public void setPrimaryStage(Stage primaryStage) {
    root = new BorderPane();
    Scene scene = new Scene(root);

    String[] cssFiles = {
        "/css/theme.css", "/css/typography.css", "/css/buttons.css",
        "/css/cards.css", "/css/forms.css", "/css/sidebar.css",
        "/css/components.css", "/css/pages/auth.css", "/css/pages/composer.css",
        "/css/pages/settings.css", "/css/pages/vault.css",
        "/css/pages/history.css", "/css/pages/catalog.css"
    };
    for (String css : cssFiles) {
      var url = getClass().getResource(css);
      if (url != null) {
        scene.getStylesheets().add(url.toExternalForm());
      } else {
        System.err.println("[VIEW] CSS not found: " + css);
      }
    }

    sidebar = createSidebar();
    primaryStage.setScene(scene);
    primaryStage.setTitle("Velmora — Olfactory Lab");
    primaryStage.setMinWidth(1100);
    primaryStage.setMinHeight(750);
    primaryStage.centerOnScreen();
    ready = true;
  }

  private VBox createSidebar() {
    VBox sb = new VBox();
    sb.setPrefWidth(220.0);
    sb.getStyleClass().add("sidebar");

    HBox brandBlock = new HBox(14.0);
    brandBlock.setAlignment(Pos.CENTER_LEFT);
    brandBlock.getStyleClass().add("brand-block");

    ImageView logoView = new ImageView();
    logoView.setFitWidth(42);
    logoView.setFitHeight(42);
    logoView.setPreserveRatio(true);
    logoView.getStyleClass().add("brand-logo");
    try {
      var imgUrl = getClass().getResource("/images/logo.png");
      if (imgUrl != null) {
        Image img = new Image(imgUrl.toExternalForm());
        if (!img.isError()) {
          logoView.setImage(img);
        }
      }
    } catch (Exception ignored) {}

    VBox brandText = new VBox(0);
    brandText.setAlignment(Pos.CENTER_LEFT);
    Label title = new Label("VELMORA");
    title.getStyleClass().add("brand-title");
    Label subtitle = new Label("Olfactory Lab");
    subtitle.getStyleClass().add("brand-subtitle");
    brandText.getChildren().addAll(title, subtitle);

    brandBlock.getChildren().addAll(logoView, brandText);

    Rectangle div1 = new Rectangle(180.0, 1.0, javafx.scene.paint.Color.web("#DDD9D1"));
    div1.setStyle("-fx-translate-x: 20;");

    VBox molMap = new VBox(4.0);
    molMap.setStyle("-fx-padding: 16 16 0 16;");
    Label molLabel = new Label("MOLECULE MAP");
    molLabel.getStyleClass().add("sidebar-section-heading");
    HBox dots1 = new HBox(6.0);
    dots1.setAlignment(Pos.CENTER_LEFT);
    dots1.setStyle("-fx-padding: 4 4 0 4;");
    for (String c : new String[]{"rgba(214,148,120,0.7)", "rgba(177,141,184,0.6)", "rgba(109,168,158,0.6)", "rgba(214,148,120,0.5)"}) {
      Circle cr = new Circle(c.contains("7") ? 6 : c.contains("6") ? 5 : 4);
      cr.setStyle("-fx-fill: " + c + ";");
      dots1.getChildren().add(cr);
    }
    HBox dots2 = new HBox(6.0);
    dots2.setAlignment(Pos.CENTER_LEFT);
    dots2.setStyle("-fx-padding: 0 4;");
    for (String c : new String[]{"rgba(177,141,184,0.4)", "rgba(90,158,143,0.6)", "rgba(214,148,120,0.5)"}) {
      Circle cr = new Circle(c.contains("6") ? 6 : 4);
      cr.setStyle("-fx-fill: " + c + ";");
      dots2.getChildren().add(cr);
    }
    HBox dots3 = new HBox(6.0);
    dots3.setAlignment(Pos.CENTER_LEFT);
    dots3.setStyle("-fx-padding: 0 4 8 4;");
    for (String c : new String[]{"rgba(177,141,184,0.5)", "rgba(109,168,158,0.5)", "rgba(214,148,120,0.4)"}) {
      Circle cr = new Circle(c.contains("5") ? 5 : 4);
      cr.setStyle("-fx-fill: " + c + ";");
      dots3.getChildren().add(cr);
    }
    Label notesLabel = new Label("4 NOTES");
    notesLabel.setStyle("-fx-font-size: 16; -fx-text-fill: #C4BFB9; -fx-letter-spacing: 1;");
    dots3.getChildren().add(notesLabel);
    molMap.getChildren().addAll(molLabel, dots1, dots2, dots3);

    Rectangle div2 = new Rectangle(180.0, 1.0, javafx.scene.paint.Color.web("#DDD9D1"));
    div2.setStyle("-fx-translate-x: 20;");

    VBox nav = new VBox(2.0);
    nav.setStyle("-fx-padding: 12 12 0 12;");
    VBox.setVgrow(nav, Priority.ALWAYS);

    navItems.clear();
    nav.getChildren().add(createNavItem("✦", "Composer", "FORMULA WORKBENCH", "composer", () -> showMain()));
    nav.getChildren().add(createNavItem("▭", "Vault", "RAW MATERIALS", "vault", () -> showVault()));
    nav.getChildren().add(createNavItem("◈", "Catalog", "FRAGRANCE LIBRARY", "catalog", this::navigateToCatalog));
    nav.getChildren().add(createNavItem("◷", "History", "SESSIONS", "history", this::showHistory));

    HBox settingsItem = createNavItem("◎", "Settings", "PREFERENCES", "settings", () -> showSettings());
    nav.getChildren().add(settingsItem);

    HBox adminItem = createNavItem("⚙", "Admin", "NOTE DATABASE", "admin", () -> showAdmin());
    adminItem.setId("adminLink");
    adminItem.setVisible(userSession.getRole() == Role.ADMIN);
    adminItem.setManaged(userSession.getRole() == Role.ADMIN);
    nav.getChildren().add(adminItem);

    VBox bottom = new VBox();
    bottom.setStyle("-fx-padding: 0 0 24 0;");
    Rectangle div3 = new Rectangle(180.0, 1.0, javafx.scene.paint.Color.web("#DDD9D1"));
    div3.setStyle("-fx-translate-x: 20;");
    VBox dir = new VBox(3.0);
    dir.setStyle("-fx-padding: 14 12 0 12;");
    Label dirHeading = new Label("DIRECTORY");
    dirHeading.getStyleClass().add("dir-heading");
    dir.getChildren().add(dirHeading);
    String[][] dirEntries = {
        {"rgba(214,148,120,0.7)", "Bergamot"},
        {"rgba(177,141,184,0.7)", "Pink Pepper"},
        {"rgba(109,168,158,0.6)", "Neroli"},
        {"rgba(177,141,184,0.5)", "Iris Absolute"},
        {"rgba(214,148,120,0.5)", "Rose Damascena"},
        {"rgba(109,168,158,0.5)", "Jasmine"}
    };
    for (String[] e : dirEntries) {
      HBox row = new HBox(8.0);
      row.setAlignment(Pos.CENTER_LEFT);
      row.setStyle("-fx-padding: 3 8;");
      Circle cr = new Circle(4.0);
      cr.setStyle("-fx-fill: " + e[0] + ";");
      Label l = new Label(e[1]);
      l.getStyleClass().add("dir-item");
      row.getChildren().addAll(cr, l);
      dir.getChildren().add(row);
    }

    HBox status = new HBox(6.0);
    status.setAlignment(Pos.CENTER_LEFT);
    status.getStyleClass().add("status-bar");
    Label online = new Label("● ONLINE");
    online.getStyleClass().add("status-online");
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    Label help = new Label("?");
    help.getStyleClass().add("status-help");
    status.getChildren().addAll(online, spacer, help);

    bottom.getChildren().addAll(div3, dir, status);

    sb.getChildren().addAll(brandBlock, div1, molMap, div2, nav, bottom);
    return sb;
  }

  private HBox createNavItem(String icon, String label, String subtitle, String pageId, Runnable action) {
    HBox item = new HBox(12.0);
    item.setAlignment(Pos.CENTER_LEFT);
    item.getStyleClass().add("nav-item");
    item.setOnMouseClicked(e -> action.run());

    Label iconLbl = new Label(icon);
    iconLbl.getStyleClass().add("nav-icon");
    VBox textBox = new VBox(1.0);
    Label nameLbl = new Label(label);
    nameLbl.getStyleClass().add("nav-label");
    Label subLbl = new Label(subtitle);
    subLbl.getStyleClass().add("nav-subtitle");
    textBox.getChildren().addAll(nameLbl, subLbl);
    item.getChildren().addAll(iconLbl, textBox);

    navItems.put(pageId, item);
    return item;
  }

  private void setActiveNav(String pageId) {
    for (Map.Entry<String, HBox> e : navItems.entrySet()) {
      HBox item = e.getValue();
      if (e.getKey().equals(pageId)) {
        item.getStyleClass().add("nav-item-active");
        if (item.getChildren().isEmpty() || !(item.getChildren().get(0) instanceof Rectangle)) {
          Rectangle bar = new Rectangle(3, 28);
          bar.getStyleClass().add("nav-active-bar");
          item.getChildren().add(0, bar);
        }
        Label icon = (Label) item.getChildren().get(item.getChildren().size() == 3 ? 1 : 0);
        icon.getStyleClass().remove("nav-icon");
        icon.getStyleClass().add("nav-icon-active");
        VBox vb = (VBox) item.getChildren().get(item.getChildren().size() == 3 ? 2 : 1);
        ((Label) vb.getChildren().get(0)).getStyleClass().remove("nav-label");
        ((Label) vb.getChildren().get(0)).getStyleClass().add("nav-label-active");
        ((Label) vb.getChildren().get(1)).getStyleClass().remove("nav-subtitle");
        ((Label) vb.getChildren().get(1)).getStyleClass().add("nav-subtitle-active");
      } else {
        item.getStyleClass().remove("nav-item-active");
        if (!item.getChildren().isEmpty() && item.getChildren().get(0) instanceof Rectangle) {
          item.getChildren().remove(0);
        }
        Label icon = (Label) item.getChildren().get(0);
        icon.getStyleClass().remove("nav-icon-active");
        if (!icon.getStyleClass().contains("nav-icon")) icon.getStyleClass().add("nav-icon");
        VBox vb = (VBox) item.getChildren().get(1);
        ((Label) vb.getChildren().get(0)).getStyleClass().remove("nav-label-active");
        if (!((Label) vb.getChildren().get(0)).getStyleClass().contains("nav-label"))
          ((Label) vb.getChildren().get(0)).getStyleClass().add("nav-label");
        ((Label) vb.getChildren().get(1)).getStyleClass().remove("nav-subtitle-active");
        if (!((Label) vb.getChildren().get(1)).getStyleClass().contains("nav-subtitle"))
          ((Label) vb.getChildren().get(1)).getStyleClass().add("nav-subtitle");
      }
    }
  }

  private void navigateToCatalog() {
    beforeCatalogNavigate.run();
    showCatalog();
  }

  public void showAuth() {
    root.setLeft(null);
    loadContent("/fxml/auth-view.fxml");
  }

  public void showMain() {
    root.setLeft(sidebar);
    loadContent("/fxml/composer-view.fxml");
    setActiveNav("composer");
  }

  public void showVault() {
    root.setLeft(sidebar);
    loadContent("/fxml/vault-view.fxml");
    setActiveNav("vault");
  }

  public void showCatalog() {
    root.setLeft(sidebar);
    loadContent("/fxml/catalog-view.fxml");
    setActiveNav("catalog");
  }

  public void showHistory() {
    root.setLeft(sidebar);
    loadContent("/fxml/history-view.fxml");
    setActiveNav("history");
  }

  public void showSettings() {
    root.setLeft(sidebar);
    loadContent("/fxml/settings-view.fxml");
    setActiveNav("settings");
  }

  public void showAdmin() {
    root.setLeft(sidebar);
    loadContent("/fxml/admin-view.fxml");
    setActiveNav("admin");
  }

  private void loadContent(String fxmlPath) {
    if (!ready) {
      System.err.println("[VIEW] Cannot load " + fxmlPath + " — stage not initialized");
      return;
    }
    try {
      var resourceUrl = getClass().getResource(fxmlPath);
      if (resourceUrl == null) throw new RuntimeException("Resource not found: " + fxmlPath);
      FXMLLoader loader = new FXMLLoader(resourceUrl);
      loader.setControllerFactory(context::getBean);
      Parent content = loader.load();
      root.setCenter(content);
    } catch (Exception e) {
      System.err.println("[VIEW] FAILED: " + fxmlPath + " — " + e.getMessage());
      e.printStackTrace(System.err);
    }
  }
}
