package cl.dsy1102.arriendo.dao;

import cl.dsy1102.arriendo.model.Vehiculo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Guarda y carga la flota en un archivo JSON usando Jackson.
 */
public class JsonVehiculoDao implements VehiculoDao {

    // Con el tipo generico explicito Jackson escribe el atributo "tipo" de cada subclase.
    private static final TypeReference<List<Vehiculo>> TIPO_LISTA = new TypeReference<>() {
    };

    private final Path archivo;
    private final ObjectMapper mapper = new ObjectMapper();

    public JsonVehiculoDao(Path archivo) {
        this.archivo = archivo;
    }

    @Override
    public List<Vehiculo> cargar() throws PersistenciaException {
        try {
            if (!Files.exists(archivo) || Files.size(archivo) == 0) {
                return new ArrayList<>();
            }
            return mapper.readValue(archivo.toFile(), TIPO_LISTA);
        } catch (IOException | IllegalArgumentException e) {
            throw new PersistenciaException("No se pudo leer la flota desde " + archivo.getFileName()
                    + ". El archivo está dañado o tiene datos inválidos.", e);
        }
    }

    @Override
    public void guardar(List<Vehiculo> vehiculos) throws PersistenciaException {
        try {
            Path carpeta = archivo.toAbsolutePath().getParent();
            if (carpeta != null) {
                Files.createDirectories(carpeta);
            }
            mapper.writerFor(TIPO_LISTA).withDefaultPrettyPrinter().writeValue(archivo.toFile(), vehiculos);
        } catch (IOException e) {
            throw new PersistenciaException("No se pudo guardar la flota en " + archivo.getFileName()
                    + ". Revisa que el disco tenga espacio y permisos de escritura.", e);
        }
    }
}
