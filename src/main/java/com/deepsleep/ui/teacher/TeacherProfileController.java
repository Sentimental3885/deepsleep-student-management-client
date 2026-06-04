package com.deepsleep.ui.teacher;

import com.deepsleep.api.dto.user.UpdateTeacherProfileRequest;
import com.deepsleep.app.AppContext;
import com.deepsleep.ui.common.BaseStaticPageController;
import com.deepsleep.ui.common.Rows;
import com.deepsleep.ui.common.StaticPageData;
import com.deepsleep.ui.common.StaticPageSpec;
import com.deepsleep.ui.common.UiAsync;

public class TeacherProfileController extends BaseStaticPageController {
    @Override
    protected StaticPageSpec pageSpec() {
        return StaticPageData.teacherProfile();
    }

    @Override
    protected void loadRemoteData() {
        AppContext.getInstance().apiServices().teacherApi().getProfile()
                .whenComplete(UiAsync.onComplete(profile -> setContent("学院：" + Rows.text(profile.deptName())
                        + "\n职称：" + Rows.text(profile.title())
                        + "\n入职日期：" + Rows.text(profile.entryDate())
                        + "\n头像：" + Rows.text(profile.avatar())), error -> showStatus(UiAsync.errorMessage(error))));
    }

    @Override
    protected void handleCreate() {
        AppContext.getInstance().apiServices().teacherApi()
                .updateProfile(new UpdateTeacherProfileRequest(formValue("职称"), formDate("入职日期")))
                .whenComplete(UiAsync.onComplete(ignored -> { showStatus("教师资料已更新。"); loadRemoteData(); }, error -> showStatus(UiAsync.errorMessage(error))));
    }
}
