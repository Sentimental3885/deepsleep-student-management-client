package com.deepsleep.ui.forms;

import com.deepsleep.api.dto.notice.NoticeRequest;
import com.deepsleep.app.AppContext;
import com.deepsleep.ui.common.BaseStaticPageController;
import com.deepsleep.ui.common.StaticPageData;
import com.deepsleep.ui.common.StaticPageSpec;
import com.deepsleep.ui.common.UiAsync;

public class NoticeFormController extends BaseStaticPageController {
    @Override
    protected StaticPageSpec pageSpec() {
        return StaticPageData.noticeForm();
    }

    @Override
    protected void handleCreate() {
        NoticeRequest request = new NoticeRequest(formValue("公告标题"), formValue("公告正文"));
        AppContext.getInstance().apiServices().noticeApi().createNotice(request)
                .whenComplete(UiAsync.onComplete(ignored -> showStatus("公告已发布。"), error -> showStatus(UiAsync.errorMessage(error))));
    }
}
