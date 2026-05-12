package velmora.composer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import velmora.composer.ui.ViewManager;

public class JavaFxApplication extends Application {

  private ConfigurableApplicationContext context;

  @Override
  public void init() {
    try {
      System.out.println("[JFX] Initializing Spring context...");
      this.context = new SpringApplicationBuilder(Main.class).run();
      System.out.println("[JFX] Spring context ready.");
    } catch (Exception e) {
      System.out.println("[JFX] FAILED to start Spring context:");
      e.printStackTrace(System.out);
      Platform.exit();
      System.exit(1);
    }
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    try {
      System.out.println("[JFX] Starting JavaFX UI...");
      ViewManager viewManager = context.getBean(ViewManager.class);
      viewManager.setPrimaryStage(primaryStage);
      viewManager.showAuth();
      primaryStage.show();
      primaryStage.toFront();
      System.out.println("[JFX] UI started successfully");
    } catch (Exception e) {
      System.out.println("[JFX] FAILED to start UI:");
      e.printStackTrace(System.out);
      Platform.exit();
      System.exit(1);
    }
  }

  @Override
  public void stop() {
    System.out.println("[JFX] Stopping application...");
    if (context != null) {
      this.context.close();
    }
    Platform.exit();
  }
}
