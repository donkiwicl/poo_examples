package cl.dsy1102.ejemplos.imagenes;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

/**
 * Controlador de parques-view.fxml.
 */
public class ParquesController {

    /** Carpeta de las imagenes dentro de src/main/resources. */
    private static final String RUTA_IMAGENES = "/cl/dsy1102/ejemplos/imagenes/img/";

    @FXML private ListView<Parque> lstParques;
    @FXML private StackPane marcoImagen;
    @FXML private ImageView imgParque;
    @FXML private ProgressIndicator prgCarga;
    @FXML private Label lblNombre;
    @FXML private Label lblRegion;
    @FXML private Label lblDescripcion;
    @FXML private Button btnCargar;
    @FXML private Button btnRestaurar;

    @FXML
    private void initialize() {
        lstParques.setItems(FXCollections.observableArrayList(Catalogo.parques()));
        lstParques.getSelectionModel().selectedItemProperty().addListener((obs, anterior, parque) -> {
            if (parque != null) {
                mostrar(parque);
            }
        });

        // TODO R2: la imagen conserva su proporcion y se ajusta al tamano de marcoImagen
        //  (fitWidth/fitHeight enlazados al ancho y alto del marco, menos el padding).

        // TODO R4: miniaturas en la lista con setCellFactory (ImageView de 64x40 + nombre).

        lstParques.getSelectionModel().selectFirst();
    }

    private void mostrar(Parque parque) {
        lblNombre.setText(parque.getNombre());
        lblRegion.setText("Región de " + parque.getRegion());
        lblDescripcion.setText(parque.getDescripcion());
        imgParque.setImage(cargarImagen(parque.getArchivoImagen()));
    }

    /**
     * TODO R1: carga una imagen desde los recursos del proyecto (getClass().getResource).
     * TODO R3: si el archivo no existe, retorna img/sin-imagen.png en vez de fallar.
     */
    private Image cargarImagen(String archivo) {
        return null;
    }

    /** TODO R5: abre un FileChooser y muestra una imagen del disco del usuario. */
    @FXML
    private void onCargar() {
    }

    /** Vuelve a mostrar la imagen del parque seleccionado. */
    @FXML
    private void onRestaurar() {
        Parque parque = lstParques.getSelectionModel().getSelectedItem();
        if (parque != null) {
            mostrar(parque);
        }
    }
}
