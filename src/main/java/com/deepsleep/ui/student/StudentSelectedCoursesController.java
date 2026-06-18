package com.deepsleep.ui.student;

import com.deepsleep.api.dto.PageQuery;
import com.deepsleep.app.AppContext;
import com.deepsleep.ui.common.BaseStaticPageController;
import com.deepsleep.ui.common.Rows;
import com.deepsleep.ui.common.StaticPageData;
import com.deepsleep.ui.common.StaticPageSpec;
import com.deepsleep.ui.common.UiAsync;

import java.util.List;

public class StudentSelectedCoursesController extends BaseStaticPageController {
    @Override
    protected StaticPageSpec pageSpec() {
        return StaticPageData.studentSelectedCourses();
    }

    @Override
    protected void loadRemoteData() {
        AppContext.getInstance().apiServices().selectionApi()
                .listSelections(new PageQuery(1, 20))
                .whenComplete(UiAsync.onComplete(page -> {
                    setTable(List.of("ID", "课程代码", "课程名称", "教师", "学期", "学分", "容量", "人数", "状态"),
                            page.records().stream().map(Rows::course).toList());
                    showStatus("已选课程加载完成，共 " + Rows.text(page.total()) + " 条。");
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    @Override
    protected void handleEdit() {
        Long courseId = selectedLong(0);
        if (courseId == null) {
            showStatus("请先选择课程。");
            return;
        }
        AppContext.getInstance().apiServices().selectionApi().dropCourse(courseId)
                .whenComplete(UiAsync.onComplete(ignored -> { showStatus("退课成功。"); loadRemoteData(); }, error -> showStatus(UiAsync.errorMessage(error))));
    }
}
