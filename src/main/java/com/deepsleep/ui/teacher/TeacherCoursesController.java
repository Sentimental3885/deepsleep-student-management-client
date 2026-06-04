package com.deepsleep.ui.teacher;

import com.deepsleep.app.AppContext;
import com.deepsleep.ui.common.BaseStaticPageController;
import com.deepsleep.ui.common.Rows;
import com.deepsleep.ui.common.StaticPageData;
import com.deepsleep.ui.common.StaticPageSpec;
import com.deepsleep.ui.common.UiAsync;

import java.util.List;

public class TeacherCoursesController extends BaseStaticPageController {
    @Override
    protected StaticPageSpec pageSpec() {
        return StaticPageData.teacherCourses();
    }

    @Override
    protected void loadRemoteData() {
        AppContext.getInstance().apiServices().teacherApi().getCourses()
                .whenComplete(UiAsync.onComplete(courses -> {
                    setTable(List.of("ID", "课程代码", "课程名称", "学期", "学分", "容量", "已选", "状态"),
                            courses.stream().map(Rows::teacherCourse).toList());
                    showStatus("授课课程加载完成。");
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    @Override
    protected void handleCreate() {
        Long id = selectedLong(0);
        showStatus(id == null ? "请先选择课程，再进入学生名单。" : "已选择课程 ID：" + id + "，请到“课程学生”页填写该课程 ID。");
    }
}
