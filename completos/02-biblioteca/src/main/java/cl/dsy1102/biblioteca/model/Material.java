package cl.dsy1102.biblioteca.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.Year;
import java.util.Locale;

/**
 * Material de la coleccion. Contiene lo comun a libros y revistas, la regla
 * general de prestamo y de devolucion; cada subclase define su plazo y su multa.
 *
 * getDisponibles() y getDiasMaximos() son valores CALCULADOS: no se guardan en
 * el JSON (no tienen setter y al leer Jackson no sabria que hacer con ellos).
 */
@JsonIgnoreProperties({"disponibles", "diasMaximos"})
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "tipo")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Libro.class, name = "LIBRO"),
        @JsonSubTypes.Type(value = Revista.class, name = "REVISTA")
})
public abstract class Material {

    public static final int MAXIMO_EJEMPLARES = 20;
    public static final int ANIO_MINIMO = 1450;

    private static final Locale CHILE = Locale.of("es", "CL");

    private String codigo;
    private String titulo;
    private int anio;
    private int ejemplares;

    // Sin setter publico: solo cambia al prestar o devolver. @JsonProperty indica
    // explicitamente a Jackson que lea y escriba este campo privado.
    @JsonProperty("prestados")
    private int prestados;

    /** Para Jackson: crea el objeto vacio y luego usa los setters (que validan). */
    protected Material() {
    }

    protected Material(String codigo, String titulo, int anio, int ejemplares) {
        setCodigo(codigo);
        setTitulo(titulo);
        setAnio(anio);
        setEjemplares(ejemplares);
    }

    /** Dias maximos de prestamo de este tipo de material. */
    public abstract int getDiasMaximos();

    /** Multa en pesos por devolver con atraso. */
    public abstract int calcularMulta(int diasAtraso);

    /** "Libro" o "Revista", para mostrar en la tabla sin preguntar por la clase. */
    public abstract String obtenerTipo();

    /** Copia independiente: permite aplicar un cambio y descartarlo si no se pudo guardar. */
    public abstract Material copiar();

    /**
     * Presta un ejemplar y retorna el mensaje de prestamo autorizado.
     *
     * @throws PrestamoRechazadoException si falta el lector, los dias estan fuera
     *                                    de rango, no quedan ejemplares o el material no se presta
     */
    public final String prestar(String lector, int dias) throws PrestamoRechazadoException {
        if (lector == null || lector.isBlank()) {
            throw new PrestamoRechazadoException("indica el nombre del lector.");
        }
        if (dias < 1 || dias > getDiasMaximos()) {
            throw new PrestamoRechazadoException("este material se presta entre 1 y " + getDiasMaximos() + " días.");
        }
        validarPrestamo();
        if (getDisponibles() == 0) {
            throw new PrestamoRechazadoException("no quedan ejemplares disponibles de \"" + titulo + "\".");
        }
        prestados++;
        return "Préstamo autorizado: \"" + titulo + "\" a " + lector.trim() + " por " + dias + " días"
                + " | Quedan " + getDisponibles() + " de " + ejemplares;
    }

    /**
     * Registra la devolucion de un ejemplar.
     *
     * @return la multa a pagar (0 si se devolvio a tiempo)
     */
    public final int devolver(int diasAtraso) throws PrestamoRechazadoException {
        if (diasAtraso < 0) {
            throw new IllegalArgumentException("Los días de atraso no pueden ser negativos.");
        }
        if (prestados == 0) {
            throw new PrestamoRechazadoException("no hay ejemplares prestados de \"" + titulo + "\".");
        }
        prestados--;
        return calcularMulta(diasAtraso);
    }

    /** Punto de extension: condiciones propias de cada tipo de material. */
    protected void validarPrestamo() throws PrestamoRechazadoException {
    }

    public int getDisponibles() {
        return ejemplares - prestados;
    }

    public String obtenerDetalle() {
        return obtenerTipo() + " " + codigo + "\n"
                + "\"" + titulo + "\" (" + anio + ")\n"
                + "Disponibles: " + getDisponibles() + " de " + ejemplares;
    }

    public static String pesos(int monto) {
        return String.format(CHILE, "$%,d", monto);
    }

    /** Copia los datos comunes a otra instancia (lo usan las subclases en copiar()). */
    protected void copiarEn(Material destino) {
        destino.codigo = codigo;
        destino.titulo = titulo;
        destino.anio = anio;
        destino.ejemplares = ejemplares;
        destino.prestados = prestados;
    }

    public String getCodigo() {
        return codigo;
    }

    /** Formato: tres letras, guion y tres digitos. Ejemplo: LIB-001. */
    public void setCodigo(String codigo) {
        if (codigo == null || !codigo.trim().toUpperCase().matches("[A-Z]{3}-\\d{3}")) {
            throw new IllegalArgumentException("El código debe tener el formato AAA-000 (ejemplo: LIB-001).");
        }
        this.codigo = codigo.trim().toUpperCase();
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("El título es obligatorio.");
        }
        this.titulo = titulo.trim();
    }

    public int getAnio() {
        return anio;
    }

    public void setAnio(int anio) {
        int actual = Year.now().getValue();
        if (anio < ANIO_MINIMO || anio > actual) {
            throw new IllegalArgumentException("El año debe estar entre " + ANIO_MINIMO + " y " + actual + ".");
        }
        this.anio = anio;
    }

    public int getEjemplares() {
        return ejemplares;
    }

    /** No puede quedar por debajo de los ejemplares que estan prestados. */
    public void setEjemplares(int ejemplares) {
        if (ejemplares < 1 || ejemplares > MAXIMO_EJEMPLARES) {
            throw new IllegalArgumentException("Los ejemplares deben estar entre 1 y " + MAXIMO_EJEMPLARES + ".");
        }
        if (ejemplares < prestados) {
            throw new IllegalArgumentException("Hay " + prestados + " ejemplares prestados: no puedes dejar menos.");
        }
        this.ejemplares = ejemplares;
    }

    public int getPrestados() {
        return prestados;
    }
}
