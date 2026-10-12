module cr.ac.una.est.laboratorioestructuras {
    requires javafx.controls;
    requires javafx.fxml;

    exports cr.ac.una.est.laboratorioestructuras.modelo;

    // javac no permite exportar paquetes todavia vacios: cuando el equipo cree
    // sus clases, si usa FXML, descomentar:
    // opens cr.ac.una.est.laboratorioestructuras.presentacion to javafx.fxml;
}
