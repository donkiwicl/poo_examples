package cl.dsy1102.ejemplos.agenda.dao;

import cl.dsy1102.ejemplos.agenda.model.Contacto;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Guarda los contactos en un archivo JSON con Jackson.
 */
public class JsonContactoDao implements ContactoDao {

    private final Path archivo;

    public JsonContactoDao(Path archivo) {
        this.archivo = archivo;
    }

    /**
     * TODO R3:
     *  - Si el archivo no existe o esta vacio: lista vacia (primera ejecucion).
     *  - Si no: ObjectMapper.readValue(...) con un TypeReference<List<Contacto>>.
     *  - IOException o IllegalArgumentException (dato rechazado por un setter)
     *    -> PersistenciaException con un mensaje comprensible.
     */
    @Override
    public List<Contacto> cargar() throws PersistenciaException {
        return new ArrayList<>();
    }

    /**
     * TODO R4:
     *  - Crea la carpeta del archivo si no existe.
     *  - Escribe con formato legible (pretty print) usando writerFor(TypeReference),
     *    para que cada objeto incluya su "tipo".
     *  - IOException -> PersistenciaException.
     */
    @Override
    public void guardar(List<Contacto> contactos) throws PersistenciaException {
    }
}
