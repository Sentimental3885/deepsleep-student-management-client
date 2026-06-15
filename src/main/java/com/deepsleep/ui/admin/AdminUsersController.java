package com.deepsleep.ui.admin;

import com.deepsleep.api.dto.admin.UserListQuery;
import com.deepsleep.app.AppContext;
import com.deepsleep.ui.common.BaseStaticPageController;
import com.deepsleep.ui.common.Rows;
import com.deepsleep.ui.common.StaticPageData;
import com.deepsleep.ui.common.StaticPageSpec;
import com.deepsleep.ui.common.UiAsync;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.util.List;

public class AdminUsersController extends BaseStaticPageController {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final TextField nameFilter = new TextField();
    private final TextField usernameFilter = new TextField();
    private final ComboBox<RoleChoice> roleFilter = new ComboBox<>();
    private final ComboBox<Integer> pageSizeSelect = new ComboBox<>();
    private final Label pageInfo = new Label("第 1 / 1 页");

    private int pageNum = 1;
    private int pageSize = DEFAULT_PAGE_SIZE;
    private long total = 0;
    private long pages = 1;

    @Override
    protected StaticPageSpec pageSpec() {
        return StaticPageData.adminUsers();
    }

    @Override
    protected void configureActions() {
        refreshButton.setText("查询");
        createButton.setText("查看详情");
        editButton.setText("重置密码");

        nameFilter.setPromptText("姓名");
        usernameFilter.setPromptText("用户名");
        nameFilter.getStyleClass().add("form-input");
        usernameFilter.getStyleClass().add("form-input");

        roleFilter.getItems().setAll(List.of(
                new RoleChoice("全部角色", null),
                new RoleChoice("管理员", 0),
                new RoleChoice("教师", 1),
                new RoleChoice("学生", 2)
        ));
        roleFilter.setValue(roleFilter.getItems().getFirst());

        pageSizeSelect.getItems().setAll(List.of(10, 20, 50, 100));
        pageSizeSelect.setValue(DEFAULT_PAGE_SIZE);
        pageSizeSelect.setOnAction(event -> {
            Integer value = pageSizeSelect.getValue();
            pageSize = value == null ? DEFAULT_PAGE_SIZE : value;
            pageNum = 1;
            loadRemoteData();
        });

        Button reset = new Button("重置");
        reset.getStyleClass().add("secondary-button");
        reset.setOnAction(event -> resetFilters());
        Button first = pageButton("首页", () -> goToPage(1));
        Button previous = pageButton("上一页", () -> goToPage(pageNum - 1));
        Button next = pageButton("下一页", () -> goToPage(pageNum + 1));
        Button last = pageButton("末页", () -> goToPage((int) pages));

        formBox.getChildren().setAll(
                new HBox(8, nameFilter, usernameFilter, roleFilter, pageSizeSelect, reset),
                new HBox(8, first, previous, pageInfo, next, last)
        );
        formBox.setVisible(true);
        formBox.setManaged(true);
    }

    @Override
    protected void loadRemoteData() {
        AppContext.getInstance().apiServices().adminApi()
                .listUsers(new UserListQuery(
                        value(nameFilter),
                        value(usernameFilter),
                        roleFilter.getValue() == null ? null : roleFilter.getValue().value(),
                        pageNum,
                        pageSize
                ))
                .whenComplete(UiAsync.onComplete(page -> {
                    setTable(List.of("ID", "用户名", "姓名", "角色", "手机号", "邮箱", "创建时间"),
                            page.records().stream().map(Rows::adminUser).toList());
                    total = page.total() == null ? 0 : page.total();
                    pages = Math.max(1, page.pages() == null ? 1 : page.pages());
                    pageNum = page.current() == null ? pageNum : page.current().intValue();
                    updatePageInfo();
                    showStatus("用户加载完成，共 " + Rows.text(total) + " 条。");
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    @Override
    protected void handleRefresh() {
        pageNum = 1;
        loadRemoteData();
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

    private void goToPage(int targetPage) {
        if (targetPage < 1 || targetPage > pages) {
            return;
        }
        pageNum = targetPage;
        loadRemoteData();
    }

    private void resetFilters() {
        nameFilter.clear();
        usernameFilter.clear();
        roleFilter.setValue(roleFilter.getItems().getFirst());
        pageSizeSelect.setValue(DEFAULT_PAGE_SIZE);
        pageSize = DEFAULT_PAGE_SIZE;
        pageNum = 1;
        loadRemoteData();
    }

    private void updatePageInfo() {
        pageInfo.setText("第 " + pageNum + " / " + pages + " 页，共 " + total + " 条");
    }

    private Button pageButton(String text, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add("secondary-button");
        button.setOnAction(event -> action.run());
        return button;
    }

    private String value(TextField field) {
        String value = field.getText();
        return value == null || value.isBlank() ? null : value.trim();
    }

    private record RoleChoice(String label, Integer value) {
        @SuppressWarnings("NullableProblems")
        @Override
        public String toString() {
            return label;
        }
    }
}
