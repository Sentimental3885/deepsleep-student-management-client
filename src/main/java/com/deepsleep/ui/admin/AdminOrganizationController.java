package com.deepsleep.ui.admin;

import com.deepsleep.api.dto.organization.ClazzRequest;
import com.deepsleep.api.dto.organization.DeptRequest;
import com.deepsleep.api.dto.organization.MajorRequest;
import com.deepsleep.api.service.OrganizationApi;
import com.deepsleep.api.vo.ClazzVO;
import com.deepsleep.api.vo.DeptVO;
import com.deepsleep.api.vo.MajorVO;
import com.deepsleep.app.AppContext;
import com.deepsleep.ui.common.Rows;
import com.deepsleep.ui.common.UiAsync;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Objects;

public class AdminOrganizationController {

    @FXML
    private VBox root;

    private final TextField keywordField = input("搜索学院 / 专业 / 班级");
    private final Label statusLabel = new Label("正在加载组织数据...");

    private final ListView<DeptVO> deptList = new ListView<>();
    private final ListView<MajorVO> majorList = new ListView<>();
    private final ListView<ClazzVO> clazzList = new ListView<>();

    private List<DeptVO> departments = List.of();
    private List<MajorVO> majors = List.of();
    private List<ClazzVO> clazzes = List.of();

    @FXML
    public void initialize() {
        root.getChildren().setAll(header(), toolbar(), columns(), statusLabel);
        statusLabel.getStyleClass().add("empty-state");
        configureLists();
        loadData();
    }

    private VBox header() {
        Label title = new Label("组织架构");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("按学院、专业、班级三级维护组织数据。选择学院后查看专业，选择专业后查看班级。");
        subtitle.getStyleClass().add("page-subtitle");
        return new VBox(6, title, subtitle);
    }

    private HBox toolbar() {
        Button refresh = button("刷新", "secondary-button");
        refresh.setOnAction(event -> loadData());
        Button reset = button("重置筛选", "secondary-button");
        reset.setOnAction(event -> {
            keywordField.clear();
            applyFilters();
        });
        keywordField.textProperty().addListener((ignored, oldValue, newValue) -> applyFilters());
        HBox toolbar = new HBox(8, keywordField, refresh, reset);
        toolbar.getStyleClass().add("toolbar");
        return toolbar;
    }

    private HBox columns() {
        HBox columns = new HBox(14,
                organizationColumn("学院", deptList,
                        button("新增学院", "primary-button", () -> showDepartmentDialog(null)),
                        button("编辑学院", "secondary-button", this::editSelectedDepartment),
                        button("删除学院", "danger-button", this::deleteSelectedDepartment)),
                organizationColumn("专业", majorList,
                        button("新增专业", "primary-button", () -> showMajorDialog(null)),
                        button("编辑专业", "secondary-button", this::editSelectedMajor),
                        button("删除专业", "danger-button", this::deleteSelectedMajor)),
                organizationColumn("班级", clazzList,
                        button("新增班级", "primary-button", () -> showClazzDialog(null)),
                        button("编辑班级", "secondary-button", this::editSelectedClazz),
                        button("删除班级", "danger-button", this::deleteSelectedClazz))
        );
        VBox.setVgrow(columns, Priority.ALWAYS);
        return columns;
    }

