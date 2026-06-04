package com.deepsleep.ui.student;

import com.deepsleep.app.AppContext;
import com.deepsleep.ui.common.BaseStaticPageController;
import com.deepsleep.ui.common.Rows;
import com.deepsleep.ui.common.StaticPageData;
import com.deepsleep.ui.common.StaticPageSpec;
import com.deepsleep.ui.common.UiAsync;

import java.util.List;

public class StudentAnalysisController extends BaseStaticPageController {
    @Override
    protected StaticPageSpec pageSpec() {
        return StaticPageData.studentAnalysis();
    }

    @Override
    protected void loadRemoteData() {
        AppContext.getInstance().apiServices().analysisApi().listMyAnalysisHistory()
                .whenComplete(UiAsync.onComplete(history -> {
                    setTable(List.of("ID", "生成时间", "内容"), history.stream().map(Rows::analysis).toList());
                    showStatus("分析历史加载完成。");
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    @Override
    protected void handleCreate() {
        AppContext.getInstance().apiServices().analysisApi().analyzeMyGrades()
                .whenComplete(UiAsync.onComplete(analysis -> {
                    setContent(analysis.content());
                    loadRemoteData();
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }
}
