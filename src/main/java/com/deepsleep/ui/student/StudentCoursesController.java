package com.deepsleep.ui.student;

import com.deepsleep.api.dto.selection.ScoreListQuery;
import com.deepsleep.api.dto.selection.SelectionQuery;
import com.deepsleep.api.enums.CourseStatus;
import com.deepsleep.api.enums.UserRole;
import com.deepsleep.api.vo.CourseVO;
import com.deepsleep.api.vo.ScoreVO;
import com.deepsleep.api.vo.ScheduleVO;
import com.deepsleep.app.AppContext;
import com.deepsleep.ui.common.CourseTables;
import com.deepsleep.ui.common.CourseWorkspacePane;
import com.deepsleep.ui.common.Rows;
import com.deepsleep.ui.common.UiAsync;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

public class StudentCoursesController {

    @FXML
    private VBox root;

    private final Label statusLabel = new Label("正在加载课程数据...");
    private final TextField keywordField = input("课程名/代码");
    private final TextField semesterField = input("学期");
    private final TextField weekdayField = input("星期");
    private final TextField creditMinField = input("最低学分");
    private final TextField creditMaxField = input("最高学分");
    private final CheckBox noConflictOnly = new CheckBox("仅无冲突");
    private final TextField scoreSemesterField = input("成绩学期");

    private final TableView<ObservableList<String>> availableTable = CourseTables.table(courseColumns());
    private final TableView<ObservableList<String>> selectedTable = CourseTables.table(courseColumns());
    private final TableView<ObservableList<String>> scheduleTable = CourseTables.table(
            List.of("排课ID", "课程ID", "课程", "星期", "节次", "周次", "教室"));
    private final TableView<ObservableList<String>> scoreTable = CourseTables.table(
            List.of("ID", "课程代码", "课程名称", "学期", "学分", "成绩", "GPA", "排名", "最高分", "最低分"));
    private CourseWorkspacePane workspace;

    @FXML
    public void initialize() {
        root.getStyleClass().add("page-root");
        workspace = new CourseWorkspacePane(UserRole.STUDENT, statusLabel::setText, this::loadAll);

        TabPane tabs = new TabPane(
                tab("可选课程", availablePane()),
                tab("已选课程", selectedPane()),
                tab("我的课表", schedulePane()),
                tab("成绩", scorePane())
        );
        tabs.getTabs().forEach(tab -> tab.setClosable(false));

        SplitPane splitPane = new SplitPane(tabs, workspace);
        splitPane.setDividerPositions(0.48);
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        root.getChildren().addAll(header(), splitPane, statusLabel);
        loadAll();
    }

    private VBox availablePane() {
        Button search = button("查询", "secondary-button");
        search.setOnAction(event -> loadAvailableCourses());
        Button detail = button("打开课程", "secondary-button");
        detail.setOnAction(event -> openSelectedCourse(availableTable));
        Button pick = button("选课", "primary-button");
        pick.setOnAction(event -> pickSelectedCourse());
        availableTable.setPrefHeight(420);
        availableTable.getSelectionModel().selectedItemProperty().addListener((ignored, oldRow, row) -> openSelectedCourse(availableTable));
        return box(row(keywordField, semesterField, weekdayField, creditMinField, creditMaxField, noConflictOnly, search),
                availableTable,
                row(detail, pick));
    }

    private VBox selectedPane() {
        Button refresh = button("刷新", "secondary-button");
        refresh.setOnAction(event -> loadSelectedCourses());
        Button detail = button("打开课程", "secondary-button");
        detail.setOnAction(event -> openSelectedCourse(selectedTable));
        Button drop = button("退课", "danger-button");
        drop.setOnAction(event -> dropSelectedCourse());
        selectedTable.setPrefHeight(420);
        selectedTable.getSelectionModel().selectedItemProperty().addListener((ignored, oldRow, row) -> openSelectedCourse(selectedTable));
        return box(row(refresh, detail, drop), selectedTable);
    }

    private VBox schedulePane() {
        Button refresh = button("刷新课表", "secondary-button");
        refresh.setOnAction(event -> loadSchedule());
        Button detail = button("打开课程", "secondary-button");
        detail.setOnAction(event -> {
            Long courseId = CourseTables.selectedLong(scheduleTable, 1);
            workspace.selectCourse(courseId);
        });
        scheduleTable.setPrefHeight(420);
        scheduleTable.getSelectionModel().selectedItemProperty().addListener((ignored, oldRow, row) -> {
            Long courseId = CourseTables.selectedLong(scheduleTable, 1);
            workspace.selectCourse(courseId);
        });
        return box(row(refresh, detail), scheduleTable);
    }

    private VBox scorePane() {
        Button search = button("查询成绩", "secondary-button");
        search.setOnAction(event -> loadScores());
        Button detail = button("打开课程", "secondary-button");
        detail.setOnAction(event -> openSelectedCourse(scoreTable));
        scoreTable.setPrefHeight(420);
        scoreTable.getSelectionModel().selectedItemProperty().addListener((ignored, oldRow, row) -> openSelectedCourse(scoreTable));
        return box(row(scoreSemesterField, search, detail), scoreTable);
    }

    private void loadAll() {
        loadAvailableCourses();
        loadSelectedCourses();
        loadSchedule();
        if (!scoreSemesterField.getText().isBlank()) {
            loadScores();
        }
    }

