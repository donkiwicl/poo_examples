package cl.dsy1102.ejemplos.imagenes;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador de parques-view.fxml.
 */
public class ParquesController {

    /** Carpeta de las imagenes dentro de src/main/resources. */
    private static final String RUTA_IMAGENES = "/cl/dsy1102/ejemplos/imagenes/img/";
    private static final String SIN_IMAGEN = "sin-imagen.png";

    @FXML private ListView<Parque> lstParques;
    @FXML private StackPane marcoImagen;
    @FXML private ImageView imgParque;
    @FXML private ProgressIndicator prgCarga;
    @FXML private Label lblNombre;
    @FXML private Label lblRegion;
    @FXML private Label lblDescripcion;
    @FXML private Button btnCargar;
    @FXML private Button btnRestaurar;

    /** Cada imagen se decodifica una sola vez y la comparten la lista y la vista grande. */
    private final Map<String, Image> cache = new HashMap<>();

    @FXML
    private void initialize() {
        lstParques.setItems(FXCollections.observableArrayList(Catalogo.parques()));
        lstParques.getSelectionModel().selectedItemProperty().addListener((obs, anterior, parque) -> {
            if (parque != null) {
                mostrar(parque);
            }
        });

        // R2: el ImageView no es un layout: no crece solo. Se enlaza su tamano maximo al marco.
        imgParque.setPreserveRatio(true);
        imgParque.setSmooth(true);
        imgParque.fitWidthProperty().bind(marcoImagen.widthProperty().subtract(20));
        imgParque.fitHeightProperty().bind(marcoImagen.heightProperty().subtract(20));

        // R4: cada celda muestra una miniatura y el nombre.
        lstParques.setCellFactory(lista -> new ListCell<>() {
            private final ImageView miniatura = crearMiniatura();

            @Override
            protected void updateItem(Parque parque, boolean vacia) {
                super.updateItem(parque, vacia);
                if (vacia || parque == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    miniatura.setImage(cargarImagen(parque.getArchivoImagen()));
                    setText(parque.getNombre());
                    setGraphic(miniatura);
                }
            }
        });

        lstParques.getSelectionModel().selectFirst();
    }

    private static ImageView crearMiniatura() {
        ImageView miniatura = new ImageView();
        miniatura.setFitWidth(64);
        miniatura.setFitHeight(40);
        miniatura.setPreserveRatio(true);
        return miniatura;
    }

    private void mostrar(Parque parque) {
        lblNombre.setText(parque.getNombre());
        lblRegion.setText("Región de " + parque.getRegion());
        lblDescripcion.setText(parque.getDescripcion());
        prgCarga.setVisible(false);
        imgParque.setImage(cargarImagen(parque.getArchivoImagen()));
    }

    /**
     * R1 y R3: carga una imagen de los recursos. Si el archivo no existe
     * retorna la imagen de reemplazo.
     */
    private Image cargarImagen(String archivo) {
        Image imagen = cache.get(archivo);
        if (imagen == null) {
            URL url = getClass().getResource(RUTA_IMAGENES + archivo);
            if (url == null) {
                // getResource retorna null (no lanza excepcion) cuando el recurso no existe.
                System.err.println("No se encontro la imagen " + archivo + ", se usa la de reemplazo.");
                url = getClass().getResource(RUTA_IMAGENES + SIN_IMAGEN);
            }
            imagen = new Image(url.toExternalForm());
            cache.put(archivo, imagen);
        }
        return imagen;
    }

    /** R5: imagen elegida por el usuario desde su disco, cargada en segundo plano. */
    @FXML
    private void onCargar() {
        FileChooser selector = new FileChooser();
        selector.setTitle("Elige una imagen");
        selector.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("Todos los archivos", "*.*"));
        File archivo = selector.showOpenDialog(btnCargar.getScene().getWindow());
        if (archivo == null) {
            return; // el usuario cancelo
        }

        // true = carga en segundo plano: la ventana no se congela con imagenes grandes.
        Image imagen = new Image(archivo.toURI().toString(), true);
        prgCarga.progressProperty().bind(imagen.progressProperty());
        prgCarga.visibleProperty().bind(imagen.progressProperty().lessThan(1));

        // Image no lanza excepciones: los errores se informan con errorProperty().
        imagen.errorProperty().addListener((obs, antes, hayError) -> {
            if (hayError) {
                informarError(archivo, imagen);
            }
        });
        if (imagen.isError()) {
            informarError(archivo, imagen);
            return;
        }
        imgParque.setImage(imagen);
        lblNombre.setText(lblNombre.getText().split(" · ")[0] + " · " + archivo.getName());
    }

    private void informarError(File archivo, Image imagen) {
        prgCarga.visibleProperty().unbind();
        prgCarga.setVisible(false);
        Alert alerta = new Alert(Alert.AlertType.ERROR,
                "El archivo \"" + archivo.getName() + "\" no es una imagen válida.\n" + imagen.getException());
        alerta.setHeaderText("No se pudo cargar la imagen");
        alerta.show();
        onRestaurar();
    }

    /** Vuelve a mostrar la imagen del parque seleccionado. */
    @FXML
    private void onRestaurar() {
        Parque parque = lstParques.getSelectionModel().getSelectedItem();
        if (parque != null) {
            prgCarga.progressProperty().unbind();
            prgCarga.visibleProperty().unbind();
            mostrar(parque);
        }
    }
}
