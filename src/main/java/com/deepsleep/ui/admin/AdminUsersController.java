package com.deepsleep.ui.admin;

import com.deepsleep.api.dto.admin.UserListQuery;
import com.deepsleep.app.AppContext;
import com.deepsleep.ui.common.BaseStaticPageController;
import com.deepsleep.ui.common.Rows;
import com.deepsleep.ui.common.StaticPageData;
import com.deepsleep.ui.common.StaticPageSpec;
import com.deepsleep.ui.common.UiAsync;

import java.util.List;

public class AdminUsersController extends BaseStaticPageController {
    @Override
    protected StaticPageSpec pageSpec() {
        return StaticPageData.adminUsers();
    }

    @Override
    protected void loadRemoteData() {
        AppContext.getInstance().apiServices().adminApi()
                .listUsers(new UserListQuery(null, null, null, 1, 20))
                .whenComplete(UiAsync.onComplete(page -> {
                    setTable(List.of("ID", "用户名", "姓名", "角色", "手机号", "邮箱", "创建时间"),
                            page.records().stream().map(Rows::adminUser).toList());
                    showStatus("用户加载完成，共 " + Rows.text(page.total()) + " 条。");
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    @Override
    protected void handleCreate() {
        Long userId = selectedLong(0);
        if (userId == null) {
            showStatus("请先选择用户。");
            return;
        }
        AppContext.getInstance().apiServices().adminApi()
                .getUser(userId)
                .whenComplete(UiAsync.onComplete(user -> showStatus("用户详情：" + user.username() + " / " + user.name()),
                        error -> showStatus(UiAsync.errorMessage(error))));
    }

    @Override
    protected void handleEdit() {
        Long userId = selectedLong(0);
        if (userId == null) {
            showStatus("请先选择要重置密码的用户。");
            return;
        }
        AppContext.getInstance().apiServices().adminApi()
                .resetUserPassword(userId)
                .whenComplete(UiAsync.onComplete(ignored -> showStatus("密码已重置。"), error -> showStatus(UiAsync.errorMessage(error))));
    }

    @Override
    protected void handleDelete() {
        Long userId = selectedLong(0);
        if (userId == null) {
            showStatus("请先选择要删除的用户。");
            return;
        }
        AppContext.getInstance().apiServices().adminApi()
                .deleteUser(userId)
                .whenComplete(UiAsync.onComplete(ignored -> {
                    showStatus("用户已删除。");
                    loadRemoteData();
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }
}
