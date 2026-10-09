package cl.dsy1102.arriendo.controller;

import cl.dsy1102.arriendo.Navegador;
import cl.dsy1102.arriendo.dao.PersistenciaException;
import cl.dsy1102.arriendo.model.ArriendoRechazadoException;
import cl.dsy1102.arriendo.model.Recargable;
import cl.dsy1102.arriendo.model.Vehiculo;
import cl.dsy1102.arriendo.repository.Repository;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Vista principal: flota con busqueda en tiempo real y acciones.
 */
public class PrincipalController {

    @FXML private TextField txtBuscar;
    @FXML private CheckBox chkSoloDisponibles;
    @FXML private TableView<Vehiculo> tblVehiculos;
    @FXML private TableColumn<Vehiculo, String> colTipo;
    @FXML private TableColumn<Vehiculo, String> colCodigo;
    @FXML private TableColumn<Vehiculo, String> colModelo;
    @FXML private TableColumn<Vehiculo, Integer> colTarifa;
    @FXML private TableColumn<Vehiculo, String> colEstado;
    @FXML private TableColumn<Vehiculo, String> colBateria;
    @FXML private Label lblTotal;

    private Repository<Vehiculo> repositorio;
    private FilteredList<Vehiculo> filtrados;

    /** Lo llama FXMLLoader al terminar de inyectar los @FXML. */
    @FXML
    private void initialize() {
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colModelo.setCellValueFactory(new PropertyValueFactory<>("modelo"));
        colTarifa.setCellValueFactory(new PropertyValueFactory<>("tarifaHora"));
        // Valores que no son getters del modelo: se calculan con un lambda.
        colTipo.setCellValueFactory(celda -> new ReadOnlyStringWrapper(celda.getValue().obtenerTipo()));
        colEstado.setCellValueFactory(celda -> new ReadOnlyStringWrapper(
                celda.getValue().isDisponible() ? "Disponible" : "Arrendado"));
        colBateria.setCellValueFactory(celda -> new ReadOnlyStringWrapper(textoBateria(celda.getValue())));

        colTarifa.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(Integer tarifa, boolean vacia) {
                super.updateItem(tarifa, vacia);
                setText(vacia || tarifa == null ? null : Vehiculo.pesos(tarifa));
            }
        });
        colEstado.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(String estado, boolean vacia) {
                super.updateItem(estado, vacia);
                getStyleClass().removeAll("estado-disponible", "estado-arrendado");
                setText(vacia ? null : estado);
                if (!vacia && estado != null) {
                    getStyleClass().add("Disponible".equals(estado) ? "estado-disponible" : "estado-arrendado");
                }
            }
        });

        // Doble clic sobre una fila abre la edicion.
        tblVehiculos.setRowFactory(tabla -> {
            TableRow<Vehiculo> fila = new TableRow<>();
            fila.setOnMouseClicked(evento -> {
                if (evento.getClickCount() == 2 && !fila.isEmpty()) {
                    abrirFormulario(fila.getItem());
                }
            });
            return fila;
        });
    }

    /** Recibe el repositorio desde quien navega hacia esta vista y enlaza la tabla. */
    public void inicializar(Repository<Vehiculo> repositorio) {
        this.repositorio = repositorio;

        filtrados = new FilteredList<>(repositorio.listar(), vehiculo -> true);
        txtBuscar.textProperty().addListener((obs, antes, texto) -> actualizarFiltro());
        chkSoloDisponibles.selectedProperty().addListener((obs, antes, marcado) -> actualizarFiltro());

        SortedList<Vehiculo> ordenados = new SortedList<>(filtrados);
        ordenados.comparatorProperty().bind(tblVehiculos.comparatorProperty());
        tblVehiculos.setItems(ordenados);

        lblTotal.textProperty().bind(Bindings.format("%d de %d vehículos",
                Bindings.size(filtrados), Bindings.size(repositorio.listar())));
    }

    @FXML
    private void onNuevo() {
        abrirFormulario(null);
    }

    @FXML
    private void onEditar() {
        Vehiculo seleccionado = obtenerSeleccion("editar");
        if (seleccionado != null) {
            abrirFormulario(seleccionado);
        }
    }

    @FXML
    private void onEliminar() {
        Vehiculo seleccionado = obtenerSeleccion("eliminar");
        if (seleccionado == null) {
            return;
        }
        if (!seleccionado.isDisponible()) {
            Alertas.advertencia("Vehículo arrendado", "Registra la devolución de " + seleccionado.getCodigo()
                    + " antes de eliminarlo.");
            return;
        }
        boolean confirma = Alertas.confirmar("Eliminar vehículo",
                "Se eliminará " + seleccionado.getCodigo() + " (" + seleccionado.getModelo() + "). ¿Continuar?");
        if (!confirma) {
            return;
        }
        try {
            repositorio.eliminar(seleccionado);
        } catch (PersistenciaException e) {
            Alertas.error("No se pudo eliminar el vehículo", e.getMessage());
        }
    }

    @FXML
    private void onArrendar() {
        Vehiculo seleccionado = obtenerSeleccion("arrendar");
        if (seleccionado != null) {
            ArriendoController arriendo = Navegador.navegar("arriendo-view.fxml", "Arrendar " + seleccionado.getCodigo());
            arriendo.inicializar(repositorio, seleccionado);
        }
    }

    @FXML
    private void onDevolver() {
        Vehiculo seleccionado = obtenerSeleccion("registrar su devolución");
        if (seleccionado == null) {
            return;
        }
        // Se modifica una copia: si el guardado falla, el original (y la tabla) quedan intactos.
        Vehiculo copia = seleccionado.copiar();
        try {
            copia.devolver();
            guardarCambio(seleccionado, copia);
        } catch (ArriendoRechazadoException e) {
            Alertas.advertencia("No se puede devolver", e.getMessage());
        }
    }

    @FXML
    private void onRecargar() {
        Vehiculo seleccionado = obtenerSeleccion("recargar");
        if (seleccionado == null) {
            return;
        }
        Vehiculo copia = seleccionado.copiar();
        // La capacidad se consulta por la interfaz, no por la clase concreta.
        if (copia instanceof Recargable recargable) {
            recargable.recargar();
            guardarCambio(seleccionado, copia);
        } else {
            Alertas.advertencia("No se puede recargar", seleccionado.getCodigo() + " no tiene batería.");
        }
    }

    private void guardarCambio(Vehiculo original, Vehiculo actualizado) {
        try {
            repositorio.actualizar(original, actualizado);
            tblVehiculos.getSelectionModel().select(actualizado);
        } catch (PersistenciaException e) {
            Alertas.error("No se pudo guardar el cambio", e.getMessage());
        }
    }

    private void abrirFormulario(Vehiculo vehiculo) {
        String titulo = vehiculo == null ? "Nuevo vehículo" : "Editar " + vehiculo.getCodigo();
        FormularioController formulario = Navegador.navegar("formulario-view.fxml", titulo);
        formulario.inicializar(repositorio, vehiculo);
    }

    private Vehiculo obtenerSeleccion(String accion) {
        Vehiculo seleccionado = tblVehiculos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            Alertas.advertencia("Ningún vehículo seleccionado", "Selecciona una fila de la tabla para " + accion + ".");
        }
        return seleccionado;
    }

    private void actualizarFiltro() {
        String texto = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase();
        boolean soloDisponibles = chkSoloDisponibles.isSelected();
        filtrados.setPredicate(vehiculo ->
                (vehiculo.getCodigo().toLowerCase().contains(texto) || vehiculo.getModelo().toLowerCase().contains(texto))
                        && (!soloDisponibles || vehiculo.isDisponible()));
    }

    private static String textoBateria(Vehiculo vehiculo) {
        return vehiculo instanceof Recargable recargable ? recargable.getBateria() + "%" : "—";
    }
}
