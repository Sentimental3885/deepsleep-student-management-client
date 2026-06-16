package com.deepsleep.ui.admin;

import com.deepsleep.api.dto.admin.CreateStudentRequest;
import com.deepsleep.api.dto.admin.CreateTeacherRequest;
import com.deepsleep.api.dto.admin.UpdateAdminStudentRequest;
import com.deepsleep.api.dto.admin.UpdateAdminTeacherRequest;
import com.deepsleep.api.dto.admin.UpdateAdminUserRequest;
import com.deepsleep.api.dto.admin.UserListQuery;
import com.deepsleep.api.enums.UserRole;
import com.deepsleep.api.vo.AdminUserDetailVO;
import com.deepsleep.api.vo.AdminUserVO;
import com.deepsleep.api.vo.ClazzVO;
import com.deepsleep.api.vo.MajorVO;
import com.deepsleep.app.AppContext;
import com.deepsleep.ui.common.BaseStaticPageController;
import com.deepsleep.ui.common.Rows;
import com.deepsleep.ui.common.StaticPageData;
import com.deepsleep.ui.common.StaticPageSpec;
import com.deepsleep.ui.common.UiAsync;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AdminUsersController extends BaseStaticPageController {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final TextField nameFilter = new TextField();
    private final TextField usernameFilter = new TextField();
    private final ComboBox<Choice<Integer>> roleFilter = new ComboBox<>();
    private final ComboBox<Integer> pageSizeSelect = new ComboBox<>();
    private final Label pageInfo = new Label("第 1 / 1 页");

    private int pageNum = 1;
    private int pageSize = DEFAULT_PAGE_SIZE;
    private long total = 0;
    private long pages = 1;
    private List<AdminUserVO> currentUsers = List.of();

    @Override
    protected StaticPageSpec pageSpec() {
        return StaticPageData.adminUsers();
    }

    @Override
    protected void configureActions() {
        refreshButton.setText("查询");
        createButton.setText("编辑资料");
        editButton.setText("重置密码");

        nameFilter.setPromptText("姓名");
        usernameFilter.setPromptText("用户名");
        nameFilter.getStyleClass().add("form-input");
        usernameFilter.getStyleClass().add("form-input");

        roleFilter.getItems().setAll(List.of(
                new Choice<>("全部角色", null),
                new Choice<>("管理员", 0),
                new Choice<>("教师", 1),
                new Choice<>("学生", 2)
        ));
        roleFilter.setValue(roleFilter.getItems().getFirst());

        pageSizeSelect.getItems().setAll(List.of(10, 20, 50, 100));
        pageSizeSelect.setValue(DEFAULT_PAGE_SIZE);
        pageSizeSelect.setOnAction(event -> {
            Integer value = pageSizeSelect.getValue();
            pageSize = value == null ? DEFAULT_PAGE_SIZE : value;
            pageNum = 1;
            loadRemoteData();
        });

        Button reset = pageButton("重置", this::resetFilters);
        Button createStudent = pageButton("新增学生", this::showCreateStudentDialog);
        createStudent.getStyleClass().setAll("primary-button");
        Button createTeacher = pageButton("新增教师", this::showCreateTeacherDialog);
        createTeacher.getStyleClass().setAll("primary-button");
        Button first = pageButton("首页", () -> goToPage(1));
        Button previous = pageButton("上一页", () -> goToPage(pageNum - 1));
        Button next = pageButton("下一页", () -> goToPage(pageNum + 1));
        Button last = pageButton("末页", () -> goToPage((int) pages));
        Label pageSizePrefix = new Label("每页");
        Label pageSizeSuffix = new Label("条");
        pageSizePrefix.getStyleClass().add("pagination-caption");
        pageSizeSuffix.getStyleClass().add("pagination-caption");

        HBox queryBar = (HBox) refreshButton.getParent();
        queryBar.getChildren().setAll(nameFilter, usernameFilter, roleFilter, reset, refreshButton);

        HBox paginationPanel = new HBox(8, pageSizePrefix, pageSizeSelect, pageSizeSuffix, first, previous, pageInfo, next, last);
        paginationPanel.setAlignment(Pos.CENTER_LEFT);
        HBox.setMargin(first, new Insets(0, 0, 0, 6));
        paginationPanel.getStyleClass().add("form-panel");
        HBox actionPanel = new HBox(8, createStudent, createTeacher, createButton, editButton, deleteButton);
        actionPanel.getStyleClass().add("form-panel");
        formBox.getStyleClass().remove("form-panel");
        formBox.getChildren().setAll(
                paginationPanel,
                actionPanel
        );
        formBox.setVisible(true);
        formBox.setManaged(true);
    }

    @Override
    protected void loadRemoteData() {
        AppContext.getInstance().apiServices().adminApi()
                .listUsers(new UserListQuery(
                        value(nameFilter),
                        value(usernameFilter),
                        roleFilter.getValue() == null ? null : roleFilter.getValue().value(),
                        pageNum,
                        pageSize
                ))
                .whenComplete(UiAsync.onComplete(page -> {
                    currentUsers = page.records() == null ? List.of() : page.records();
                    setTable(List.of("用户名", "姓名", "角色", "手机号", "邮箱", "创建时间"),
                            currentUsers.stream().map(Rows::adminUser).toList());
                    total = page.total() == null ? 0 : page.total();
                    pages = Math.max(1, page.pages() == null ? 1 : page.pages());
                    pageNum = page.current() == null ? pageNum : page.current().intValue();
                    updatePageInfo();
                    showStatus("用户加载完成，共 " + Rows.text(total) + " 条。");
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    @Override
    protected void handleRefresh() {
        pageNum = 1;
        loadRemoteData();
    }

    @Override
    protected void handleCreate() {
        Long userId = selectedUserId();
        if (userId == null) {
            showStatus("请先选择要编辑的用户。");
            return;
        }
        AppContext.getInstance().apiServices().adminApi()
                .getUser(userId)
                .whenComplete(UiAsync.onComplete(this::showUserEditDialog, error -> showStatus(UiAsync.errorMessage(error))));
    }

    @Override
    protected void handleEdit() {
        Long userId = selectedUserId();
        if (userId == null) {
            showStatus("请先选择要重置密码的用户。");
            return;
        }
        AppContext.getInstance().apiServices().adminApi()
                .resetUserPassword(userId)
                .whenComplete(UiAsync.onComplete(ignored -> showStatus("密码已重置。"), error -> showStatus(UiAsync.errorMessage(error))));
    }

    @Override
    protected void handleDelete() {
        Long userId = selectedUserId();
        if (userId == null) {
            showStatus("请先选择要删除的用户。");
            return;
        }
        AppContext.getInstance().apiServices().adminApi()
                .deleteUser(userId)
                .whenComplete(UiAsync.onComplete(ignored -> {
                    showStatus("用户已删除。");
                    loadRemoteData();
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    private void showCreateStudentDialog() {
        Dialog<ButtonType> dialog = dialog("新增学生", "创建学生账号与学生扩展信息");
        TextField ssidField = new TextField();
        TextField nameField = new TextField();
        ComboBox<Choice<Integer>> genderSelect = genderSelect(null);
        OrgFields orgFields = orgFields(true);
        DatePicker entryDatePicker = new DatePicker();
        Label messageLabel = messageLabel();
        ssidField.setPromptText("数字学号");
        nameField.setPromptText("姓名");

        GridPane grid = dialogGrid();
        grid.addRow(0, new Label("学号"), ssidField);
        grid.addRow(1, new Label("姓名"), nameField);
        grid.addRow(2, new Label("性别"), genderSelect);
        grid.addRow(3, new Label("学院"), orgFields.deptSelect());
        grid.addRow(4, new Label("专业"), orgFields.majorSelect());
        grid.addRow(5, new Label("班级"), orgFields.clazzSelect());
        grid.addRow(6, new Label("入学日期"), entryDatePicker);
        grid.add(messageLabel, 0, 7, 2, 1);
        dialog.getDialogPane().setContent(grid);
        loadDepartments(orgFields, null, null, null);

        Button saveButton = saveButton(dialog);
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            event.consume();
            messageLabel.setText("");
            Long ssid = requireLong(value(ssidField), "学号", messageLabel);
            String name = requireName(nameField, messageLabel);
            Integer gender = requireSelected(genderSelect, "性别", messageLabel);
            Long deptId = requireSelected(orgFields.deptSelect(), "学院", messageLabel);
            Long majorId = requireSelected(orgFields.majorSelect(), "专业", messageLabel);
            Long clazzId = requireSelected(orgFields.clazzSelect(), "班级", messageLabel);
            LocalDate entryDate = requireEntryDate(entryDatePicker, messageLabel);
            if (hasMessage(messageLabel)) {
                return;
            }
            saveButton.setDisable(true);
            AppContext.getInstance().apiServices().adminApi()
                    .createStudent(new CreateStudentRequest(ssid, name, gender, deptId, majorId, clazzId, entryDate))
                    .whenComplete(UiAsync.onComplete(ignored -> {
                        showStatus("学生已新增。");
                        loadRemoteData();
                        dialog.close();
                    }, error -> {
                        saveButton.setDisable(false);
                        messageLabel.setText(UiAsync.errorMessage(error));
                    }));
        });

        dialog.showAndWait();
    }

    private void showCreateTeacherDialog() {
        Dialog<ButtonType> dialog = dialog("新增教师", "创建教师账号与教师扩展信息");
        TextField tsidField = new TextField();
        TextField nameField = new TextField();
        ComboBox<Choice<Integer>> genderSelect = genderSelect(null);
        OrgFields orgFields = orgFields(false);
        TextField titleField = new TextField();
        DatePicker entryDatePicker = new DatePicker();
        Label messageLabel = messageLabel();
        tsidField.setPromptText("数字工号");
        nameField.setPromptText("姓名");

        GridPane grid = dialogGrid();
        grid.addRow(0, new Label("工号"), tsidField);
        grid.addRow(1, new Label("姓名"), nameField);
        grid.addRow(2, new Label("性别"), genderSelect);
        grid.addRow(3, new Label("学院"), orgFields.deptSelect());
        grid.addRow(4, new Label("职称"), titleField);
        grid.addRow(5, new Label("入职日期"), entryDatePicker);
        grid.add(messageLabel, 0, 6, 2, 1);
        dialog.getDialogPane().setContent(grid);
        loadDepartments(orgFields, null, null, null);

        Button saveButton = saveButton(dialog);
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            event.consume();
            messageLabel.setText("");
            Long tsid = requireLong(value(tsidField), "工号", messageLabel);
            String name = requireName(nameField, messageLabel);
            Integer gender = requireSelected(genderSelect, "性别", messageLabel);
            Long deptId = requireSelected(orgFields.deptSelect(), "学院", messageLabel);
            if (hasMessage(messageLabel)) {
                return;
            }
            saveButton.setDisable(true);
            AppContext.getInstance().apiServices().adminApi()
                    .createTeacher(new CreateTeacherRequest(tsid, name, gender, deptId, value(titleField),
                            entryDatePicker.getValue()))
                    .whenComplete(UiAsync.onComplete(ignored -> {
                        showStatus("教师已新增。");
                        loadRemoteData();
                        dialog.close();
                    }, error -> {
                        saveButton.setDisable(false);
                        messageLabel.setText(UiAsync.errorMessage(error));
                    }));
        });

        dialog.showAndWait();
    }

    private void showUserEditDialog(AdminUserDetailVO user) {
        UserRole role = UserRole.of(user.role());
        Dialog<ButtonType> dialog = dialog("编辑用户资料", user.username() + " / " + role.label());

        TextField nameField = new TextField(Rows.text(user.name()));
        ComboBox<Choice<Integer>> genderSelect = genderSelect(user.gender());
        TextField phoneField = new TextField(Rows.text(user.phone()));
        TextField emailField = new TextField(Rows.text(user.email()));
        Label messageLabel = messageLabel();

        GridPane grid = dialogGrid();
        int row = 0;
        grid.addRow(row++, new Label("用户名"), new Label(Rows.text(user.username())));
        grid.addRow(row++, new Label("角色"), new Label(role.label()));
        grid.addRow(row++, new Label("姓名"), nameField);
        grid.addRow(row++, new Label("性别"), genderSelect);
        grid.addRow(row++, new Label("手机号"), phoneField);
        grid.addRow(row++, new Label("邮箱"), emailField);

        OrgFields studentOrgFields = null;
        TextField positionField = null;
        DatePicker studentEntryDatePicker = null;
        OrgFields teacherOrgFields = null;
        TextField titleField = null;
        DatePicker teacherEntryDatePicker = null;

        if (role == UserRole.STUDENT) {
            AdminUserDetailVO.StudentInfo studentInfo = user.studentInfo();
            studentOrgFields = orgFields(true);
            positionField = new TextField(studentInfo == null ? "" : Rows.text(studentInfo.position()));
            studentEntryDatePicker = new DatePicker(studentInfo == null ? null : studentInfo.entryDate());
            grid.addRow(row++, new Label("学院"), studentOrgFields.deptSelect());
            grid.addRow(row++, new Label("专业"), studentOrgFields.majorSelect());
            grid.addRow(row++, new Label("班级"), studentOrgFields.clazzSelect());
            grid.addRow(row++, new Label("职务"), positionField);
            grid.addRow(row++, new Label("入学日期"), studentEntryDatePicker);
            loadDepartments(studentOrgFields,
                    studentInfo == null ? null : studentInfo.deptId(),
                    studentInfo == null ? null : studentInfo.majorId(),
                    studentInfo == null ? null : studentInfo.clazzId());
        } else if (role == UserRole.TEACHER) {
            AdminUserDetailVO.TeacherInfo teacherInfo = user.teacherInfo();
            teacherOrgFields = orgFields(false);
            titleField = new TextField(teacherInfo == null ? "" : Rows.text(teacherInfo.title()));
            teacherEntryDatePicker = new DatePicker(teacherInfo == null ? null : teacherInfo.entryDate());
            grid.addRow(row++, new Label("学院"), teacherOrgFields.deptSelect());
            grid.addRow(row++, new Label("职称"), titleField);
            grid.addRow(row++, new Label("入职日期"), teacherEntryDatePicker);
            loadDepartments(teacherOrgFields, teacherInfo == null ? null : teacherInfo.deptId(), null, null);
        }

        grid.add(messageLabel, 0, row, 2, 1);
        dialog.getDialogPane().setContent(grid);

        OrgFields finalStudentOrgFields = studentOrgFields;
        TextField finalPositionField = positionField;
        DatePicker finalStudentEntryDatePicker = studentEntryDatePicker;
        OrgFields finalTeacherOrgFields = teacherOrgFields;
        TextField finalTitleField = titleField;
        DatePicker finalTeacherEntryDatePicker = teacherEntryDatePicker;

        Button saveButton = saveButton(dialog);
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            event.consume();
            messageLabel.setText("");
            String name = requireName(nameField, messageLabel);
            Integer gender = requireSelected(genderSelect, "性别", messageLabel);
            if (hasMessage(messageLabel)) {
                return;
            }
            saveButton.setDisable(true);
            var adminApi = AppContext.getInstance().apiServices().adminApi();
            CompletableFuture<Void> saveFuture = adminApi.updateUser(user.id(), new UpdateAdminUserRequest(
                    name,
                    gender,
                    value(phoneField),
                    value(emailField)
            ));
            if (role == UserRole.STUDENT) {
                saveFuture = saveFuture.thenCompose(ignored -> adminApi.updateStudent(user.id(),
                        new UpdateAdminStudentRequest(
                                selectedValue(finalStudentOrgFields.deptSelect()),
                                selectedValue(finalStudentOrgFields.majorSelect()),
                                selectedValue(finalStudentOrgFields.clazzSelect()),
                                value(finalPositionField),
                                finalStudentEntryDatePicker.getValue()
                        )));
            } else if (role == UserRole.TEACHER) {
                saveFuture = saveFuture.thenCompose(ignored -> adminApi.updateTeacher(user.id(),
                        new UpdateAdminTeacherRequest(
                                selectedValue(finalTeacherOrgFields.deptSelect()),
                                value(finalTitleField),
                                finalTeacherEntryDatePicker.getValue()
                        )));
            }
            saveFuture.whenComplete(UiAsync.onComplete(ignored -> {
                showStatus("用户资料已更新。");
                loadRemoteData();
                dialog.close();
            }, error -> {
                saveButton.setDisable(false);
                messageLabel.setText(UiAsync.errorMessage(error));
            }));
        });

        dialog.showAndWait();
    }

    private Long selectedUserId() {
        int index = dataTable.getSelectionModel().getSelectedIndex();
        if (index < 0 || index >= currentUsers.size()) {
            return null;
        }
        return currentUsers.get(index).id();
    }

    private void loadDepartments(OrgFields fields, Long selectedDeptId, Long selectedMajorId, Long selectedClazzId) {
        fields.deptSelect().setDisable(true);
        fields.majorSelect().setDisable(true);
        if (fields.includeClazz()) {
            fields.clazzSelect().setDisable(true);
        }
        AppContext.getInstance().apiServices().organizationApi()
                .listDepartments()
                .whenComplete(UiAsync.onComplete(departments -> {
                    fields.deptSelect().setDisable(false);
                    fields.deptSelect().getItems().setAll(departments.stream()
                            .map(dept -> new Choice<>(dept.name(), dept.id()))
                            .toList());
                    selectById(fields.deptSelect(), selectedDeptId);
                    loadMajors(fields, selectedDeptId, selectedMajorId, selectedClazzId);
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    private void loadMajors(OrgFields fields, Long deptId, Long selectedMajorId, Long selectedClazzId) {
        fields.majorSelect().setDisable(true);
        if (fields.includeClazz()) {
            fields.clazzSelect().setDisable(true);
            fields.clazzSelect().getItems().clear();
        }
        AppContext.getInstance().apiServices().organizationApi()
                .listMajors(deptId)
                .whenComplete(UiAsync.onComplete(majors -> {
                    fields.majorSelect().setDisable(false);
                    fields.majorSelect().getItems().setAll(majors.stream()
                            .map(this::majorChoice)
                            .toList());
                    selectById(fields.majorSelect(), selectedMajorId);
                    if (fields.includeClazz()) {
                        loadClazzes(fields, selectedMajorId, selectedClazzId);
                    }
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    private void loadClazzes(OrgFields fields, Long majorId, Long selectedClazzId) {
        fields.clazzSelect().setDisable(true);
        AppContext.getInstance().apiServices().organizationApi()
                .listClazzes(majorId)
                .whenComplete(UiAsync.onComplete(clazzes -> {
                    fields.clazzSelect().setDisable(false);
                    fields.clazzSelect().getItems().setAll(clazzes.stream()
                            .map(this::clazzChoice)
                            .toList());
                    selectById(fields.clazzSelect(), selectedClazzId);
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    private OrgFields orgFields(boolean includeClazz) {
        ComboBox<Choice<Long>> deptSelect = new ComboBox<>();
        ComboBox<Choice<Long>> majorSelect = new ComboBox<>();
        ComboBox<Choice<Long>> clazzSelect = new ComboBox<>();
        deptSelect.setPromptText("选择学院");
        majorSelect.setPromptText("选择专业");
        clazzSelect.setPromptText("选择班级");
        deptSelect.setOnAction(event -> {
            Choice<Long> selected = deptSelect.getValue();
            loadMajors(new OrgFields(deptSelect, majorSelect, clazzSelect, includeClazz),
                    selected == null ? null : selected.value(), null, null);
        });
        majorSelect.setOnAction(event -> {
            if (!includeClazz) {
                return;
            }
            Choice<Long> selected = majorSelect.getValue();
            loadClazzes(new OrgFields(deptSelect, majorSelect, clazzSelect, true),
                    selected == null ? null : selected.value(), null);
        });
        return new OrgFields(deptSelect, majorSelect, clazzSelect, includeClazz);
    }

    private ComboBox<Choice<Integer>> genderSelect(Integer selectedGender) {
        ComboBox<Choice<Integer>> select = new ComboBox<>();
        select.getItems().setAll(List.of(
                new Choice<>("男", 0),
                new Choice<>("女", 1)
        ));
        select.setPromptText("选择性别");
        selectByValue(select, selectedGender);
        return select;
    }

    private Choice<Long> majorChoice(MajorVO major) {
        return new Choice<>(major.name(), major.id());
    }

    private Choice<Long> clazzChoice(ClazzVO clazz) {
        return new Choice<>(clazz.name(), clazz.id());
    }

    private void goToPage(int targetPage) {
        if (targetPage < 1 || targetPage > pages) {
            return;
        }
        pageNum = targetPage;
        loadRemoteData();
    }

    private void resetFilters() {
        nameFilter.clear();
        usernameFilter.clear();
        roleFilter.setValue(roleFilter.getItems().getFirst());
        pageSizeSelect.setValue(DEFAULT_PAGE_SIZE);
        pageSize = DEFAULT_PAGE_SIZE;
        pageNum = 1;
        loadRemoteData();
    }

    private void updatePageInfo() {
        pageInfo.setText("第 " + pageNum + " / " + pages + " 页，共 " + total + " 条");
    }

    private Dialog<ButtonType> dialog(String title, String header) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        ButtonType saveButtonType = new ButtonType("保存", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().setAll(saveButtonType, ButtonType.CANCEL);
        dialog.getDialogPane().setPrefWidth(520);
        return dialog;
    }

    private GridPane dialogGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        return grid;
    }

    private Label messageLabel() {
        Label label = new Label();
        label.getStyleClass().add("empty-state");
        label.setWrapText(true);
        return label;
    }

    private Button saveButton(Dialog<ButtonType> dialog) {
        ButtonType saveButtonType = dialog.getDialogPane().getButtonTypes().stream()
                .filter(type -> type.getButtonData() == ButtonBar.ButtonData.OK_DONE)
                .findFirst()
                .orElseThrow();
        return (Button) dialog.getDialogPane().lookupButton(saveButtonType);
    }

    private Button pageButton(String text, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add("secondary-button");
        button.setOnAction(event -> action.run());
        return button;
    }

    private <T> void selectByValue(ComboBox<Choice<T>> select, T value) {
        select.getItems().stream()
                .filter(choice -> value == null ? choice.value() == null : value.equals(choice.value()))
                .findFirst()
                .ifPresent(select::setValue);
    }

    private void selectById(ComboBox<Choice<Long>> select, Long id) {
        selectByValue(select, id);
    }

    private <T> T selectedValue(ComboBox<Choice<T>> select) {
        Choice<T> choice = select.getValue();
        return choice == null ? null : choice.value();
    }

    private <T> T requireSelected(ComboBox<Choice<T>> select, String label, Label messageLabel) {
        T value = selectedValue(select);
        if (value == null && !hasMessage(messageLabel)) {
            messageLabel.setText("请选择" + label + "。");
        }
        return value;
    }

    private Long requireLong(String value, String label, Label messageLabel) {
        if (value == null || value.isBlank()) {
            if (!hasMessage(messageLabel)) {
                messageLabel.setText("请填写" + label + "。");
            }
            return null;
        }
        try {
            return Long.valueOf(value.trim());
        } catch (NumberFormatException ex) {
            if (!hasMessage(messageLabel)) {
                messageLabel.setText(label + "请填写数字。");
            }
            return null;
        }
    }

    private String requireName(TextField field, Label messageLabel) {
        String value = value(field);
        if ((value == null || value.isBlank()) && !hasMessage(messageLabel)) {
            messageLabel.setText("请填写姓名。");
        }
        return value;
    }

    private LocalDate requireEntryDate(DatePicker picker, Label messageLabel) {
        LocalDate value = picker.getValue();
        if (value == null && !hasMessage(messageLabel)) {
            messageLabel.setText("请选择入学日期。");
        }
        return value;
    }

    private boolean hasMessage(Label label) {
        return label.getText() != null && !label.getText().isBlank();
    }

    private String value(TextField field) {
        String value = field.getText();
        return value == null || value.isBlank() ? null : value.trim();
    }

    private record OrgFields(
            ComboBox<Choice<Long>> deptSelect,
            ComboBox<Choice<Long>> majorSelect,
            ComboBox<Choice<Long>> clazzSelect,
            boolean includeClazz
    ) {
    }

    private record Choice<T>(String label, T value) {
        @SuppressWarnings("NullableProblems")
        @Override
        public String toString() {
            return label;
        }
    }
}
