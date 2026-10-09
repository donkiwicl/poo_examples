/**
 * javafx.base necesita leer el modelo por reflexion: PropertyValueFactory
 * busca los metodos nombreProperty()/getNombre() de Equipo.
 */
module cl.dsy1102.ejemplos.liga {
    requires javafx.controls;
    requires javafx.fxml;

    opens cl.dsy1102.ejemplos.liga to javafx.fxml, javafx.base;
    exports cl.dsy1102.ejemplos.liga;
}
