package cl.dsy1102.almacen.controller;

import cl.dsy1102.almacen.Navegador;
import cl.dsy1102.almacen.dao.PersistenciaException;
import cl.dsy1102.almacen.model.Formato;
import cl.dsy1102.almacen.model.LineaVenta;
import cl.dsy1102.almacen.model.Producto;
import cl.dsy1102.almacen.model.ProductoPerecible;
import cl.dsy1102.almacen.model.Venta;
import cl.dsy1102.almacen.model.VentaRechazadaException;
import cl.dsy1102.almacen.repository.Repository;
import cl.dsy1102.almacen.repository.VentaRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.time.LocalDate;

/**
 * Caja: arma una venta en memoria y la registra completa al confirmar.
 */
public class VentaController {

    @FXML private Label lblFecha;
    @FXML private ComboBox<Producto> cmbProducto;
    @FXML private Spinner<Integer> spnCantidad;
    @FXML private Label lblPrecio;
    @FXML private Label lblMensaje;
    @FXML private TableView<LineaVenta> tblLineas;
    @FXML private TableColumn<LineaVenta, String> colCodigo;
    @FXML private TableColumn<LineaVenta, String> colProducto;
    @FXML private TableColumn<LineaVenta, Integer> colCantidad;
    @FXML private TableColumn<LineaVenta, Integer> colPrecio;
    @FXML private TableColumn<LineaVenta, Integer> colSubtotal;
    @FXML private Label lblTotal;

    private final ObservableList<LineaVenta> lineas = FXCollections.observableArrayList();
    private VentaRepository ventas;
    private Venta venta = new Venta();

    @FXML
    private void initialize() {
        lblFecha.setText("Fecha: " + LocalDate.now().format(Formato.FECHA));
        spnCantidad.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));
        spnCantidad.setEditable(true);

        cmbProducto.setConverter(new StringConverter<>() {
            @Override
            public String toString(Producto producto) {
                return producto == null ? "" : producto + " · stock " + producto.getStock();
            }

            @Override
            public Producto fromString(String texto) {
                return null; // el ComboBox no es editable
            }
        });
        cmbProducto.valueProperty().addListener((obs, antes, producto) -> mostrarPrecio(producto));

        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colProducto.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        colPrecio.setCellFactory(columna -> celdaPesos());
        colSubtotal.setCellFactory(columna -> celdaPesos());
        tblLineas.setItems(lineas);
        actualizarTotal();
    }

    public void inicializar(Repository<Producto> productos, VentaRepository ventas) {
        this.ventas = ventas;
        // Solo productos con stock. La lista filtrada sigue a la del repositorio:
        // si se recargan los productos, el ComboBox se actualiza solo.
        cmbProducto.setItems(new FilteredList<>(productos.listar(), producto -> producto.getStock() > 0));
    }

    @FXML
    private void onAgregar() {
        Producto producto = cmbProducto.getValue();
        if (producto == null) {
            mostrarMensaje("Selecciona un producto.", false);
            return;
        }
        try {
            mostrarMensaje(venta.agregar(producto, spnCantidad.getValue(), LocalDate.now()), true);
            lineas.setAll(venta.getLineas());
            actualizarTotal();
            spnCantidad.getValueFactory().setValue(1);
        } catch (VentaRechazadaException e) {
            mostrarMensaje(e.getMessage(), false);
        }
    }

    @FXML
    private void onQuitar() {
        int indice = tblLineas.getSelectionModel().getSelectedIndex();
        if (indice < 0) {
            mostrarMensaje("Selecciona la línea que quieres quitar.", false);
            return;
        }
        venta.quitar(indice);
        lineas.setAll(venta.getLineas());
        actualizarTotal();
        mostrarMensaje("Línea quitada.", true);
    }

    @FXML
    private void onConfirmar() {
        if (venta.estaVacia()) {
            mostrarMensaje("Agrega al menos un producto antes de confirmar.", false);
            return;
        }
        if (!Alertas.confirmar("Confirmar venta", "Total a pagar: " + Formato.pesos(venta.getTotal())
                + " (" + venta.getUnidades() + " unidades).\n¿Registrar la venta?")) {
            return;
        }
        try {
            ventas.registrar(venta);
            Alertas.informacion("Venta registrada", "Boleta N° " + venta.getId() + " por "
                    + Formato.pesos(venta.getTotal()) + ".");
            Navegador.volverAlInicio();
        } catch (PersistenciaException e) {
            // Nada quedó guardado (rollback). La venta sigue en pantalla para corregirla.
            Alertas.error("No se pudo registrar la venta", e.getMessage());
            mostrarMensaje("La venta no se registró. Corrige las cantidades y vuelve a confirmar.", false);
        }
    }

    @FXML
    private void onCancelar() {
        if (!venta.estaVacia() && !Alertas.confirmar("Cancelar venta", "Se descartarán los productos agregados.")) {
            return;
        }
        Navegador.volverAlInicio();
    }

    private void mostrarPrecio(Producto producto) {
        if (producto == null) {
            lblPrecio.setText("");
            return;
        }
        LocalDate hoy = LocalDate.now();
        String texto = "Precio hoy: " + Formato.pesos(producto.precioVenta(hoy));
        if (producto instanceof ProductoPerecible perecible) {
            if (perecible.estaVencido(hoy)) {
                texto = "Vencido el " + perecible.getFechaVencimiento().format(Formato.FECHA) + ": no se puede vender";
            } else if (producto.precioVenta(hoy) < producto.getPrecio()) {
                texto += " (30 % de descuento: vence en " + perecible.diasParaVencer(hoy) + " días)";
            }
        }
        lblPrecio.setText(texto);
    }

    private void actualizarTotal() {
        lblTotal.setText("Total: " + Formato.pesos(venta.getTotal()) + " · " + venta.getUnidades() + " unidades");
    }

    private void mostrarMensaje(String texto, boolean exito) {
        lblMensaje.setText(texto);
        lblMensaje.getStyleClass().removeAll("resultado-ok", "resultado-error");
        lblMensaje.getStyleClass().add(exito ? "resultado-ok" : "resultado-error");
    }

    private static TableCell<LineaVenta, Integer> celdaPesos() {
        return new TableCell<>() {
            @Override
            protected void updateItem(Integer monto, boolean vacia) {
                super.updateItem(monto, vacia);
                setText(vacia || monto == null ? null : Formato.pesos(monto));
            }
        };
    }
}
