package com.deepsleep.ui.admin;

import com.deepsleep.api.dto.PageQuery;
import com.deepsleep.api.dto.notice.NoticeRequest;
import com.deepsleep.api.vo.NoticeVO;
import com.deepsleep.app.AppContext;
import com.deepsleep.ui.common.BaseStaticPageController;
import com.deepsleep.ui.common.Rows;
import com.deepsleep.ui.common.StaticPageData;
import com.deepsleep.ui.common.StaticPageSpec;
import com.deepsleep.ui.common.UiAsync;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableRow;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;

import java.util.List;

public class AdminNoticesController extends BaseStaticPageController {

    private static final int PAGE_SIZE = 20;

    private List<NoticeVO> currentNotices = List.of();

    @Override
    protected StaticPageSpec pageSpec() {
        return StaticPageData.adminNotices();
    }

    @Override
    protected void configureActions() {
        refreshButton.setText("刷新");
        createButton.setText("发布公告");
        editButton.setText("编辑公告");
        deleteButton.setText("删除公告");
        dataTable.setRowFactory(table -> {
            TableRow<ObservableList<String>> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    dataTable.getSelectionModel().select(row.getIndex());
                    showSelectedNoticeEditor();
                }
            });
            return row;
        });
    }

    @Override
    protected void loadRemoteData() {
        AppContext.getInstance().apiServices().noticeApi()
                .listNotices(new PageQuery(1, PAGE_SIZE))
                .whenComplete(UiAsync.onComplete(page -> {
                    currentNotices = page.records() == null ? List.of() : page.records();
                    setTable(List.of("标题", "发布人", "创建时间", "更新时间"),
                            currentNotices.stream().map(Rows::noticeSummary).toList());
                    showStatus("公告加载完成，共 " + Rows.text(page.total()) + " 条。双击公告可编辑内容。");
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    @Override
    protected void handleCreate() {
        showNoticeDialog(null);
    }

    @Override
    protected void handleEdit() {
        showSelectedNoticeEditor();
    }

    @Override
    protected void handleDelete() {
        NoticeVO selected = selectedNotice();
        if (selected == null || selected.id() == null) {
            showStatus("请先选择公告。");
            return;
        }
        AppContext.getInstance().apiServices().noticeApi()
                .deleteNotice(selected.id())
                .whenComplete(UiAsync.onComplete(ignored -> {
                    showStatus("公告已删除。");
                    loadRemoteData();
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    private void showSelectedNoticeEditor() {
        NoticeVO selected = selectedNotice();
        if (selected == null || selected.id() == null) {
            showStatus("请先在表格中选择要编辑的公告。");
            return;
        }
        AppContext.getInstance().apiServices().noticeApi()
                .getNotice(selected.id())
                .whenComplete(UiAsync.onComplete(this::showNoticeDialog, error -> showStatus(UiAsync.errorMessage(error))));
    }

    private NoticeVO selectedNotice() {
        int index = dataTable.getSelectionModel().getSelectedIndex();
        if (index < 0 || index >= currentNotices.size()) {
            return null;
        }
        return currentNotices.get(index);
    }

    private void showNoticeDialog(NoticeVO notice) {
        boolean editing = notice != null && notice.id() != null;
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(editing ? "编辑公告" : "发布公告");
        dialog.setHeaderText(editing ? "修改公告标题和正文" : "填写公告标题和正文");
        ButtonType saveButtonType = new ButtonType(editing ? "保存" : "发布", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().setAll(saveButtonType, ButtonType.CANCEL);
        dialog.getDialogPane().setPrefWidth(620);

        TextField titleField = new TextField(editing ? Rows.text(notice.title()) : "");
        TextArea contentArea = new TextArea(editing ? Rows.text(notice.content()) : "");
        Label messageLabel = new Label();
        titleField.setPromptText("公告标题");
        contentArea.setPromptText("公告正文");
        contentArea.setWrapText(true);
        contentArea.setPrefRowCount(12);
        messageLabel.getStyleClass().add("empty-state");
        messageLabel.setWrapText(true);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.addRow(0, new Label("标题"), titleField);
        grid.addRow(1, new Label("正文"), contentArea);
        grid.add(messageLabel, 0, 2, 2, 1);
        dialog.getDialogPane().setContent(grid);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            event.consume();
            String title = value(titleField);
            String content = value(contentArea);
            if (title == null || title.isBlank()) {
                messageLabel.setText("请填写公告标题。");
                return;
            }
            if (content == null || content.isBlank()) {
                messageLabel.setText("请填写公告正文。");
                return;
            }
            saveButton.setDisable(true);
            NoticeRequest request = new NoticeRequest(title, content);
            var noticeApi = AppContext.getInstance().apiServices().noticeApi();
            var action = editing ? noticeApi.updateNotice(notice.id(), request) : noticeApi.createNotice(request);
            action.whenComplete(UiAsync.onComplete(ignored -> {
                showStatus(editing ? "公告已更新。" : "公告已发布。");
                loadRemoteData();
                dialog.close();
            }, error -> {
                saveButton.setDisable(false);
                messageLabel.setText(UiAsync.errorMessage(error));
            }));
        });

        dialog.showAndWait();
    }

    private String value(TextField field) {
        String value = field.getText();
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String value(TextArea area) {
        String value = area.getText();
        return value == null || value.isBlank() ? null : value.trim();
    }
}
