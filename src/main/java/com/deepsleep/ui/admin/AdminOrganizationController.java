package com.deepsleep.ui.admin;

import com.deepsleep.api.dto.organization.ClazzRequest;
import com.deepsleep.api.dto.organization.DeptRequest;
import com.deepsleep.api.dto.organization.MajorRequest;
import com.deepsleep.app.AppContext;
import com.deepsleep.ui.common.BaseStaticPageController;
import com.deepsleep.ui.common.Rows;
import com.deepsleep.ui.common.StaticPageData;
import com.deepsleep.ui.common.StaticPageSpec;
import com.deepsleep.ui.common.UiAsync;

import java.util.ArrayList;
import java.util.List;

public class AdminOrganizationController extends BaseStaticPageController {
    @Override
    protected StaticPageSpec pageSpec() {
        return StaticPageData.adminOrganization();
    }

    @Override
    protected void loadRemoteData() {
        var api = AppContext.getInstance().apiServices().organizationApi();
        api.listDepartments()
                .thenCompose(depts -> api.listMajors(null).thenCompose(majors ->
                        api.listClazzes(null).thenApply(clazzes -> {
                            List<List<String>> rows = new ArrayList<>();
                            depts.stream().map(Rows::dept).forEach(rows::add);
                            majors.stream().map(Rows::major).forEach(rows::add);
                            clazzes.stream().map(Rows::clazz).forEach(rows::add);
                            return rows;
                        })))
                .whenComplete(UiAsync.onComplete(rows -> {
                    setTable(List.of("ID", "层级", "名称", "上级", "年级"), rows);
                    showStatus("组织数据加载完成。");
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    @Override
    protected void handleCreate() {
        String type = formValue("类型");
        var api = AppContext.getInstance().apiServices().organizationApi();
        switch (type) {
            case "学院" -> api.createDepartment(new DeptRequest(formValue("名称")))
                    .whenComplete(UiAsync.onComplete(ignored -> {
                        showStatus("学院已新增。");
                        loadRemoteData();
                    }, error -> showStatus(UiAsync.errorMessage(error))));
            case "专业" -> api.createMajor(new MajorRequest(formValue("名称"), formLong("学院 ID")))
                    .whenComplete(UiAsync.onComplete(ignored -> {
                        showStatus("专业已新增。");
                        loadRemoteData();
                    }, error -> showStatus(UiAsync.errorMessage(error))));
            case "班级" ->
                    api.createClazz(new ClazzRequest(formValue("名称"), formLong("学院 ID"), formLong("专业 ID"), formInt("年级")))
                            .whenComplete(UiAsync.onComplete(ignored -> {
                                showStatus("班级已新增。");
                                loadRemoteData();
                            }, error -> showStatus(UiAsync.errorMessage(error))));
            case null, default -> showStatus("类型请填写：学院、专业或班级。");
        }
    }

    @Override
    protected void handleEdit() {
        String type = formValue("类型");
        Long id = formLong("ID");
        if (id == null) {
            showStatus("请填写要编辑的 ID。");
            return;
        }
        var api = AppContext.getInstance().apiServices().organizationApi();
        switch (type) {
            case "学院" -> api.updateDepartment(id, new DeptRequest(formValue("名称")))
                    .whenComplete(UiAsync.onComplete(ignored -> {
                        showStatus("学院已更新。");
                        loadRemoteData();
                    }, error -> showStatus(UiAsync.errorMessage(error))));
            case "专业" -> api.updateMajor(id, new MajorRequest(formValue("名称"), formLong("学院 ID")))
                    .whenComplete(UiAsync.onComplete(ignored -> {
                        showStatus("专业已更新。");
                        loadRemoteData();
                    }, error -> showStatus(UiAsync.errorMessage(error))));
            case "班级" ->
                    api.updateClazz(id, new ClazzRequest(formValue("名称"), formLong("学院 ID"), formLong("专业 ID"), formInt("年级")))
                            .whenComplete(UiAsync.onComplete(ignored -> {
                                showStatus("班级已更新。");
                                loadRemoteData();
                            }, error -> showStatus(UiAsync.errorMessage(error))));
            case null, default -> showStatus("类型请填写：学院、专业或班级。");
        }
    }

    @Override
    protected void handleDelete() {
        Long id = selectedLong(0);
        String type = selectedString(1);
        if (id == null || type == null) {
            showStatus("请先选择要删除的组织记录。");
            return;
        }
        var api = AppContext.getInstance().apiServices().organizationApi();
        switch (type) {
            case "学院" -> api.deleteDepartment(id).whenComplete(UiAsync.onComplete(ignored -> {
                showStatus("学院已删除。");
                loadRemoteData();
            }, error -> showStatus(UiAsync.errorMessage(error))));
            case "专业" -> api.deleteMajor(id).whenComplete(UiAsync.onComplete(ignored -> {
                showStatus("专业已删除。");
                loadRemoteData();
            }, error -> showStatus(UiAsync.errorMessage(error))));
            case "班级" -> api.deleteClazz(id).whenComplete(UiAsync.onComplete(ignored -> {
                showStatus("班级已删除。");
                loadRemoteData();
            }, error -> showStatus(UiAsync.errorMessage(error))));
        }
    }
}
