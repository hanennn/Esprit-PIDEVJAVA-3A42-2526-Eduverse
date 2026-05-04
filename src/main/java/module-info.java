module eduverse.dao {
    requires java.base;
    requires java.sql;
    requires java.net.http;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires javafx.web;
    requires jdk.jsobject;
    requires itextpdf;
    requires org.jfree.jfreechart;
    requires java.desktop;
    requires org.json;

    opens controllers to javafx.fxml;
    opens models to javafx.base;
    opens app to javafx.graphics, javafx.fxml;

    exports controllers;
    exports models;
    exports services;
    exports utils;
    exports app;
}
