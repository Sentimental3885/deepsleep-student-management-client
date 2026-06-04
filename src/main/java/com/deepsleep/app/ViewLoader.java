package com.deepsleep.app;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;
import java.net.URL;

public final class ViewLoader {

    private ViewLoader() {
    }

    public static Parent load(String resourcePath) {
        URL resource = ViewLoader.class.getResource(resourcePath);
        if (resource == null) {
            throw new IllegalStateException("FXML resource not found: " + resourcePath);
        }
        try {
            return FXMLLoader.load(resource);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load FXML resource: " + resourcePath, e);
        }
    }
}
