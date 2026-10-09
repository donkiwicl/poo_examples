package cl.dsy1102.ejemplos.escenas;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
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
        // Corre en el hilo "JavaFX-Launcher": aun no existe la ventana.
        System.out.println("[Ciclo de vida] init()  - hilo: " + Thread.currentThread().getName());
    }

    @Override
    public void start(Stage stage) {
        // Corre en el "JavaFX Application Thread". El Stage lo crea JavaFX.
        System.out.println("[Ciclo de vida] start() - hilo: " + Thread.currentThread().getName());
        this.stage = stage;
        stage.setTitle("Ramada Los Copihues - Aforo");
        stage.setMinWidth(360);
        stage.setMinHeight(300);
        stage.setScene(crearEscenaBienvenida());
        stage.show();
    }

    @Override
    public void stop() {
        // Se ejecuta al cerrar la ultima ventana. Aqui se guardarian datos pendientes.
        System.out.println("[Ciclo de vida] stop()  - hilo: " + Thread.currentThread().getName());
        if (contador != null) {
            System.out.println("Personas dentro al cerrar: " + contador.getPersonas());
        }
    }

    /** Escena inicial donde se ingresa el aforo maximo. */
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

    /** Escena del contador: se crea de nuevo cada vez que se comienza. */
    private Scene crearEscenaContador() {
        Label titulo = new Label("Aforo maximo: " + contador.getAforoMaximo());
        titulo.setStyle("-fx-font-size: 16px;");

        Label lblPersonas = new Label();
        lblPersonas.setStyle("-fx-font-size: 64px; -fx-font-weight: bold;");
        ProgressBar barra = new ProgressBar();
        barra.setMaxWidth(Double.MAX_VALUE);
        Label lblEstado = new Label();

        Button btnEntra = new Button("+ Entra");
        Button btnSale = new Button("- Sale");
        Button btnVolver = new Button("Volver");

        // Los botones solo hablan con el modelo; actualizar() refleja el modelo en la vista.
        btnEntra.setOnAction(e -> {
            contador.entrar();
            actualizar(lblPersonas, barra, lblEstado, btnEntra, btnSale);
        });
        btnSale.setOnAction(e -> {
            contador.salir();
            actualizar(lblPersonas, barra, lblEstado, btnEntra, btnSale);
        });
        // Misma ventana (Stage), distinta escena (Scene).
        btnVolver.setOnAction(e -> stage.setScene(crearEscenaBienvenida()));

        VBox centro = new VBox(12, lblPersonas, barra, lblEstado);
        centro.setAlignment(Pos.CENTER);
        centro.setPadding(new Insets(0, 24, 0, 24));

        HBox botones = new HBox(10, btnEntra, btnSale, btnVolver);
        botones.setAlignment(Pos.CENTER);

        BorderPane raiz = new BorderPane(centro);
        raiz.setTop(titulo);
        raiz.setBottom(botones);
        BorderPane.setAlignment(titulo, Pos.CENTER);
        raiz.setPadding(new Insets(20));

        actualizar(lblPersonas, barra, lblEstado, btnEntra, btnSale);
        return new Scene(raiz, 420, 320);
    }

    private void actualizar(Label lblPersonas, ProgressBar barra, Label lblEstado, Button btnEntra, Button btnSale) {
        lblPersonas.setText(String.valueOf(contador.getPersonas()));
        barra.setProgress(contador.getOcupacion());
        btnEntra.setDisable(contador.estaCompleto());
        btnSale.setDisable(contador.getPersonas() == 0);
        if (contador.estaCompleto()) {
            lblEstado.setText("AFORO COMPLETO");
            lblEstado.setStyle("-fx-text-fill: #b3261e; -fx-font-weight: bold;");
        } else {
            lblEstado.setText("Quedan " + (contador.getAforoMaximo() - contador.getPersonas()) + " lugares");
            lblEstado.setStyle("-fx-text-fill: #1e6b34;");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
