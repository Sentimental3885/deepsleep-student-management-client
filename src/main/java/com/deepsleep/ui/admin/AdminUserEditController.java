package com.deepsleep.ui.admin;

import com.deepsleep.api.dto.admin.UpdateAdminUserRequest;
import com.deepsleep.app.AppContext;
import com.deepsleep.ui.common.BaseStaticPageController;
import com.deepsleep.ui.common.StaticPageData;
import com.deepsleep.ui.common.StaticPageSpec;
import com.deepsleep.ui.common.UiAsync;

public class AdminUserEditController extends BaseStaticPageController {
    @Override
    protected StaticPageSpec pageSpec() {
        return StaticPageData.adminUserEdit();
    }

    @Override
    protected void handleCreate() {
        Long userId = formLong("用户 ID");
        if (userId == null) {
            showStatus("请填写用户 ID。");
            return;
        }
        AppContext.getInstance().apiServices().adminApi()
                .updateUser(userId, new UpdateAdminUserRequest(formValue("姓名"), formInt("性别"), formValue("手机号"), formValue("邮箱")))
                .whenComplete(UiAsync.onComplete(ignored -> showStatus("用户基础信息已更新。"), error -> showStatus(UiAsync.errorMessage(error))));
    }
}
