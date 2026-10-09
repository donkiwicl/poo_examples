/**
 * Modulo de la aplicacion Biblioteca Gabriela Mistral.
 *
 * requires: modulos externos que usa el proyecto.
 * opens:    paquetes que otras bibliotecas leen por reflexion
 *           (FXML inyecta los @FXML de los controladores y Jackson
 *           lee/escribe los atributos del modelo).
 * exports:  paquete de la clase Application, que JavaFX debe poder instanciar.
 */
module cl.dsy1102.biblioteca {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;

    opens cl.dsy1102.biblioteca.controller to javafx.fxml;
    opens cl.dsy1102.biblioteca.model to com.fasterxml.jackson.databind, javafx.base;

    exports cl.dsy1102.biblioteca;
}
