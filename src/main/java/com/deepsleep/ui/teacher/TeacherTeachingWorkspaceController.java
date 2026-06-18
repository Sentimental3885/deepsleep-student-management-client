package com.deepsleep.ui.teacher;

import com.deepsleep.api.enums.CourseStatus;
import com.deepsleep.api.enums.UserRole;
import com.deepsleep.api.vo.ScheduleVO;
import com.deepsleep.api.vo.TeacherCourseVO;
import com.deepsleep.app.AppContext;
import com.deepsleep.ui.common.CourseTables;
import com.deepsleep.ui.common.CourseWorkspacePane;
import com.deepsleep.ui.common.Rows;
import com.deepsleep.ui.common.UiAsync;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

public class TeacherTeachingWorkspaceController {

    @FXML
    private VBox root;

    private final Label statusLabel = new Label("正在加载教学数据...");
    private final TableView<ObservableList<String>> courseTable = CourseTables.table(
            List.of("ID", "代码", "课程", "学期", "学分", "容量", "人数", "状态"));
    private final TableView<ObservableList<String>> scheduleTable = CourseTables.table(
            List.of("排课ID", "课程ID", "课程", "星期", "节次", "周次", "教室"));
    private CourseWorkspacePane workspace;

    @FXML
    public void initialize() {
        root.getStyleClass().add("page-root");
        workspace = new CourseWorkspacePane(UserRole.TEACHER, statusLabel::setText, this::loadCourses);
        CourseTables.setColumnWidths(courseTable, 74, 98, 168, 120, 76, 76, 76, 88);
        CourseTables.setColumnWidths(scheduleTable, 74, 74, 168, 80, 80, 92, 120);

        TabPane leftTabs = new TabPane(
                tab("授课课程", coursesPane()),
                tab("教学课表", schedulePane())
        );
        leftTabs.getTabs().forEach(tab -> tab.setClosable(false));

        SplitPane splitPane = new SplitPane(leftTabs, workspace);
        splitPane.setDividerPositions(0.44);
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        root.getChildren().addAll(header(), splitPane, statusLabel);
        loadCourses();
        loadSchedule();
    }

    private VBox coursesPane() {
        Button refresh = button("刷新课程", "secondary-button");
        refresh.setOnAction(event -> loadCourses());
        Button open = button("打开课程空间", "primary-button");
        open.setOnAction(event -> openSelectedCourse());
        courseTable.setPrefHeight(520);
        courseTable.getSelectionModel().selectedItemProperty().addListener((ignored, oldRow, row) -> openSelectedCourse());
        return box(row(refresh, open), courseTable);
    }

    private VBox schedulePane() {
        Button refresh = button("刷新课表", "secondary-button");
        refresh.setOnAction(event -> loadSchedule());
        Button open = button("打开课程空间", "primary-button");
        open.setOnAction(event -> {
            Long courseId = CourseTables.selectedLong(scheduleTable, 1);
            workspace.selectCourse(courseId);
        });
        scheduleTable.setPrefHeight(520);
        scheduleTable.getSelectionModel().selectedItemProperty().addListener((ignored, oldRow, row) -> {
            Long courseId = CourseTables.selectedLong(scheduleTable, 1);
            workspace.selectCourse(courseId);
        });
        return box(row(refresh, open), scheduleTable);
    }

    private void loadCourses() {
        AppContext.getInstance().apiServices().teacherApi().getCourses()
                .whenComplete(UiAsync.onComplete(courses -> {
                    CourseTables.setRows(courseTable, courses.stream().map(this::courseRow).toList());
                    statusLabel.setText("授课课程加载完成，共 " + courses.size() + " 门。");
                }, error -> statusLabel.setText(UiAsync.errorMessage(error))));
    }

    private void loadSchedule() {
        AppContext.getInstance().apiServices().teacherApi().getSchedule()
                .whenComplete(UiAsync.onComplete(schedules -> {
                    CourseTables.setRows(scheduleTable, schedules.stream().map(this::scheduleRow).toList());
                    statusLabel.setText("教学课表加载完成。");
                }, error -> statusLabel.setText(UiAsync.errorMessage(error))));
    }

    private void openSelectedCourse() {
        Long courseId = CourseTables.selectedLong(courseTable, 0);
        if (courseId != null) {
            workspace.selectCourse(courseId);
        }
    }

    private List<String> courseRow(TeacherCourseVO course) {
        return List.of(
                Rows.text(course.id()),
                Rows.text(course.code()),
                Rows.text(course.name()),
                Rows.text(course.semester()),
                Rows.text(course.credit()),
                Rows.text(course.capacity()),
                Rows.text(course.size()),
                CourseStatus.of(course.status()).label()
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

    private VBox header() {
        Label title = new Label("教学工作台");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("从授课课程或教学课表进入课程空间，维护排课、考试和学生成绩。");
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

    private Button button(String text, String styleClass) {
        Button button = new Button(text);
        button.getStyleClass().add(styleClass);
        return button;
    }
}
