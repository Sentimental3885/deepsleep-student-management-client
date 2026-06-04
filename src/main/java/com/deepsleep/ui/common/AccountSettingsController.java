package com.deepsleep.ui.common;

import com.deepsleep.api.dto.auth.EmailCodeRequest;
import com.deepsleep.api.dto.user.UpdateEmailRequest;
import com.deepsleep.api.dto.user.UpdatePasswordRequest;
import com.deepsleep.api.dto.user.UpdatePhoneRequest;
import com.deepsleep.api.enums.UserRole;
import com.deepsleep.api.vo.MyUserInfoVO;
import com.deepsleep.app.AppContext;
import javafx.fxml.FXML;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class AccountSettingsController {

    private static final long MAX_AVATAR_SIZE = 5L * 1024 * 1024;
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
        String email = value(newEmailField);
        if (email.isBlank()) {
            setStatus("请先填写新邮箱。");
            return;
        }
        sendCode(email, "验证码已发送到新邮箱。");
    }

    @FXML
    public void onSendCurrentEmailCode() {
        if (currentUser == null || blank(currentUser.email())) {
            setStatus("当前账号没有可用邮箱，无法发送验证码。");
            return;
        }
        sendCode(currentUser.email(), "验证码已发送到当前绑定邮箱。");
    }

    @FXML
    public void onUpdateEmail() {
        String email = value(newEmailField);
        String code = value(emailCodeField);
        if (email.isBlank() || code.isBlank()) {
            setStatus("请填写新邮箱和验证码。");
            return;
        }
        AppContext.getInstance().apiServices().userApi()
                .updateEmail(new UpdateEmailRequest(email, code))
                .whenComplete(UiAsync.onComplete(ignored -> {
                    setStatus("邮箱更新成功。");
                    clear(newEmailField, emailCodeField);
                    loadProfile();
                }, error -> setStatus(UiAsync.errorMessage(error))));
    }

    @FXML
    public void onUpdatePhone() {
        String phone = value(newPhoneField);
        String code = value(phoneCodeField);
        if (phone.isBlank() || code.isBlank()) {
            setStatus("请填写新手机号和验证码。");
            return;
        }
        AppContext.getInstance().apiServices().userApi()
                .updatePhone(new UpdatePhoneRequest(phone, code))
                .whenComplete(UiAsync.onComplete(ignored -> {
                    setStatus("手机号更新成功。");
                    clear(newPhoneField, phoneCodeField);
                    loadProfile();
                }, error -> setStatus(UiAsync.errorMessage(error))));
    }

    @FXML
    public void onUpdatePassword() {
        if (currentUser == null || blank(currentUser.email())) {
            setStatus("当前账号没有可用邮箱，无法修改密码。");
            return;
        }
        String code = value(passwordCodeField);
        String password = newPasswordField.getText() == null ? "" : newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText() == null ? "" : confirmPasswordField.getText();
        if (code.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            setStatus("请填写验证码、新密码和确认密码。");
            return;
        }
        if (!password.equals(confirmPassword)) {
            setStatus("两次输入的新密码不一致。");
            return;
        }
        AppContext.getInstance().apiServices().userApi()
                .updatePassword(new UpdatePasswordRequest(currentUser.email(), code, password))
                .whenComplete(UiAsync.onComplete(ignored -> {
                    setStatus("密码更新成功。");
                    clear(passwordCodeField, newPasswordField, confirmPasswordField);
                }, error -> setStatus(UiAsync.errorMessage(error))));
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

    private void sendCode(String email, String successMessage) {
        AppContext.getInstance().apiServices().emailApi()
                .sendCode(new EmailCodeRequest(email))
                .whenComplete(UiAsync.onComplete(ignored -> setStatus(successMessage), error -> setStatus(UiAsync.errorMessage(error))));
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
