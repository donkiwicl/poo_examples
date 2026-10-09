package cl.dsy1102.veterinaria.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Una atencion del historial clinico. Es inmutable: una vez registrada no cambia.
 * El costo se calcula al registrarla y se guarda, porque los precios pueden
 * cambiar despues.
 */
public class Atencion {

    public static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    public static final double PESO_MINIMO = 0.05;
    public static final double PESO_MAXIMO = 120;
    public static final int LARGO_MAXIMO_OBSERVACION = 200;

    private LocalDate fecha;
    private TipoAtencion tipo;
    private double pesoKg;
    private int costo;
    private String observacion;

    /** Para Jackson: los campos privados se asignan directamente. */
    protected Atencion() {
    }

    Atencion(LocalDate fecha, TipoAtencion tipo, double pesoKg, int costo, String observacion) {
        if (pesoKg < PESO_MINIMO || pesoKg > PESO_MAXIMO) {
            throw new IllegalArgumentException("El peso debe estar entre 0,05 y 120 kg.");
        }
        if (observacion != null && observacion.length() > LARGO_MAXIMO_OBSERVACION) {
            throw new IllegalArgumentException("La observación admite hasta " + LARGO_MAXIMO_OBSERVACION + " caracteres.");
        }
        this.fecha = fecha;
        this.tipo = tipo;
        this.pesoKg = pesoKg;
        this.costo = costo;
        this.observacion = observacion == null || observacion.isBlank() ? null : observacion.trim();
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public TipoAtencion getTipo() {
        return tipo;
    }

    public double getPesoKg() {
        return pesoKg;
    }

    public int getCosto() {
        return costo;
    }

    public String getObservacion() {
        return observacion;
    }

    @Override
    public String toString() {
        return fecha.format(FORMATO_FECHA) + " · " + tipo + " · " + pesoKg + " kg · " + Paciente.pesos(costo);
    }
}
