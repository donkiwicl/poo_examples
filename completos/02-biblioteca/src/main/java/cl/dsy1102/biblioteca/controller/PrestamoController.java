package cl.dsy1102.biblioteca.controller;

import cl.dsy1102.biblioteca.Navegador;
import cl.dsy1102.biblioteca.dao.PersistenciaException;
import cl.dsy1102.biblioteca.model.Material;
import cl.dsy1102.biblioteca.model.PrestamoRechazadoException;
import cl.dsy1102.biblioteca.repository.Repository;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;

/**
 * Prestamo de un material. Las reglas las aplica el modelo; el controlador
 * solo lee los campos y muestra el resultado.
 */
public class PrestamoController {

    @FXML private Label lblTitulo;
    @FXML private Label lblDetalle;
    @FXML private TextField txtLector;
    @FXML private Spinner<Integer> spnDias;
    @FXML private Button btnPrestar;
    @FXML private Label lblResultado;

    private Repository<Material> repositorio;
    private Material material;

    public void inicializar(Repository<Material> repositorio, Material material) {
        this.repositorio = repositorio;
        this.material = material;
        lblTitulo.setText("Préstamo de " + material.getCodigo());
        lblDetalle.setText(material.obtenerDetalle());
        // El maximo depende del tipo de material: se pregunta al modelo (polimorfismo).
        spnDias.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                1, material.getDiasMaximos(), material.getDiasMaximos()));
    }

    @FXML
    private void onPrestar() {
        Material copia = material.copiar();
        try {
            String mensaje = copia.prestar(txtLector.getText(), spnDias.getValue());
            repositorio.actualizar(material, copia);
            material = copia;
            lblDetalle.setText(material.obtenerDetalle());
            txtLector.clear();
            mostrar(mensaje, true);
        } catch (PrestamoRechazadoException e) {
            mostrar("Préstamo rechazado: " + e.getMessage(), false);
        } catch (PersistenciaException e) {
            Alertas.error("No se pudo registrar el préstamo", e.getMessage());
        }
    }

    @FXML
    private void onVolver() {
        PrincipalController principal = Navegador.navegar("principal-view.fxml", "Biblioteca Gabriela Mistral");
        principal.inicializar(repositorio);
    }

    private void mostrar(String texto, boolean exito) {
        lblResultado.setText(texto);
        lblResultado.getStyleClass().setAll("label", "resultado", exito ? "resultado-ok" : "resultado-error");
    }
}
