package com.deepsleep.ui.common;

import com.deepsleep.api.dto.user.SendEmailCodeRequest;
import com.deepsleep.api.dto.user.SendPhoneCodeRequest;
import com.deepsleep.api.dto.user.UpdateEmailRequest;
import com.deepsleep.api.dto.user.UpdatePasswordRequest;
import com.deepsleep.api.dto.user.UpdatePhoneRequest;
import com.deepsleep.api.enums.UserRole;
import com.deepsleep.api.result.ApiException;
import com.deepsleep.api.result.ResultCode;
import com.deepsleep.api.vo.MyUserInfoVO;
import com.deepsleep.app.AppContext;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class AccountSettingsController {

    private static final long MAX_AVATAR_SIZE = 5L * 1024 * 1024;
    private static final int CODE_COOLDOWN_SECONDS = 60;
    private static final String SEND_CODE_TEXT = "发送验证码";
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final List<String> ALLOWED_AVATAR_SUFFIXES = List.of(".png", ".jpg", ".jpeg", ".webp");

    @FXML
    private ImageView profileAvatarView;
    @FXML
    private Label profileNameLabel;
    @FXML
    private Label profileUsernameLabel;
    @FXML
    private Label profileRoleLabel;
    @FXML
    private Label profileEmailLabel;
    @FXML
    private Label profilePhoneLabel;
    @FXML
    private Label profileCreateTimeLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label contactMessageLabel;
    @FXML
    private Label passwordMessageLabel;
    @FXML
    private TextField newEmailField;
    @FXML
    private TextField emailCodeField;
    @FXML
    private TextField newPhoneField;
    @FXML
    private TextField phoneCodeField;
    @FXML
    private TextField passwordCodeField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Button sendEmailCodeButton;
    @FXML
    private Button sendPhoneCodeButton;
    @FXML
    private Button sendPasswordCodeButton;
    @FXML
    private VBox avatarDropZone;
    @FXML
    private Label selectedAvatarLabel;

    private MyUserInfoVO currentUser;
    private Path selectedAvatarPath;

    @FXML
    public void initialize() {
        loadProfile();
    }

    @FXML
    public void onRefreshProfile() {
        loadProfile();
    }

    @FXML
    public void onSendEmailCode() {
        clearMessage(contactMessageLabel);
        String email = value(newEmailField);
        if (email.isBlank()) {
            setError(contactMessageLabel, "请先填写新邮箱。");
            return;
        }
        sendCode(sendEmailCodeButton,
                () -> AppContext.getInstance().apiServices().userApi().sendEmailCode(new SendEmailCodeRequest(email)),
                contactMessageLabel,
                "验证码已发送到新邮箱。");
    }

    @FXML
    public void onSendPhoneCode() {
        clearMessage(contactMessageLabel);
        String phone = value(newPhoneField);
        if (phone.isBlank()) {
            setError(contactMessageLabel, "请先填写新手机号。");
            return;
        }
        sendCode(sendPhoneCodeButton,
                () -> AppContext.getInstance().apiServices().userApi().sendPhoneCode(new SendPhoneCodeRequest(phone)),
                contactMessageLabel,
                "验证码已发送到新手机号。");
    }

    @FXML
    public void onSendPasswordCode() {
        clearMessage(passwordMessageLabel);
        if (currentUser == null || blank(currentUser.email())) {
            setError(passwordMessageLabel, "当前账号没有可用邮箱，无法发送验证码。");
            return;
        }
        sendCode(sendPasswordCodeButton,
                () -> AppContext.getInstance().apiServices().userApi().sendPasswordCode(),
                passwordMessageLabel,
                "验证码已发送到当前绑定邮箱。");
    }

    @FXML
    public void onUpdateEmail() {
        clearMessage(contactMessageLabel);
        String email = value(newEmailField);
        String code = value(emailCodeField);
        if (email.isBlank() || code.isBlank()) {
            setError(contactMessageLabel, "请填写新邮箱和验证码。");
            return;
        }
        AppContext.getInstance().apiServices().userApi()
                .updateEmail(new UpdateEmailRequest(email, code))
                .whenComplete(UiAsync.onComplete(ignored -> {
                    setSuccess(contactMessageLabel, "邮箱更新成功。");
                    clear(newEmailField, emailCodeField);
                    loadProfile();
                }, error -> showAccountError(error, contactMessageLabel)));
    }

    @FXML
    public void onUpdatePhone() {
        clearMessage(contactMessageLabel);
        String phone = value(newPhoneField);
        String code = value(phoneCodeField);
        if (phone.isBlank() || code.isBlank()) {
            setError(contactMessageLabel, "请填写新手机号和验证码。");
            return;
        }
        AppContext.getInstance().apiServices().userApi()
                .updatePhone(new UpdatePhoneRequest(phone, code))
                .whenComplete(UiAsync.onComplete(ignored -> {
                    setSuccess(contactMessageLabel, "手机号更新成功。");
                    clear(newPhoneField, phoneCodeField);
                    loadProfile();
                }, error -> showAccountError(error, contactMessageLabel)));
    }

    @FXML
    public void onUpdatePassword() {
        clearMessage(passwordMessageLabel);
        if (currentUser == null || blank(currentUser.email())) {
            setError(passwordMessageLabel, "当前账号没有可用邮箱，无法修改密码。");
            return;
        }
        String code = value(passwordCodeField);
        String password = newPasswordField.getText() == null ? "" : newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText() == null ? "" : confirmPasswordField.getText();
        if (code.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            setError(passwordMessageLabel, "请填写验证码、新密码和确认密码。");
            return;
        }
        if (!password.equals(confirmPassword)) {
            setError(passwordMessageLabel, "两次输入的新密码不一致。");
            return;
        }
        AppContext.getInstance().apiServices().userApi()
                .updatePassword(new UpdatePasswordRequest(code, password))
                .whenComplete(UiAsync.onComplete(ignored -> {
                    setSuccess(passwordMessageLabel, "密码更新成功。");
                    clear(passwordCodeField, newPasswordField, confirmPasswordField);
                }, error -> showAccountError(error, passwordMessageLabel)));
    }

    @FXML
    public void onChooseAvatar() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("选择头像图片");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "图片文件", "*.png", "*.jpg", "*.jpeg", "*.webp"
        ));
        Window window = avatarDropZone.getScene() == null ? null : avatarDropZone.getScene().getWindow();
        File file = chooser.showOpenDialog(window);
        if (file != null) {
            selectAvatar(file.toPath());
        }
    }

    @FXML
    public void onAvatarDragOver(DragEvent event) {
        if (event.getGestureSource() != avatarDropZone && event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    @FXML
    public void onAvatarDropped(DragEvent event) {
        List<File> files = event.getDragboard().getFiles();
        if (!files.isEmpty()) {
            selectAvatar(files.getFirst().toPath());
            event.setDropCompleted(true);
        } else {
            event.setDropCompleted(false);
        }
        event.consume();
    }

    @FXML
    public void onUploadAvatar() {
        if (selectedAvatarPath == null) {
            setStatus("请先选择或拖拽头像图片。");
            return;
        }
        AppContext.getInstance().apiServices().userApi()
                .uploadAvatar(selectedAvatarPath)
                .whenComplete(UiAsync.onComplete(avatar -> {
                    setStatus("头像上传成功。");
                    selectedAvatarPath = null;
                    selectedAvatarLabel.setText("尚未选择文件");
                    loadProfile();
                    if (!blank(avatar.avatarUrl())) {
                        setAvatar(avatar.avatarUrl());
                    }
                }, error -> setStatus(UiAsync.errorMessage(error))));
    }

    private void loadProfile() {
        setStatus("正在加载账号资料...");
        AppContext.getInstance().apiServices().userApi()
                .getCurrentUser()
                .whenComplete(UiAsync.onComplete(user -> {
                    currentUser = user;
                    renderProfile(user);
                    setStatus("账号资料已加载。");
                }, error -> setStatus(UiAsync.errorMessage(error))));
    }

    private void renderProfile(MyUserInfoVO user) {
        profileNameLabel.setText(orDash(user.name()));
        profileUsernameLabel.setText(orDash(user.username()));
        profileRoleLabel.setText(UserRole.of(user.role()).label());
        profileEmailLabel.setText(orDash(user.email()));
        profilePhoneLabel.setText(orDash(user.phone()));
        profileCreateTimeLabel.setText(user.createTime() == null ? "-" : DATE_TIME.format(user.createTime()));
        setAvatar(user.avatar());
    }

    private void sendCode(
            Button button,
            Supplier<CompletableFuture<Void>> requestSupplier,
            Label messageLabel,
            String successMessage
    ) {
        button.setDisable(true);
        button.setText("发送中...");
        try {
            requestSupplier.get()
                    .whenComplete(UiAsync.onComplete(ignored -> {
                        setSuccess(messageLabel, successMessage);
                        startCodeCountdown(button);
                    }, error -> {
                        restoreCodeButton(button);
                        showAccountError(error, messageLabel);
                    }));
        } catch (RuntimeException ex) {
            restoreCodeButton(button);
            showAccountError(ex, messageLabel);
        }
    }

    private void startCodeCountdown(Button button) {
        final int[] secondsLeft = {CODE_COOLDOWN_SECONDS};
        button.setDisable(true);
        button.setText(secondsLeft[0] + "秒后重试");

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            secondsLeft[0]--;
            if (secondsLeft[0] <= 0) {
                restoreCodeButton(button);
                return;
            }
            button.setText(secondsLeft[0] + "秒后重试");
        }));
        timeline.setCycleCount(CODE_COOLDOWN_SECONDS);
        timeline.play();
    }

    private void restoreCodeButton(Button button) {
        button.setDisable(false);
        button.setText(SEND_CODE_TEXT);
    }

    private void showAccountError(Throwable error, Label targetLabel) {
        setError(targetLabel, accountErrorMessage(error));
    }

    private String accountErrorMessage(Throwable error) {
        if (error instanceof ApiException apiException) {
            ResultCode code = apiException.getResultCode();
            return switch (code) {
                case EMAIL_OCCUPIED -> "该邮箱已被占用，请更换邮箱。";
                case PHONE_OCCUPIED -> "该手机号已被占用，请更换手机号。";
                case EMAIL_NOT_BOUND -> "当前账号未绑定邮箱，无法发送密码验证码。";
                case EMAIL_CODE_EXPIRED, CODE_NOT_EXISTS -> "验证码不存在或已过期，请重新发送。";
                case EMAIL_CODE_ERROR, CODE_INCORRECT -> "验证码错误，请检查后重试。";
                case CODE_SEND_TOO_FREQUENTLY -> "验证码发送过于频繁，请稍后再试。";
                case CODE_FAILED_ATTEMPTS_TOO_MUCH -> "验证码校验失败次数过多，请重新发送。";
                case EMAIL_SEND_FAILED -> "邮件发送失败，请稍后重试。";
                case SMS_SEND_FAILED -> "短信发送失败，请稍后重试。";
                case BAD_REQUEST -> "请检查填写内容后再提交。";
                case UNAUTHORIZED -> "登录状态已失效，请重新登录。";
                default -> UiAsync.errorMessage(apiException);
            };
        }
        return UiAsync.errorMessage(error);
    }

    private void clearMessage(Label label) {
        setMessage(label, "", false);
    }

    private void setSuccess(Label label, String message) {
        setMessage(label, message, false);
    }

    private void setError(Label label, String message) {
        setMessage(label, message, true);
    }

    private void setMessage(Label label, String message, boolean error) {
        label.setText(message == null ? "" : message);
        label.getStyleClass().removeAll("form-message-success", "form-message-error");
        label.getStyleClass().add(error ? "form-message-error" : "form-message-success");
    }

    private void selectAvatar(Path file) {
        try {
            validateAvatar(file);
            selectedAvatarPath = file;
            selectedAvatarLabel.setText(file.getFileName().toString());
            setAvatar(file.toUri().toString());
            setStatus("头像已选择，确认后可上传。");
        } catch (IllegalArgumentException | IOException ex) {
            setStatus(ex.getMessage());
        }
    }

    private void validateAvatar(Path file) throws IOException {
        if (file == null || !Files.isRegularFile(file)) {
            throw new IllegalArgumentException("请选择有效的图片文件。");
        }
        String filename = file.getFileName().toString().toLowerCase(Locale.ROOT);
        boolean supported = ALLOWED_AVATAR_SUFFIXES.stream().anyMatch(filename::endsWith);
        if (!supported) {
            throw new IllegalArgumentException("头像仅支持 PNG、JPG、JPEG、WEBP。");
        }
        if (Files.size(file) <= 0) {
            throw new IllegalArgumentException("头像文件不能为空。");
        }
        if (Files.size(file) > MAX_AVATAR_SIZE) {
            throw new IllegalArgumentException("头像文件不能超过 5MB。");
        }
    }

    private void setAvatar(String source) {
        if (blank(source)) {
            profileAvatarView.setImage(null);
            return;
        }
        profileAvatarView.setImage(new Image(source, 96, 96, true, true, true));
    }

    private String value(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    private void clear(TextField... fields) {
        for (TextField field : fields) {
            field.clear();
        }
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private String orDash(String value) {
        return blank(value) ? "-" : value;
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
    }
}
