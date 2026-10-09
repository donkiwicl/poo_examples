package cl.dsy1102.arriendo.model;

import java.util.Locale;

/**
 * Vehiculo de la flota. Contiene lo comun a bicicletas y scooters y la regla
 * general de arriendo; cada subclase define su costo y puede agregar condiciones.
 */
public abstract class Vehiculo {

    public static final int MAXIMO_HORAS = 8;
    public static final int TARIFA_MINIMA = 500;
    public static final int TARIFA_MAXIMA = 20_000;

    private static final Locale CHILE = Locale.of("es", "CL");

    private String codigo;
    private String modelo;
    private int tarifaHora;

    // Sin setter publico: solo cambia al arrendar o devolver.
    private boolean disponible = true;

    protected Vehiculo(String codigo, String modelo, int tarifaHora) {
        setCodigo(codigo);
        setModelo(modelo);
        setTarifaHora(tarifaHora);
    }

    /** Costo de arrendar este vehiculo por la cantidad de horas indicada. */
    public abstract int calcularCosto(int horas);

    /** Copia independiente: permite aplicar un cambio y descartarlo si no se pudo guardar. */
    public abstract Vehiculo copiar();

    /**
     * Arrienda el vehiculo y retorna el mensaje de arriendo autorizado.
     *
     * @throws ArriendoRechazadoException si no esta disponible, las horas estan
     *                                    fuera de rango o una condicion propia del vehiculo lo impide
     */
    public final String arrendar(int horas) throws ArriendoRechazadoException {
        if (!disponible) {
            throw new ArriendoRechazadoException(codigo + " ya está arrendado.");
        }
        if (horas < 1 || horas > MAXIMO_HORAS) {
            throw new ArriendoRechazadoException("las horas deben estar entre 1 y " + MAXIMO_HORAS + ".");
        }
        validarCondiciones(horas);
        int total = calcularCosto(horas);
        disponible = false;
        registrarUso(horas);
        return "Arriendo autorizado: " + codigo + " por " + horas + " h | Total: " + pesos(total);
    }

    /** Marca el vehiculo como disponible. */
    public void devolver() throws ArriendoRechazadoException {
        if (disponible) {
            throw new ArriendoRechazadoException(codigo + " no está arrendado.");
        }
        disponible = true;
    }

    /** Punto de extension: condiciones adicionales de cada tipo de vehiculo. */
    protected void validarCondiciones(int horas) throws ArriendoRechazadoException {
    }

    /** Punto de extension: efectos del uso (por ejemplo, gasto de bateria). */
    protected void registrarUso(int horas) {
    }

    public String obtenerDetalle() {
        return codigo + " - " + modelo + "\n"
                + "Tarifa: " + pesos(tarifaHora) + " por hora\n"
                + "Estado: " + (disponible ? "Disponible" : "Arrendado");
    }

    public static String pesos(int monto) {
        return String.format(CHILE, "$%,d", monto);
    }

    /** Copia los datos comunes a otra instancia (lo usan las subclases en copiar()). */
    protected void copiarEn(Vehiculo destino) {
        destino.codigo = codigo;
        destino.modelo = modelo;
        destino.tarifaHora = tarifaHora;
        destino.disponible = disponible;
    }

    public String getCodigo() {
        return codigo;
    }

    /** Formato: dos letras, guion y dos digitos. Ejemplo: BK-01. */
    public void setCodigo(String codigo) {
        if (codigo == null || !codigo.trim().toUpperCase().matches("[A-Z]{2}-\\d{2}")) {
            throw new IllegalArgumentException("El código debe tener el formato AA-00 (ejemplo: BK-01).");
        }
        this.codigo = codigo.trim().toUpperCase();
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        if (modelo == null || modelo.isBlank()) {
            throw new IllegalArgumentException("El modelo es obligatorio.");
        }
        this.modelo = modelo.trim();
    }

    public int getTarifaHora() {
        return tarifaHora;
    }

    public void setTarifaHora(int tarifaHora) {
        if (tarifaHora < TARIFA_MINIMA || tarifaHora > TARIFA_MAXIMA) {
            throw new IllegalArgumentException("La tarifa por hora debe estar entre "
                    + pesos(TARIFA_MINIMA) + " y " + pesos(TARIFA_MAXIMA) + ".");
        }
        this.tarifaHora = tarifaHora;
    }

    public boolean isDisponible() {
        return disponible;
    }
}
