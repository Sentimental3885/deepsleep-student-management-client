package com.deepsleep.ui.admin;

import com.deepsleep.api.dto.classroom.ClassroomRequest;
import com.deepsleep.api.vo.ClassroomVO;
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
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;

import java.util.List;

public class AdminClassroomsController extends BaseStaticPageController {

    private List<ClassroomVO> currentClassrooms = List.of();

    @Override
    protected StaticPageSpec pageSpec() {
        return StaticPageData.adminClassrooms();
    }

    @Override
    protected void configureActions() {
        refreshButton.setText("刷新");
        createButton.setText("新增教室");
        editButton.setText("编辑教室");
        deleteButton.setText("删除教室");
        dataTable.setRowFactory(table -> {
            TableRow<ObservableList<String>> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    dataTable.getSelectionModel().select(row.getIndex());
                    showSelectedClassroomEditor();
                }
            });
            return row;
        });
    }

    @Override
    protected void loadRemoteData() {
        AppContext.getInstance().apiServices().classroomApi().listClassrooms()
                .whenComplete(UiAsync.onComplete(classrooms -> {
                    currentClassrooms = classrooms == null ? List.of() : classrooms;
                    setTable(List.of("教室名称"), currentClassrooms.stream().map(Rows::classroomSummary).toList());
                    showStatus("教室加载完成，共 " + currentClassrooms.size() + " 间。双击教室可编辑名称。");
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    @Override
    protected void handleCreate() {
        showClassroomDialog(null);
    }

    @Override
    protected void handleEdit() {
        showSelectedClassroomEditor();
    }

    @Override
    protected void handleDelete() {
        ClassroomVO selected = selectedClassroom();
        if (selected == null || selected.id() == null) {
            showStatus("请先选择教室。");
            return;
        }
        AppContext.getInstance().apiServices().classroomApi()
                .deleteClassroom(selected.id())
                .whenComplete(UiAsync.onComplete(ignored -> {
                    showStatus("教室已删除。");
                    loadRemoteData();
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    private void showSelectedClassroomEditor() {
        ClassroomVO selected = selectedClassroom();
        if (selected == null || selected.id() == null) {
            showStatus("请先在表格中选择要编辑的教室。");
            return;
        }
        showClassroomDialog(selected);
    }

    private ClassroomVO selectedClassroom() {
        int index = dataTable.getSelectionModel().getSelectedIndex();
        if (index < 0 || index >= currentClassrooms.size()) {
            return null;
        }
        return currentClassrooms.get(index);
    }

    private void showClassroomDialog(ClassroomVO classroom) {
        boolean editing = classroom != null && classroom.id() != null;
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(editing ? "编辑教室" : "新增教室");
        dialog.setHeaderText(editing ? "修改教室名称" : "填写教室名称");
        ButtonType saveButtonType = new ButtonType(editing ? "保存" : "新增", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().setAll(saveButtonType, ButtonType.CANCEL);

        TextField nameField = new TextField(editing ? Rows.text(classroom.name()) : "");
        Label messageLabel = new Label();
        nameField.setPromptText("教室名称");
        messageLabel.getStyleClass().add("empty-state");
        messageLabel.setWrapText(true);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.addRow(0, new Label("教室名称"), nameField);
        grid.add(messageLabel, 0, 1, 2, 1);
        dialog.getDialogPane().setContent(grid);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            event.consume();
            String name = value(nameField);
            if (name == null || name.isBlank()) {
                messageLabel.setText("请填写教室名称。");
                return;
            }
            saveButton.setDisable(true);
            var classroomApi = AppContext.getInstance().apiServices().classroomApi();
            var action = editing
                    ? classroomApi.updateClassroom(classroom.id(), new ClassroomRequest(name))
                    : classroomApi.createClassroom(new ClassroomRequest(name));
            action.whenComplete(UiAsync.onComplete(ignored -> {
                showStatus(editing ? "教室已更新。" : "教室已新增。");
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
}
