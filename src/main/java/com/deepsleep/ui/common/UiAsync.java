package com.deepsleep.ui.common;

import com.deepsleep.api.result.ApiException;
import com.deepsleep.api.result.ClientException;
import javafx.application.Platform;

import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

public final class UiAsync {

    private UiAsync() {
    }

    public static <T> java.util.function.BiConsumer<T, Throwable> onComplete(Consumer<T> success, Consumer<Throwable> failure) {
        return (value, throwable) -> Platform.runLater(() -> {
            if (throwable == null) {
                success.accept(value);
            } else {
                failure.accept(unwrap(throwable));
            }
        });
    }

    public static String errorMessage(Throwable throwable) {
        Throwable unwrapped = unwrap(throwable);
        if (unwrapped instanceof ApiException apiException) {
            return "接口错误：" + apiException.getMessage();
        }
        if (unwrapped instanceof ClientException clientException) {
            return "客户端错误：" + clientException.getMessage();
        }
        return "操作失败：" + unwrapped.getMessage();
    }

    private static Throwable unwrap(Throwable throwable) {
        if (throwable instanceof CompletionException && throwable.getCause() != null) {
            return unwrap(throwable.getCause());
        }
        return throwable.getCause() == null ? throwable : unwrap(throwable.getCause());
    }
}
