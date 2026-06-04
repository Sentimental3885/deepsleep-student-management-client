package com.deepsleep.ui.common;

import com.deepsleep.app.AppContext;

import java.util.List;

public class ExamDetailController extends BaseStaticPageController {
    @Override
    protected StaticPageSpec pageSpec() {
        return StaticPageData.examDetail();
    }

    @Override
    protected void loadRemoteData() {
        Long examId = formLong("考试 ID");
        if (examId == null) {
            showStatus("请输入考试 ID 后加载考试详情。");
            return;
        }
        AppContext.getInstance().apiServices().examApi()
                .getExam(examId)
                .whenComplete(UiAsync.onComplete(exam -> {
                    setTable(List.of("ID", "课程", "类型", "考试时间", "时长", "教室", "监考教师", "备注"), List.of(Rows.exam(exam)));
                    showStatus("考试详情加载完成。");
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }
}
