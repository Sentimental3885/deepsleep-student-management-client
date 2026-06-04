package com.deepsleep.ui.admin;

import com.deepsleep.api.dto.PageQuery;
import com.deepsleep.api.dto.exam.CreateExamRequest;
import com.deepsleep.api.dto.exam.UpdateExamRequest;
import com.deepsleep.app.AppContext;
import com.deepsleep.ui.common.BaseStaticPageController;
import com.deepsleep.ui.common.Rows;
import com.deepsleep.ui.common.StaticPageData;
import com.deepsleep.ui.common.StaticPageSpec;
import com.deepsleep.ui.common.UiAsync;

import java.util.List;

public class AdminExamsController extends BaseStaticPageController {
    @Override
    protected StaticPageSpec pageSpec() {
        return StaticPageData.adminExams();
    }

    @Override
    protected void loadRemoteData() {
        AppContext.getInstance().apiServices().adminApi()
                .listExams(new PageQuery(1, 20))
                .whenComplete(UiAsync.onComplete(page -> {
                    setTable(List.of("ID", "课程", "类型", "考试时间", "时长", "教室", "监考教师", "备注"),
                            page.records().stream().map(Rows::exam).toList());
                    showStatus("考试加载完成，共 " + Rows.text(page.total()) + " 条。");
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    @Override
    protected void handleCreate() {
        AppContext.getInstance().apiServices().examApi()
                .createExam(new CreateExamRequest(formLong("课程"), formInt("考试类型"), formDateTime("考试时间"),
                        formInt("时长"), formLong("教室"), formLong("监考教师"), formValue("备注")))
                .whenComplete(UiAsync.onComplete(ignored -> { showStatus("考试已新增。"); loadRemoteData(); }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    @Override
    protected void handleEdit() {
        Long id = selectedLong(0);
        if (id == null) {
            showStatus("请先选择考试。");
            return;
        }
        AppContext.getInstance().apiServices().examApi()
                .updateExam(id, new UpdateExamRequest(formInt("考试类型"), formDateTime("考试时间"),
                        formInt("时长"), formLong("教室"), formLong("监考教师"), formValue("备注")))
                .whenComplete(UiAsync.onComplete(ignored -> { showStatus("考试已更新。"); loadRemoteData(); }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    @Override
    protected void handleDelete() {
        Long id = selectedLong(0);
        if (id == null) {
            showStatus("请先选择考试。");
            return;
        }
        AppContext.getInstance().apiServices().examApi()
                .deleteExam(id)
                .whenComplete(UiAsync.onComplete(ignored -> { showStatus("考试已删除。"); loadRemoteData(); }, error -> showStatus(UiAsync.errorMessage(error))));
    }
}
