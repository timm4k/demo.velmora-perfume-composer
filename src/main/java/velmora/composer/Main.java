package velmora.composer;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "velmora.composer")
public class Main {
  public static void main(String[] args) {
    Application.launch(JavaFxApplication.class, args);
  }
}