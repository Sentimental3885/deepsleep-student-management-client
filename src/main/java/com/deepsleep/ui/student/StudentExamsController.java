package com.deepsleep.ui.student;

import com.deepsleep.app.AppContext;
import com.deepsleep.ui.common.BaseStaticPageController;
import com.deepsleep.ui.common.Rows;
import com.deepsleep.ui.common.StaticPageData;
import com.deepsleep.ui.common.StaticPageSpec;
import com.deepsleep.ui.common.UiAsync;

import java.util.List;

public class StudentExamsController extends BaseStaticPageController {
    @Override
    protected StaticPageSpec pageSpec() {
        return StaticPageData.studentExams();
    }

    @Override
    protected void loadRemoteData() {
        AppContext.getInstance().apiServices().studentApi().getExams()
                .whenComplete(UiAsync.onComplete(exams -> {
                    setTable(List.of("ID", "课程", "类型", "考试时间", "时长", "教室", "监考教师", "备注"),
                            exams.stream().map(Rows::exam).toList());
                    showStatus("考试列表加载完成。");
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }
}
