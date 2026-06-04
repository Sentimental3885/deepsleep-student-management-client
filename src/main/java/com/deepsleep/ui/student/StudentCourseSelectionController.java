package com.deepsleep.ui.student;

import com.deepsleep.api.dto.PageQuery;
import com.deepsleep.app.AppContext;
import com.deepsleep.ui.common.BaseStaticPageController;
import com.deepsleep.ui.common.Rows;
import com.deepsleep.ui.common.StaticPageData;
import com.deepsleep.ui.common.StaticPageSpec;
import com.deepsleep.ui.common.UiAsync;

import java.util.List;

public class StudentCourseSelectionController extends BaseStaticPageController {
    @Override
    protected StaticPageSpec pageSpec() {
        return StaticPageData.studentCourseSelection();
    }

    @Override
    protected void loadRemoteData() {
        AppContext.getInstance().apiServices().selectionApi()
                .listAvailableCourses(new PageQuery(1, 20))
                .whenComplete(UiAsync.onComplete(page -> {
                    setTable(List.of("ID", "课程代码", "课程名称", "教师", "学期", "学分", "容量", "已选", "状态"),
                            page.records().stream().map(Rows::course).toList());
                    showStatus("可选课程加载完成，共 " + Rows.text(page.total()) + " 条。");
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    @Override
    protected void handleCreate() {
        Long courseId = selectedLong(0);
        if (courseId == null) {
            showStatus("请先选择课程。");
            return;
        }
        AppContext.getInstance().apiServices().selectionApi().pickCourse(courseId)
                .whenComplete(UiAsync.onComplete(ignored -> { showStatus("选课成功。"); loadRemoteData(); }, error -> showStatus(UiAsync.errorMessage(error))));
    }
}
