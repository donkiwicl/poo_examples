package cl.dsy1102.veterinaria.dao;

import cl.dsy1102.veterinaria.model.Paciente;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Guarda y carga los pacientes en un archivo JSON usando Jackson.
 */
public class JsonPacienteDao implements PacienteDao {

    // Con el tipo generico explicito Jackson escribe el atributo "tipo" de cada subclase.
    private static final TypeReference<List<Paciente>> TIPO_LISTA = new TypeReference<>() {
    };

    private final Path archivo;
    // JavaTimeModule: ensena a Jackson a convertir LocalDate.
    // Sin WRITE_DATES_AS_TIMESTAMPS la fecha se escribe "2026-10-08" en vez de [2026,10,8].
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public JsonPacienteDao(Path archivo) {
        this.archivo = archivo;
    }

    @Override
    public List<Paciente> cargar() throws PersistenciaException {
        try {
            if (!Files.exists(archivo) || Files.size(archivo) == 0) {
                return new ArrayList<>();
            }
            return mapper.readValue(archivo.toFile(), TIPO_LISTA);
        } catch (IOException | IllegalArgumentException e) {
            throw new PersistenciaException("No se pudieron leer los pacientes desde " + archivo.getFileName()
                    + ". El archivo está dañado o tiene datos inválidos.", e);
        }
    }

    @Override
    public void guardar(List<Paciente> pacientes) throws PersistenciaException {
        try {
            Path carpeta = archivo.toAbsolutePath().getParent();
            if (carpeta != null) {
                Files.createDirectories(carpeta);
            }
            mapper.writerFor(TIPO_LISTA).withDefaultPrettyPrinter().writeValue(archivo.toFile(), pacientes);
        } catch (IOException e) {
            throw new PersistenciaException("No se pudieron guardar los pacientes en " + archivo.getFileName()
                    + ". Revisa que el disco tenga espacio y permisos de escritura.", e);
        }
    }
}
