module com.example.inventory5 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.example.inventory5 to javafx.fxml;
    exports com.example.inventory5;
}