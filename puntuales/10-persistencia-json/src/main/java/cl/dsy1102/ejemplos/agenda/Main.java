package cl.dsy1102.ejemplos.agenda;

import cl.dsy1102.ejemplos.agenda.dao.ContactoDao;
import cl.dsy1102.ejemplos.agenda.dao.JsonContactoDao;
import cl.dsy1102.ejemplos.agenda.dao.PersistenciaException;
import cl.dsy1102.ejemplos.agenda.model.Contacto;
import cl.dsy1102.ejemplos.agenda.model.ContactoLaboral;
import cl.dsy1102.ejemplos.agenda.model.ContactoPersonal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Demostracion de la persistencia. Ejecuta con: mvn compile exec:java
 * Ya esta resuelta: el trabajo esta en el modelo (R1, R2) y en JsonContactoDao (R3, R4).
 */
public class Main {

    public static void main(String[] args) throws IOException {
        Path archivo = Path.of("data", "contactos.json");
        Files.deleteIfExists(archivo);   // la demo siempre parte de cero

        // Main solo conoce la interfaz: cambiar a otro formato no afectaria este codigo.
        ContactoDao dao = new JsonContactoDao(archivo);

        System.out.println("== 1. Primera ejecucion (el archivo no existe)");
        mostrar(dao);

        System.out.println("\n== 2. Guardar tres contactos");
        List<Contacto> contactos = List.of(
                new ContactoPersonal("Ana Rojas", "+56911112222", "Ana.Rojas@mail.cl", "Anita", true),
                new ContactoPersonal("Bruno Diaz", "+56933334444", "bruno@mail.cl", "", false),
                new ContactoLaboral("Carla Soto", "225551234", "csoto@kiwi.cl", "Kiwi SpA", "Jefa de TI"));
        try {
            dao.guardar(contactos);
            System.out.println("Guardado en " + archivo.toAbsolutePath());
            System.out.println(Files.readString(archivo));
        } catch (PersistenciaException e) {
            System.out.println("ERROR: " + e.getMessage());
        }

        System.out.println("\n== 3. Cargar con un DAO nuevo (como si se reiniciara el programa)");
        mostrar(new JsonContactoDao(archivo));

        System.out.println("\n== 4. Archivo danado");
        Path danado = Path.of("data", "danado.json");
        Files.writeString(danado, "[ { \"tipo\" : \"PERSONAL\", \"nombre\" : ");
        mostrar(new JsonContactoDao(danado));

        System.out.println("\n== 5. Archivo con un dato que el modelo rechaza");
        Path invalido = Path.of("data", "invalido.json");
        Files.writeString(invalido, """
                [ { "tipo" : "LABORAL", "nombre" : "Diego", "telefono" : "123", "email" : "d@x.cl",
                    "empresa" : "X", "cargo" : "Y" } ]""");
        mostrar(new JsonContactoDao(invalido));
    }

    private static void mostrar(ContactoDao dao) {
        try {
            List<Contacto> contactos = dao.cargar();
            System.out.println(contactos.size() + " contacto(s)");
            for (Contacto contacto : contactos) {
                System.out.println("  " + contacto.obtenerDetalle());
            }
        } catch (PersistenciaException e) {
            System.out.println("ERROR: " + e.getMessage());
            System.out.println("  causa: " + e.getCause().getClass().getSimpleName());
        }
    }
}
