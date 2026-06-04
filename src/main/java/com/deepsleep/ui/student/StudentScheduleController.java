package com.deepsleep.ui.student;

import com.deepsleep.app.AppContext;
import com.deepsleep.ui.common.BaseStaticPageController;
import com.deepsleep.ui.common.Rows;
import com.deepsleep.ui.common.StaticPageData;
import com.deepsleep.ui.common.StaticPageSpec;
import com.deepsleep.ui.common.UiAsync;

import java.util.List;

public class StudentScheduleController extends BaseStaticPageController {
    @Override
    protected StaticPageSpec pageSpec() {
        return StaticPageData.studentSchedule();
    }

    @Override
    protected void loadRemoteData() {
        AppContext.getInstance().apiServices().studentApi().getSchedule()
                .whenComplete(UiAsync.onComplete(schedules -> {
                    setTable(List.of("课程ID", "课程", "星期", "节次", "周次", "教室"),
                            schedules.stream().map(Rows::schedule).toList());
                    showStatus("课表加载完成。");
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }
}
