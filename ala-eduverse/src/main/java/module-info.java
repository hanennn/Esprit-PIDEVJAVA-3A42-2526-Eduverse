module com.eduverse.forum {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires java.sql;
    requires java.net.http;
    requires org.json;

    opens com.eduverse.forum to javafx.fxml;
    opens com.eduverse.forum.controllers to javafx.fxml;
    opens com.eduverse.forum.models to javafx.base;

    exports com.eduverse.forum;
}