package cl.dsy1102.almacen.dao;

import cl.dsy1102.almacen.model.Producto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Versión EA2: guarda los productos en un archivo JSON. Cada operación lee el
 * archivo completo, lo modifica en memoria y lo vuelve a escribir.
 */
public class JsonProductoDao implements ProductoDao {

    private static final TypeReference<List<Producto>> TIPO_LISTA = new TypeReference<>() {
    };

    private final Path archivo;
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public JsonProductoDao(Path archivo) {
        this.archivo = archivo;
    }

    @Override
    public List<Producto> listarTodos() throws PersistenciaException {
        List<Producto> productos = leer();
        productos.sort(Comparator.comparing(Producto::getCodigo));
        return productos;
    }

    @Override
    public void insertar(Producto producto) throws PersistenciaException {
        List<Producto> productos = leer();
        validarCodigoUnico(productos, producto);
        int maximo = productos.stream().mapToInt(Producto::getId).max().orElse(0);
        producto.setId(maximo + 1);
        productos.add(producto);
        escribir(productos);
    }

    @Override
    public boolean actualizar(Producto producto) throws PersistenciaException {
        List<Producto> productos = leer();
        for (int i = 0; i < productos.size(); i++) {
            if (productos.get(i).getId() == producto.getId()) {
                validarCodigoUnico(productos, producto);
                productos.set(i, producto);
                escribir(productos);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean eliminar(int id) throws PersistenciaException {
        List<Producto> productos = leer();
        boolean eliminado = productos.removeIf(p -> p.getId() == id);
        if (eliminado) {
            escribir(productos);
        }
        return eliminado;
    }

    private void validarCodigoUnico(List<Producto> productos, Producto producto) throws PersistenciaException {
        for (Producto otro : productos) {
            if (otro.getId() != producto.getId() && otro.getCodigo().equals(producto.getCodigo())) {
                throw new PersistenciaException("Ya existe un producto con el código " + producto.getCodigo() + ".");
            }
        }
    }

    private List<Producto> leer() throws PersistenciaException {
        try {
            if (!Files.exists(archivo) || Files.size(archivo) == 0) {
                return new ArrayList<>();
            }
            return new ArrayList<>(mapper.readValue(archivo.toFile(), TIPO_LISTA));
        } catch (IOException | IllegalArgumentException e) {
            throw new PersistenciaException("No se pudieron leer los productos desde " + archivo.getFileName()
                    + ". El archivo está dañado o tiene datos inválidos.", e);
        }
    }

    private void escribir(List<Producto> productos) throws PersistenciaException {
        try {
            Path carpeta = archivo.toAbsolutePath().getParent();
            if (carpeta != null) {
                Files.createDirectories(carpeta);
            }
            mapper.writerFor(TIPO_LISTA).withDefaultPrettyPrinter().writeValue(archivo.toFile(), productos);
        } catch (IOException e) {
            throw new PersistenciaException("No se pudieron guardar los productos en " + archivo.getFileName() + ".", e);
        }
    }
}
