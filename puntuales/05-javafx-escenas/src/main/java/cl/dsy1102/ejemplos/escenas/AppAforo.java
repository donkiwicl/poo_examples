package cl.dsy1102.ejemplos.escenas;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Control de aforo de la Ramada Los Copihues, construido solo con codigo
 * Java (sin FXML). Ejecuta con: mvn javafx:run
 */
public class AppAforo extends Application {

    private Stage stage;
    private Contador contador;

    @Override
    public void init() {
        // TODO R1: traza "[Ciclo de vida] init() - hilo: ..." con Thread.currentThread().getName()
    }

    @Override
    public void start(Stage stage) {
        // TODO R1: traza start() igual que init().
        this.stage = stage;
        stage.setTitle("Ramada Los Copihues - Aforo");
        stage.setScene(crearEscenaBienvenida());
        stage.show();
    }

    @Override
    public void stop() {
        // TODO R1: traza stop() e imprime cuantas personas quedaron dentro (si hay contador).
    }

    /** Ejemplo resuelto: escena inicial donde se ingresa el aforo maximo. */
    private Scene crearEscenaBienvenida() {
        Label titulo = new Label("Control de aforo");
        titulo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        TextField txtAforo = new TextField();
        txtAforo.setPromptText("Aforo maximo (ej: 5)");
        txtAforo.setMaxWidth(200);

        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: #b3261e;");

        Button btnComenzar = new Button("Comenzar");
        btnComenzar.setDefaultButton(true);
        // Manejador de eventos con lambda: se ejecuta al hacer clic o presionar Enter.
        btnComenzar.setOnAction(evento -> {
            try {
                contador = new Contador(Integer.parseInt(txtAforo.getText().trim()));
                stage.setScene(crearEscenaContador());
            } catch (NumberFormatException e) {
                lblError.setText("Ingresa un numero entero.");
            } catch (IllegalArgumentException e) {
                lblError.setText(e.getMessage() + ".");
            }
        });

        VBox raiz = new VBox(12, titulo, new Label("¿Cuantas personas caben en la ramada?"), txtAforo, btnComenzar, lblError);
        raiz.setAlignment(Pos.CENTER);
        raiz.setPadding(new Insets(24));
        return new Scene(raiz, 420, 320);
    }

    /**
     * TODO R2: escena del contador.
     *  - BorderPane: arriba un titulo, al centro un VBox con el numero grande de
     *    personas, una ProgressBar con la ocupacion y un Label de estado; abajo
     *    un HBox con los botones "+ Entra", "- Sale" y "Volver".
     *  - Cada boton modifica el Contador y llama a un metodo actualizar(...)
     *    que refresca los textos y la barra.
     *  - Con el aforo completo, "+ Entra" queda deshabilitado y el estado dice
     *    "AFORO COMPLETO" en rojo.
     *  - "Volver" regresa a la escena de bienvenida (R3).
     */
    private Scene crearEscenaContador() {
        return new Scene(new Label("TODO: escena del contador"), 420, 320);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
