package velmora.composer.ui.controller;

import javafx.fxml.FXML;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import velmora.composer.service.UserService;

@Component
@RequiredArgsConstructor
public class MainController {

  private final UserService userService;

  @FXML
  public void handleTestConnection() {
    System.out.println("Service connection: " + (userService != null));
  }
}