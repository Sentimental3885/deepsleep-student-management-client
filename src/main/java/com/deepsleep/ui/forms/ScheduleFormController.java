package com.deepsleep.ui.forms;

import com.deepsleep.api.dto.schedule.ScheduleRequest;
import com.deepsleep.app.AppContext;
import com.deepsleep.ui.common.BaseStaticPageController;
import com.deepsleep.ui.common.StaticPageData;
import com.deepsleep.ui.common.StaticPageSpec;
import com.deepsleep.ui.common.UiAsync;

public class ScheduleFormController extends BaseStaticPageController {
    @Override
    protected StaticPageSpec pageSpec() {
        return StaticPageData.scheduleForm();
    }

    @Override
    protected void handleCreate() {
        Long courseId = formLong("课程 ID");
        Long scheduleId = formLong("排课 ID");
        if (courseId == null) {
            showStatus("请填写课程 ID。");
            return;
        }
        ScheduleRequest request = new ScheduleRequest(formInt("星期"), formInt("节次"), formInt("开始周"), formInt("结束周"), formLong("教室"));
        if (scheduleId == null) {
            AppContext.getInstance().apiServices().courseApi().createSchedule(courseId, request)
                    .whenComplete(UiAsync.onComplete(ignored -> showStatus("排课已新增。"), error -> showStatus(UiAsync.errorMessage(error))));
        } else {
            AppContext.getInstance().apiServices().courseApi().updateSchedule(courseId, scheduleId, request)
                    .whenComplete(UiAsync.onComplete(ignored -> showStatus("排课已更新。"), error -> showStatus(UiAsync.errorMessage(error))));
        }
    }

    @Override
    protected void handleDelete() {
        Long courseId = formLong("课程 ID");
        Long scheduleId = formLong("排课 ID");
        if (courseId == null || scheduleId == null) {
            showStatus("请填写课程 ID 和排课 ID。");
            return;
        }
        AppContext.getInstance().apiServices().courseApi().deleteSchedule(courseId, scheduleId)
                .whenComplete(UiAsync.onComplete(ignored -> showStatus("排课已删除。"), error -> showStatus(UiAsync.errorMessage(error))));
    }
}
