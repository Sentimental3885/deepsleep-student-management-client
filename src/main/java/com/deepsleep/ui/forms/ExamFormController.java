package com.deepsleep.ui.forms;

import com.deepsleep.api.dto.exam.CreateExamRequest;
import com.deepsleep.api.dto.exam.UpdateExamRequest;
import com.deepsleep.app.AppContext;
import com.deepsleep.ui.common.BaseStaticPageController;
import com.deepsleep.ui.common.StaticPageData;
import com.deepsleep.ui.common.StaticPageSpec;
import com.deepsleep.ui.common.UiAsync;

public class ExamFormController extends BaseStaticPageController {
    @Override
    protected StaticPageSpec pageSpec() {
        return StaticPageData.examForm();
    }

    @Override
    protected void handleCreate() {
        Long id = formLong("考试 ID");
        if (id == null) {
            AppContext.getInstance().apiServices().examApi()
                    .createExam(new CreateExamRequest(formLong("课程"), formInt("考试类型"), formDateTime("考试时间"),
                            formInt("时长"), formLong("教室"), formLong("监考教师"), formValue("备注")))
                    .whenComplete(UiAsync.onComplete(ignored -> showStatus("考试已新增。"), error -> showStatus(UiAsync.errorMessage(error))));
            return;
        }
        AppContext.getInstance().apiServices().examApi()
                .updateExam(id, new UpdateExamRequest(formInt("考试类型"), formDateTime("考试时间"),
                        formInt("时长"), formLong("教室"), formLong("监考教师"), formValue("备注")))
                .whenComplete(UiAsync.onComplete(ignored -> showStatus("考试已更新。"), error -> showStatus(UiAsync.errorMessage(error))));
    }
}
