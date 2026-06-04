package com.deepsleep.ui.teacher;

import com.deepsleep.api.dto.selection.EndSelectionRequest;
import com.deepsleep.app.AppContext;
import com.deepsleep.ui.common.BaseStaticPageController;
import com.deepsleep.ui.common.Rows;
import com.deepsleep.ui.common.StaticPageData;
import com.deepsleep.ui.common.StaticPageSpec;
import com.deepsleep.ui.common.UiAsync;

import java.util.List;

public class TeacherCourseStudentsController extends BaseStaticPageController {
    @Override
    protected StaticPageSpec pageSpec() {
        return StaticPageData.teacherCourseStudents();
    }

    @Override
    protected void loadRemoteData() {
        Long courseId = formLong("课程 ID");
        if (courseId == null) {
            showStatus("请输入课程 ID 后加载学生名单。");
            return;
        }
        AppContext.getInstance().apiServices().selectionApi().listCourseStudents(courseId)
                .whenComplete(UiAsync.onComplete(students -> {
                    setTable(List.of("学生ID", "学号", "姓名", "选课状态", "成绩"),
                            students.stream().map(Rows::courseStudent).toList());
                    showStatus("课程学生加载完成。");
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    @Override
    protected void handleCreate() {
        Long courseId = formLong("课程 ID");
        Long studentId = formLong("学生 ID");
        if (courseId == null || studentId == null) {
            showStatus("请填写课程 ID 和学生 ID。");
            return;
        }
        AppContext.getInstance().apiServices().selectionApi()
                .endCourse(new EndSelectionRequest(studentId, courseId, formDecimal("成绩")))
                .whenComplete(UiAsync.onComplete(ignored -> { showStatus("成绩已提交/课程已结束。"); loadRemoteData(); }, error -> showStatus(UiAsync.errorMessage(error))));
    }
}
