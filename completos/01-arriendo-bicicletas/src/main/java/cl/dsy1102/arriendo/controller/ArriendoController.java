package cl.dsy1102.arriendo.controller;

import cl.dsy1102.arriendo.Navegador;
import cl.dsy1102.arriendo.dao.PersistenciaException;
import cl.dsy1102.arriendo.model.ArriendoRechazadoException;
import cl.dsy1102.arriendo.model.Vehiculo;
import cl.dsy1102.arriendo.repository.Repository;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Arriendo de un vehiculo. Las reglas (disponibilidad, horas, bateria) las
 * aplica el modelo; el controlador solo convierte el texto y muestra el resultado.
 */
public class ArriendoController {

    @FXML private Label lblTitulo;
    @FXML private Label lblDetalle;
    @FXML private TextField txtHoras;
    @FXML private Label lblCosto;
    @FXML private Button btnArrendar;
    @FXML private Label lblResultado;

    private Repository<Vehiculo> repositorio;
    private Vehiculo vehiculo;

    @FXML
    private void initialize() {
        // Costo estimado mientras se escribe.
        txtHoras.textProperty().addListener((obs, antes, texto) -> actualizarCosto());
    }

    public void inicializar(Repository<Vehiculo> repositorio, Vehiculo vehiculo) {
        this.repositorio = repositorio;
        this.vehiculo = vehiculo;
        lblTitulo.setText("Arrendar " + vehiculo.getCodigo());
        lblDetalle.setText(vehiculo.obtenerDetalle());
        actualizarCosto();
    }

    @FXML
    private void onArrendar() {
        int horas;
        try {
            horas = Integer.parseInt(txtHoras.getText().trim());
        } catch (NumberFormatException e) {
            mostrar("Ingresa las horas como un número entero.", false);
            return;
        }
        // Se arrienda una copia; el original se reemplaza solo si el cambio se guardo.
        Vehiculo copia = vehiculo.copiar();
        try {
            String mensaje = copia.arrendar(horas);
            repositorio.actualizar(vehiculo, copia);
            vehiculo = copia;
            lblDetalle.setText(vehiculo.obtenerDetalle());
            btnArrendar.setDisable(true);
            mostrar(mensaje, true);
        } catch (ArriendoRechazadoException e) {
            mostrar("Arriendo rechazado: " + e.getMessage(), false);
        } catch (PersistenciaException e) {
            Alertas.error("No se pudo registrar el arriendo", e.getMessage());
        }
    }

    @FXML
    private void onVolver() {
        PrincipalController principal = Navegador.navegar("principal-view.fxml", "Arriendo Cerro Alegre");
        principal.inicializar(repositorio);
    }

    private void actualizarCosto() {
        if (vehiculo == null) {
            return;
        }
        try {
            int horas = Integer.parseInt(txtHoras.getText().trim());
            lblCosto.setText(horas >= 1 && horas <= Vehiculo.MAXIMO_HORAS
                    ? "Costo estimado: " + Vehiculo.pesos(vehiculo.calcularCosto(horas))
                    : "Entre 1 y " + Vehiculo.MAXIMO_HORAS + " horas");
        } catch (NumberFormatException e) {
            lblCosto.setText("");
        }
    }

    private void mostrar(String texto, boolean exito) {
        lblResultado.setText(texto);
        lblResultado.getStyleClass().setAll("label", "resultado", exito ? "resultado-ok" : "resultado-error");
    }
}
