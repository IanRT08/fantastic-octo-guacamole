module mx.edu.utez.fantasticoctoguacamole {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires ucp;
    requires java.desktop;
    requires jdk.jfr;
    requires javafx.base;
    requires javafx.graphics;
    requires java.prefs;


    opens mx.edu.utez.fantasticoctoguacamole to javafx.fxml;
    opens mx.edu.utez.fantasticoctoguacamole.modelo to javafx.fxml;
    exports mx.edu.utez.fantasticoctoguacamole;
    exports mx.edu.utez.fantasticoctoguacamole.modelo;
    exports mx.edu.utez.fantasticoctoguacamole.modelo.dao;
    opens mx.edu.utez.fantasticoctoguacamole.modelo.dao to javafx.fxml;
}