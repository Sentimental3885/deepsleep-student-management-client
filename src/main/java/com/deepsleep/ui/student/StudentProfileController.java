package com.deepsleep.ui.student;

import com.deepsleep.api.dto.user.UpdateStudentProfileRequest;
import com.deepsleep.app.AppContext;
import com.deepsleep.ui.common.BaseStaticPageController;
import com.deepsleep.ui.common.Rows;
import com.deepsleep.ui.common.StaticPageData;
import com.deepsleep.ui.common.StaticPageSpec;
import com.deepsleep.ui.common.UiAsync;

public class StudentProfileController extends BaseStaticPageController {
    @Override
    protected StaticPageSpec pageSpec() {
        return StaticPageData.studentProfile();
    }

    @Override
    protected void loadRemoteData() {
        AppContext.getInstance().apiServices().studentApi().getProfile()
                .whenComplete(UiAsync.onComplete(profile -> setContent("学院：" + Rows.text(profile.deptName())
                        + "\n专业：" + Rows.text(profile.majorName())
                        + "\n班级：" + Rows.text(profile.clazzName())
                        + "\n职务：" + Rows.text(profile.position())
                        + "\n入学日期：" + Rows.text(profile.entryDate())
                        + "\n头像：" + Rows.text(profile.avatar())), error -> showStatus(UiAsync.errorMessage(error))));
    }

    @Override
    protected void handleCreate() {
        AppContext.getInstance().apiServices().studentApi()
                .updateProfile(new UpdateStudentProfileRequest(formLong("班级"), formValue("职务"), formDate("入学日期")))
                .whenComplete(UiAsync.onComplete(ignored -> { showStatus("学生资料已更新。"); loadRemoteData(); }, error -> showStatus(UiAsync.errorMessage(error))));
    }
}
