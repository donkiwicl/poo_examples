package cl.dsy1102.ejemplos.liga;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/**
 * Controlador de liga-view.fxml.
 */
public class LigaController {

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbComuna;
    @FXML private TableView<Equipo> tblEquipos;
    @FXML private TableColumn<Equipo, Void> colPosicion;
    @FXML private TableColumn<Equipo, String> colNombre;
    @FXML private TableColumn<Equipo, String> colComuna;
    @FXML private TableColumn<Equipo, Integer> colJugados;
    @FXML private TableColumn<Equipo, Integer> colGanados;
    @FXML private TableColumn<Equipo, Integer> colEmpatados;
    @FXML private TableColumn<Equipo, Integer> colPerdidos;
    @FXML private TableColumn<Equipo, Integer> colGolesFavor;
    @FXML private TableColumn<Equipo, Integer> colGolesContra;
    @FXML private TableColumn<Equipo, Integer> colDiferencia;
    @FXML private TableColumn<Equipo, Integer> colPuntos;
    @FXML private Label lblTotal;
    @FXML private ComboBox<Equipo> cmbLocal;
    @FXML private ComboBox<Equipo> cmbVisita;
    @FXML private TextField txtGolesLocal;
    @FXML private TextField txtGolesVisita;
    @FXML private TextField txtNuevoNombre;
    @FXML private TextField txtNuevaComuna;

    private Liga liga;

    @FXML
    private void initialize() {
        // TODO R1: setCellValueFactory de cada columna.
        //  - Propiedades del modelo: new PropertyValueFactory<>("nombre"), "comuna", "ganados"...
        //  - Valores calculados (PJ, DIF, PTS): lambda con jugadosBinding().asObject(), etc.

        // TODO R2: orden inicial por PTS, luego DIF y luego GF, de mayor a menor.

        // TODO R4: columna # con la posicion de la fila y DIF con signo y color (setCellFactory).
    }

    /** Lo llama AppLiga despues de cargar la vista. */
    public void setLiga(Liga liga) {
        this.liga = liga;
        // TODO R3: reemplaza esta linea por FilteredList + SortedList y enlaza los filtros.
        tblEquipos.setItems(liga.getEquipos());
    }

    /** TODO R5: lee los goles, delega en liga.registrarResultado(...) e informa errores con un Alert. */
    @FXML
    private void onRegistrar() {
    }

    /** TODO R6: crea y agrega un equipo; si el modelo lo rechaza, informa con un Alert. */
    @FXML
    private void onAgregar() {
    }

    /** TODO R6: elimina el equipo seleccionado, previa confirmacion. */
    @FXML
    private void onEliminar() {
    }
}
