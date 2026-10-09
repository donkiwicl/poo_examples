package cl.dsy1102.arriendo.model;

/**
 * Bicicleta. Desde 4 horas se cobra tarifa de jornada con 20 % de descuento.
 */
public class Bicicleta extends Vehiculo {

    public static final int HORAS_JORNADA = 4;
    public static final double DESCUENTO_JORNADA = 0.20;
    public static final String[] CATEGORIAS = {"Urbana", "Montaña", "Paseo"};

    private String categoria;
    private boolean conCanasto;

    public Bicicleta(String codigo, String modelo, int tarifaHora, String categoria, boolean conCanasto) {
        super(codigo, modelo, tarifaHora);
        setCategoria(categoria);
        this.conCanasto = conCanasto;
    }

    @Override
    public int calcularCosto(int horas) {
        int bruto = getTarifaHora() * horas;
        return horas >= HORAS_JORNADA ? (int) Math.round(bruto * (1 - DESCUENTO_JORNADA)) : bruto;
    }

    @Override
    public Vehiculo copiar() {
        Bicicleta copia = new Bicicleta(getCodigo(), getModelo(), getTarifaHora(), categoria, conCanasto);
        copiarEn(copia);
        return copia;
    }

    @Override
    public String obtenerDetalle() {
        return super.obtenerDetalle() + "\nCategoría: " + categoria + (conCanasto ? " · con canasto" : "")
                + "\nDesde " + HORAS_JORNADA + " h: 20 % de descuento";
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        for (String valida : CATEGORIAS) {
            if (valida.equals(categoria)) {
                this.categoria = categoria;
                return;
            }
        }
        throw new IllegalArgumentException("Categoría inválida: " + categoria);
    }

    public boolean isConCanasto() {
        return conCanasto;
    }

    public void setConCanasto(boolean conCanasto) {
        this.conCanasto = conCanasto;
    }
}