    private VBox organizationColumn(String title, ListView<?> listView, Button create, Button edit, Button delete) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("settings-section-title");
        listView.setPrefHeight(520);
        VBox.setVgrow(listView, Priority.ALWAYS);
        HBox actions = new HBox(8, create, edit, delete);
        VBox column = new VBox(12, titleLabel, listView, actions);
        column.getStyleClass().add("form-panel");
        HBox.setHgrow(column, Priority.ALWAYS);
        return column;
    }

    private void configureLists() {
        deptList.setCellFactory(list -> textCell(dept -> Rows.text(dept.name())));
        majorList.setCellFactory(list -> textCell(major -> Rows.text(major.name())));
        clazzList.setCellFactory(list -> textCell(clazz -> Rows.text(clazz.name()) + " / " + Rows.text(clazz.grade()) + "级"));

        deptList.getSelectionModel().selectedItemProperty().addListener((ignored, oldValue, newValue) -> refreshMajors());
        majorList.getSelectionModel().selectedItemProperty().addListener((ignored, oldValue, newValue) -> refreshClazzes());
    }

    private <T> ListCell<T> textCell(java.util.function.Function<T, String> textProvider) {
        return new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : textProvider.apply(item));
            }
        };
    }

    private void loadData() {
        statusLabel.setText("正在加载组织数据...");
        OrganizationApi api = AppContext.getInstance().apiServices().organizationApi();
        api.listDepartments()
                .thenCompose(depts -> api.listMajors(null).thenCompose(majorValues ->
                        api.listClazzes(null).thenApply(clazzValues -> new OrganizationData(depts, majorValues, clazzValues))))
                .whenComplete(UiAsync.onComplete(data -> {
                    departments = data.departments() == null ? List.of() : data.departments();
                    majors = data.majors() == null ? List.of() : data.majors();
                    clazzes = data.clazzes() == null ? List.of() : data.clazzes();
                    applyFilters();
                    statusLabel.setText("组织数据加载完成：学院 " + departments.size()
                            + " 个，专业 " + majors.size() + " 个，班级 " + clazzes.size() + " 个。");
                }, error -> statusLabel.setText(UiAsync.errorMessage(error))));
    }

    private void applyFilters() {
        Long selectedDeptId = selectedDepartment() == null ? null : selectedDepartment().id();
        Long selectedMajorId = selectedMajor() == null ? null : selectedMajor().id();

        List<DeptVO> filteredDepartments = departments.stream()
                .filter(this::matchesDepartment)
                .toList();
        deptList.getItems().setAll(filteredDepartments);
        selectDepartment(selectedDeptId);
        if (deptList.getSelectionModel().getSelectedItem() == null && !filteredDepartments.isEmpty()) {
            deptList.getSelectionModel().selectFirst();
        }

        refreshMajors();
        selectMajor(selectedMajorId);
        if (majorList.getSelectionModel().getSelectedItem() == null && !majorList.getItems().isEmpty()) {
            majorList.getSelectionModel().selectFirst();
        }
        refreshClazzes();
    }

    private void refreshMajors() {
        DeptVO selected = selectedDepartment();
        List<MajorVO> filteredMajors = majors.stream()
                .filter(major -> selected == null || Objects.equals(major.deptId(), selected.id()))
                .filter(this::matchesMajor)
                .toList();
        majorList.getItems().setAll(filteredMajors);
        refreshClazzes();
    }

    private void refreshClazzes() {
        MajorVO selected = selectedMajor();
        List<ClazzVO> filteredClazzes = clazzes.stream()
                .filter(clazz -> selected == null || Objects.equals(clazz.majorId(), selected.id()))
                .filter(this::matchesClazz)
                .toList();
        clazzList.getItems().setAll(filteredClazzes);
    }

    private boolean matchesDepartment(DeptVO dept) {
        String keyword = keyword();
        if (keyword.isBlank()) {
            return true;
        }
        boolean selfMatches = contains(dept.name(), keyword);
        boolean childMatches = majors.stream().anyMatch(major -> Objects.equals(major.deptId(), dept.id()) && matchesMajor(major))
                || clazzes.stream().anyMatch(clazz -> Objects.equals(clazz.deptId(), dept.id()) && matchesClazz(clazz));
        return selfMatches || childMatches;
    }

    private boolean matchesMajor(MajorVO major) {
        String keyword = keyword();
        if (keyword.isBlank()) {
            return true;
        }
        return contains(major.name(), keyword) || contains(major.deptName(), keyword)
                || clazzes.stream().anyMatch(clazz -> Objects.equals(clazz.majorId(), major.id()) && matchesClazz(clazz));
    }

    private boolean matchesClazz(ClazzVO clazz) {
        String keyword = keyword();
        if (keyword.isBlank()) {
            return true;
        }
        return contains(clazz.name(), keyword)
                || contains(clazz.majorName(), keyword)
                || contains(clazz.deptName(), keyword)
                || contains(Rows.text(clazz.grade()), keyword);
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
    }

    private String keyword() {
        String value = keywordField.getText();
        return value == null ? "" : value.trim().toLowerCase();
    }

    private void showDepartmentDialog(DeptVO department) {
        boolean editing = department != null;
        TextField nameField = input("学院名称");
        nameField.setText(editing ? Rows.text(department.name()) : "");
        showFormDialog(editing ? "编辑学院" : "新增学院", grid -> grid.addRow(0, new Label("学院名称"), nameField), () -> {
            String name = requiredText(nameField, "学院名称");
            if (name == null) {
                return false;
            }
            OrganizationApi api = AppContext.getInstance().apiServices().organizationApi();
            var action = editing ? api.updateDepartment(department.id(), new DeptRequest(name)) : api.createDepartment(new DeptRequest(name));
            save(action, editing ? "学院已更新。" : "学院已新增。");
            return true;
        });
    }

    private void showMajorDialog(MajorVO major) {
        DeptVO defaultDepartment = major == null ? selectedDepartment() : departmentById(major.deptId());
        if (defaultDepartment == null && major == null) {
            statusLabel.setText("请先选择学院，再新增专业。");
            return;
        }
        boolean editing = major != null;
        TextField nameField = input("专业名称");
        nameField.setText(editing ? Rows.text(major.name()) : "");
        ComboBox<Choice<DeptVO>> deptSelect = departmentSelect(defaultDepartment);
        showFormDialog(editing ? "编辑专业" : "新增专业", grid -> {
            grid.addRow(0, new Label("所属学院"), deptSelect);
            grid.addRow(1, new Label("专业名称"), nameField);
        }, () -> {
            DeptVO department = choiceValue(deptSelect);
            String name = requiredText(nameField, "专业名称");
            if (department == null || name == null) {
                statusLabel.setText(department == null ? "请选择所属学院。" : statusLabel.getText());
                return false;
            }
            OrganizationApi api = AppContext.getInstance().apiServices().organizationApi();
            MajorRequest request = new MajorRequest(name, department.id());
            var action = editing ? api.updateMajor(major.id(), request) : api.createMajor(request);
            save(action, editing ? "专业已更新。" : "专业已新增。");
            return true;
        });
    }

    private void showClazzDialog(ClazzVO clazz) {
        MajorVO defaultMajor = clazz == null ? selectedMajor() : majorById(clazz.majorId());
        DeptVO defaultDepartment = clazz == null ? selectedDepartment() : departmentById(clazz.deptId());
        if (defaultMajor == null && clazz == null) {
            statusLabel.setText("请先选择专业，再新增班级。");
            return;
        }
        boolean editing = clazz != null;
        TextField nameField = input("班级名称");
        nameField.setText(editing ? Rows.text(clazz.name()) : "");
        TextField gradeField = input("年级");
        gradeField.setText(editing ? Rows.text(clazz.grade()) : "");
        ComboBox<Choice<DeptVO>> deptSelect = departmentSelect(defaultDepartment);
        ComboBox<Choice<MajorVO>> majorSelect = majorSelect(defaultDepartment, defaultMajor);
        deptSelect.setOnAction(event -> majorSelect.getItems().setAll(majorChoices(choiceValue(deptSelect))));

        showFormDialog(editing ? "编辑班级" : "新增班级", grid -> {
            grid.addRow(0, new Label("所属学院"), deptSelect);
            grid.addRow(1, new Label("所属专业"), majorSelect);
            grid.addRow(2, new Label("年级"), gradeField);
            grid.addRow(3, new Label("班级名称"), nameField);
        }, () -> {
            DeptVO department = choiceValue(deptSelect);
            MajorVO major = choiceValue(majorSelect);
            String name = requiredText(nameField, "班级名称");
            Integer grade = requiredInt(gradeField, "年级");
            if (department == null || major == null || name == null || grade == null) {
                if (department == null) {
                    statusLabel.setText("请选择所属学院。");
                } else if (major == null) {
                    statusLabel.setText("请选择所属专业。");
                }
                return false;
            }
            OrganizationApi api = AppContext.getInstance().apiServices().organizationApi();
            ClazzRequest request = new ClazzRequest(name, department.id(), major.id(), grade);
            var action = editing ? api.updateClazz(clazz.id(), request) : api.createClazz(request);
            save(action, editing ? "班级已更新。" : "班级已新增。");
            return true;
        });
    }

    private void deleteSelectedDepartment() {
        DeptVO selected = selectedDepartment();
        if (selected == null) {
            statusLabel.setText("请先选择学院。");
            return;
        }
        if (confirmDelete("删除学院", "删除学院前需确保该学院下没有专业、学生或教师。")) {
            save(AppContext.getInstance().apiServices().organizationApi().deleteDepartment(selected.id()), "学院已删除。");
        }
    }

    private void editSelectedDepartment() {
        DeptVO selected = selectedDepartment();
        if (selected == null) {
            statusLabel.setText("请先选择要编辑的学院。");
            return;
        }
        showDepartmentDialog(selected);
    }

    private void editSelectedMajor() {
        MajorVO selected = selectedMajor();
        if (selected == null) {
            statusLabel.setText("请先选择要编辑的专业。");
            return;
        }
        showMajorDialog(selected);
    }

    private void editSelectedClazz() {
        ClazzVO selected = selectedClazz();
        if (selected == null) {
            statusLabel.setText("请先选择要编辑的班级。");
            return;
        }
        showClazzDialog(selected);
    }

    private void deleteSelectedMajor() {
        MajorVO selected = selectedMajor();
        if (selected == null) {
            statusLabel.setText("请先选择专业。");
            return;
        }
        if (confirmDelete("删除专业", "删除专业前需确保该专业下没有班级或学生。")) {
            save(AppContext.getInstance().apiServices().organizationApi().deleteMajor(selected.id()), "专业已删除。");
        }
    }

    private void deleteSelectedClazz() {
        ClazzVO selected = selectedClazz();
        if (selected == null) {
            statusLabel.setText("请先选择班级。");
            return;
        }
        if (confirmDelete("删除班级", "删除班级前需确保该班级下没有学生。")) {
            save(AppContext.getInstance().apiServices().organizationApi().deleteClazz(selected.id()), "班级已删除。");
        }
    }

    private void showFormDialog(String title, java.util.function.Consumer<GridPane> contentBuilder, SaveAction saveAction) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(title);
        ButtonType saveButtonType = new ButtonType("保存", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().setAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        contentBuilder.accept(grid);
        dialog.getDialogPane().setContent(grid);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setOnAction(event -> {
            event.consume();
            if (saveAction.save()) {
                dialog.close();
            }
        });
        dialog.showAndWait();
    }

    private void save(java.util.concurrent.CompletableFuture<Void> action, String successMessage) {
        action.whenComplete(UiAsync.onComplete(ignored -> {
            statusLabel.setText(successMessage);
            loadData();
        }, error -> statusLabel.setText(UiAsync.errorMessage(error))));
    }

    private boolean confirmDelete(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private ComboBox<Choice<DeptVO>> departmentSelect(DeptVO selected) {
        ComboBox<Choice<DeptVO>> comboBox = new ComboBox<>();
        comboBox.setPromptText("选择学院");
        comboBox.getItems().setAll(departments.stream()
                .map(dept -> new Choice<>(Rows.text(dept.name()), dept))
                .toList());
        selectChoice(comboBox, selected);
        return comboBox;
    }

    private ComboBox<Choice<MajorVO>> majorSelect(DeptVO department, MajorVO selected) {
        ComboBox<Choice<MajorVO>> comboBox = new ComboBox<>();
        comboBox.setPromptText("选择专业");
        comboBox.getItems().setAll(majorChoices(department));
        selectChoice(comboBox, selected);
        return comboBox;
    }

    private List<Choice<MajorVO>> majorChoices(DeptVO department) {
        Long deptId = department == null ? null : department.id();
        return majors.stream()
                .filter(major -> deptId == null || Objects.equals(major.deptId(), deptId))
                .map(major -> new Choice<>(Rows.text(major.name()), major))
                .toList();
    }

    private <T> void selectChoice(ComboBox<Choice<T>> comboBox, T selected) {
        if (selected == null) {
            return;
        }
        comboBox.getItems().stream()
                .filter(choice -> Objects.equals(entityId(choice.value()), entityId(selected)))
                .findFirst()
                .ifPresent(comboBox::setValue);
    }

    private Object entityId(Object value) {
        return switch (value) {
            case DeptVO dept -> dept.id();
            case MajorVO major -> major.id();
            case ClazzVO clazz -> clazz.id();
            default -> null;
        };
    }

    private <T> T choiceValue(ComboBox<Choice<T>> comboBox) {
        return comboBox.getValue() == null ? null : comboBox.getValue().value();
    }

    private String requiredText(TextField field, String label) {
        String value = field.getText();
        if (value == null || value.isBlank()) {
            statusLabel.setText("请填写" + label + "。");
            return null;
        }
        return value.trim();
    }

    private Integer requiredInt(TextField field, String label) {
        String value = requiredText(field, label);
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            statusLabel.setText(label + "请填写数字。");
            return null;
        }
    }

    private DeptVO selectedDepartment() {
        return deptList.getSelectionModel().getSelectedItem();
    }

    private MajorVO selectedMajor() {
        return majorList.getSelectionModel().getSelectedItem();
    }

    private ClazzVO selectedClazz() {
        return clazzList.getSelectionModel().getSelectedItem();
    }

    private void selectDepartment(Long id) {
        deptList.getItems().stream()
                .filter(dept -> Objects.equals(dept.id(), id))
                .findFirst()
                .ifPresent(deptList.getSelectionModel()::select);
    }

    private void selectMajor(Long id) {
        majorList.getItems().stream()
                .filter(major -> Objects.equals(major.id(), id))
                .findFirst()
                .ifPresent(majorList.getSelectionModel()::select);
    }

    private DeptVO departmentById(Long id) {
        return departments.stream().filter(dept -> Objects.equals(dept.id(), id)).findFirst().orElse(null);
    }

    private MajorVO majorById(Long id) {
        return majors.stream().filter(major -> Objects.equals(major.id(), id)).findFirst().orElse(null);
    }

    private TextField input(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.getStyleClass().add("form-input");
        return field;
    }

    private Button button(String text, String styleClass) {
        Button button = new Button(text);
        button.getStyleClass().add(styleClass);
        return button;
    }

    private Button button(String text, String styleClass, Runnable action) {
        Button button = button(text, styleClass);
        button.setOnAction(event -> action.run());
        return button;
    }

    private record OrganizationData(List<DeptVO> departments, List<MajorVO> majors, List<ClazzVO> clazzes) {
    }

    private record Choice<T>(String label, T value) {
        @SuppressWarnings("NullableProblems")
        @Override
        public String toString() {
            return label;
        }
    }

    @FunctionalInterface
    private interface SaveAction {
        boolean save();
    }
}
