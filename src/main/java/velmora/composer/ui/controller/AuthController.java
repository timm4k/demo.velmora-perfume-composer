package velmora.composer.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import velmora.composer.dto.auth.LoginRequest;
import velmora.composer.dto.auth.RegistrationRequest;
import velmora.composer.facade.AuthFacade;
import velmora.composer.ui.ViewManager;

@Component
@RequiredArgsConstructor
public class AuthController {

  private final AuthFacade authFacade;
  private final ViewManager viewManager;

  @FXML private VBox loginForm;
  @FXML private VBox registerForm;
  @FXML private VBox verifyForm;
  @FXML private TextField loginEmail;
  @FXML private PasswordField loginPassword;
  @FXML private TextField regNickname;
  @FXML private TextField regEmail;
  @FXML private PasswordField regPassword;
  @FXML private TextField verifyCode;
  @FXML private Label verifyInfoText;
  @FXML private Label loginError;
  @FXML private Label regError;
  @FXML private Label verifyError;

  private String pendingEmail;

  @FXML
  public void initialize() {
    loginError.setManaged(false);
    regError.setManaged(false);
    verifyError.setManaged(false);
    setupEnterKeyHandlers();
  }

  private void setupEnterKeyHandlers() {
    loginPassword.setOnKeyPressed(e -> {
      if (e.getCode() == KeyCode.ENTER) handleLogin();
    });
    regPassword.setOnKeyPressed(e -> {
      if (e.getCode() == KeyCode.ENTER) handleRegister();
    });
    verifyCode.setOnKeyPressed(e -> {
      if (e.getCode() == KeyCode.ENTER) handleVerify();
    });
  }

  @FXML
  public void handleLogin() {
    hideError(loginError);

    String email = loginEmail.getText().trim();
    String password = loginPassword.getText();

    if (!validateLogin(email, password)) return;

    try {
      LoginRequest request = LoginRequest.builder()
          .email(email)
          .password(password)
          .build();
      authFacade.login(request);
      viewManager.showMain();
    } catch (Exception e) {
      showError(loginError, e.getMessage());
    }
  }

  @FXML
  public void handleRegister() {
    hideError(regError);

    String nickname = regNickname.getText().trim();
    String email = regEmail.getText().trim();
    String password = regPassword.getText();

    if (!validateRegistration(nickname, email, password)) return;

    try {
      RegistrationRequest request = RegistrationRequest.builder()
          .nickname(nickname)
          .email(email)
          .password(password)
          .build();
      authFacade.register(request);

      pendingEmail = email;
      switchToVerify();
    } catch (Exception e) {
      showError(regError, e.getMessage());
    }
  }

  @FXML
  public void handleVerify() {
    hideError(verifyError);

    String code = verifyCode.getText().trim();

    if (code.isEmpty()) {
      showError(verifyError, "Enter the verification code");
      return;
    }

    try {
      authFacade.verifyEmail(pendingEmail, code);
      viewManager.showMain();
    } catch (Exception e) {
      showError(verifyError, e.getMessage());
    }
  }

  @FXML
  public void switchToRegister() {
    loginForm.setVisible(false);
    loginForm.setManaged(false);
    verifyForm.setVisible(false);
    verifyForm.setManaged(false);
    registerForm.setVisible(true);
    registerForm.setManaged(true);
    hideError(loginError);
    hideError(verifyError);
  }

  @FXML
  public void switchToLogin() {
    registerForm.setVisible(false);
    registerForm.setManaged(false);
    verifyForm.setVisible(false);
    verifyForm.setManaged(false);
    loginForm.setVisible(true);
    loginForm.setManaged(true);
    hideError(regError);
    hideError(verifyError);
  }

  private void switchToVerify() {
    registerForm.setVisible(false);
    registerForm.setManaged(false);
    loginForm.setVisible(false);
    loginForm.setManaged(false);
    verifyForm.setVisible(true);
    verifyForm.setManaged(true);
    verifyInfoText.setText("Enter the 6-digit code sent to:\n" + pendingEmail);
    verifyCode.clear();
    hideError(regError);
  }

  private boolean validateLogin(String email, String password) {
    if (email.isEmpty() || password.isEmpty()) {
      showError(loginError, "Please fill in all fields");
      return false;
    }
    return true;
  }

  private boolean validateRegistration(String nickname, String email, String password) {
    if (nickname.isEmpty() || email.isEmpty() || password.isEmpty()) {
      showError(regError, "Please fill in all fields");
      return false;
    }
    if (password.length() < 6) {
      showError(regError, "Password must be at least 6 characters");
      return false;
    }
    if (!email.contains("@")) {
      showError(regError, "Please enter a valid email address");
      return false;
    }
    return true;
  }

  private void showError(Label label, String message) {
    label.setText(message);
    label.setManaged(true);
    label.setVisible(true);
  }

  private void hideError(Label label) {
    label.setManaged(false);
    label.setVisible(false);
  }
}
