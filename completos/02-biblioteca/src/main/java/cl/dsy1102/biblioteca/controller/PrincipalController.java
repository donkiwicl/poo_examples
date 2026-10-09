package cl.dsy1102.biblioteca.controller;

import cl.dsy1102.biblioteca.Navegador;
import cl.dsy1102.biblioteca.dao.PersistenciaException;
import cl.dsy1102.biblioteca.model.ConsultaEnSala;
import cl.dsy1102.biblioteca.model.Material;
import cl.dsy1102.biblioteca.model.PrestamoRechazadoException;
import cl.dsy1102.biblioteca.repository.Repository;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.Optional;

/**
 * Vista principal: coleccion con busqueda, filtro por tipo y acciones.
 */
public class PrincipalController {

    private static final String TODOS = "Todos";

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbTipo;
    @FXML private TableView<Material> tblMateriales;
    @FXML private TableColumn<Material, String> colTipo;
    @FXML private TableColumn<Material, String> colCodigo;
    @FXML private TableColumn<Material, String> colTitulo;
    @FXML private TableColumn<Material, Integer> colAnio;
    @FXML private TableColumn<Material, Integer> colDisponibles;
    @FXML private TableColumn<Material, String> colPrestamo;
    @FXML private Label lblTotal;

    private Repository<Material> repositorio;
    private FilteredList<Material> filtrados;

