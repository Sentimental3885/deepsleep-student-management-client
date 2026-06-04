package com.deepsleep.ui.common;

import com.deepsleep.app.AppContext;

import java.util.List;

public class CourseDetailController extends BaseStaticPageController {
    @Override
    protected StaticPageSpec pageSpec() {
        return StaticPageData.courseDetail();
    }

    @Override
    protected void loadRemoteData() {
        Long courseId = formLong("课程 ID");
        if (courseId == null) {
            showStatus("请输入课程 ID 后加载课程详情和排课。");
            return;
        }
        AppContext.getInstance().apiServices().courseApi()
                .getCourse(courseId)
                .thenCompose(course -> AppContext.getInstance().apiServices().courseApi()
                        .listSchedules(courseId)
                        .thenApply(schedules -> new CourseWithSchedules(course, schedules)))
                .whenComplete(UiAsync.onComplete(result -> {
                    setTable(List.of("课程ID", "课程", "星期", "节次", "周次", "教室"),
                            result.schedules().stream().map(Rows::schedule).toList());
                    setContent("课程：" + result.course().name()
                            + "\n代码：" + result.course().code()
                            + "\n教师：" + result.course().teacherName()
                            + "\n学期：" + result.course().semester()
                            + "\n学分：" + Rows.text(result.course().credit())
                            + "\n容量：" + Rows.text(result.course().capacity())
                            + "\n简介：" + Rows.text(result.course().introduction()));
                    showStatus("课程详情加载完成。");
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    private record CourseWithSchedules(com.deepsleep.api.vo.CourseVO course, List<com.deepsleep.api.vo.ScheduleVO> schedules) {
    }
}
