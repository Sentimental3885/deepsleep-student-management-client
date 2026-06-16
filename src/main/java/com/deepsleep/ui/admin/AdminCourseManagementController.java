package com.deepsleep.ui.admin;

import com.deepsleep.api.dto.course.CourseQuery;
import com.deepsleep.api.dto.course.CreateCourseRequest;
import com.deepsleep.api.dto.teacher.TeacherOptionQuery;
import com.deepsleep.api.enums.CourseStatus;
import com.deepsleep.api.enums.UserRole;
import com.deepsleep.api.vo.ClazzVO;
import com.deepsleep.api.vo.CourseVO;
import com.deepsleep.api.vo.TeacherOptionVO;
import com.deepsleep.app.AppContext;
import com.deepsleep.ui.common.CourseTables;
import com.deepsleep.ui.common.CourseWorkspacePane;
import com.deepsleep.ui.common.Rows;
import com.deepsleep.ui.common.UiAsync;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.SelectionMode;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdminCourseManagementController {

    @FXML
    private VBox root;

    private final Label statusLabel = new Label("正在加载课程列表...");
    private final TextField keywordField = input("课程名/代码");
    private final ComboBox<String> semesterBox = new ComboBox<>();
    private final ComboBox<Choice<Integer>> statusBox = new ComboBox<>();
    private final ComboBox<Choice<TeacherOptionVO>> teacherFilterBox = new ComboBox<>();
    private final ComboBox<Choice<ClazzVO>> clazzFilterBox = new ComboBox<>();

    private final TableView<ObservableList<String>> courseTable = CourseTables.table(
            List.of("代码", "课程", "教师", "学期", "学分", "容量", "已选", "状态", "开课班级"));
    private CourseWorkspacePane workspace;
    private List<CourseVO> currentCourses = List.of();

    @FXML
    public void initialize() {
        root.getStyleClass().add("page-root");
        workspace = new CourseWorkspacePane(UserRole.ADMIN, statusLabel::setText, this::loadCourses);
        configureFilters();

        SplitPane splitPane = new SplitPane(leftPane(), workspace);
        splitPane.setDividerPositions(0.48);
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        root.getChildren().addAll(header(), splitPane, statusLabel);
        loadCourses();
    }

    private VBox leftPane() {
        Button search = button("查询", "secondary-button");
        search.setOnAction(event -> loadCourses());
        Button reset = button("重置", "secondary-button");
        reset.setOnAction(event -> resetFilters());
        Button create = button("新增课程", "primary-button");
        create.setOnAction(event -> showCreateCourseDialog());
        courseTable.setPrefHeight(620);
        courseTable.getSelectionModel().selectedItemProperty().addListener((ignored, oldRow, row) -> openSelectedCourse());
        return new VBox(12,
                row(keywordField, semesterBox, statusBox, teacherFilterBox, clazzFilterBox, search, reset, create),
                courseTable);
    }

    private void loadCourses() {
        CourseQuery query = new CourseQuery(
                keywordField.getText(),
                "全部".equals(semesterBox.getValue()) ? null : semesterBox.getValue(),
                statusBox.getValue() == null ? null : statusBox.getValue().value(),
                teacherFilterBox.getValue() == null || teacherFilterBox.getValue().value() == null
                        ? null : teacherFilterBox.getValue().value().id(),
                clazzFilterBox.getValue() == null || clazzFilterBox.getValue().value() == null
                        ? null : clazzFilterBox.getValue().value().id(),
                1,
                20
        );
        AppContext.getInstance().apiServices().courseApi().listCourses(query)
                .whenComplete(UiAsync.onComplete(page -> {
                    currentCourses = page.records() == null ? List.of() : page.records();
                    CourseTables.setRows(courseTable, currentCourses.stream().map(this::courseRow).toList());
                    statusLabel.setText("课程列表加载完成，共 " + Rows.text(page.total()) + " 条。");
                }, error -> statusLabel.setText(UiAsync.errorMessage(error))));
    }

    private void showCreateCourseDialog() {
        Dialog<CreateCourseRequest> dialog = new Dialog<>();
        dialog.setTitle("新建课程");
        dialog.setHeaderText("填写课程基础信息并选择授课教师、开课班级");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        TextField nameField = input("课程名称");
        TextField codeField = input("课程代码");
        ComboBox<String> semesterSelect = semesterSelect();
        TextField creditField = input("学分");
        TextField capacityField = input("容量");
        ComboBox<Choice<Integer>> statusSelect = statusSelect();
        ComboBox<Choice<TeacherOptionVO>> teacherSelect = new ComboBox<>();
        teacherSelect.setPromptText("授课教师");
        ListView<Choice<ClazzVO>> clazzList = new ListView<>();
        clazzList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        clazzList.setPrefHeight(160);
        TextArea introduction = new TextArea();
        introduction.setPromptText("课程简介");
        introduction.setPrefRowCount(4);

        loadTeachers(teacherSelect);
        loadClazzes(clazzList);

        GridPane grid = formGrid();
        addField(grid, 0, 0, "课程名称*", nameField);
        addField(grid, 0, 1, "课程代码*", codeField);
        addField(grid, 1, 0, "学期*", semesterSelect);
        addField(grid, 1, 1, "学分*", creditField);
        addField(grid, 2, 0, "容量*", capacityField);
        addField(grid, 2, 1, "状态*", statusSelect);
        addField(grid, 3, 0, "授课教师*", teacherSelect);
        VBox form = new VBox(10,
                grid,
                new Label("开课班级*"),
                clazzList,
                new Label("课程简介"),
                introduction);
        dialog.getDialogPane().setContent(form);
        dialog.setResultConverter(buttonType -> {
            if (buttonType != ButtonType.OK) {
                return null;
            }
            Choice<TeacherOptionVO> teacher = teacherSelect.getValue();
            List<Long> clazzIds = clazzList.getSelectionModel().getSelectedItems().stream()
                    .map(choice -> choice.value().id())
                    .toList();
            return new CreateCourseRequest(
                    nameField.getText(),
                    teacher == null ? null : teacher.value().id(),
                    CourseTables.intValue(capacityField),
                    codeField.getText(),
                    semesterSelect.getValue(),
                    CourseTables.decimalValue(creditField),
                    statusSelect.getValue() == null ? null : statusSelect.getValue().value(),
                    introduction.getText(),
                    clazzIds
            );
        });
        dialog.showAndWait().ifPresent(request -> AppContext.getInstance().apiServices().courseApi()
                .addCourse(request)
                .whenComplete(UiAsync.onComplete(ignored -> {
                    statusLabel.setText("课程已新增。");
                    loadCourses();
                }, error -> statusLabel.setText(UiAsync.errorMessage(error)))));
    }

    private void openSelectedCourse() {
        Long courseId = selectedCourseId();
        if (courseId != null) {
            workspace.selectCourse(courseId);
        }
    }

    private List<String> courseRow(CourseVO course) {
        return List.of(
                Rows.text(course.code()),
                Rows.text(course.name()),
                Rows.text(course.teacherName()),
                Rows.text(course.semester()),
                Rows.text(course.credit()),
                Rows.text(course.capacity()),
                Rows.text(course.size()),
                CourseStatus.of(course.status()).label(),
                clazzNames(course.clazzes())
        );
    }

    private Long selectedCourseId() {
        int index = courseTable.getSelectionModel().getSelectedIndex();
        if (index < 0 || index >= currentCourses.size()) {
            return null;
        }
        return currentCourses.get(index).id();
    }

    private String clazzNames(List<ClazzVO> clazzes) {
        if (clazzes == null || clazzes.isEmpty()) {
            return "";
        }
        return String.join("、", clazzes.stream().map(ClazzVO::name).filter(Objects::nonNull).toList());
    }

    private VBox header() {
        Label title = new Label("课程与排课管理");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("以课程列表为入口维护课程基础信息、开课班级、排课、考试和选课名单。");
        subtitle.getStyleClass().add("page-subtitle");
        return new VBox(6, title, subtitle);
    }

    private HBox row(javafx.scene.Node... children) {
        return new HBox(8, children);
    }

    private GridPane formGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        return grid;
    }

    private void addField(GridPane grid, int row, int pairColumn, String labelText, Node control) {
        int labelColumn = pairColumn * 2;
        Label label = new Label(labelText);
        label.getStyleClass().add("settings-field-label");
        grid.add(label, labelColumn, row);
        grid.add(control, labelColumn + 1, row);
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

    private void configureFilters() {
        semesterBox.getItems().setAll("全部", "2024-2025-1", "2024-2025-2", "2025-2026-1", "2025-2026-2");
        semesterBox.setValue("全部");
        statusBox.getItems().setAll(List.of(new Choice<>("全部", null), new Choice<>("未开课", 0), new Choice<>("开课", 1)));
        statusBox.setValue(statusBox.getItems().getFirst());
        teacherFilterBox.setPromptText("授课教师");
        clazzFilterBox.setPromptText("开课班级");
        loadFilterTeachers();
        loadFilterClazzes();
    }

    private void resetFilters() {
        keywordField.clear();
        semesterBox.setValue("全部");
        statusBox.setValue(statusBox.getItems().getFirst());
        teacherFilterBox.setValue(teacherFilterBox.getItems().isEmpty() ? null : teacherFilterBox.getItems().getFirst());
        clazzFilterBox.setValue(clazzFilterBox.getItems().isEmpty() ? null : clazzFilterBox.getItems().getFirst());
        loadCourses();
    }

    private ComboBox<String> semesterSelect() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().setAll("2024-2025-1", "2024-2025-2", "2025-2026-1", "2025-2026-2");
        comboBox.setPromptText("学期");
        return comboBox;
    }

    private ComboBox<Choice<Integer>> statusSelect() {
        ComboBox<Choice<Integer>> comboBox = new ComboBox<>();
        comboBox.getItems().add(new Choice<>("未开课", 0));
        comboBox.getItems().add(new Choice<>("开课", 1));
        comboBox.setPromptText("状态");
        return comboBox;
    }

    private void loadTeachers(ComboBox<Choice<TeacherOptionVO>> comboBox) {
        AppContext.getInstance().apiServices().teacherApi()
                .listOptions(new TeacherOptionQuery(null, null, 1, 100))
                .whenComplete(UiAsync.onComplete(page -> {
                    List<TeacherOptionVO> teachers = page.records() == null ? List.of() : page.records();
                    comboBox.getItems().setAll(teachers.stream()
                            .map(teacher -> new Choice<>(teacherLabel(teacher), teacher))
                            .toList());
                }, error -> statusLabel.setText(UiAsync.errorMessage(error))));
    }

    private void loadFilterTeachers() {
        AppContext.getInstance().apiServices().teacherApi()
                .listOptions(new TeacherOptionQuery(null, null, 1, 100))
                .whenComplete(UiAsync.onComplete(page -> {
                    List<Choice<TeacherOptionVO>> choices = new ArrayList<>();
                    choices.add(new Choice<>("全部教师", null));
                    List<TeacherOptionVO> teachers = page.records() == null ? List.of() : page.records();
                    choices.addAll(teachers.stream()
                            .map(teacher -> new Choice<>(teacherLabel(teacher), teacher))
                            .toList());
                    teacherFilterBox.getItems().setAll(choices);
                    teacherFilterBox.setValue(teacherFilterBox.getItems().getFirst());
                }, error -> statusLabel.setText(UiAsync.errorMessage(error))));
    }

    private void loadClazzes(ListView<Choice<ClazzVO>> listView) {
        AppContext.getInstance().apiServices().organizationApi().listClazzes(null)
                .whenComplete(UiAsync.onComplete(clazzes -> listView.getItems().setAll(clazzes.stream()
                        .map(clazz -> new Choice<>(clazzLabel(clazz), clazz))
                        .toList()), error -> statusLabel.setText(UiAsync.errorMessage(error))));
    }

    private void loadFilterClazzes() {
        AppContext.getInstance().apiServices().organizationApi().listClazzes(null)
                .whenComplete(UiAsync.onComplete(clazzes -> {
                    List<Choice<ClazzVO>> choices = new ArrayList<>();
                    choices.add(new Choice<>("全部班级", null));
                    choices.addAll(clazzes.stream()
                            .map(clazz -> new Choice<>(clazzLabel(clazz), clazz))
                            .toList());
                    clazzFilterBox.getItems().setAll(choices);
                    clazzFilterBox.setValue(clazzFilterBox.getItems().getFirst());
                }, error -> statusLabel.setText(UiAsync.errorMessage(error))));
    }

    private String teacherLabel(TeacherOptionVO teacher) {
        return Rows.text(teacher.name()) + " / " + Rows.text(teacher.username())
                + " / " + Rows.text(teacher.deptName())
                + (teacher.title() == null || teacher.title().isBlank() ? "" : " / " + teacher.title());
    }

    private String clazzLabel(ClazzVO clazz) {
        return Rows.text(clazz.deptName()) + " / " + Rows.text(clazz.majorName()) + " / " + Rows.text(clazz.name());
    }

    private record Choice<T>(String label, T value) {
        @SuppressWarnings("NullableProblems")
        @Override
        public String toString() {
            return label;
        }
    }
}
