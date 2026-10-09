package cl.dsy1102.arriendo.controller;

import cl.dsy1102.arriendo.Navegador;
import cl.dsy1102.arriendo.dao.PersistenciaException;
import cl.dsy1102.arriendo.model.Bicicleta;
import cl.dsy1102.arriendo.model.ScooterElectrico;
import cl.dsy1102.arriendo.model.Vehiculo;
import cl.dsy1102.arriendo.repository.Repository;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/**
 * Formulario para crear o editar un vehiculo.
 *
 * El controlador valida campos vacios y numeros que no se pueden convertir;
 * los rangos (tarifa, bateria, formato del codigo) los valida el modelo en
 * sus setters y el mensaje se muestra al usuario.
 */
public class FormularioController {

    private static final String BICICLETA = "Bicicleta";
    private static final String SCOOTER = "Scooter";
    private static final String CLASE_ERROR = "campo-error";

    @FXML private Label lblTitulo;
    @FXML private ComboBox<String> cmbTipo;
    @FXML private TextField txtCodigo;
    @FXML private TextField txtModelo;
    @FXML private TextField txtTarifa;
    @FXML private VBox boxBicicleta;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private CheckBox chkCanasto;
    @FXML private VBox boxScooter;
    @FXML private TextField txtBateria;
    @FXML private TextField txtAutonomia;

    private Repository<Vehiculo> repositorio;
    private Vehiculo original;

    @FXML
    private void initialize() {
        cmbTipo.getItems().setAll(BICICLETA, SCOOTER);
        cmbCategoria.getItems().setAll(Bicicleta.CATEGORIAS);
        // Solo se muestran los campos del tipo elegido; managed evita que ocupen espacio ocultos.
        boxBicicleta.visibleProperty().bind(cmbTipo.valueProperty().isEqualTo(BICICLETA));
        boxBicicleta.managedProperty().bind(boxBicicleta.visibleProperty());
        boxScooter.visibleProperty().bind(cmbTipo.valueProperty().isEqualTo(SCOOTER));
        boxScooter.managedProperty().bind(boxScooter.visibleProperty());
    }

    /**
     * Recibe los datos desde la vista principal.
     *
     * @param vehiculo el vehiculo a editar, o null para registrar uno nuevo
     */
    public void inicializar(Repository<Vehiculo> repositorio, Vehiculo vehiculo) {
        this.repositorio = repositorio;
        this.original = vehiculo;
        if (vehiculo == null) {
            lblTitulo.setText("Nuevo vehículo");
            return;
        }
        lblTitulo.setText("Editar " + vehiculo.getCodigo());
        cmbTipo.setValue(vehiculo.obtenerTipo());
        cmbTipo.setDisable(true);
        txtCodigo.setText(vehiculo.getCodigo());
        txtCodigo.setDisable(true);   // el codigo identifica al vehiculo
        txtModelo.setText(vehiculo.getModelo());
        txtTarifa.setText(String.valueOf(vehiculo.getTarifaHora()));
        if (vehiculo instanceof Bicicleta bicicleta) {
            cmbCategoria.setValue(bicicleta.getCategoria());
            chkCanasto.setSelected(bicicleta.isConCanasto());
        } else if (vehiculo instanceof ScooterElectrico scooter) {
            txtBateria.setText(String.valueOf(scooter.getBateria()));
            txtAutonomia.setText(String.valueOf(scooter.getAutonomiaKm()));
        }
    }

    @FXML
    private void onGuardar() {
        limpiarErrores();
        List<String> errores = new ArrayList<>();

        String tipo = cmbTipo.getValue();
        if (tipo == null) {
            marcar(cmbTipo, "Selecciona el tipo de vehículo.", errores);
        }
        String codigo = leerTexto(txtCodigo, "Código", errores);
        String modelo = leerTexto(txtModelo, "Modelo", errores);
        Integer tarifa = leerEntero(txtTarifa, "Tarifa por hora", errores);
        Integer bateria = null;
        Integer autonomia = null;
        if (BICICLETA.equals(tipo) && cmbCategoria.getValue() == null) {
            marcar(cmbCategoria, "Selecciona la categoría de la bicicleta.", errores);
        } else if (SCOOTER.equals(tipo)) {
            bateria = leerEntero(txtBateria, "Batería", errores);
            autonomia = leerEntero(txtAutonomia, "Autonomía", errores);
        }

        if (!errores.isEmpty()) {
            Alertas.advertencia("Revisa los datos del formulario", String.join("\n", errores));
            return;
        }

        try {
            if (original == null) {
                Vehiculo nuevo = BICICLETA.equals(tipo)
                        ? new Bicicleta(codigo, modelo, tarifa, cmbCategoria.getValue(), chkCanasto.isSelected())
                        : new ScooterElectrico(codigo, modelo, tarifa, bateria, autonomia);
                repositorio.agregar(nuevo);
            } else {
                // Se edita una copia: conserva el estado (disponible) y si algo falla el original no cambia.
                Vehiculo editado = original.copiar();
                editado.setModelo(modelo);
                editado.setTarifaHora(tarifa);
                if (editado instanceof Bicicleta bicicleta) {
                    bicicleta.setCategoria(cmbCategoria.getValue());
                    bicicleta.setConCanasto(chkCanasto.isSelected());
                } else if (editado instanceof ScooterElectrico scooter) {
                    scooter.setBateria(bateria);
                    scooter.setAutonomiaKm(autonomia);
                }
                repositorio.actualizar(original, editado);
            }
            volver();
        } catch (IllegalArgumentException e) {
            // Rango invalido (modelo) o codigo repetido (repositorio).
            Alertas.advertencia("Dato no válido", e.getMessage());
        } catch (PersistenciaException e) {
            Alertas.error("No se pudo guardar el vehículo", e.getMessage());
        }
    }

    @FXML
    private void onVolver() {
        volver();
    }

    private void volver() {
        PrincipalController principal = Navegador.navegar("principal-view.fxml", "Arriendo Cerro Alegre");
        principal.inicializar(repositorio);
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
        List.of(cmbTipo, txtCodigo, txtModelo, txtTarifa, cmbCategoria, txtBateria, txtAutonomia)
                .forEach(control -> control.getStyleClass().remove(CLASE_ERROR));
    }
}
