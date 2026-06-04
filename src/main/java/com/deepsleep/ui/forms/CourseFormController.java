package com.deepsleep.ui.forms;

import com.deepsleep.api.dto.course.CreateCourseRequest;
import com.deepsleep.api.dto.course.UpdateCourseRequest;
import com.deepsleep.app.AppContext;
import com.deepsleep.ui.common.BaseStaticPageController;
import com.deepsleep.ui.common.StaticPageData;
import com.deepsleep.ui.common.StaticPageSpec;
import com.deepsleep.ui.common.UiAsync;

public class CourseFormController extends BaseStaticPageController {
    @Override
    protected StaticPageSpec pageSpec() {
        return StaticPageData.courseForm();
    }

    @Override
    protected void handleCreate() {
        Long id = formLong("课程 ID");
        if (id == null) {
            AppContext.getInstance().apiServices().courseApi()
                    .addCourse(new CreateCourseRequest(formValue("课程名称"), formLong("教师 ID"), formInt("容量"),
                            formValue("课程代码"), formValue("学期"), formDecimal("学分"), formInt("状态"),
                            formValue("课程简介"), formLongList("适用班级 IDs")))
                    .whenComplete(UiAsync.onComplete(ignored -> showStatus("课程已新增。"), error -> showStatus(UiAsync.errorMessage(error))));
            return;
        }
        AppContext.getInstance().apiServices().courseApi()
                .updateCourse(id, new UpdateCourseRequest(formInt("容量"), formInt("状态"), formValue("课程简介")))
                .whenComplete(UiAsync.onComplete(ignored -> showStatus("课程已更新。"), error -> showStatus(UiAsync.errorMessage(error))));
    }
}
