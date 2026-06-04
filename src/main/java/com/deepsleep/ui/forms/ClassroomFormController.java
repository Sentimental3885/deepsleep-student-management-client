package com.deepsleep.ui.forms;

import com.deepsleep.api.dto.classroom.ClassroomRequest;
import com.deepsleep.app.AppContext;
import com.deepsleep.ui.common.BaseStaticPageController;
import com.deepsleep.ui.common.StaticPageData;
import com.deepsleep.ui.common.StaticPageSpec;
import com.deepsleep.ui.common.UiAsync;

public class ClassroomFormController extends BaseStaticPageController {
    @Override
    protected StaticPageSpec pageSpec() {
        return StaticPageData.classroomForm();
    }

    @Override
    protected void handleCreate() {
        Long id = formLong("教室 ID");
        ClassroomRequest request = new ClassroomRequest(formValue("教室名称"));
        if (id == null) {
            AppContext.getInstance().apiServices().classroomApi().createClassroom(request)
                    .whenComplete(UiAsync.onComplete(ignored -> showStatus("教室已新增。"), error -> showStatus(UiAsync.errorMessage(error))));
        } else {
            AppContext.getInstance().apiServices().classroomApi().updateClassroom(id, request)
                    .whenComplete(UiAsync.onComplete(ignored -> showStatus("教室已更新。"), error -> showStatus(UiAsync.errorMessage(error))));
        }
    }
}
