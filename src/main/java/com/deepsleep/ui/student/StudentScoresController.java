package com.deepsleep.ui.student;

import com.deepsleep.api.dto.selection.ScoreListQuery;
import com.deepsleep.app.AppContext;
import com.deepsleep.ui.common.BaseStaticPageController;
import com.deepsleep.ui.common.Rows;
import com.deepsleep.ui.common.StaticPageData;
import com.deepsleep.ui.common.StaticPageSpec;
import com.deepsleep.ui.common.UiAsync;

import java.util.List;

public class StudentScoresController extends BaseStaticPageController {
    @Override
    protected StaticPageSpec pageSpec() {
        return StaticPageData.studentScores();
    }

    @Override
    protected void loadRemoteData() {
        String semester = formValue("学期");
        if (semester.isBlank()) {
            showStatus("请输入学期后查询成绩。");
            return;
        }
        AppContext.getInstance().apiServices().selectionApi()
                .listScores(new ScoreListQuery(semester, 1, 20))
                .whenComplete(UiAsync.onComplete(page -> {
                    setTable(List.of("ID", "课程代码", "课程名称", "学期", "学分", "成绩", "GPA", "排名", "最高分", "最低分"),
                            page.records().stream().map(Rows::score).toList());
                    showStatus("成绩加载完成，共 " + Rows.text(page.total()) + " 条。");
                }, error -> showStatus(UiAsync.errorMessage(error))));
    }

    @Override
    protected void handleCreate() {
        Long courseId = selectedLong(0);
        if (courseId == null) {
            showStatus("请先选择成绩记录。");
            return;
        }
        AppContext.getInstance().apiServices().selectionApi().getScore(courseId)
                .whenComplete(UiAsync.onComplete(score -> setContent("课程：" + score.name()
                        + "\n成绩：" + Rows.text(score.score())
                        + "\nGPA：" + Rows.text(score.gpa())
                        + "\n排名：" + Rows.text(score.rank())
                        + "\n最高分：" + Rows.text(score.maxScore())
                        + "\n最低分：" + Rows.text(score.minScore())), error -> showStatus(UiAsync.errorMessage(error))));
    }
}
