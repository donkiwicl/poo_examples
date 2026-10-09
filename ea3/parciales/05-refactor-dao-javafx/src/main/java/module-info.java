/**
 * Módulo de la aplicación Lavandería La Burbuja.
 *
 * java.sql: interfaces de JDBC (Connection, PreparedStatement...).
 * El driver de MySQL no se declara: DriverManager lo encuentra como servicio.
 */
module cl.dsy1102.ejemplos.lavanderia {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens cl.dsy1102.ejemplos.lavanderia.controller to javafx.fxml;
    opens cl.dsy1102.ejemplos.lavanderia.model to javafx.base;

    exports cl.dsy1102.ejemplos.lavanderia;
}
