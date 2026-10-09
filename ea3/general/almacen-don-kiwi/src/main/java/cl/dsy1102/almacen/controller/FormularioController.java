package cl.dsy1102.almacen.controller;

import cl.dsy1102.almacen.Navegador;
import cl.dsy1102.almacen.dao.PersistenciaException;
import cl.dsy1102.almacen.model.Categoria;
import cl.dsy1102.almacen.model.Formato;
import cl.dsy1102.almacen.model.Producto;
import cl.dsy1102.almacen.model.ProductoNoPerecible;
import cl.dsy1102.almacen.model.ProductoPerecible;
import cl.dsy1102.almacen.repository.Repository;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.converter.LocalDateStringConverter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Formulario para crear o editar un producto.
 */
public class FormularioController {

    private static final String PERECIBLE = "Perecible";
    private static final String NO_PERECIBLE = "No perecible";
    private static final String CLASE_ERROR = "campo-error";

    @FXML private Label lblTitulo;
    @FXML private ComboBox<String> cmbTipo;
    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private ComboBox<Categoria> cmbCategoria;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtStock;
    @FXML private VBox boxPerecible;
    @FXML private DatePicker dpVencimiento;
    @FXML private VBox boxNoPerecible;
    @FXML private TextField txtMarca;

    private Repository<Producto> productos;
    private Producto original;

    @FXML
    private void initialize() {
        cmbTipo.getItems().setAll(PERECIBLE, NO_PERECIBLE);
        cmbCategoria.getItems().setAll(Categoria.values());
        boxPerecible.visibleProperty().bind(cmbTipo.valueProperty().isEqualTo(PERECIBLE));
        boxPerecible.managedProperty().bind(boxPerecible.visibleProperty());
        boxNoPerecible.visibleProperty().bind(cmbTipo.valueProperty().isEqualTo(NO_PERECIBLE));
        boxNoPerecible.managedProperty().bind(boxNoPerecible.visibleProperty());
        dpVencimiento.setConverter(new LocalDateStringConverter(Formato.FECHA, Formato.FECHA));
    }

    /**
     * @param producto el producto a editar, o null para registrar uno nuevo
     */
    public void inicializar(Repository<Producto> productos, Producto producto) {
        this.productos = productos;
        this.original = producto;
        if (producto == null) {
            lblTitulo.setText("Nuevo producto");
            return;
        }
        lblTitulo.setText("Editar " + producto.getNombre());
        cmbTipo.setValue(producto instanceof ProductoPerecible ? PERECIBLE : NO_PERECIBLE);
        cmbTipo.setDisable(true);
        txtCodigo.setText(producto.getCodigo());
        txtNombre.setText(producto.getNombre());
        cmbCategoria.setValue(producto.getCategoria());
        txtPrecio.setText(String.valueOf(producto.getPrecio()));
        txtStock.setText(String.valueOf(producto.getStock()));
        if (producto instanceof ProductoPerecible perecible) {
            dpVencimiento.setValue(perecible.getFechaVencimiento());
        } else if (producto instanceof ProductoNoPerecible noPerecible) {
            txtMarca.setText(noPerecible.getMarca());
        }
    }

    @FXML
    private void onGuardar() {
        limpiarErrores();
        List<String> errores = new ArrayList<>();

        String tipo = cmbTipo.getValue();
        if (tipo == null) {
            marcar(cmbTipo, "Selecciona el tipo de producto.", errores);
        }
        String codigo = leerTexto(txtCodigo, "Código", errores);
        String nombre = leerTexto(txtNombre, "Nombre", errores);
        Categoria categoria = cmbCategoria.getValue();
        if (categoria == null) {
            marcar(cmbCategoria, "Selecciona la categoría.", errores);
        }
        Integer precio = leerEntero(txtPrecio, "Precio", errores);
        Integer stock = leerEntero(txtStock, "Stock", errores);
        if (PERECIBLE.equals(tipo) && dpVencimiento.getValue() == null) {
            marcar(dpVencimiento, "Selecciona la fecha de vencimiento.", errores);
        } else if (NO_PERECIBLE.equals(tipo)) {
            leerTexto(txtMarca, "Marca", errores);
        }

        if (!errores.isEmpty()) {
            Alertas.advertencia("Revisa los datos del formulario", String.join("\n", errores));
            return;
        }

        try {
            if (original == null) {
                productos.agregar(crear(tipo, codigo, nombre, categoria, precio, stock));
            } else {
                // Se edita una copia: si algo falla, el original queda intacto.
                Producto editado = original.copiar();
                editado.setCodigo(codigo);
                editado.setNombre(nombre);
                editado.setCategoria(categoria);
                editado.setPrecio(precio);
                editado.setStock(stock);
                if (editado instanceof ProductoPerecible perecible) {
                    perecible.setFechaVencimiento(dpVencimiento.getValue());
                } else if (editado instanceof ProductoNoPerecible noPerecible) {
                    noPerecible.setMarca(txtMarca.getText());
                }
                productos.actualizar(original, editado);
            }
            Navegador.volverAlInicio();
        } catch (IllegalArgumentException e) {
            Alertas.advertencia("Dato no válido", e.getMessage());
        } catch (PersistenciaException e) {
            Alertas.error("No se pudo guardar el producto", e.getMessage());
        }
    }

    private Producto crear(String tipo, String codigo, String nombre, Categoria categoria, int precio, int stock) {
        if (PERECIBLE.equals(tipo)) {
            return new ProductoPerecible(codigo, nombre, categoria, precio, stock, dpVencimiento.getValue());
        }
        return new ProductoNoPerecible(codigo, nombre, categoria, precio, stock, txtMarca.getText());
    }

    @FXML
    private void onVolver() {
        Navegador.volverAlInicio();
    }

    private String leerTexto(TextField campo, String nombreCampo, List<String> errores) {
        String texto = campo.getText().trim();
        if (texto.isEmpty()) {
            marcar(campo, "El campo " + nombreCampo + " es obligatorio.", errores);
            return null;
        }
        return texto;
    }

    private Integer leerEntero(TextField campo, String nombreCampo, List<String> errores) {
        String texto = leerTexto(campo, nombreCampo, errores);
        if (texto == null) {
            return null;
        }
        try {
            return Integer.parseInt(texto.replace(".", ""));
        } catch (NumberFormatException e) {
            marcar(campo, "El campo " + nombreCampo + " debe ser un número entero.", errores);
            return null;
        }
    }

    private void marcar(Control campo, String mensaje, List<String> errores) {
        errores.add(mensaje);
        campo.getStyleClass().add(CLASE_ERROR);
    }

    private void limpiarErrores() {
        List.of(cmbTipo, txtCodigo, txtNombre, cmbCategoria, txtPrecio, txtStock, dpVencimiento, txtMarca)
                .forEach(control -> control.getStyleClass().remove(CLASE_ERROR));
    }
}