    private void loadAvailableCourses() {
        AppContext.getInstance().apiServices().selectionApi()
                .listAvailableCourses(selectionQuery())
                .whenComplete(UiAsync.onComplete(page -> {
                    List<CourseVO> records = page.records() == null ? List.of() : page.records();
                    CourseTables.setRows(availableTable, records.stream().map(this::courseRow).toList());
                    statusLabel.setText("可选课程加载完成，共 " + Rows.text(page.total()) + " 条。");
                }, error -> statusLabel.setText(UiAsync.errorMessage(error))));
    }

    private void loadSelectedCourses() {
        AppContext.getInstance().apiServices().selectionApi()
                .listSelections(SelectionQuery.firstPage())
                .whenComplete(UiAsync.onComplete(page -> {
                    List<CourseVO> records = page.records() == null ? List.of() : page.records();
                    CourseTables.setRows(selectedTable, records.stream().map(this::courseRow).toList());
                    statusLabel.setText("已选课程加载完成，共 " + Rows.text(page.total()) + " 条。");
                }, error -> statusLabel.setText(UiAsync.errorMessage(error))));
    }

    private void loadSchedule() {
        AppContext.getInstance().apiServices().studentApi().getSchedule()
                .whenComplete(UiAsync.onComplete(schedules -> {
                    CourseTables.setRows(scheduleTable, schedules.stream().map(this::scheduleRow).toList());
                    statusLabel.setText("课表加载完成。");
                }, error -> statusLabel.setText(UiAsync.errorMessage(error))));
    }

    private void loadScores() {
        if (scoreSemesterField.getText().isBlank()) {
            statusLabel.setText("请先填写成绩学期。");
            return;
        }
        AppContext.getInstance().apiServices().selectionApi()
                .listScores(new ScoreListQuery(scoreSemesterField.getText(), 1, 20))
                .whenComplete(UiAsync.onComplete(page -> {
                    List<ScoreVO> records = page.records() == null ? List.of() : page.records();
                    CourseTables.setRows(scoreTable, records.stream().map(Rows::score).toList());
                    statusLabel.setText("成绩加载完成，共 " + Rows.text(page.total()) + " 条。");
                }, error -> statusLabel.setText(UiAsync.errorMessage(error))));
    }

    private void pickSelectedCourse() {
        Long courseId = CourseTables.selectedLong(availableTable, 0);
        if (courseId == null) {
            statusLabel.setText("请先选择课程。");
            return;
        }
        AppContext.getInstance().apiServices().selectionApi().pickCourse(courseId)
                .whenComplete(UiAsync.onComplete(ignored -> {
                    statusLabel.setText("选课成功。");
                    workspace.selectCourse(courseId);
                    loadAll();
                }, error -> statusLabel.setText(UiAsync.errorMessage(error))));
    }

    private void dropSelectedCourse() {
        Long courseId = CourseTables.selectedLong(selectedTable, 0);
        if (courseId == null) {
            statusLabel.setText("请先选择课程。");
            return;
        }
        AppContext.getInstance().apiServices().selectionApi().dropCourse(courseId)
                .whenComplete(UiAsync.onComplete(ignored -> {
                    statusLabel.setText("退课成功。");
                    workspace.selectCourse(courseId);
                    loadAll();
                }, error -> statusLabel.setText(UiAsync.errorMessage(error))));
    }

    private void openSelectedCourse(TableView<ObservableList<String>> table) {
        Long courseId = CourseTables.selectedLong(table, 0);
        if (courseId != null) {
            workspace.selectCourse(courseId);
        }
    }

    private SelectionQuery selectionQuery() {
        return new SelectionQuery(
                1,
                20,
                keywordField.getText(),
                semesterField.getText(),
                CourseTables.intValue(weekdayField),
                CourseTables.decimalValue(creditMinField),
                CourseTables.decimalValue(creditMaxField),
                noConflictOnly.isSelected() ? Boolean.TRUE : null
        );
    }

    private List<String> courseRow(CourseVO course) {
        return List.of(
                Rows.text(course.id()),
                Rows.text(course.code()),
                Rows.text(course.name()),
                Rows.text(course.teacherName()),
                Rows.text(course.semester()),
                Rows.text(course.credit()),
                Rows.text(course.capacity()),
                Rows.text(course.size()),
                CourseStatus.of(course.status()).label(),
                Boolean.TRUE.equals(course.selectable()) ? "可选" : Rows.text(course.unselectableReason())
        );
    }

    private List<String> scheduleRow(ScheduleVO schedule) {
        return List.of(
                Rows.text(schedule.id()),
                Rows.text(schedule.courseId()),
                Rows.text(schedule.courseName()),
                "周" + Rows.text(schedule.weekday()),
                Rows.text(schedule.section()),
                Rows.text(schedule.startWeek()) + "-" + Rows.text(schedule.endWeek()),
                Rows.text(schedule.classroomName())
        );
    }

    private List<String> courseColumns() {
        return List.of("ID", "代码", "课程", "教师", "学期", "学分", "容量", "已选", "状态", "可选性");
    }

    private VBox header() {
        Label title = new Label("我的课程");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("在一个入口里完成选课、退课、查看课表和成绩，并从任意课程进入课程空间。");
        subtitle.getStyleClass().add("page-subtitle");
        return new VBox(6, title, subtitle);
    }

    private Tab tab(String title, javafx.scene.Node content) {
        return new Tab(title, content);
    }

    private VBox box(javafx.scene.Node... children) {
        return new VBox(10, children);
    }

    private HBox row(javafx.scene.Node... children) {
        return new HBox(8, children);
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
}
