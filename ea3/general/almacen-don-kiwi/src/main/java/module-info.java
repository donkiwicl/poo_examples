/**
 * Módulo de la aplicación Almacén Don Kiwi.
 *
 * opens: paquetes que otras bibliotecas leen por reflexión (FXML inyecta los
 *        @FXML de los controladores, Jackson y las tablas leen el modelo).
 */
module cl.dsy1102.almacen {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;

    opens cl.dsy1102.almacen.controller to javafx.fxml;
    opens cl.dsy1102.almacen.model to com.fasterxml.jackson.databind, javafx.base;

    exports cl.dsy1102.almacen;
}
