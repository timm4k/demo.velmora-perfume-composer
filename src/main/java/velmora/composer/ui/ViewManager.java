package velmora.composer.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Setter;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ViewManager {

  private final ApplicationContext context;

  @Setter
  private Stage primaryStage;

  public ViewManager(ApplicationContext context) {
    this.context = context;
  }

  public void showAuth() {
    showScene("/fxml/auth-view.fxml");
  }

  public void showMain() {
    showScene("/fxml/main-view.fxml");
  }

  private void showScene(String fxmlPath) {
    try {
      var resourceUrl = getClass().getResource(fxmlPath);
      if (resourceUrl == null) {
        throw new RuntimeException("Resource not found: " + fxmlPath);
      }

      FXMLLoader loader = new FXMLLoader(resourceUrl);
      loader.setControllerFactory(context::getBean);

      Parent root = loader.load();
      Scene scene = new Scene(root);

      var cssUrl = getClass().getResource("/css/velmora.css");
      if (cssUrl != null) {
        scene.getStylesheets().add(cssUrl.toExternalForm());
      }

      primaryStage.setScene(scene);
      primaryStage.centerOnScreen();
      primaryStage.show();
    } catch (Exception e) {
      throw new RuntimeException("Failed to load: " + fxmlPath + " | " + e.getMessage(), e);
    }
  }
}
