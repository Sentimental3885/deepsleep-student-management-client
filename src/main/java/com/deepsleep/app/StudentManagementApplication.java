package com.deepsleep.app;

import javafx.application.Application;
import javafx.stage.Stage;

public class StudentManagementApplication extends Application {

    @Override
    public void start(Stage stage) {
        AppContext.getInstance().setPrimaryStage(stage);
        AppContext.getInstance().showLogin();
    }
}
