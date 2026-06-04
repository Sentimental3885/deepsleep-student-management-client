package com.deepsleep.app;

import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

public final class NavigationService {

    private final StackPane contentHost;

    public NavigationService(StackPane contentHost) {
        this.contentHost = contentHost;
    }

    public void navigate(ViewRoute route) {
        Parent page = ViewLoader.load(route.resourcePath());
        contentHost.getChildren().setAll(page);
    }
}
