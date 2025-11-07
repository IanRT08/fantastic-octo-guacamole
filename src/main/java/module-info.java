module mx.edu.utez.fantasticoctoguacamole {
    requires javafx.controls;
    requires javafx.fxml;


    opens mx.edu.utez.fantasticoctoguacamole to javafx.fxml;
    exports mx.edu.utez.fantasticoctoguacamole;
}