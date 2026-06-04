package com.deepsleep.ui.auth;

import com.deepsleep.api.enums.UserRole;
import com.deepsleep.api.dto.auth.LoginRequest;
import com.deepsleep.app.AppContext;
import com.deepsleep.app.LoginIdentity;
import com.deepsleep.ui.common.UiAsync;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label statusLabel;

    @FXML
    public void onLogin() {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();
        if (username.isBlank() || password.isBlank()) {
            statusLabel.setText("请输入用户名和密码。");
            return;
        }

        statusLabel.setText("正在登录...");
        AppContext.getInstance().apiServices().authApi()
                .login(new LoginRequest(username, password))
                .whenComplete(UiAsync.onComplete(login -> {
                    UserRole role = UserRole.of(login.role());
                    AppContext.getInstance().showMainShell(new LoginIdentity(login.name(), role));
                }, error -> statusLabel.setText(UiAsync.errorMessage(error))));
    }
}
