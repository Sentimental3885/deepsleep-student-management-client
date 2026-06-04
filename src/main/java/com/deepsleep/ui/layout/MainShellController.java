package com.deepsleep.ui.layout;

import com.deepsleep.api.enums.UserRole;
import com.deepsleep.app.AppContext;
import com.deepsleep.app.LoginIdentity;
import com.deepsleep.app.NavigationService;
import com.deepsleep.app.ViewRoute;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainShellController {

    @FXML
    private Label userNameLabel;
    @FXML
    private Label userRoleLabel;
    @FXML
    private Label sectionTitleLabel;
    @FXML
    private VBox navigationBox;
    @FXML
    private StackPane contentHost;

    private NavigationService navigationService;
    private Button activeButton;

    @FXML
    public void initialize() {
        LoginIdentity identity = AppContext.getInstance().currentUser();
        UserRole role = identity == null ? UserRole.ADMIN : identity.role();
        userNameLabel.setText(identity == null ? "演示用户" : identity.name());
        userRoleLabel.setText(role.label());

        navigationService = new NavigationService(contentHost);
        buildNavigation(role);
        navigate(ViewRoute.defaultFor(role), null);
    }

    @FXML
    public void onLogout() {
        AppContext.getInstance().showLogin();
    }

    private void buildNavigation(UserRole role) {
        navigationBox.getChildren().clear();
        Map<String, List<ViewRoute>> routesByGroup = new LinkedHashMap<>();
        ViewRoute.forRole(role).forEach(route ->
                routesByGroup.computeIfAbsent(route.group(), ignored -> new java.util.ArrayList<>()).add(route));

        routesByGroup.forEach((group, routes) -> {
            Label groupLabel = new Label(group);
            groupLabel.getStyleClass().add("nav-group");
            navigationBox.getChildren().add(groupLabel);
            routes.forEach(route -> {
                Button button = new Button(route.label());
                button.getStyleClass().add("nav-button");
                button.setMaxWidth(Double.MAX_VALUE);
                button.setOnAction(event -> navigate(route, button));
                navigationBox.getChildren().add(button);
            });
        });
    }

    private void navigate(ViewRoute route, Button source) {
        navigationService.navigate(route);
        sectionTitleLabel.setText(route.label());
        if (activeButton != null) {
            activeButton.getStyleClass().remove("nav-button-active");
        }
        if (source != null) {
            source.getStyleClass().add("nav-button-active");
            activeButton = source;
        }
    }
}
