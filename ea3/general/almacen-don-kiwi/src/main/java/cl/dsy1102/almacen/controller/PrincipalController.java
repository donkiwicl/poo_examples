package cl.dsy1102.almacen.controller;

import cl.dsy1102.almacen.Navegador;
import cl.dsy1102.almacen.dao.PersistenciaException;
import cl.dsy1102.almacen.model.Categoria;
import cl.dsy1102.almacen.model.Formato;
import cl.dsy1102.almacen.model.Producto;
import cl.dsy1102.almacen.model.ProductoPerecible;
import cl.dsy1102.almacen.repository.Repository;
import cl.dsy1102.almacen.repository.VentaRepository;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
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
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;

/**
 * Vista principal: catálogo de productos con búsqueda, filtro y acciones.
 */
public class PrincipalController {

    private static final String TODAS = "Todas";

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private TableView<Producto> tblProductos;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, Categoria> colCategoria;
    @FXML private TableColumn<Producto, String> colTipo;
    @FXML private TableColumn<Producto, Integer> colPrecio;
    @FXML private TableColumn<Producto, Integer> colStock;
    @FXML private TableColumn<Producto, LocalDate> colVence;
    @FXML private Label lblTotal;

    private Repository<Producto> productos;
    private VentaRepository ventas;
    private FilteredList<Producto> filtrados;

    @FXML
    private void initialize() {
        cmbCategoria.getItems().add(TODAS);
        for (Categoria categoria : Categoria.values()) {
            cmbCategoria.getItems().add(categoria.getNombre());
        }
        cmbCategoria.setValue(TODAS);

        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colTipo.setCellValueFactory(celda -> new ReadOnlyStringWrapper(celda.getValue().obtenerTipo()));
        // El valor es la fecha (ordena cronológicamente); el texto se formatea en la celda.
        colVence.setCellValueFactory(celda -> new ReadOnlyObjectWrapper<>(
                celda.getValue() instanceof ProductoPerecible perecible ? perecible.getFechaVencimiento() : null));
        colVence.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate fecha, boolean vacia) {
                super.updateItem(fecha, vacia);
                setText(vacia ? null : fecha == null ? "—" : fecha.format(Formato.FECHA));
            }
        });
        colPrecio.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(Integer precio, boolean vacia) {
                super.updateItem(precio, vacia);
                setText(vacia || precio == null ? null : Formato.pesos(precio));
            }
        });

        // Filas con stock bajo destacadas; doble clic para editar.
        tblProductos.setRowFactory(tabla -> {
            TableRow<Producto> fila = new TableRow<>() {
                @Override
                protected void updateItem(Producto producto, boolean vacia) {
                    super.updateItem(producto, vacia);
                    getStyleClass().remove("stock-bajo");
                    if (!vacia && producto != null && producto.tieneStockBajo()) {
                        getStyleClass().add("stock-bajo");
                    }
                }
            };
            fila.setOnMouseClicked(evento -> {
                if (evento.getClickCount() == 2 && !fila.isEmpty()) {
                    abrirFormulario(fila.getItem());
                }
            });
            return fila;
        });
    }

    public void inicializar(Repository<Producto> productos, VentaRepository ventas) {
        this.productos = productos;
        this.ventas = ventas;

        filtrados = new FilteredList<>(productos.listar(), producto -> true);
        txtBuscar.textProperty().addListener((obs, antes, texto) -> actualizarFiltro());
        cmbCategoria.valueProperty().addListener((obs, antes, categoria) -> actualizarFiltro());

        SortedList<Producto> ordenados = new SortedList<>(filtrados);
        ordenados.comparatorProperty().bind(tblProductos.comparatorProperty());
        tblProductos.setItems(ordenados);

        lblTotal.textProperty().bind(Bindings.format("%d de %d productos",
                Bindings.size(filtrados), Bindings.size(productos.listar())));
    }

    @FXML
    private void onNuevo() {
        abrirFormulario(null);
    }

    @FXML
    private void onEditar() {
        Producto seleccionado = obtenerSeleccion("editar");
        if (seleccionado != null) {
            abrirFormulario(seleccionado);
        }
    }

    @FXML
    private void onEliminar() {
        Producto seleccionado = obtenerSeleccion("eliminar");
        if (seleccionado == null
                || !Alertas.confirmar("Eliminar producto", "Se eliminará " + seleccionado + ".\n¿Continuar?")) {
            return;
        }
        try {
            productos.eliminar(seleccionado);
        } catch (PersistenciaException e) {
            Alertas.error("No se pudo eliminar el producto", e.getMessage());
        }
    }

    @FXML
    private void onRecargar() {
        try {
            productos.cargar();
        } catch (PersistenciaException e) {
            Alertas.error("No se pudieron cargar los productos", e.getMessage());
        }
    }

    @FXML
    private void onNuevaVenta() {
        VentaController venta = Navegador.navegar("venta-view.fxml", "Nueva venta");
        venta.inicializar(productos, ventas);
    }

    @FXML
    private void onReportes() {
        ReportesController reportes = Navegador.navegar("reportes-view.fxml", "Reportes de ventas");
        reportes.inicializar(ventas);
    }

    private void abrirFormulario(Producto producto) {
        String titulo = producto == null ? "Nuevo producto" : "Editar " + producto.getNombre();
        FormularioController formulario = Navegador.navegar("formulario-view.fxml", titulo);
        formulario.inicializar(productos, producto);
    }

    private Producto obtenerSeleccion(String accion) {
        Producto seleccionado = tblProductos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            Alertas.advertencia("Ningún producto seleccionado", "Selecciona una fila de la tabla para " + accion + ".");
        }
        return seleccionado;
    }

    private void actualizarFiltro() {
        String texto = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase();
        String categoria = cmbCategoria.getValue();
        filtrados.setPredicate(producto ->
                (producto.getNombre().toLowerCase().contains(texto) || producto.getCodigo().toLowerCase().contains(texto))
                        && (categoria == null || TODAS.equals(categoria)
                        || producto.getCategoria().getNombre().equals(categoria)));
    }
}
