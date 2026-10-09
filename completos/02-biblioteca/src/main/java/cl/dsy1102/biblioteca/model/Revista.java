package cl.dsy1102.biblioteca.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Revista: se presta hasta 3 dias, la multa es de $100 por dia de atraso y
 * puede estar restringida a consulta en sala.
 */
public class Revista extends Material implements ConsultaEnSala {

    public static final int DIAS_MAXIMOS = 3;
    public static final int MULTA_DIARIA = 100;

    private int numero;

    // Sin setter: solo se restringe con restringirASala(). Jackson lo lee y escribe por el campo.
    @JsonProperty("soloSala")
    private boolean soloSala;

    /** Para Jackson. */
    protected Revista() {
    }

    public Revista(String codigo, String titulo, int anio, int ejemplares, int numero) {
        super(codigo, titulo, anio, ejemplares);
        setNumero(numero);
    }

    @Override
    public int getDiasMaximos() {
        return DIAS_MAXIMOS;
    }

    @Override
    public int calcularMulta(int diasAtraso) {
        return diasAtraso * MULTA_DIARIA;
    }

    @Override
    public String obtenerTipo() {
        return "Revista";
    }

    @Override
    public Material copiar() {
        Revista copia = new Revista();
        copiarEn(copia);
        copia.numero = numero;
        copia.soloSala = soloSala;
        return copia;
    }

    @Override
    protected void validarPrestamo() throws PrestamoRechazadoException {
        if (soloSala) {
            throw new PrestamoRechazadoException("\"" + getTitulo() + "\" es de consulta en sala.");
        }
    }

    @Override
    public String obtenerDetalle() {
        return super.obtenerDetalle() + "\nNúmero " + numero
                + (soloSala ? "\nSolo consulta en sala" : "\nPréstamo hasta " + DIAS_MAXIMOS + " días · Multa "
                + pesos(MULTA_DIARIA) + " por día");
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        if (numero < 1 || numero > 999) {
            throw new IllegalArgumentException("El número debe estar entre 1 y 999.");
        }
        this.numero = numero;
    }

    @Override
    public boolean isSoloSala() {
        return soloSala;
    }

    /** Una vez restringida no se libera: la interfaz solo permite restringir. */
    @Override
    public void restringirASala() {
        this.soloSala = true;
    }
}