    @FXML
    private void initialize() {
        cmbTipo.getItems().setAll(TODOS, "Libro", "Revista");
        cmbTipo.setValue(TODOS);

        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colAnio.setCellValueFactory(new PropertyValueFactory<>("anio"));
        // getDisponibles() es un getter calculado: PropertyValueFactory tambien lo encuentra.
        colDisponibles.setCellValueFactory(new PropertyValueFactory<>("disponibles"));
        colTipo.setCellValueFactory(celda -> new ReadOnlyStringWrapper(celda.getValue().obtenerTipo()));
        colPrestamo.setCellValueFactory(celda -> new ReadOnlyStringWrapper(textoPrestamo(celda.getValue())));

        // "1 de 3" y en rojo cuando no quedan ejemplares.
        colDisponibles.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(Integer disponibles, boolean vacia) {
                super.updateItem(disponibles, vacia);
                getStyleClass().remove("agotado");
                // La celda solo recibe el numero; el total de ejemplares se lee de la fila.
                Material material = vacia || getTableRow() == null ? null : getTableRow().getItem();
                if (material == null || disponibles == null) {
                    setText(null);
                    return;
                }
                setText(disponibles + " de " + material.getEjemplares());
                if (disponibles == 0) {
                    getStyleClass().add("agotado");
                }
            }
        });

        tblMateriales.setRowFactory(tabla -> {
            TableRow<Material> fila = new TableRow<>();
            fila.setOnMouseClicked(evento -> {
                if (evento.getClickCount() == 2 && !fila.isEmpty()) {
                    abrirFormulario(fila.getItem());
                }
            });
            return fila;
        });
    }

    public void inicializar(Repository<Material> repositorio) {
        this.repositorio = repositorio;

        filtrados = new FilteredList<>(repositorio.listar(), material -> true);
        txtBuscar.textProperty().addListener((obs, antes, texto) -> actualizarFiltro());
        cmbTipo.valueProperty().addListener((obs, antes, tipo) -> actualizarFiltro());

        SortedList<Material> ordenados = new SortedList<>(filtrados);
        ordenados.comparatorProperty().bind(tblMateriales.comparatorProperty());
        tblMateriales.setItems(ordenados);

        lblTotal.textProperty().bind(Bindings.format("%d de %d materiales",
                Bindings.size(filtrados), Bindings.size(repositorio.listar())));
    }

    @FXML
    private void onNuevo() {
        abrirFormulario(null);
    }

    @FXML
    private void onEditar() {
        Material seleccionado = obtenerSeleccion("editar");
        if (seleccionado != null) {
            abrirFormulario(seleccionado);
        }
    }

    @FXML
    private void onEliminar() {
        Material seleccionado = obtenerSeleccion("eliminar");
        if (seleccionado == null) {
            return;
        }
        if (seleccionado.getPrestados() > 0) {
            Alertas.advertencia("Material prestado", "\"" + seleccionado.getTitulo() + "\" tiene "
                    + seleccionado.getPrestados() + " ejemplar(es) prestado(s). Registra su devolución antes de eliminarlo.");
            return;
        }
        if (!Alertas.confirmar("Eliminar material", "Se eliminará \"" + seleccionado.getTitulo() + "\". ¿Continuar?")) {
            return;
        }
        try {
            repositorio.eliminar(seleccionado);
        } catch (PersistenciaException e) {
            Alertas.error("No se pudo eliminar el material", e.getMessage());
        }
    }

    @FXML
    private void onPrestar() {
        Material seleccionado = obtenerSeleccion("prestar");
        if (seleccionado != null) {
            PrestamoController prestamo = Navegador.navegar("prestamo-view.fxml", "Préstamo - " + seleccionado.getCodigo());
            prestamo.inicializar(repositorio, seleccionado);
        }
    }

    /** Devolucion con TextInputDialog: pide los dias de atraso y muestra la multa. */
    @FXML
    private void onDevolver() {
        Material seleccionado = obtenerSeleccion("registrar su devolución");
        if (seleccionado == null) {
            return;
        }
        TextInputDialog dialogo = new TextInputDialog("0");
        dialogo.setTitle("Devolución");
        dialogo.setHeaderText("Devolución de \"" + seleccionado.getTitulo() + "\"");
        dialogo.setContentText("Días de atraso:");
        Optional<String> respuesta = dialogo.showAndWait();
        if (respuesta.isEmpty()) {
            return; // cancelo
        }

        int atraso;
        try {
            atraso = Integer.parseInt(respuesta.get().trim());
        } catch (NumberFormatException e) {
            Alertas.advertencia("Dato no válido", "Los días de atraso deben ser un número entero.");
            return;
        }

        Material copia = seleccionado.copiar();
        try {
            int multa = copia.devolver(atraso);
            repositorio.actualizar(seleccionado, copia);
            tblMateriales.getSelectionModel().select(copia);
            Alertas.informacion("Devolución registrada", multa == 0
                    ? "Sin multa."
                    : "Multa a pagar: " + Material.pesos(multa) + " (" + atraso + " días de atraso).");
        } catch (PrestamoRechazadoException e) {
            Alertas.advertencia("No se puede registrar la devolución", e.getMessage());
        } catch (IllegalArgumentException e) {
            Alertas.advertencia("Dato no válido", e.getMessage());
        } catch (PersistenciaException e) {
            Alertas.error("No se pudo guardar la devolución", e.getMessage());
        }
    }

    private void abrirFormulario(Material material) {
        String titulo = material == null ? "Nuevo material" : "Editar " + material.getCodigo();
        FormularioController formulario = Navegador.navegar("formulario-view.fxml", titulo);
        formulario.inicializar(repositorio, material);
    }

    private Material obtenerSeleccion(String accion) {
        Material seleccionado = tblMateriales.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            Alertas.advertencia("Ningún material seleccionado", "Selecciona una fila de la tabla para " + accion + ".");
        }
        return seleccionado;
    }

    private void actualizarFiltro() {
        String texto = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase();
        String tipo = cmbTipo.getValue();
        filtrados.setPredicate(material ->
                (material.getTitulo().toLowerCase().contains(texto) || material.getCodigo().toLowerCase().contains(texto))
                        && (tipo == null || TODOS.equals(tipo) || material.obtenerTipo().equals(tipo)));
    }

    private static String textoPrestamo(Material material) {
        if (material instanceof ConsultaEnSala sala && sala.isSoloSala()) {
            return "Solo sala";
        }
        return "Hasta " + material.getDiasMaximos() + " días";
    }
}
