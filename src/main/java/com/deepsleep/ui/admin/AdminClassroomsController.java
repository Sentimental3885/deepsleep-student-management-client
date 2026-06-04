package com.deepsleep.ui.admin;

import com.deepsleep.api.dto.classroom.ClassroomRequest;
import com.deepsleep.app.AppContext;
import com.deepsleep.ui.common.BaseStaticPageController;
import com.deepsleep.ui.common.Rows;
import com.deepsleep.ui.common.StaticPageData;
import com.deepsleep.ui.common.StaticPageSpec;
import com.deepsleep.ui.common.UiAsync;

import java.util.List;

public class AdminClassroomsController extends BaseStaticPageController {
    @Override
    protected StaticPageSpec pageSpec() {
        return StaticPageData.adminClassrooms();
    }

    @Override
    protected void loadRemoteData() {
        AppContext.getInstance().apiServices().classroomApi().listClassrooms()
                .whenComplete(UiAsync.onComplete(classrooms -> {
                    setTable(List.of("ID", "教室名称"), classrooms.stream().map(Rows::classroom).toList());
                    showStatus("教室加载完成。");
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    @Override
    protected void handleCreate() {
        AppContext.getInstance().apiServices().classroomApi()
                .createClassroom(new ClassroomRequest(formValue("教室名称")))
                .whenComplete(UiAsync.onComplete(ignored -> { showStatus("教室已新增。"); loadRemoteData(); }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    @Override
    protected void handleEdit() {
        Long id = formLong("教室 ID");
        if (id == null) {
            id = selectedLong(0);
        }
        if (id == null) {
            showStatus("请填写或选择教室 ID。");
            return;
        }
        AppContext.getInstance().apiServices().classroomApi()
                .updateClassroom(id, new ClassroomRequest(formValue("教室名称")))
                .whenComplete(UiAsync.onComplete(ignored -> { showStatus("教室已更新。"); loadRemoteData(); }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    @Override
    protected void handleDelete() {
        Long id = selectedLong(0);
        if (id == null) {
            showStatus("请先选择教室。");
            return;
        }
        AppContext.getInstance().apiServices().classroomApi()
                .deleteClassroom(id)
                .whenComplete(UiAsync.onComplete(ignored -> { showStatus("教室已删除。"); loadRemoteData(); }, error -> showStatus(UiAsync.errorMessage(error))));
    }
}
