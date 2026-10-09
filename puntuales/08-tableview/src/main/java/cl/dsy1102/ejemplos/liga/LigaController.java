package cl.dsy1102.ejemplos.liga;

import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

/**
 * Controlador de liga-view.fxml.
 */
public class LigaController {

    private static final String TODAS = "Todas";

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
    private FilteredList<Equipo> filtrados;

    @FXML
    private void initialize() {
        // R1: PropertyValueFactory busca nombreProperty() (o getNombre()) por reflexion.
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colComuna.setCellValueFactory(new PropertyValueFactory<>("comuna"));
        colGanados.setCellValueFactory(new PropertyValueFactory<>("ganados"));
        colEmpatados.setCellValueFactory(new PropertyValueFactory<>("empatados"));
        colPerdidos.setCellValueFactory(new PropertyValueFactory<>("perdidos"));
        colGolesFavor.setCellValueFactory(new PropertyValueFactory<>("golesFavor"));
        colGolesContra.setCellValueFactory(new PropertyValueFactory<>("golesContra"));
        // Los valores calculados no siguen la convencion xProperty(): se entregan con un lambda.
        // asObject() convierte el IntegerBinding (Number) en ObservableValue<Integer>.
        colJugados.setCellValueFactory(celda -> celda.getValue().jugadosBinding().asObject());
        colDiferencia.setCellValueFactory(celda -> celda.getValue().diferenciaBinding().asObject());
        colPuntos.setCellValueFactory(celda -> celda.getValue().puntosBinding().asObject());

        // R2: orden inicial. La tabla ordena primero por PTS; si empatan, por DIF y luego por GF.
        List.of(colPuntos, colDiferencia, colGolesFavor).forEach(c -> c.setSortType(TableColumn.SortType.DESCENDING));
        tblEquipos.getSortOrder().setAll(List.of(colPuntos, colDiferencia, colGolesFavor));

        // R4: la posicion no es un dato del equipo, es el indice de la fila.
        colPosicion.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean vacia) {
                super.updateItem(item, vacia);
                setText(vacia ? null : String.valueOf(getIndex() + 1));
            }
        });
        // R4: DIF con signo y color. Se reemplazan las clases CSS en cada actualizacion
        // porque las celdas se reutilizan al ordenar o desplazarse.
        colDiferencia.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(Integer dif, boolean vacia) {
                super.updateItem(dif, vacia);
                getStyleClass().removeAll("dif-positiva", "dif-negativa");
                if (vacia || dif == null) {
                    setText(null);
                    return;
                }
                setText(dif > 0 ? "+" + dif : String.valueOf(dif));
                if (dif > 0) {
                    getStyleClass().add("dif-positiva");
                } else if (dif < 0) {
                    getStyleClass().add("dif-negativa");
                }
            }
        });
    }

    /** Lo llama AppLiga despues de cargar la vista. */
    public void setLiga(Liga liga) {
        this.liga = liga;

        // R3: Lista original -> FilteredList (filtra) -> SortedList (ordena segun la tabla) -> TableView.
        filtrados = new FilteredList<>(liga.getEquipos());
        SortedList<Equipo> ordenados = new SortedList<>(filtrados);
        ordenados.comparatorProperty().bind(tblEquipos.comparatorProperty());
        tblEquipos.setItems(ordenados);

        txtBuscar.textProperty().addListener((obs, antes, ahora) -> actualizarFiltro());
        cmbComuna.valueProperty().addListener((obs, antes, ahora) -> actualizarFiltro());
        actualizarComunas();
        // Si se agrega o elimina un equipo, la lista de comunas se recalcula.
        liga.getEquipos().addListener((ListChangeListener<Equipo>) cambio -> actualizarComunas());

        lblTotal.textProperty().bind(Bindings.format("Mostrando %d de %d equipos",
                Bindings.size(filtrados), Bindings.size(liga.getEquipos())));

        // Los ComboBox de resultado usan la misma lista observable: siempre estan al dia.
        cmbLocal.setItems(liga.getEquipos());
        cmbVisita.setItems(liga.getEquipos());
    }

    private void actualizarFiltro() {
        String texto = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase();
        String comuna = cmbComuna.getValue();
        filtrados.setPredicate(equipo ->
                equipo.getNombre().toLowerCase().contains(texto)
                        && (comuna == null || TODAS.equals(comuna) || equipo.getComuna().equals(comuna)));
    }

    private void actualizarComunas() {
        String seleccionada = cmbComuna.getValue();
        List<String> comunas = liga.getEquipos().stream().map(Equipo::getComuna).distinct().sorted().toList();
        cmbComuna.getItems().setAll(TODAS);
        cmbComuna.getItems().addAll(comunas);
        cmbComuna.setValue(comunas.contains(seleccionada) ? seleccionada : TODAS);
    }

    @FXML
    private void onRegistrar() {
        int golesLocal;
        int golesVisita;
        try {
            golesLocal = Integer.parseInt(txtGolesLocal.getText().trim());
            golesVisita = Integer.parseInt(txtGolesVisita.getText().trim());
        } catch (NumberFormatException e) {
            advertir("Resultado inválido", "Los goles deben ser números enteros.");
            return;
        }
        try {
            liga.registrarResultado(cmbLocal.getValue(), cmbVisita.getValue(), golesLocal, golesVisita);
            // No se llama a tblEquipos.refresh(): las propiedades del modelo avisan a la tabla.
            txtGolesLocal.clear();
            txtGolesVisita.clear();
        } catch (IllegalArgumentException e) {
            advertir("Resultado inválido", e.getMessage());
        }
    }

    @FXML
    private void onAgregar() {
        try {
            liga.agregar(new Equipo(txtNuevoNombre.getText(), txtNuevaComuna.getText()));
            txtNuevoNombre.clear();
            txtNuevaComuna.clear();
        } catch (IllegalArgumentException e) {
            advertir("No se pudo agregar el equipo", e.getMessage());
        }
    }

    @FXML
    private void onEliminar() {
        Equipo seleccionado = tblEquipos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            advertir("Ningún equipo seleccionado", "Selecciona una fila de la tabla.");
            return;
        }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "Se eliminará " + seleccionado.getNombre() + " y sus estadísticas. ¿Continuar?");
        confirmacion.setHeaderText("Eliminar equipo");
        if (confirmacion.showAndWait().filter(ButtonType.OK::equals).isPresent()) {
            // Se elimina de la lista original, no de la tabla (sus items son una SortedList de solo lectura).
            liga.eliminar(seleccionado);
        }
    }

    private static void advertir(String encabezado, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING, mensaje);
        alerta.setHeaderText(encabezado);
        alerta.showAndWait();
    }
}
