package com.deepsleep.ui.admin;

import com.deepsleep.api.dto.PageQuery;
import com.deepsleep.api.dto.course.CourseQuery;
import com.deepsleep.api.dto.exam.CreateExamRequest;
import com.deepsleep.api.dto.exam.UpdateExamRequest;
import com.deepsleep.api.dto.teacher.TeacherOptionQuery;
import com.deepsleep.api.enums.ExamType;
import com.deepsleep.api.vo.ClassroomVO;
import com.deepsleep.api.vo.CourseVO;
import com.deepsleep.api.vo.ExamVO;
import com.deepsleep.api.vo.TeacherOptionVO;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableRow;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class AdminExamsController extends BaseStaticPageController {

    private static final int PAGE_SIZE = 20;
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private List<ExamVO> currentExams = List.of();

    @Override
    protected StaticPageSpec pageSpec() {
        return StaticPageData.adminExams();
    }

    @Override
    protected void configureActions() {
        refreshButton.setText("刷新");
        createButton.setText("发布考试");
        editButton.setText("编辑考试");
        deleteButton.setText("删除考试");
        dataTable.setRowFactory(table -> {
            TableRow<ObservableList<String>> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    dataTable.getSelectionModel().select(row.getIndex());
                    showSelectedExamEditor();
                }
            });
            return row;
        });
    }

    @Override
    protected void loadRemoteData() {
        AppContext.getInstance().apiServices().adminApi()
                .listExams(new PageQuery(1, PAGE_SIZE))
                .whenComplete(UiAsync.onComplete(page -> {
                    currentExams = page.records() == null ? List.of() : page.records();
                    setTable(List.of("课程", "类型", "考试时间", "时长", "教室", "监考教师", "备注"),
                            currentExams.stream().map(this::examRow).toList());
                    showStatus("考试加载完成，共 " + Rows.text(page.total()) + " 条。双击考试可编辑安排。");
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    @Override
    protected void handleCreate() {
        showExamDialog(null);
    }

    @Override
    protected void handleEdit() {
        showSelectedExamEditor();
    }

    @Override
    protected void handleDelete() {
        ExamVO selected = selectedExam();
        if (selected == null || selected.id() == null) {
            showStatus("请先选择考试。");
            return;
        }
        AppContext.getInstance().apiServices().examApi()
                .deleteExam(selected.id())
                .whenComplete(UiAsync.onComplete(ignored -> {
                    showStatus("考试已删除。");
                    loadRemoteData();
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    private void showSelectedExamEditor() {
        ExamVO selected = selectedExam();
        if (selected == null || selected.id() == null) {
            showStatus("请先在表格中选择要编辑的考试。");
            return;
        }
        AppContext.getInstance().apiServices().examApi()
                .getExam(selected.id())
                .whenComplete(UiAsync.onComplete(this::showExamDialog, error -> showStatus(UiAsync.errorMessage(error))));
    }

    private ExamVO selectedExam() {
        int index = dataTable.getSelectionModel().getSelectedIndex();
        if (index < 0 || index >= currentExams.size()) {
            return null;
        }
        return currentExams.get(index);
    }

    private void showExamDialog(ExamVO exam) {
        boolean editing = exam != null && exam.id() != null;
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(editing ? "编辑考试" : "发布考试");
        dialog.setHeaderText(editing ? "修改考试安排" : "填写考试安排");
        ButtonType saveButtonType = new ButtonType(editing ? "保存" : "发布", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().setAll(saveButtonType, ButtonType.CANCEL);
        dialog.getDialogPane().setPrefWidth(620);

        ComboBox<Choice<CourseVO>> courseSelect = new ComboBox<>();
        ComboBox<Choice<Integer>> typeSelect = examTypeSelect(exam == null ? null : exam.type());
        TextField examTimeField = new TextField(exam == null || exam.examTime() == null ? "" : DATE_TIME.format(exam.examTime()));
        TextField durationField = new TextField(exam == null ? "" : Rows.text(exam.duration()));
        ComboBox<Choice<ClassroomVO>> classroomSelect = new ComboBox<>();
        ComboBox<Choice<TeacherOptionVO>> invigilatorSelect = new ComboBox<>();
        TextField remarkField = new TextField(exam == null ? "" : Rows.text(exam.remark()));
        Label messageLabel = new Label();

        courseSelect.setPromptText("选择课程");
        classroomSelect.setPromptText("选择教室");
        invigilatorSelect.setPromptText("选择监考教师");
        examTimeField.setPromptText("yyyy-MM-dd HH:mm");
        durationField.setPromptText("分钟");
        remarkField.setPromptText("备注");
        messageLabel.getStyleClass().add("empty-state");
        messageLabel.setWrapText(true);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        int row = 0;
        if (editing) {
            grid.addRow(row++, new Label("课程"), new Label(Rows.text(exam.courseName())));
        } else {
            grid.addRow(row++, new Label("课程"), courseSelect);
            loadCourses(courseSelect);
        }
        grid.addRow(row++, new Label("考试类型"), typeSelect);
        grid.addRow(row++, new Label("考试时间"), examTimeField);
        grid.addRow(row++, new Label("时长"), durationField);
        grid.addRow(row++, new Label("教室"), classroomSelect);
        grid.addRow(row++, new Label("监考教师"), invigilatorSelect);
        grid.addRow(row++, new Label("备注"), remarkField);
        grid.add(messageLabel, 0, row, 2, 1);
        dialog.getDialogPane().setContent(grid);

        loadClassrooms(classroomSelect, exam == null ? null : exam.classroomId());
        loadTeachers(invigilatorSelect, exam == null ? null : exam.invigilatorId());

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            event.consume();
            messageLabel.setText("");
            Long courseId = editing ? exam.courseId() : requireSelectedId(courseSelect, "课程", messageLabel);
            Integer type = requireExamType(typeSelect, messageLabel);
            LocalDateTime examTime = requireDateTime(examTimeField, messageLabel);
            Integer duration = requireDuration(durationField, messageLabel);
            Long classroomId = requireSelectedId(classroomSelect, "教室", messageLabel);
            Long invigilatorId = requireSelectedId(invigilatorSelect, "监考教师", messageLabel);
            if (hasMessage(messageLabel)) {
                return;
            }
            saveButton.setDisable(true);
            var examApi = AppContext.getInstance().apiServices().examApi();
            var action = editing
                    ? examApi.updateExam(exam.id(), new UpdateExamRequest(type, examTime, duration, classroomId, invigilatorId, value(remarkField)))
                    : examApi.createExam(new CreateExamRequest(courseId, type, examTime, duration, classroomId, invigilatorId, value(remarkField)));
            action.whenComplete(UiAsync.onComplete(ignored -> {
                showStatus(editing ? "考试已更新。" : "考试已新增。");
                loadRemoteData();
                dialog.close();
            }, error -> {
                saveButton.setDisable(false);
                messageLabel.setText(UiAsync.errorMessage(error));
            }));
        });

        dialog.showAndWait();
    }

    private List<String> examRow(ExamVO exam) {
        return List.of(
                Rows.text(exam.courseName()),
                Rows.text(exam.typeName()),
                Rows.text(exam.examTime()),
                Rows.text(exam.duration()),
                Rows.text(exam.classroomName()),
                Rows.text(exam.invigilatorName()),
                Rows.text(exam.remark())
        );
    }

    private void loadCourses(ComboBox<Choice<CourseVO>> comboBox) {
        comboBox.setDisable(true);
        AppContext.getInstance().apiServices().courseApi()
                .listCourses(new CourseQuery(null, null, null, null, null, 1, 100))
                .whenComplete(UiAsync.onComplete(page -> {
                    comboBox.setDisable(false);
                    List<CourseVO> courses = page.records() == null ? List.of() : page.records();
                    comboBox.getItems().setAll(courses.stream()
                            .map(course -> new Choice<>(courseLabel(course), course))
                            .toList());
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    private void loadClassrooms(ComboBox<Choice<ClassroomVO>> comboBox, Long selectedId) {
        comboBox.setDisable(true);
        AppContext.getInstance().apiServices().classroomApi()
                .listClassrooms()
                .whenComplete(UiAsync.onComplete(classrooms -> {
                    comboBox.setDisable(false);
                    comboBox.getItems().setAll(classrooms.stream()
                            .map(classroom -> new Choice<>(Rows.text(classroom.name()), classroom))
                            .toList());
                    selectById(comboBox, selectedId);
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    private void loadTeachers(ComboBox<Choice<TeacherOptionVO>> comboBox, Long selectedId) {
        comboBox.setDisable(true);
        AppContext.getInstance().apiServices().teacherApi()
                .listOptions(new TeacherOptionQuery(null, null, 1, 100))
                .whenComplete(UiAsync.onComplete(page -> {
                    comboBox.setDisable(false);
                    List<TeacherOptionVO> teachers = page.records() == null ? List.of() : page.records();
                    comboBox.getItems().setAll(teachers.stream()
                            .map(teacher -> new Choice<>(teacherLabel(teacher), teacher))
                            .toList());
                    selectById(comboBox, selectedId);
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    private ComboBox<Choice<Integer>> examTypeSelect(Integer selectedType) {
        ComboBox<Choice<Integer>> comboBox = new ComboBox<>();
        comboBox.getItems().setAll(List.of(
                new Choice<>(ExamType.MIDTERM.label(), ExamType.MIDTERM.code()),
                new Choice<>(ExamType.FINAL.label(), ExamType.FINAL.code()),
                new Choice<>(ExamType.MAKEUP.label(), ExamType.MAKEUP.code())
        ));
        comboBox.setPromptText("选择考试类型");
        selectByValue(comboBox, selectedType);
        return comboBox;
    }

    private String courseLabel(CourseVO course) {
        return Rows.text(course.name()) + " / " + Rows.text(course.code()) + " / " + Rows.text(course.semester());
    }

    private String teacherLabel(TeacherOptionVO teacher) {
        return Rows.text(teacher.name()) + " / " + Rows.text(teacher.username())
                + (teacher.deptName() == null || teacher.deptName().isBlank() ? "" : " / " + teacher.deptName());
    }

    private <T> void selectById(ComboBox<Choice<T>> comboBox, Long selectedId) {
        if (selectedId == null) {
            return;
        }
        comboBox.getItems().stream()
                .filter(choice -> selectedId.equals(choice.id()))
                .findFirst()
                .ifPresent(comboBox::setValue);
    }

    private <T> void selectByValue(ComboBox<Choice<T>> comboBox, T selectedValue) {
        if (selectedValue == null) {
            return;
        }
        comboBox.getItems().stream()
                .filter(choice -> selectedValue.equals(choice.value()))
                .findFirst()
                .ifPresent(comboBox::setValue);
    }

    private Integer requireExamType(ComboBox<Choice<Integer>> comboBox, Label messageLabel) {
        Choice<Integer> selected = comboBox.getValue();
        if (selected == null || selected.value() == null) {
            if (!hasMessage(messageLabel)) {
                messageLabel.setText("请选择考试类型。");
            }
            return null;
        }
        return selected.value();
    }

    private <T> Long requireSelectedId(ComboBox<Choice<T>> comboBox, String label, Label messageLabel) {
        Choice<T> selected = comboBox.getValue();
        if (selected == null || selected.id() == null) {
            if (!hasMessage(messageLabel)) {
                messageLabel.setText("请选择" + label + "。");
            }
            return null;
        }
        return selected.id();
    }

    private LocalDateTime requireDateTime(TextField field, Label messageLabel) {
        String value = value(field);
        if (value == null || value.isBlank()) {
            if (!hasMessage(messageLabel)) {
                messageLabel.setText("请填写考试时间。");
            }
            return null;
        }
        String normalized = value.replace(' ', 'T');
        try {
            return LocalDateTime.parse(normalized);
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDateTime.parse(value, DATE_TIME);
            } catch (DateTimeParseException ex) {
                if (!hasMessage(messageLabel)) {
                    messageLabel.setText("考试时间格式应为 yyyy-MM-dd HH:mm。");
                }
                return null;
            }
        }
    }

    private Integer requireDuration(TextField field, Label messageLabel) {
        String value = value(field);
        if (value == null || value.isBlank()) {
            if (!hasMessage(messageLabel)) {
                messageLabel.setText("请填写时长。");
            }
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            if (!hasMessage(messageLabel)) {
                messageLabel.setText("时长请填写数字。");
            }
            return null;
        }
    }

    private boolean hasMessage(Label label) {
        return label.getText() != null && !label.getText().isBlank();
    }

    private String value(TextField field) {
        String value = field.getText();
        return value == null || value.isBlank() ? null : value.trim();
    }

    private record Choice<T>(String label, T value) {
        Long id() {
            return switch (value) {
                case CourseVO course -> course.id();
                case ClassroomVO classroom -> classroom.id();
                case TeacherOptionVO teacher -> teacher.id();
                default -> null;
            };
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public String toString() {
            return label;
        }
    }
}
