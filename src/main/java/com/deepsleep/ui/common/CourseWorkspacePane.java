package com.deepsleep.ui.common;

import com.deepsleep.api.dto.classroom.ClassroomAvailableQuery;
import com.deepsleep.api.dto.course.CourseClazzUpdateRequest;
import com.deepsleep.api.dto.course.UpdateCourseRequest;
import com.deepsleep.api.dto.exam.CreateExamRequest;
import com.deepsleep.api.dto.exam.UpdateExamRequest;
import com.deepsleep.api.dto.schedule.ClassroomScheduleQuery;
import com.deepsleep.api.dto.schedule.ScheduleRequest;
import com.deepsleep.api.dto.selection.CourseStudentQuery;
import com.deepsleep.api.dto.selection.EndSelectionBatchRequest;
import com.deepsleep.api.dto.teacher.TeacherOptionQuery;
import com.deepsleep.api.enums.CourseStatus;
import com.deepsleep.api.enums.UserRole;
import com.deepsleep.api.vo.ClassroomVO;
import com.deepsleep.api.vo.ClazzVO;
import com.deepsleep.api.vo.CourseStudentVO;
import com.deepsleep.api.vo.CourseVO;
import com.deepsleep.api.vo.ExamVO;
import com.deepsleep.api.vo.ScheduleVO;
import com.deepsleep.api.vo.SelectionCheckVO;
import com.deepsleep.api.vo.TeacherOptionVO;
import com.deepsleep.app.AppContext;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public class CourseWorkspacePane extends VBox {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final UserRole role;
    private final Consumer<String> statusSink;
    private final Runnable externalRefresh;
    private Long courseId;

    private final Label titleLabel = new Label("请选择课程");
    private final Label metaLabel = new Label("从左侧列表或课表中选择一门课程后，这里会展示课程空间。");
    private final TextArea detailArea = new TextArea();
    private final Label clazzLabel = new Label("开课班级：");
    private CourseVO currentCourse;

    private final TableView<ObservableList<String>> scheduleTable = CourseTables.table(
            List.of("排课ID", "课程ID", "课程", "星期", "节次", "周次", "教室"));

    private final TableView<ObservableList<String>> examTable = CourseTables.table(
            List.of("考试ID", "课程", "类型", "时间", "时长", "教室", "监考教师", "备注"));

    private final TableView<ObservableList<String>> studentTable = CourseTables.table(
            List.of("学生ID", "学号", "姓名", "选课状态", "成绩"));
    private final TextField studentKeywordField = input("学生姓名/学号");
    private final ComboBox<Choice<Integer>> studentStatusBox = selectionStatusSelect();
    private final Set<Long> dirtyScoreStudentIds = new HashSet<>();

    private final Label selectionCheckLabel = new Label("选择课程后可查看选课可用性。");
    private final TableView<ObservableList<String>> conflictTable = CourseTables.table(
            List.of("排课ID", "课程ID", "课程", "星期", "节次", "周次", "教室"));

    public CourseWorkspacePane(UserRole role, Consumer<String> statusSink, Runnable externalRefresh) {
        this.role = role;
        this.statusSink = statusSink;
        this.externalRefresh = externalRefresh;
        getStyleClass().add("course-workspace");
        setSpacing(14);
        setPadding(new Insets(4, 0, 0, 0));
        hideTechnicalColumns();
        if (role == UserRole.TEACHER) {
            configureStudentScoreEditing();
        }
        build();
    }

    public Long courseId() {
        return courseId;
    }

    public void selectCourse(Long courseId) {
        this.courseId = courseId;
        if (courseId == null) {
            titleLabel.setText("请选择课程");
            metaLabel.setText("从左侧列表或课表中选择一门课程后，这里会展示课程空间。");
            detailArea.clear();
            CourseTables.setRows(scheduleTable, List.of());
            CourseTables.setRows(examTable, List.of());
            CourseTables.setRows(studentTable, List.of());
            CourseTables.setRows(conflictTable, List.of());
            dirtyScoreStudentIds.clear();
            return;
        }
        loadCourse();
        loadSchedules();
        loadExams();
        if (role == UserRole.ADMIN || role == UserRole.TEACHER) {
            loadStudents();
        }
        if (role == UserRole.STUDENT) {
            checkSelection();
        }
    }

    private void build() {
        titleLabel.getStyleClass().add("page-title");
        metaLabel.getStyleClass().add("page-subtitle");
        detailArea.setEditable(false);
        detailArea.setWrapText(true);

        TabPane tabs = new TabPane();
        tabs.getTabs().add(new Tab("概览", overviewPane()));
        tabs.getTabs().add(new Tab("排课", schedulePane()));
        tabs.getTabs().add(new Tab("考试", examPane()));
        if (role == UserRole.ADMIN || role == UserRole.TEACHER) {
            tabs.getTabs().add(new Tab("学生与成绩", studentsPane()));
        }
        if (role == UserRole.STUDENT) {
            tabs.getTabs().add(new Tab("选课状态", selectionPane()));
        }
        tabs.getTabs().forEach(tab -> tab.setClosable(false));
        VBox.setVgrow(tabs, Priority.ALWAYS);

        getChildren().addAll(titleLabel, metaLabel, tabs);
    }

    private VBox overviewPane() {
        VBox box = section();
        box.getChildren().add(detailArea);
        if (role == UserRole.ADMIN || role == UserRole.TEACHER) {
            Button editSettings = button("编辑课程设置", "primary-button");
            editSettings.setOnAction(event -> showCourseSettingsDialog());
            box.getChildren().add(editSettings);
        }
        if (role == UserRole.ADMIN) {
            Button saveClazzes = button("维护开课班级", "secondary-button");
            saveClazzes.setOnAction(event -> showClazzDialog());
            Button delete = button("删除课程", "danger-button");
            delete.setOnAction(event -> deleteCourse());
            box.getChildren().addAll(clazzLabel, row(saveClazzes, delete));
        }
        return box;
    }

    private VBox schedulePane() {
        VBox box = section();
        scheduleTable.setPrefHeight(260);
        scheduleTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && selectedScheduleId() != null) {
                showScheduleDialog(true);
            }
        });
        Button refresh = button("刷新排课", "secondary-button");
        refresh.setOnAction(event -> loadSchedules());
        Button add = button("新增排课", "primary-button");
        add.setOnAction(event -> showScheduleDialog(false));
        Button update = button("编辑排课", "secondary-button");
        update.setOnAction(event -> showScheduleDialog(true));
        Button delete = button("删除排课", "danger-button");
        delete.setOnAction(event -> deleteSchedule());
        Button classroomSchedule = button("教室占用", "secondary-button");
        classroomSchedule.setOnAction(event -> showClassroomScheduleDialog());
        box.getChildren().addAll(scheduleTable,
                row(refresh, add, update, delete, classroomSchedule));
        return box;
    }

    private VBox examPane() {
        VBox box = section();
        examTable.setPrefHeight(240);
        examTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && selectedExamId() != null) {
                showExamDialog(true);
            }
        });
        Button refresh = button("刷新考试", "secondary-button");
        refresh.setOnAction(event -> loadExams());
        box.getChildren().addAll(examTable, refresh);
        if (role == UserRole.ADMIN || role == UserRole.TEACHER) {
            Button add = button("新增考试", "primary-button");
            add.setOnAction(event -> showExamDialog(false));
            Button update = button("编辑考试", "secondary-button");
            update.setOnAction(event -> showExamDialog(true));
            Button delete = button("删除考试", "danger-button");
            delete.setOnAction(event -> deleteExam());
            box.getChildren().add(row(add, update, delete));
        }
        return box;
    }

    private VBox studentsPane() {
        VBox box = section();
        studentTable.setPrefHeight(260);
        Button refresh = button("查询学生", "secondary-button");
        refresh.setOnAction(event -> loadStudents());
        box.getChildren().addAll(
                row(studentKeywordField, studentStatusBox, refresh),
                studentTable
        );
        if (role == UserRole.TEACHER) {
            Button saveScores = button("保存学生成绩", "primary-button");
            saveScores.setOnAction(event -> saveEditedScores());
            box.getChildren().add(saveScores);
        }
        return box;
    }

    private VBox selectionPane() {
        VBox box = section();
        conflictTable.setPrefHeight(180);
        Button check = button("重新检查", "secondary-button");
        check.setOnAction(event -> checkSelection());
        Button pick = button("选课", "primary-button");
        pick.setOnAction(event -> pickCourse());
        Button drop = button("退课", "danger-button");
        drop.setOnAction(event -> dropCourse());
        box.getChildren().addAll(selectionCheckLabel, conflictTable, row(check, pick, drop));
        return box;
    }

    private void loadCourse() {
        if (missingCourse()) {
            return;
        }
        AppContext.getInstance().apiServices().courseApi().getCourse(courseId)
                .whenComplete(UiAsync.onComplete(course -> {
                    currentCourse = course;
                    titleLabel.setText(course.code() + " " + course.name());
                    metaLabel.setText("教师：" + Rows.text(course.teacherName())
                            + "  学期：" + Rows.text(course.semester())
                            + "  容量：" + Rows.text(course.size()) + "/" + Rows.text(course.capacity())
                            + "  状态：" + CourseStatus.of(course.status()).label());
                    detailArea.setText(courseDetail(course));
                    clazzLabel.setText("开课班级：" + clazzNames(course.clazzes()));
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    private void loadSchedules() {
        if (missingCourse()) {
            return;
        }
        AppContext.getInstance().apiServices().courseApi().listSchedules(courseId)
                .whenComplete(UiAsync.onComplete(schedules -> {
                    CourseTables.setRows(scheduleTable, schedules.stream().map(this::scheduleRow).toList());
                    showStatus("课程排课已加载。");
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    private void loadExams() {
        if (missingCourse()) {
            return;
        }
        AppContext.getInstance().apiServices().courseApi().listCourseExams(courseId)
                .whenComplete(UiAsync.onComplete(exams ->
                        CourseTables.setRows(examTable, exams.stream().map(this::examRow).toList()),
                        error -> showStatus(UiAsync.errorMessage(error))));
    }

    private void loadStudents() {
        if (missingCourse()) {
            return;
        }
        CourseStudentQuery query = new CourseStudentQuery(
                CourseTables.value(studentKeywordField),
                choiceValue(studentStatusBox),
                1,
                100
        );
        AppContext.getInstance().apiServices().selectionApi().listCourseStudents(courseId, query)
                .whenComplete(UiAsync.onComplete(page -> {
                    List<CourseStudentVO> students = page.records() == null ? List.of() : page.records();
                    CourseTables.setRows(studentTable, students.stream().map(Rows::courseStudent).toList());
                    dirtyScoreStudentIds.clear();
                    showStatus("课程学生已加载，共 " + Rows.text(page.total()) + " 人。");
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    private void checkSelection() {
        if (missingCourse()) {
            return;
        }
        AppContext.getInstance().apiServices().selectionApi().checkCourse(courseId)
                .whenComplete(UiAsync.onComplete(check -> {
                    selectionCheckLabel.setText(selectionText(check));
                    List<ScheduleVO> conflicts = check.conflictSchedules() == null ? List.of() : check.conflictSchedules();
                    CourseTables.setRows(conflictTable, conflicts.stream().map(this::scheduleRow).toList());
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    private void updateCourse(UpdateCourseRequest request) {
        if (missingCourse()) {
            return;
        }
        AppContext.getInstance().apiServices().courseApi()
                .updateCourse(courseId, request)
                .whenComplete(UiAsync.onComplete(ignored -> {
                    showStatus("课程基础信息已保存。");
                    loadCourse();
                    refreshExternal();
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    private void updateClazzes(List<Long> clazzIds) {
        if (missingCourse()) {
            return;
        }
        AppContext.getInstance().apiServices().courseApi()
                .updateCourseClazzes(courseId, new CourseClazzUpdateRequest(clazzIds))
                .whenComplete(UiAsync.onComplete(ignored -> {
                    showStatus("开课班级已保存。");
                    loadCourse();
                    refreshExternal();
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    private void deleteCourse() {
        if (missingCourse()) {
            return;
        }
        AppContext.getInstance().apiServices().courseApi().deleteCourse(courseId)
                .whenComplete(UiAsync.onComplete(ignored -> {
                    showStatus("课程已删除。");
                    selectCourse(null);
                    refreshExternal();
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    private void saveSchedule(boolean update, ScheduleRequest request, Long scheduleId) {
        if (missingCourse()) {
            return;
        }
        if (update && scheduleId == null) {
            showStatus("请先选择排课。");
            return;
        }
        var future = update
                ? AppContext.getInstance().apiServices().courseApi().updateSchedule(courseId, scheduleId, request)
                : AppContext.getInstance().apiServices().courseApi().createSchedule(courseId, request);
        future.whenComplete(UiAsync.onComplete(ignored -> {
            showStatus(update ? "排课已更新。" : "排课已新增。");
            loadSchedules();
        }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    private void deleteSchedule() {
        if (missingCourse()) {
            return;
        }
        Long scheduleId = selectedScheduleId();
        if (scheduleId == null) {
            showStatus("请先选择排课。");
            return;
        }
        AppContext.getInstance().apiServices().courseApi().deleteSchedule(courseId, scheduleId)
                .whenComplete(UiAsync.onComplete(ignored -> {
                    showStatus("排课已删除。");
                    loadSchedules();
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    private void loadAvailableClassrooms(ComboBox<Choice<ClassroomVO>> classroomBox,
                                         Integer weekday,
                                         Integer section,
                                         Integer startWeek,
                                         Integer endWeek,
                                         String semester) {
        ClassroomAvailableQuery query = new ClassroomAvailableQuery(
                weekday,
                section,
                startWeek,
                endWeek,
                semester
        );
        AppContext.getInstance().apiServices().classroomApi().listAvailableClassrooms(query)
                .whenComplete(UiAsync.onComplete(classrooms -> {
                    classroomBox.getItems().setAll(classrooms.stream()
                            .map(classroom -> new Choice<>(classroom.name(), classroom))
                            .toList());
                    showStatus("可用教室已加载。");
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    private void loadClassroomSchedule(Long classroomId, String semester, Integer startWeek, Integer endWeek, Integer weekday) {
        ClassroomScheduleQuery query = new ClassroomScheduleQuery(
                semester,
                startWeek,
                endWeek,
                weekday
        );
        AppContext.getInstance().apiServices().scheduleApi().listClassroomSchedule(classroomId, query)
                .whenComplete(UiAsync.onComplete(schedules ->
                        showStatus("该教室占用：" + schedules.stream().map(this::scheduleBrief).toList()),
                        error -> showStatus(UiAsync.errorMessage(error))));
    }

    private void saveExam(boolean update, Long examId, UpdateExamRequest request) {
        if (missingCourse()) {
            return;
        }
        if (update && examId == null) {
            showStatus("请先选择考试。");
            return;
        }
        var future = update
                ? AppContext.getInstance().apiServices().examApi().updateExam(examId, request)
                : AppContext.getInstance().apiServices().examApi().createExam(new CreateExamRequest(
                        courseId,
                        request.type(),
                        request.examTime(),
                        request.duration(),
                        request.classroomId(),
                        request.invigilatorId(),
                        request.remark()
                ));
        future.whenComplete(UiAsync.onComplete(ignored -> {
            showStatus(update ? "考试已更新。" : "考试已新增。");
            loadExams();
        }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    private void deleteExam() {
        Long examId = selectedExamId();
        if (examId == null) {
            showStatus("请先选择考试。");
            return;
        }
        AppContext.getInstance().apiServices().examApi().deleteExam(examId)
                .whenComplete(UiAsync.onComplete(ignored -> {
                    showStatus("考试已删除。");
                    loadExams();
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    private void saveEditedScores() {
        if (missingCourse()) {
            return;
        }
        if (role != UserRole.TEACHER) {
            showStatus("当前角色没有修改学生成绩的权限。");
            return;
        }
        if (dirtyScoreStudentIds.isEmpty()) {
            showStatus("没有需要保存的成绩。");
            return;
        }
        List<EndSelectionBatchRequest.Item> items = new ArrayList<>();
        for (ObservableList<String> row : studentTable.getItems()) {
            Long studentId = studentId(row);
            if (studentId == null || !dirtyScoreStudentIds.contains(studentId)) {
                continue;
            }
            BigDecimal score = scoreValue(row);
            if (score == null) {
                return;
            }
            items.add(new EndSelectionBatchRequest.Item(studentId, score));
        }
        if (items.isEmpty()) {
            showStatus("没有需要保存的成绩。");
            return;
        }
        AppContext.getInstance().apiServices().selectionApi()
                .endCourseBatch(new EndSelectionBatchRequest(courseId, items))
                .whenComplete(UiAsync.onComplete(ignored -> {
                    dirtyScoreStudentIds.clear();
                    showStatus("学生成绩已保存。");
                    loadStudents();
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    private void pickCourse() {
        if (missingCourse()) {
            return;
        }
        AppContext.getInstance().apiServices().selectionApi().pickCourse(courseId)
                .whenComplete(UiAsync.onComplete(ignored -> {
                    showStatus("选课成功。");
                    checkSelection();
                    refreshExternal();
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    private void dropCourse() {
        if (missingCourse()) {
            return;
        }
        AppContext.getInstance().apiServices().selectionApi().dropCourse(courseId)
                .whenComplete(UiAsync.onComplete(ignored -> {
                    showStatus("退课成功。");
                    checkSelection();
                    refreshExternal();
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    private void showCourseSettingsDialog() {
        if (missingCourse()) {
            return;
        }
        Dialog<UpdateCourseRequest> dialog = new Dialog<>();
        dialog.setTitle("编辑课程设置");
        dialog.setHeaderText("维护当前课程的容量、状态和简介");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        TextField capacityField = input("容量");
        ComboBox<Choice<Integer>> statusBox = statusSelect();
        TextArea introductionArea = new TextArea();
        introductionArea.setPromptText("课程简介");
        introductionArea.setPrefRowCount(4);

        if (currentCourse != null) {
            capacityField.setText(Rows.text(currentCourse.capacity()));
            statusBox.setValue(statusChoice(currentCourse.status(), statusBox.getItems()));
            introductionArea.setText(Rows.text(currentCourse.introduction()));
        }

        GridPane grid = formGrid();
        addField(grid, 0, 0, "容量*", capacityField);
        addField(grid, 0, 1, "状态*", statusBox);
        VBox content = new VBox(10, grid, new Label("课程简介"), introductionArea);
        dialog.getDialogPane().setContent(content);
        dialog.setResultConverter(buttonType -> buttonType == ButtonType.OK
                ? new UpdateCourseRequest(
                        CourseTables.intValue(capacityField),
                        statusBox.getValue() == null ? null : statusBox.getValue().value(),
                        introductionArea.getText()
                )
                : null);
        dialog.showAndWait().ifPresent(this::updateCourse);
    }

    private void showClazzDialog() {
        if (missingCourse()) {
            return;
        }
        Dialog<List<Long>> dialog = new Dialog<>();
        dialog.setTitle("维护开课班级");
        dialog.setHeaderText("选择该课程面向的班级");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        ListView<Choice<ClazzVO>> clazzList = new ListView<>();
        clazzList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        clazzList.setPrefHeight(260);
        loadClazzes(clazzList, List.of());
        dialog.getDialogPane().setContent(clazzList);
        dialog.setResultConverter(buttonType -> buttonType == ButtonType.OK
                ? clazzList.getSelectionModel().getSelectedItems().stream().map(choice -> choice.value().id()).toList()
                : null);
        dialog.showAndWait().ifPresent(this::updateClazzes);
    }

    private void showScheduleDialog(boolean update) {
        if (missingCourse()) {
            return;
        }
        Long scheduleId = update ? selectedScheduleId() : null;
        if (update && scheduleId == null) {
            showStatus("请先选择排课。");
            return;
        }
        ObservableList<String> selected = scheduleTable.getSelectionModel().getSelectedItem();
        Dialog<ScheduleRequest> dialog = new Dialog<>();
        dialog.setTitle(update ? "编辑排课" : "新增排课");
        dialog.setHeaderText(update ? "修改选中的排课安排" : "为当前课程新增排课安排");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        ComboBox<Choice<Integer>> weekdayBox = weekdaySelect();
        ComboBox<Choice<Integer>> sectionBox = sectionSelect();
        TextField startWeekField = input("开始周");
        TextField endWeekField = input("结束周");
        ComboBox<String> semesterBox = semesterSelect();
        ComboBox<Choice<ClassroomVO>> classroomBox = new ComboBox<>();
        classroomBox.setPromptText("教室");
        Button loadClassrooms = button("加载可用教室", "secondary-button");
        loadClassrooms.setOnAction(event -> loadAvailableClassrooms(
                classroomBox,
                weekdayBox.getValue() == null ? null : weekdayBox.getValue().value(),
                sectionBox.getValue() == null ? null : sectionBox.getValue().value(),
                CourseTables.intValue(startWeekField),
                CourseTables.intValue(endWeekField),
                semesterBox.getValue()
        ));

        if (selected != null) {
            weekdayBox.setValue(statusLikeChoice(valueAt(selected, 3).replace("周", ""), weekdayBox.getItems()));
            sectionBox.setValue(statusLikeChoice(valueAt(selected, 4), sectionBox.getItems()));
            String[] weeks = valueAt(selected, 5).split("-");
            if (weeks.length == 2) {
                startWeekField.setText(weeks[0]);
                endWeekField.setText(weeks[1]);
            }
        }

        HBox classroomRow = row(classroomBox, loadClassrooms);
        GridPane form = formGrid();
        addField(form, 0, 0, "星期*", weekdayBox);
        addField(form, 0, 1, "节次*", sectionBox);
        addField(form, 1, 0, "开始周*", startWeekField);
        addField(form, 1, 1, "结束周*", endWeekField);
        addField(form, 2, 0, "学期", semesterBox);
        addField(form, 2, 1, "教室*", classroomRow);
        dialog.getDialogPane().setContent(form);
        dialog.setResultConverter(buttonType -> buttonType == ButtonType.OK
                ? new ScheduleRequest(
                        weekdayBox.getValue() == null ? null : weekdayBox.getValue().value(),
                        sectionBox.getValue() == null ? null : sectionBox.getValue().value(),
                        CourseTables.intValue(startWeekField),
                        CourseTables.intValue(endWeekField),
                        classroomBox.getValue() == null ? null : classroomBox.getValue().value().id()
                )
                : null);
        dialog.showAndWait().ifPresent(request -> saveSchedule(update, request, scheduleId));
    }

    private void showClassroomScheduleDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("教室占用");
        dialog.setHeaderText("选择教室和条件后查看占用");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        ComboBox<Choice<ClassroomVO>> classroomBox = new ComboBox<>();
        classroomBox.setPromptText("教室");
        ComboBox<String> semesterBox = semesterSelect();
        TextField startWeekField = input("开始周");
        TextField endWeekField = input("结束周");
        ComboBox<Choice<Integer>> weekdayBox = weekdaySelect();
        Button load = button("查看占用", "secondary-button");
        load.setOnAction(event -> {
            if (classroomBox.getValue() == null) {
                showStatus("请先选择教室。");
                return;
            }
            loadClassroomSchedule(
                    classroomBox.getValue().value().id(),
                    semesterBox.getValue(),
                    CourseTables.intValue(startWeekField),
                    CourseTables.intValue(endWeekField),
                    weekdayBox.getValue() == null ? null : weekdayBox.getValue().value()
            );
        });
        loadAllClassrooms(classroomBox);
        GridPane form = formGrid();
        addField(form, 0, 0, "教室*", classroomBox);
        addField(form, 0, 1, "学期", semesterBox);
        addField(form, 1, 0, "开始周", startWeekField);
        addField(form, 1, 1, "结束周", endWeekField);
        addField(form, 2, 0, "星期", weekdayBox);
        addField(form, 2, 1, "", load);
        dialog.getDialogPane().setContent(form);
        dialog.showAndWait();
    }

    private void showExamDialog(boolean update) {
        if (missingCourse()) {
            return;
        }
        Long examId = update ? selectedExamId() : null;
        if (update && examId == null) {
            showStatus("请先选择考试。");
            return;
        }
        ObservableList<String> selected = examTable.getSelectionModel().getSelectedItem();
        Dialog<UpdateExamRequest> dialog = new Dialog<>();
        dialog.setTitle(update ? "编辑考试" : "新增考试");
        dialog.setHeaderText(update ? "修改选中的考试安排" : "为当前课程新增考试安排");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        ComboBox<Choice<Integer>> typeBox = examTypeSelect();
        TextField timeField = input("考试时间 yyyy-MM-dd HH:mm");
        TextField durationField = input("时长");
        ComboBox<Choice<ClassroomVO>> classroomBox = new ComboBox<>();
        classroomBox.setPromptText("教室");
        ComboBox<Choice<TeacherOptionVO>> invigilatorBox = new ComboBox<>();
        invigilatorBox.setPromptText("监考教师");
        TextField remarkField = input("备注");
        loadAllClassrooms(classroomBox);
        loadTeachers(invigilatorBox);

        if (selected != null) {
            typeBox.setValue(statusLikeChoice(valueAt(selected, 2), typeBox.getItems()));
            timeField.setText(valueAt(selected, 3));
            durationField.setText(valueAt(selected, 4));
            remarkField.setText(valueAt(selected, 7));
        }

        GridPane form = formGrid();
        addField(form, 0, 0, "考试类型*", typeBox);
        addField(form, 0, 1, "考试时间*", timeField);
        addField(form, 1, 0, "时长*", durationField);
        addField(form, 1, 1, "教室*", classroomBox);
        addField(form, 2, 0, "监考教师*", invigilatorBox);
        addField(form, 2, 1, "备注", remarkField);
        dialog.getDialogPane().setContent(form);
        dialog.setResultConverter(buttonType -> buttonType == ButtonType.OK
                ? new UpdateExamRequest(
                        typeBox.getValue() == null ? null : typeBox.getValue().value(),
                        parseDateTime(timeField.getText()),
                        CourseTables.intValue(durationField),
                        classroomBox.getValue() == null ? null : classroomBox.getValue().value().id(),
                        invigilatorBox.getValue() == null ? null : invigilatorBox.getValue().value().id(),
                        remarkField.getText()
                )
                : null);
        dialog.showAndWait().ifPresent(request -> saveExam(update, examId, request));
    }

    private String courseDetail(CourseVO course) {
        return "课程：" + Rows.text(course.name())
                + "\n代码：" + Rows.text(course.code())
                + "\n教师：" + Rows.text(course.teacherName())
                + "\n学期：" + Rows.text(course.semester())
                + "\n学分：" + Rows.text(course.credit())
                + "\n容量：" + Rows.text(course.size()) + "/" + Rows.text(course.capacity())
                + "\n状态：" + CourseStatus.of(course.status()).label()
                + "\n开课班级：" + clazzNames(course.clazzes())
                + "\n选课状态：" + Rows.text(course.mySelectionStatus())
                + "\n不可选原因：" + Rows.text(course.unselectableReason())
                + "\n简介：" + Rows.text(course.introduction());
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

    private List<String> examRow(ExamVO exam) {
        return List.of(
                Rows.text(exam.id()),
                Rows.text(exam.courseName()),
                Rows.text(exam.type()),
                Rows.text(exam.examTime()),
                Rows.text(exam.duration()),
                Rows.text(exam.classroomName()),
                Rows.text(exam.invigilatorName()),
                Rows.text(exam.remark())
        );
    }

    private String selectionText(SelectionCheckVO check) {
        boolean selectable = Boolean.TRUE.equals(check.selectable());
        return selectable ? "当前课程可以选。" : "当前课程不可选：" + Rows.text(check.reason());
    }

    private String scheduleBrief(ScheduleVO schedule) {
        return Rows.text(schedule.courseName()) + " 周" + Rows.text(schedule.weekday())
                + " " + Rows.text(schedule.section())
                + "节 " + Rows.text(schedule.startWeek()) + "-" + Rows.text(schedule.endWeek()) + "周";
    }

    private String clazzNames(List<ClazzVO> clazzes) {
        if (clazzes == null || clazzes.isEmpty()) {
            return "";
        }
        return String.join("、", clazzes.stream().map(ClazzVO::name).filter(Objects::nonNull).toList());
    }

    private Long studentId(ObservableList<String> row) {
        String raw = valueAt(row, 0);
        return raw.isBlank() ? null : Long.valueOf(raw);
    }

    private BigDecimal scoreValue(ObservableList<String> row) {
        String raw = valueAt(row, 4);
        if (raw.isBlank()) {
            showStatus("请填写 " + valueAt(row, 2) + " 的成绩。");
            return null;
        }
        try {
            BigDecimal score = new BigDecimal(raw.trim());
            if (score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(BigDecimal.valueOf(100)) > 0) {
                showStatus(valueAt(row, 2) + " 的成绩必须在 0-100 之间。");
                return null;
            }
            if (score.stripTrailingZeros().scale() > 2) {
                showStatus(valueAt(row, 2) + " 的成绩最多保留 2 位小数。");
                return null;
            }
            return score;
        } catch (NumberFormatException error) {
            showStatus(valueAt(row, 2) + " 的成绩不是有效数字。");
            return null;
        }
    }

    private LocalDateTime parseDateTime(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String normalized = raw.trim().replace(' ', 'T');
        try {
            return LocalDateTime.parse(normalized);
        } catch (DateTimeParseException ignored) {
            return LocalDateTime.parse(raw.trim(), DATE_TIME);
        }
    }

    private boolean missingCourse() {
        if (courseId == null) {
            showStatus("请先选择课程。");
            return true;
        }
        return false;
    }

    private void refreshExternal() {
        if (externalRefresh != null) {
            externalRefresh.run();
        }
    }

    private void showStatus(String message) {
        if (statusSink != null) {
            statusSink.accept(message);
        }
    }

    private void hideTechnicalColumns() {
        if (!scheduleTable.getColumns().isEmpty()) {
            scheduleTable.getColumns().getFirst().setVisible(false);
        }
        if (!examTable.getColumns().isEmpty()) {
            examTable.getColumns().getFirst().setVisible(false);
        }
        if (!studentTable.getColumns().isEmpty()) {
            studentTable.getColumns().getFirst().setVisible(false);
        }
    }

    private void configureStudentScoreEditing() {
        CourseTables.makeEditableTextColumn(studentTable, 4, row -> {
            Long studentId = studentId(row);
            if (studentId != null) {
                dirtyScoreStudentIds.add(studentId);
            }
        });
    }

    private Long selectedScheduleId() {
        return CourseTables.selectedLong(scheduleTable, 0);
    }

    private Long selectedExamId() {
        return CourseTables.selectedLong(examTable, 0);
    }

    private ComboBox<Choice<Integer>> selectionStatusSelect() {
        ComboBox<Choice<Integer>> comboBox = new ComboBox<>();
        comboBox.getItems().setAll(List.of(
                new Choice<>("未退选", null),
                new Choice<>("已选", 1),
                new Choice<>("已退选", 2),
                new Choice<>("已结课", 3)
        ));
        comboBox.setValue(comboBox.getItems().getFirst());
        comboBox.setPromptText("选课状态");
        return comboBox;
    }

    private <T> T choiceValue(ComboBox<Choice<T>> comboBox) {
        return comboBox.getValue() == null ? null : comboBox.getValue().value();
    }

    private ComboBox<Choice<Integer>> statusSelect() {
        ComboBox<Choice<Integer>> comboBox = new ComboBox<>();
        comboBox.getItems().add(new Choice<>("未开课", 0));
        comboBox.getItems().add(new Choice<>("开课", 1));
        comboBox.setPromptText("状态");
        return comboBox;
    }

    private Choice<Integer> statusChoice(Integer status, List<Choice<Integer>> choices) {
        if (status == null) {
            return null;
        }
        return choices.stream()
                .filter(choice -> Objects.equals(choice.value(), status))
                .findFirst()
                .orElse(null);
    }

    private ComboBox<String> semesterSelect() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().setAll("2024-2025-1", "2024-2025-2", "2025-2026-1", "2025-2026-2");
        comboBox.setPromptText("学期");
        return comboBox;
    }

    private ComboBox<Choice<Integer>> weekdaySelect() {
        ComboBox<Choice<Integer>> comboBox = new ComboBox<>();
        comboBox.getItems().setAll(List.of(
                new Choice<>("周一", 1),
                new Choice<>("周二", 2),
                new Choice<>("周三", 3),
                new Choice<>("周四", 4),
                new Choice<>("周五", 5),
                new Choice<>("周六", 6),
                new Choice<>("周日", 7)
        ));
        comboBox.setPromptText("星期");
        return comboBox;
    }

    private ComboBox<Choice<Integer>> sectionSelect() {
        ComboBox<Choice<Integer>> comboBox = new ComboBox<>();
        comboBox.getItems().setAll(List.of(
                new Choice<>("第 1 节", 1),
                new Choice<>("第 2 节", 2),
                new Choice<>("第 3 节", 3),
                new Choice<>("第 4 节", 4),
                new Choice<>("第 5 节", 5)
        ));
        comboBox.setPromptText("节次");
        return comboBox;
    }

    private ComboBox<Choice<Integer>> examTypeSelect() {
        ComboBox<Choice<Integer>> comboBox = new ComboBox<>();
        comboBox.getItems().setAll(List.of(
                new Choice<>("期中", 1),
                new Choice<>("期末", 2),
                new Choice<>("补考", 3)
        ));
        comboBox.setPromptText("考试类型");
        return comboBox;
    }

    private Choice<Integer> statusLikeChoice(String value, List<Choice<Integer>> choices) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        return choices.stream()
                .filter(choice -> normalized.equals(choice.label()) || normalized.equals(Rows.text(choice.value())))
                .findFirst()
                .orElse(null);
    }

    private void loadTeachers(ComboBox<Choice<TeacherOptionVO>> comboBox) {
        AppContext.getInstance().apiServices().teacherApi()
                .listOptions(new TeacherOptionQuery(null, null, 1, 100))
                .whenComplete(UiAsync.onComplete(page -> {
                    List<TeacherOptionVO> teachers = page.records() == null ? List.of() : page.records();
                    comboBox.getItems().setAll(teachers.stream()
                            .map(teacher -> new Choice<>(teacherLabel(teacher), teacher))
                            .toList());
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    private void loadAllClassrooms(ComboBox<Choice<ClassroomVO>> comboBox) {
        AppContext.getInstance().apiServices().classroomApi().listClassrooms()
                .whenComplete(UiAsync.onComplete(classrooms -> comboBox.getItems().setAll(classrooms.stream()
                        .map(classroom -> new Choice<>(classroom.name(), classroom))
                        .toList()), error -> showStatus(UiAsync.errorMessage(error))));
    }

    private void loadClazzes(ListView<Choice<ClazzVO>> listView, List<Long> selectedIds) {
        AppContext.getInstance().apiServices().organizationApi().listClazzes(null)
                .whenComplete(UiAsync.onComplete(clazzes -> {
                    listView.getItems().setAll(clazzes.stream()
                            .map(clazz -> new Choice<>(clazzLabel(clazz), clazz))
                            .toList());
                    if (selectedIds != null && !selectedIds.isEmpty()) {
                        listView.getItems().stream()
                                .filter(choice -> selectedIds.contains(choice.value().id()))
                                .forEach(choice -> listView.getSelectionModel().select(choice));
                    }
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    private String teacherLabel(TeacherOptionVO teacher) {
        return Rows.text(teacher.name()) + " / " + Rows.text(teacher.username())
                + " / " + Rows.text(teacher.deptName())
                + (teacher.title() == null || teacher.title().isBlank() ? "" : " / " + teacher.title());
    }

    private String clazzLabel(ClazzVO clazz) {
        return Rows.text(clazz.deptName()) + " / " + Rows.text(clazz.majorName()) + " / " + Rows.text(clazz.name());
    }

    private VBox section() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(12));
        return box;
    }

    private HBox row(javafx.scene.Node... nodes) {
        HBox row = new HBox(8, nodes);
        row.setFillHeight(true);
        return row;
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

    private String valueAt(ObservableList<String> row, int index) {
        return row.size() > index ? row.get(index) : "";
    }

    private record Choice<T>(String label, T value) {
        @SuppressWarnings("NullableProblems")
        @Override
        public String toString() {
            return label;
        }
    }
}
