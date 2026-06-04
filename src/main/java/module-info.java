module com.deepsleep {
    requires javafx.controls;
    requires javafx.fxml;
    requires okhttp3;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires static lombok;


    opens com.deepsleep.ui.auth to javafx.fxml;
    opens com.deepsleep.ui.layout to javafx.fxml;
    opens com.deepsleep.ui.common to javafx.fxml;
    opens com.deepsleep.ui.admin to javafx.fxml;
    opens com.deepsleep.ui.teacher to javafx.fxml;
    opens com.deepsleep.ui.student to javafx.fxml;
    opens com.deepsleep.ui.forms to javafx.fxml;
    opens com.deepsleep.api.dto to com.fasterxml.jackson.databind;
    opens com.deepsleep.api.dto.auth to com.fasterxml.jackson.databind;
    opens com.deepsleep.api.dto.user to com.fasterxml.jackson.databind;
    opens com.deepsleep.api.dto.admin to com.fasterxml.jackson.databind;
    opens com.deepsleep.api.dto.course to com.fasterxml.jackson.databind;
    opens com.deepsleep.api.dto.selection to com.fasterxml.jackson.databind;
    opens com.deepsleep.api.dto.exam to com.fasterxml.jackson.databind;
    opens com.deepsleep.api.dto.notice to com.fasterxml.jackson.databind;
    opens com.deepsleep.api.dto.organization to com.fasterxml.jackson.databind;
    opens com.deepsleep.api.dto.classroom to com.fasterxml.jackson.databind;
    opens com.deepsleep.api.dto.schedule to com.fasterxml.jackson.databind;
    opens com.deepsleep.api.result to com.fasterxml.jackson.databind;
    opens com.deepsleep.api.vo to com.fasterxml.jackson.databind;

    exports com.deepsleep;
    exports com.deepsleep.app;
    exports com.deepsleep.api.config;
    exports com.deepsleep.api.dto;
    exports com.deepsleep.api.dto.auth;
    exports com.deepsleep.api.dto.user;
    exports com.deepsleep.api.dto.admin;
    exports com.deepsleep.api.dto.course;
    exports com.deepsleep.api.dto.selection;
    exports com.deepsleep.api.dto.exam;
    exports com.deepsleep.api.dto.notice;
    exports com.deepsleep.api.dto.organization;
    exports com.deepsleep.api.dto.classroom;
    exports com.deepsleep.api.dto.schedule;
    exports com.deepsleep.api.enums;
    exports com.deepsleep.api.http;
    exports com.deepsleep.api.result;
    exports com.deepsleep.api.service;
    exports com.deepsleep.api.vo;
    exports com.deepsleep.auth;
}
