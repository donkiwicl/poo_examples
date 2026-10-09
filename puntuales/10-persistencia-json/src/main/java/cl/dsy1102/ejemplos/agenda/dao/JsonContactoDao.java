package cl.dsy1102.ejemplos.agenda.dao;

import cl.dsy1102.ejemplos.agenda.model.Contacto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Guarda los contactos en un archivo JSON con Jackson.
 */
public class JsonContactoDao implements ContactoDao {

    // List<Contacto> pierde el tipo generico al compilar (type erasure): TypeReference lo conserva.
    private static final TypeReference<List<Contacto>> TIPO_LISTA = new TypeReference<>() {
    };

    private final Path archivo;
    private final ObjectMapper mapper = new ObjectMapper();

    public JsonContactoDao(Path archivo) {
        this.archivo = archivo;
    }

    @Override
    public List<Contacto> cargar() throws PersistenciaException {
        try {
            if (!Files.exists(archivo) || Files.size(archivo) == 0) {
                return new ArrayList<>();
            }
            return mapper.readValue(archivo.toFile(), TIPO_LISTA);
        } catch (IOException | IllegalArgumentException e) {
            // IllegalArgumentException: un setter del modelo rechazo un valor del archivo.
            throw new PersistenciaException("No se pudieron leer los contactos de " + archivo.getFileName()
                    + ": el archivo esta danado o tiene datos invalidos.", e);
        }
    }

    @Override
    public void guardar(List<Contacto> contactos) throws PersistenciaException {
        try {
            Path carpeta = archivo.toAbsolutePath().getParent();
            if (carpeta != null) {
                Files.createDirectories(carpeta);   // no falla si ya existe
            }
            // writerFor: sin el, Jackson veria una List "cruda" y omitiria el atributo "tipo".
            mapper.writerFor(TIPO_LISTA).withDefaultPrettyPrinter().writeValue(archivo.toFile(), contactos);
        } catch (IOException e) {
            throw new PersistenciaException("No se pudieron guardar los contactos en " + archivo.getFileName()
                    + ". Revisa permisos y espacio en disco.", e);
        }
    }
}
