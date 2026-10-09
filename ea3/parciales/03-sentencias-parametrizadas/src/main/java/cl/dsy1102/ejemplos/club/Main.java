package cl.dsy1102.ejemplos.club;

import cl.dsy1102.ejemplos.club.dao.ConexionBD;
import cl.dsy1102.ejemplos.club.dao.SocioDao;
import cl.dsy1102.ejemplos.club.dao.SocioDuplicadoException;
import cl.dsy1102.ejemplos.club.model.Categoria;
import cl.dsy1102.ejemplos.club.model.Socio;

import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Demostración de sentencias parametrizadas. Ejecuta con: mvn compile exec:java
 * (reinicia antes la tabla con sql/tablas.sql y sql/datos.sql para ver la misma salida).
 */
public class Main {

    public static void main(String[] args) {
        SocioDao dao = new SocioDao(ConexionBD.desdeRecurso("/db.properties"));
        try {
            System.out.println("=== Club Deportivo Los Halcones ===");
            dao.listarTodos().forEach(System.out::println);

            String ataque = "' OR '1'='1";
            System.out.println("\n-- Búsqueda con el texto: " + ataque);
            System.out.println("Insegura: " + dao.buscarPorNombreInseguro(ataque).size() + " socios (¡todos!)");
            System.out.println("Segura:   " + dao.buscarPorNombre(ataque).size() + " socios");

            System.out.println("\n-- Búsqueda: O'Higgins");
            try {
                dao.buscarPorNombreInseguro("O'Higgins");
            } catch (SQLException e) {
                System.out.println("Insegura: error de SQL (" + e.getClass().getSimpleName() + ")");
            }
            System.out.println("Segura:   " + dao.buscarPorNombre("O'Higgins"));

            System.out.println("\n-- Insertar");
            Socio nuevo = new Socio("20.111.222-3", "Javiera Lagos", Categoria.ADULTO, 18000, LocalDate.now());
            dao.insertar(nuevo);
            System.out.println("Insertado con id " + nuevo.getId());
            try {
                dao.insertar(new Socio("20.111.222-3", "Otra Persona", Categoria.ADULTO, 18000, LocalDate.now()));
            } catch (SocioDuplicadoException e) {
                System.out.println("Rechazado: " + e.getMessage());
            }

            System.out.println("\n-- Actualizar y eliminar");
            nuevo.setCategoria(Categoria.SENIOR);
            nuevo.setCuotaMensual(12000);
            System.out.println("Actualizar #" + nuevo.getId() + ": " + dao.actualizar(nuevo));
            System.out.println("Eliminar #" + nuevo.getId() + ": " + dao.eliminar(nuevo.getId()));
            System.out.println("Eliminar #" + nuevo.getId() + " otra vez: " + dao.eliminar(nuevo.getId()));

            System.out.println("\n-- Reajuste de 10 % a INFANTIL (solo activos)");
            System.out.println(dao.reajustarCuotas(Categoria.INFANTIL, 10) + " socio(s) reajustado(s)");

            System.out.println("\n-- ADULTO que ingresaron desde 2022");
            dao.filtrar(Categoria.ADULTO, LocalDate.of(2022, 1, 1)).forEach(System.out::println);
        } catch (SQLException | SocioDuplicadoException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
