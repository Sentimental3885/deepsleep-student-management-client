package com.deepsleep.ui.admin;

import com.deepsleep.api.dto.admin.CreateStudentRequest;
import com.deepsleep.api.dto.admin.UpdateAdminStudentRequest;
import com.deepsleep.app.AppContext;
import com.deepsleep.ui.common.BaseStaticPageController;
import com.deepsleep.ui.common.StaticPageData;
import com.deepsleep.ui.common.StaticPageSpec;
import com.deepsleep.ui.common.UiAsync;

public class AdminStudentFormController extends BaseStaticPageController {
    @Override
    protected StaticPageSpec pageSpec() {
        return StaticPageData.adminStudentForm();
    }

    @Override
    protected void handleCreate() {
        Long userId = formLong("用户 ID");
        if (userId == null) {
            AppContext.getInstance().apiServices().adminApi()
                    .createStudent(new CreateStudentRequest(formLong("学号 ssid"), formValue("姓名"), formInt("性别"),
                            formLong("学院"), formLong("专业"), formLong("班级"), formDate("入学日期")))
                    .whenComplete(UiAsync.onComplete(ignored -> showStatus("学生已新增。"), error -> showStatus(UiAsync.errorMessage(error))));
            return;
        }
        AppContext.getInstance().apiServices().adminApi()
                .updateStudent(userId, new UpdateAdminStudentRequest(formLong("学院"), formLong("专业"), formLong("班级"),
                        formValue("职务"), formDate("入学日期")))
                .whenComplete(UiAsync.onComplete(ignored -> showStatus("学生信息已更新。"), error -> showStatus(UiAsync.errorMessage(error))));
    }
}
