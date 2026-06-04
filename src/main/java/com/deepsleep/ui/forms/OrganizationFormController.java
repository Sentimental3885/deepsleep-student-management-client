package com.deepsleep.ui.forms;

import com.deepsleep.api.dto.organization.ClazzRequest;
import com.deepsleep.api.dto.organization.DeptRequest;
import com.deepsleep.api.dto.organization.MajorRequest;
import com.deepsleep.app.AppContext;
import com.deepsleep.ui.common.BaseStaticPageController;
import com.deepsleep.ui.common.StaticPageData;
import com.deepsleep.ui.common.StaticPageSpec;
import com.deepsleep.ui.common.UiAsync;

public class OrganizationFormController extends BaseStaticPageController {
    @Override
    protected StaticPageSpec pageSpec() {
        return StaticPageData.organizationForm();
    }

    @Override
    protected void handleCreate() {
        String type = formValue("类型");
        Long id = formLong("ID");
        var api = AppContext.getInstance().apiServices().organizationApi();
        switch (type) {
            case "学院" -> {
                var request = new DeptRequest(formValue("名称"));
                if (id == null) {
                    api.createDepartment(request).whenComplete(UiAsync.onComplete(ignored -> showStatus("学院已新增。"), error -> showStatus(UiAsync.errorMessage(error))));
                } else {
                    api.updateDepartment(id, request).whenComplete(UiAsync.onComplete(ignored -> showStatus("学院已更新。"), error -> showStatus(UiAsync.errorMessage(error))));
                }
            }
            case "专业" -> {
                var request = new MajorRequest(formValue("名称"), formLong("所属学院"));
                if (id == null) {
                    api.createMajor(request).whenComplete(UiAsync.onComplete(ignored -> showStatus("专业已新增。"), error -> showStatus(UiAsync.errorMessage(error))));
                } else {
                    api.updateMajor(id, request).whenComplete(UiAsync.onComplete(ignored -> showStatus("专业已更新。"), error -> showStatus(UiAsync.errorMessage(error))));
                }
            }
            case "班级" -> {
                var request = new ClazzRequest(formValue("名称"), formLong("所属学院"), formLong("所属专业"), formInt("年级"));
                if (id == null) {
                    api.createClazz(request).whenComplete(UiAsync.onComplete(ignored -> showStatus("班级已新增。"), error -> showStatus(UiAsync.errorMessage(error))));
                } else {
                    api.updateClazz(id, request).whenComplete(UiAsync.onComplete(ignored -> showStatus("班级已更新。"), error -> showStatus(UiAsync.errorMessage(error))));
                }
            }
            case null, default -> showStatus("类型请填写：学院、专业或班级。");
        }
    }
}
