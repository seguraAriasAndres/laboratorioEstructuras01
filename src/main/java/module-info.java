module cr.ac.una.est.laboratorioestructuras {
    requires javafx.controls;
    requires javafx.fxml;


    opens cr.ac.una.est.laboratorioestructuras to javafx.fxml;
    exports cr.ac.una.est.laboratorioestructuras;
}