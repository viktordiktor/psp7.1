module com.example.psp71 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.psp71 to javafx.fxml;
    exports com.example.psp71;
}