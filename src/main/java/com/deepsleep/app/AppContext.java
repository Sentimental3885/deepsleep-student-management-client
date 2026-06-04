package com.deepsleep.app;

import com.deepsleep.api.service.ApiServices;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Setter;

import java.util.Objects;

public final class AppContext {

    private static final AppContext INSTANCE = new AppContext();
    private static final String APP_STYLESHEET = "/com/deepsleep/ui/styles/app.css";
    private static final String DEFAULT_BASE_URL = "http://localhost:8080";

    @Setter
    private Stage primaryStage;
    private LoginIdentity currentUser;
    private final ApiServices apiServices;

    private AppContext() {
        this.apiServices = ApiServices.create(resolveBaseUrl());
    }

    public static AppContext getInstance() {
        return INSTANCE;
    }

    public LoginIdentity currentUser() {
        return currentUser;
    }

    public ApiServices apiServices() {
        return apiServices;
    }

    public void showLogin() {
        currentUser = null;
        Parent root = ViewLoader.load("/com/deepsleep/ui/auth/login-view.fxml");
        Scene scene = createScene(root, 1180, 760);
        primaryStage.setTitle("DeepSleep 教务管理系统");
        primaryStage.setMinWidth(1080);
        primaryStage.setMinHeight(720);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void showMainShell(LoginIdentity identity) {
        currentUser = identity;
        Parent root = ViewLoader.load("/com/deepsleep/ui/layout/main-shell.fxml");
        Scene scene = createScene(root, 1320, 860);
        primaryStage.setTitle("DeepSleep 教务管理系统 - " + identity.role().label());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Scene createScene(Parent root, double width, double height) {
        Scene scene = new Scene(root, width, height);
        String stylesheet = Objects.requireNonNull(getClass().getResource(APP_STYLESHEET),
                "应用样式表资源不存在: " + APP_STYLESHEET).toExternalForm();
        scene.getStylesheets().add(stylesheet);
        return scene;
    }

    private String resolveBaseUrl() {
        String property = System.getProperty("deepsleep.api.baseUrl");
        if (property != null && !property.isBlank()) {
            return property;
        }
        String env = System.getenv("DEEPSLEEP_API_BASE_URL");
        if (env != null && !env.isBlank()) {
            return env;
        }
        return DEFAULT_BASE_URL;
    }
}
