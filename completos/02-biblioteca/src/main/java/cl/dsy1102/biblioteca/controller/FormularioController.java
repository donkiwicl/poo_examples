package cl.dsy1102.biblioteca.controller;

import cl.dsy1102.biblioteca.Navegador;
import cl.dsy1102.biblioteca.dao.PersistenciaException;
import cl.dsy1102.biblioteca.model.Libro;
import cl.dsy1102.biblioteca.model.Material;
import cl.dsy1102.biblioteca.model.Revista;
import cl.dsy1102.biblioteca.repository.Repository;
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
 * Formulario para crear o editar un material.
 *
 * El controlador valida campos vacios y numeros que no se pueden convertir;
 * los rangos y formatos los valida el modelo en sus setters.
 */
public class FormularioController {

    private static final String LIBRO = "Libro";
    private static final String REVISTA = "Revista";
    private static final String CLASE_ERROR = "campo-error";

    @FXML private Label lblTitulo;
    @FXML private ComboBox<String> cmbTipo;
    @FXML private TextField txtCodigo;
    @FXML private TextField txtTitulo;
    @FXML private TextField txtAnio;
    @FXML private TextField txtEjemplares;
    @FXML private VBox boxLibro;
    @FXML private TextField txtAutor;
    @FXML private TextField txtPaginas;
    @FXML private VBox boxRevista;
    @FXML private TextField txtNumero;
    @FXML private CheckBox chkSoloSala;

    private Repository<Material> repositorio;
    private Material original;

    @FXML
    private void initialize() {
        cmbTipo.getItems().setAll(LIBRO, REVISTA);
        boxLibro.visibleProperty().bind(cmbTipo.valueProperty().isEqualTo(LIBRO));
        boxLibro.managedProperty().bind(boxLibro.visibleProperty());
        boxRevista.visibleProperty().bind(cmbTipo.valueProperty().isEqualTo(REVISTA));
        boxRevista.managedProperty().bind(boxRevista.visibleProperty());
    }

    /**
     * @param material el material a editar, o null para registrar uno nuevo
     */
    public void inicializar(Repository<Material> repositorio, Material material) {
        this.repositorio = repositorio;
        this.original = material;
        if (material == null) {
            lblTitulo.setText("Nuevo material");
            return;
        }
        lblTitulo.setText("Editar " + material.getCodigo());
        cmbTipo.setValue(material.obtenerTipo());
        cmbTipo.setDisable(true);
        txtCodigo.setText(material.getCodigo());
        txtCodigo.setDisable(true);
        txtTitulo.setText(material.getTitulo());
        txtAnio.setText(String.valueOf(material.getAnio()));
        txtEjemplares.setText(String.valueOf(material.getEjemplares()));
        if (material instanceof Libro libro) {
            txtAutor.setText(libro.getAutor());
            txtPaginas.setText(String.valueOf(libro.getPaginas()));
        } else if (material instanceof Revista revista) {
            txtNumero.setText(String.valueOf(revista.getNumero()));
            chkSoloSala.setSelected(revista.isSoloSala());
            // ConsultaEnSala solo permite restringir, no liberar.
            chkSoloSala.setDisable(revista.isSoloSala());
        }
    }

    @FXML
    private void onGuardar() {
        limpiarErrores();
        List<String> errores = new ArrayList<>();

        String tipo = cmbTipo.getValue();
        if (tipo == null) {
            marcar(cmbTipo, "Selecciona el tipo de material.", errores);
        }
        String codigo = leerTexto(txtCodigo, "Código", errores);
        String titulo = leerTexto(txtTitulo, "Título", errores);
        Integer anio = leerEntero(txtAnio, "Año", errores);
        Integer ejemplares = leerEntero(txtEjemplares, "Ejemplares", errores);
        String autor = null;
        Integer paginas = null;
        Integer numero = null;
        if (LIBRO.equals(tipo)) {
            autor = leerTexto(txtAutor, "Autor", errores);
            paginas = leerEntero(txtPaginas, "Páginas", errores);
        } else if (REVISTA.equals(tipo)) {
            numero = leerEntero(txtNumero, "Número", errores);
        }

        if (!errores.isEmpty()) {
            Alertas.advertencia("Revisa los datos del formulario", String.join("\n", errores));
            return;
        }

        try {
            if (original == null) {
                Material nuevo = LIBRO.equals(tipo)
                        ? new Libro(codigo, titulo, anio, ejemplares, autor, paginas)
                        : crearRevista(codigo, titulo, anio, ejemplares, numero);
                repositorio.agregar(nuevo);
            } else {
                // Se edita una copia: conserva los prestamos y si algo falla el original no cambia.
                Material editado = original.copiar();
                editado.setTitulo(titulo);
                editado.setAnio(anio);
                editado.setEjemplares(ejemplares);   // rechaza dejar menos que los prestados
                if (editado instanceof Libro libro) {
                    libro.setAutor(autor);
                    libro.setPaginas(paginas);
                } else if (editado instanceof Revista revista) {
                    revista.setNumero(numero);
                    if (chkSoloSala.isSelected()) {
                        revista.restringirASala();
                    }
                }
                repositorio.actualizar(original, editado);
            }
            volver();
        } catch (IllegalArgumentException e) {
            Alertas.advertencia("Dato no válido", e.getMessage());
        } catch (PersistenciaException e) {
            Alertas.error("No se pudo guardar el material", e.getMessage());
        }
    }

    @FXML
    private void onVolver() {
        volver();
    }

    private Revista crearRevista(String codigo, String titulo, int anio, int ejemplares, int numero) {
        Revista revista = new Revista(codigo, titulo, anio, ejemplares, numero);
        if (chkSoloSala.isSelected()) {
            revista.restringirASala();
        }
        return revista;
    }

    private void volver() {
        PrincipalController principal = Navegador.navegar("principal-view.fxml", "Biblioteca Gabriela Mistral");
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
            return Integer.parseInt(texto);
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
        List.of(cmbTipo, txtCodigo, txtTitulo, txtAnio, txtEjemplares, txtAutor, txtPaginas, txtNumero)
                .forEach(control -> control.getStyleClass().remove(CLASE_ERROR));
    }
}
