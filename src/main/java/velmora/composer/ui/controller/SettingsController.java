package velmora.composer.ui.controller;

import java.time.format.DateTimeFormatter;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import velmora.composer.model.Composition;
import velmora.composer.model.User;
import velmora.composer.repository.CompositionRepository;
import velmora.composer.service.PoolDemoService;
import velmora.composer.service.UserService;
import velmora.composer.ui.UserSession;
import velmora.composer.ui.ViewManager;

@Component
@RequiredArgsConstructor
public class SettingsController {

  private final UserService userService;
  private final CompositionRepository compositionRepository;
  private final PoolDemoService poolDemoService;
  private final ViewManager viewManager;
  private final UserSession userSession;

  @FXML private Label userInitials;
  @FXML private Label profileInitials;
  @FXML private Label profileName;
  @FXML private Label profileEmail;
  @FXML private Label compCount;
  @FXML private Label poolAvailable;
  @FXML private Label poolUsed;
  @FXML private Label poolMax;
  @FXML private TextField nicknameField;
  @FXML private TextField emailField;
  @FXML private PasswordField currentPasswordField;
  @FXML private PasswordField newPasswordField;
  @FXML private Label saveFeedback;
  @FXML private ListView<Composition> compositionsList;

  private User currentUser;

  @FXML
  public void initialize() {
    saveFeedback.setManaged(false);
    saveFeedback.setVisible(false);
    if (userSession.getUserId() == null) {
      profileName.setText("Not logged in");
      profileEmail.setText("Log in to manage your profile");
      return;
    }
    loadUser();
    loadCompositions();
    showPoolStatus();
  }

  private void loadUser() {
    currentUser = userService.findById(userSession.getUserId());
    String initials = userSession.getInitials() != null ? userSession.getInitials() : "?";
    userInitials.setText(initials);
    profileInitials.setText(initials);
    profileName.setText(currentUser.getNickname());
    profileEmail.setText(currentUser.getEmail());
    nicknameField.setText(currentUser.getNickname());
    emailField.setText(currentUser.getEmail());
  }

  private void loadCompositions() {
    var items = compositionRepository.findByUserId(userSession.getUserId());
    compCount.setText(String.valueOf(items.size()));
    compositionsList.setItems(FXCollections.observableArrayList(items));
    compositionsList.setCellFactory(lv -> new ListCell<>() {
      @Override
      protected void updateItem(Composition c, boolean empty) {
        super.updateItem(c, empty);
        if (empty || c == null) {
          setText(null);
          setGraphic(null);
        } else {
          HBox box = new HBox(12);
          box.setStyle("-fx-padding: 6 0;");

          Circle dot = new Circle(3);
          dot.setFill(Color.web("#78A0A0"));

          Label name = new Label(c.getName());
          name.setStyle("-fx-font-size: 13; -fx-font-weight: 500;");
          HBox.setHgrow(name, Priority.ALWAYS);

          Label date = new Label(
              c.getCreatedAt() != null
                  ? c.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                  : ""
          );
          date.setStyle("-fx-font-size: 11; -fx-text-fill: #B0ADA8;");

          box.getChildren().addAll(dot, name, date);
          setGraphic(box);
        }
      }
    });
  }

  private void showPoolStatus() {
    var status = poolDemoService.getStatus();
    poolAvailable.setText(String.valueOf(status.available()));
    poolUsed.setText(String.valueOf(status.used()));
    poolMax.setText(String.valueOf(status.maxSize()));
  }

  @FXML
  public void handleSave() {
    if (userSession.getUserId() == null) {
      viewManager.showAuth();
      return;
    }
    hideFeedback();
    String newNickname = nicknameField.getText().trim();
    String newEmail = emailField.getText().trim();
    String oldPass = currentPasswordField.getText();
    String newPass = newPasswordField.getText();

    try {
      boolean changed = false;

      if (!newNickname.equals(currentUser.getNickname())) {
        currentUser = userService.updateNickname(currentUser.getId(), newNickname);
        profileName.setText(currentUser.getNickname());
        userSession.setNickname(newNickname);
        userSession.setInitials(newNickname.isEmpty() ? "?" : newNickname.substring(0, 1).toUpperCase());
        userInitials.setText(userSession.getInitials());
        profileInitials.setText(userSession.getInitials());
        changed = true;
      }

      if (!newEmail.equals(currentUser.getEmail())) {
        currentUser = userService.updateEmail(currentUser.getId(), newEmail);
        profileEmail.setText(currentUser.getEmail());
        userSession.setEmail(newEmail);
        changed = true;
      }

      if (!oldPass.isEmpty() && !newPass.isEmpty()) {
        currentUser = userService.updatePassword(currentUser.getId(), oldPass, newPass);
        currentPasswordField.clear();
        newPasswordField.clear();
        changed = true;
      } else if (!oldPass.isEmpty() || !newPass.isEmpty()) {
        showFeedback("Fill in both current and new password to change");
        return;
      }

      if (changed) {
        showFeedback("Settings saved successfully");
      } else {
        showFeedback("No changes to save");
      }
    } catch (Exception e) {
      showFeedback(e.getMessage());
    }
  }

  private void showFeedback(String message) {
    saveFeedback.setText(message);
    saveFeedback.setManaged(true);
    saveFeedback.setVisible(true);
  }

  private void hideFeedback() {
    saveFeedback.setManaged(false);
    saveFeedback.setVisible(false);
  }

  @FXML
  public void handleBack() {
    viewManager.showMain();
  }

  @FXML
  public void handleLogout() {
    userSession.clear();
    viewManager.showAuth();
  }
}
