package velmora.composer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class JavaFxApplication extends Application {

  private ConfigurableApplicationContext context;

  @Override
  public void init() {
    this.context = new SpringApplicationBuilder(Main.class).run();
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main-view.fxml"));
    loader.setControllerFactory(context::getBean);

    Parent root = loader.load();
    primaryStage.setTitle("Velmora Perfume Composer");
    primaryStage.setScene(new Scene(root, 1000, 700));
    primaryStage.show();
  }

  @Override
  public void stop() {
    this.context.close();
    Platform.exit();
  }
}