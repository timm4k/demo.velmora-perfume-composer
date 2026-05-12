package velmora.composer.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ViewManager {

  private final ApplicationContext context;

  private BorderPane root;
  private boolean ready;

  public ViewManager(ApplicationContext context) {
    this.context = context;
  }

  public void setPrimaryStage(Stage primaryStage) {
    root = new BorderPane();
    Scene scene = new Scene(root);

    var cssUrl = getClass().getResource("/css/velmora.css");
    if (cssUrl != null) {
      scene.getStylesheets().add(cssUrl.toExternalForm());
    }

    primaryStage.setScene(scene);
    primaryStage.setTitle("Velmora — Olfactory Lab");
    primaryStage.setMinWidth(1100);
    primaryStage.setMinHeight(750);
    primaryStage.centerOnScreen();
    ready = true;

    System.out.println("[VIEW] Stage initialized with BorderPane root");
  }

  public void showAuth() {
    loadContent("/fxml/auth-view.fxml");
  }

  public void showMain() {
    loadContent("/fxml/composer-view.fxml");
  }

  public void showSettings() {
    loadContent("/fxml/settings-view.fxml");
  }

  private void loadContent(String fxmlPath) {
    if (!ready) {
      System.err.println("[VIEW] Cannot load " + fxmlPath + " — stage not initialized");
      return;
    }
    try {
      System.out.println("[VIEW] Loading content: " + fxmlPath);

      var resourceUrl = getClass().getResource(fxmlPath);
      if (resourceUrl == null) {
        throw new RuntimeException("Resource not found: " + fxmlPath);
      }

      FXMLLoader loader = new FXMLLoader(resourceUrl);
      loader.setControllerFactory(context::getBean);

      Parent content = loader.load();
      root.setCenter(content);

      System.out.println("[VIEW] Content loaded: " + fxmlPath);
    } catch (Exception e) {
      System.err.println("[VIEW] FAILED: " + fxmlPath + " — " + e.getMessage());
      e.printStackTrace(System.err);
    }
  }
}
