package com.deepsleep.ui.admin;

import com.deepsleep.api.dto.course.CreateCourseRequest;
import com.deepsleep.api.dto.course.UpdateCourseRequest;
import com.deepsleep.app.AppContext;
import com.deepsleep.ui.common.BaseStaticPageController;
import com.deepsleep.ui.common.Rows;
import com.deepsleep.ui.common.StaticPageData;
import com.deepsleep.ui.common.StaticPageSpec;
import com.deepsleep.ui.common.UiAsync;

import java.util.List;

public class AdminCoursesController extends BaseStaticPageController {
    @Override
    protected StaticPageSpec pageSpec() {
        return StaticPageData.adminCourses();
    }

    @Override
    protected void loadRemoteData() {
        showStatus("后端文档暂未提供管理员课程分页/列表接口；可在表单中填写课程 ID 查看详情，或执行新增、更新、删除。");
    }

    @Override
    protected void handleCreate() {
        AppContext.getInstance().apiServices().courseApi()
                .addCourse(new CreateCourseRequest(formValue("课程名称"), formLong("教师 ID"), formInt("容量"),
                        formValue("课程代码"), formValue("学期"), formDecimal("学分"), formInt("状态"),
                        formValue("课程简介"), formLongList("适用班级 IDs")))
                .whenComplete(UiAsync.onComplete(ignored -> showStatus("课程已新增。"), error -> showStatus(UiAsync.errorMessage(error))));
    }

    @Override
    protected void handleEdit() {
        Long id = formLong("课程 ID");
        if (id == null) {
            id = selectedLong(0);
        }
        if (id == null) {
            showStatus("请填写或选择课程 ID。");
            return;
        }
        AppContext.getInstance().apiServices().courseApi()
                .updateCourse(id, new UpdateCourseRequest(formInt("容量"), formInt("状态"), formValue("课程简介")))
                .whenComplete(UiAsync.onComplete(ignored -> showStatus("课程已更新。"), error -> showStatus(UiAsync.errorMessage(error))));
    }

    @Override
    protected void handleDelete() {
        Long id = formLong("课程 ID");
        if (id == null) {
            id = selectedLong(0);
        }
        if (id == null) {
            showStatus("请填写或选择课程 ID。");
            return;
        }
        AppContext.getInstance().apiServices().courseApi()
                .deleteCourse(id)
                .whenComplete(UiAsync.onComplete(ignored -> showStatus("课程已删除。"), error -> showStatus(UiAsync.errorMessage(error))));
    }

    @Override
    protected void handleRefresh() {
        Long id = formLong("课程 ID");
        if (id == null) {
            loadRemoteData();
            return;
        }
        AppContext.getInstance().apiServices().courseApi()
                .getCourse(id)
                .whenComplete(UiAsync.onComplete(course -> {
                    setTable(List.of("ID", "课程代码", "课程名称", "教师", "学期", "学分", "容量", "已选", "状态"),
                            List.of(Rows.course(course)));
                    showStatus("课程详情加载完成。");
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }
}
