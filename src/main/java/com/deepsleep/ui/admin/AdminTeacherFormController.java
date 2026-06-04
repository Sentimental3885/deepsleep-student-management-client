package com.deepsleep.ui.admin;

import com.deepsleep.api.dto.admin.CreateTeacherRequest;
import com.deepsleep.api.dto.admin.UpdateAdminTeacherRequest;
import com.deepsleep.app.AppContext;
import com.deepsleep.ui.common.BaseStaticPageController;
import com.deepsleep.ui.common.StaticPageData;
import com.deepsleep.ui.common.StaticPageSpec;
import com.deepsleep.ui.common.UiAsync;

public class AdminTeacherFormController extends BaseStaticPageController {
    @Override
    protected StaticPageSpec pageSpec() {
        return StaticPageData.adminTeacherForm();
    }

    @Override
    protected void handleCreate() {
        Long userId = formLong("用户 ID");
        if (userId == null) {
            AppContext.getInstance().apiServices().adminApi()
                    .createTeacher(new CreateTeacherRequest(formLong("工号 tsid"), formValue("姓名"), formInt("性别"),
                            formLong("学院"), formValue("职称"), formDate("入职日期")))
                    .whenComplete(UiAsync.onComplete(ignored -> showStatus("教师已新增。"), error -> showStatus(UiAsync.errorMessage(error))));
            return;
        }
        AppContext.getInstance().apiServices().adminApi()
                .updateTeacher(userId, new UpdateAdminTeacherRequest(formLong("学院"), formValue("职称"), formDate("入职日期")))
                .whenComplete(UiAsync.onComplete(ignored -> showStatus("教师信息已更新。"), error -> showStatus(UiAsync.errorMessage(error))));
    }
}
