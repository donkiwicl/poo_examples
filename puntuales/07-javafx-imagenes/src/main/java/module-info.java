module cl.dsy1102.ejemplos.imagenes {
    requires javafx.controls;
    requires javafx.fxml;

    opens cl.dsy1102.ejemplos.imagenes to javafx.fxml;
    exports cl.dsy1102.ejemplos.imagenes;
}
