package cl.dsy1102.biblioteca.dao;

import cl.dsy1102.biblioteca.model.Material;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Guarda y carga la colección en un archivo JSON usando Jackson.
 */
public class JsonMaterialDao implements MaterialDao {

    // Con el tipo generico explicito Jackson escribe el atributo "tipo" de cada subclase.
    private static final TypeReference<List<Material>> TIPO_LISTA = new TypeReference<>() {
    };

    private final Path archivo;
    private final ObjectMapper mapper = new ObjectMapper();

    public JsonMaterialDao(Path archivo) {
        this.archivo = archivo;
    }

    @Override
    public List<Material> cargar() throws PersistenciaException {
        try {
            if (!Files.exists(archivo) || Files.size(archivo) == 0) {
                return new ArrayList<>();
            }
            return mapper.readValue(archivo.toFile(), TIPO_LISTA);
        } catch (IOException | IllegalArgumentException e) {
            throw new PersistenciaException("No se pudo leer la colección desde " + archivo.getFileName()
                    + ". El archivo está dañado o tiene datos inválidos.", e);
        }
    }

    @Override
    public void guardar(List<Material> materiales) throws PersistenciaException {
        try {
            Path carpeta = archivo.toAbsolutePath().getParent();
            if (carpeta != null) {
                Files.createDirectories(carpeta);
            }
            mapper.writerFor(TIPO_LISTA).withDefaultPrettyPrinter().writeValue(archivo.toFile(), materiales);
        } catch (IOException e) {
            throw new PersistenciaException("No se pudo guardar la colección en " + archivo.getFileName()
                    + ". Revisa que el disco tenga espacio y permisos de escritura.", e);
        }
    }
}
