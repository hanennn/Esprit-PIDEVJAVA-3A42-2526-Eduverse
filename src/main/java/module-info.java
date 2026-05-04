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
    requires okhttp3;
    requires okio;
    requires kotlin.stdlib;
    requires com.google.gson;
    requires com.fasterxml.jackson.databind;
    requires jakarta.mail;
    requires java.prefs;
    requires jdk.httpserver;
    requires javafx.media;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;

    opens app to javafx.graphics, javafx.fxml;
    opens org.example to javafx.graphics, javafx.fxml;
    opens org.example.controllers to javafx.fxml;
    opens org.example.entities to javafx.base;
    opens org.example.services to javafx.base;
    opens org.example.social to javafx.graphics, javafx.fxml, javafx.base;
    opens org.example.utils to javafx.base;

    exports app;
    exports org.example;
    exports org.example.controllers;
    exports org.example.entities;
    exports org.example.services;
    exports org.example.social;
    exports org.example.utils;
}
