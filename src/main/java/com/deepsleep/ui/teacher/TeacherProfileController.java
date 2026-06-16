package com.deepsleep.ui.teacher;

import com.deepsleep.api.dto.user.UpdateTeacherProfileRequest;
import com.deepsleep.api.vo.TeacherProfileVO;
import com.deepsleep.app.AppContext;
import com.deepsleep.ui.common.UiAsync;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class TeacherProfileController {

    private static final int MAX_TITLE_LENGTH = 50;

    @FXML
    private ImageView avatarView;
    @FXML
    private Label avatarStatusLabel;
    @FXML
    private Label deptLabel;
    @FXML
    private TextField titleField;
    @FXML
    private DatePicker entryDatePicker;
    @FXML
    private Button refreshButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button resetButton;
    @FXML
    private Label messageLabel;

    private TeacherProfileVO currentProfile;

    @FXML
    public void initialize() {
        loadProfile();
    }

    @FXML
    private void onRefresh() {
        loadProfile();
    }

    @FXML
    private void onReset() {
        if (currentProfile == null) {
            titleField.clear();
            entryDatePicker.setValue(null);
            setMessage("暂无可重置的资料。", true);
            return;
        }
        renderProfile(currentProfile);
        setMessage("已恢复为最近一次加载的资料。", false);
    }

    @FXML
    private void onSave() {
        if (currentProfile == null) {
            setMessage("资料尚未加载完成，请稍后再保存。", true);
            return;
        }

        String title = value(titleField);
        if (title.length() > MAX_TITLE_LENGTH) {
            setMessage("职称不能超过 " + MAX_TITLE_LENGTH + " 个字符。", true);
            return;
        }

        setActionsDisabled(true);
        setMessage("正在保存教师资料...", false);
        AppContext.getInstance().apiServices().teacherApi()
                .updateProfile(new UpdateTeacherProfileRequest(title, entryDatePicker.getValue()))
                .whenComplete(UiAsync.onComplete(ignored -> loadProfile("教师资料已更新。"), error -> {
                    setActionsDisabled(false);
                    setMessage(UiAsync.errorMessage(error), true);
                }));
    }

    private void loadProfile() {
        loadProfile("教师资料已加载。");
    }

    private void loadProfile(String successMessage) {
        setActionsDisabled(true);
        setMessage("正在加载教师资料...", false);
        AppContext.getInstance().apiServices().teacherApi()
                .getProfile()
                .whenComplete(UiAsync.onComplete(profile -> {
                    currentProfile = profile;
                    renderProfile(profile);
                    setActionsDisabled(false);
                    setMessage(successMessage, false);
                }, error -> {
                    setActionsDisabled(false);
                    setMessage(UiAsync.errorMessage(error), true);
                }));
    }

    private void renderProfile(TeacherProfileVO profile) {
        deptLabel.setText(orDash(profile.deptName()));
        titleField.setText(blank(profile.title()) ? "" : profile.title());
        entryDatePicker.setValue(profile.entryDate());
        setAvatar(profile.avatar());
    }

    private void setAvatar(String source) {
        if (blank(source)) {
            avatarView.setImage(null);
            avatarStatusLabel.setText("未设置头像");
            return;
        }
        avatarView.setImage(new Image(source, 96, 96, true, true, true));
        avatarStatusLabel.setText("头像已加载");
    }

    private void setActionsDisabled(boolean disabled) {
        refreshButton.setDisable(disabled);
        saveButton.setDisable(disabled);
        resetButton.setDisable(disabled);
    }

    private void setMessage(String message, boolean error) {
        messageLabel.setText(message == null ? "" : message);
        messageLabel.getStyleClass().removeAll("form-message-success", "form-message-error");
        messageLabel.getStyleClass().add(error ? "form-message-error" : "form-message-success");
    }

    private String value(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    private String orDash(String value) {
        return blank(value) ? "-" : value;
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
