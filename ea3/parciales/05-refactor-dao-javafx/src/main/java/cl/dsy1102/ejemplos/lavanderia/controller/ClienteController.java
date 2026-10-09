package cl.dsy1102.ejemplos.lavanderia.controller;

import cl.dsy1102.ejemplos.lavanderia.model.Cliente;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Controlador de la vista de clientes.
 *
 * ESTA CLASE FUNCIONA, PERO ESTÁ MAL DISEÑADA: mezcla la interfaz gráfica con
 * el acceso a la base de datos. Es el punto de partida de la refactorización
 * (lee el README antes de modificarla).
 */
public class ClienteController {

    @FXML private TableView<Cliente> tblClientes;
    @FXML private TableColumn<Cliente, Integer> colId;
    @FXML private TableColumn<Cliente, String> colNombre;
    @FXML private TableColumn<Cliente, String> colTelefono;
    @FXML private TableColumn<Cliente, String> colCorreo;
    @FXML private TableColumn<Cliente, String> colComuna;
    @FXML private TextField txtBuscar;
    @FXML private TextField txtNombre;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtComuna;
    @FXML private Label lblEstado;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colCorreo.setCellValueFactory(new PropertyValueFactory<>("correo"));
        colComuna.setCellValueFactory(new PropertyValueFactory<>("comuna"));
        cargarClientes();
    }

    private void cargarClientes() {
        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/lavanderia_burbuja", "root", "");
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM cliente ORDER BY nombre");
            tblClientes.getItems().clear();
            while (rs.next()) {
                tblClientes.getItems().add(new Cliente(rs.getInt(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getString(5)));
            }
            lblEstado.setText(tblClientes.getItems().size() + " clientes");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onRecargar() {
        txtBuscar.clear();
        cargarClientes();
    }

    @FXML
    private void onBuscar() {
        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/lavanderia_burbuja", "root", "");
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM cliente WHERE nombre LIKE '%" + txtBuscar.getText()
                    + "%' ORDER BY nombre");
            tblClientes.getItems().clear();
            while (rs.next()) {
                tblClientes.getItems().add(new Cliente(rs.getInt(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getString(5)));
            }
            lblEstado.setText(tblClientes.getItems().size() + " clientes");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onGuardar() {
        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/lavanderia_burbuja", "root", "");
            Statement st = con.createStatement();
            st.executeUpdate("INSERT INTO cliente (nombre, telefono, correo, comuna) VALUES ('" + txtNombre.getText()
                    + "', '" + txtTelefono.getText() + "', '" + txtCorreo.getText() + "', '" + txtComuna.getText() + "')");
            cargarClientes();
            onLimpiar();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onActualizar() {
        Cliente seleccionado = tblClientes.getSelectionModel().getSelectedItem();
        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/lavanderia_burbuja", "root", "");
            Statement st = con.createStatement();
            st.executeUpdate("UPDATE cliente SET nombre = '" + txtNombre.getText() + "', telefono = '"
                    + txtTelefono.getText() + "', correo = '" + txtCorreo.getText() + "', comuna = '"
                    + txtComuna.getText() + "' WHERE id = " + seleccionado.getId());
            lblEstado.setText("Cliente actualizado");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onEliminar() {
        Cliente seleccionado = tblClientes.getSelectionModel().getSelectedItem();
        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/lavanderia_burbuja", "root", "");
            Statement st = con.createStatement();
            st.executeUpdate("DELETE FROM cliente WHERE id = " + seleccionado.getId());
            tblClientes.getItems().remove(seleccionado);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onLimpiar() {
        txtNombre.clear();
        txtTelefono.clear();
        txtCorreo.clear();
        txtComuna.clear();
        tblClientes.getSelectionModel().clearSelection();
    }
}
