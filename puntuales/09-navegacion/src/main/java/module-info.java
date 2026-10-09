module cl.dsy1102.ejemplos.navegacion {
    requires javafx.controls;
    requires javafx.fxml;

    opens cl.dsy1102.ejemplos.navegacion.controller to javafx.fxml;

    exports cl.dsy1102.ejemplos.navegacion;
}
