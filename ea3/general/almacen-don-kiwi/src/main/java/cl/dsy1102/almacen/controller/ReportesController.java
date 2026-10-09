package cl.dsy1102.almacen.controller;

import cl.dsy1102.almacen.Navegador;
import cl.dsy1102.almacen.dao.PersistenciaException;
import cl.dsy1102.almacen.model.Formato;
import cl.dsy1102.almacen.model.ProductoVendido;
import cl.dsy1102.almacen.model.ResumenCategoria;
import cl.dsy1102.almacen.repository.VentaRepository;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.converter.LocalDateStringConverter;

import java.time.LocalDate;
import java.util.List;

/**
 * Reportes de ventas calculados por la base de datos (GROUP BY, JOIN).
 */
public class ReportesController {

    private static final int TOP = 5;

    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;
    @FXML private Label lblResumen;
    @FXML private TableView<ResumenCategoria> tblCategorias;
    @FXML private TableColumn<ResumenCategoria, String> colCategoria;
    @FXML private TableColumn<ResumenCategoria, Integer> colUnidadesCategoria;
    @FXML private TableColumn<ResumenCategoria, Integer> colTotalCategoria;
    @FXML private TableView<ProductoVendido> tblMasVendidos;
    @FXML private TableColumn<ProductoVendido, String> colCodigo;
    @FXML private TableColumn<ProductoVendido, String> colProducto;
    @FXML private TableColumn<ProductoVendido, Integer> colUnidades;
    @FXML private TableColumn<ProductoVendido, Integer> colTotal;

    private VentaRepository ventas;

    @FXML
    private void initialize() {
        dpDesde.setConverter(new LocalDateStringConverter(Formato.FECHA, Formato.FECHA));
        dpHasta.setConverter(new LocalDateStringConverter(Formato.FECHA, Formato.FECHA));
        dpHasta.setValue(LocalDate.now());
        dpDesde.setValue(LocalDate.now().minusDays(6)); // últimos 7 días

        // Los records no tienen getX(): PropertyValueFactory no sirve, se usan lambdas.
        colCategoria.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().categoria().getNombre()));
        colUnidadesCategoria.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().unidades()));
        colTotalCategoria.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().total()));
        colTotalCategoria.setCellFactory(columna -> celdaPesos());

        colCodigo.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().codigo()));
        colProducto.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().nombre()));
        colUnidades.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().unidades()));
        colTotal.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().total()));
        colTotal.setCellFactory(columna -> celdaPesos());
    }

    public void inicializar(VentaRepository ventas) {
        this.ventas = ventas;
        onConsultar();
    }

    @FXML
    private void onConsultar() {
        LocalDate desde = dpDesde.getValue();
        LocalDate hasta = dpHasta.getValue();
        if (desde == null || hasta == null || desde.isAfter(hasta)) {
            Alertas.advertencia("Período inválido", "Elige las dos fechas; \"desde\" no puede ser posterior a \"hasta\".");
            return;
        }
        try {
            List<ResumenCategoria> porCategoria = ventas.ventasPorCategoria(desde, hasta);
            tblCategorias.getItems().setAll(porCategoria);
            tblMasVendidos.getItems().setAll(ventas.masVendidos(desde, hasta, TOP));
            int boletas = ventas.contarVentas(desde, hasta);
            int total = porCategoria.stream().mapToInt(ResumenCategoria::total).sum();
            lblResumen.setText(boletas + (boletas == 1 ? " venta" : " ventas") + " · " + Formato.pesos(total));
        } catch (PersistenciaException e) {
            tblCategorias.getItems().clear();
            tblMasVendidos.getItems().clear();
            lblResumen.setText("Sin datos");
            Alertas.error("No se pudieron consultar las ventas", e.getMessage());
        }
    }

    @FXML
    private void onVolver() {
        Navegador.volverAlInicio();
    }

    private static <T> TableCell<T, Integer> celdaPesos() {
        return new TableCell<>() {
            @Override
            protected void updateItem(Integer monto, boolean vacia) {
                super.updateItem(monto, vacia);
                setText(vacia || monto == null ? null : Formato.pesos(monto));
            }
        };
    }
}
