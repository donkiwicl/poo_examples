/**
 * opens ... to javafx.fxml: FXMLLoader asigna por reflexion los atributos
 * privados marcados con @FXML y llama a los metodos de evento del controlador.
 */
module cl.dsy1102.ejemplos.inscripcion {
    requires javafx.controls;
    requires javafx.fxml;

    opens cl.dsy1102.ejemplos.inscripcion to javafx.fxml;
    exports cl.dsy1102.ejemplos.inscripcion;
}
