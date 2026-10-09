package cl.dsy1102.veterinaria.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Paciente de la clinica con su historial de atenciones (composicion: las
 * atenciones pertenecen al paciente y se guardan dentro de el en el JSON).
 *
 * Los getters calculados (edad, ultima atencion, peso actual, total y los dias
 * entre vacunas de Vacunable) no se guardan: no tienen setter y romperian la lectura.
 */
@JsonIgnoreProperties({"edadAnios", "ultimaAtencion", "pesoActual", "totalAtenciones", "diasEntreVacunas"})
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "tipo")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Perro.class, name = "PERRO"),
        @JsonSubTypes.Type(value = Gato.class, name = "GATO"),
        @JsonSubTypes.Type(value = Exotico.class, name = "EXOTICO")
})
public abstract class Paciente {

    private static final Locale CHILE = Locale.of("es", "CL");

    private String nombre;
    private String tutor;
    private String telefonoTutor;
    private LocalDate fechaNacimiento;

    // Sin setter: solo crece con registrarAtencion(), que aplica las reglas.
    @JsonProperty("atenciones")
    private List<Atencion> atenciones = new ArrayList<>();

    /** Para Jackson: crea el objeto vacio y luego usa los setters (que validan). */
    protected Paciente() {
    }

    protected Paciente(String nombre, String tutor, String telefonoTutor, LocalDate fechaNacimiento) {
        setNombre(nombre);
        setTutor(tutor);
        setTelefonoTutor(telefonoTutor);
        setFechaNacimiento(fechaNacimiento);
    }

    /** "Perro", "Gato" o la especie del exotico, para la tabla. */
    public abstract String obtenerEspecie();

    /** Factor por el que se multiplica el precio base de cada servicio. */
    protected abstract double factorCosto();

    /** Copia independiente (incluido el historial): permite descartar un cambio no guardado. */
    public abstract Paciente copiar();

    public int calcularCosto(TipoAtencion tipo) {
        return (int) Math.round(tipo.getPrecioBase() * factorCosto());
    }

    /**
     * Registra una atencion en el historial y retorna el mensaje de confirmacion.
     *
     * @throws AtencionRechazadaException si la fecha no es valida o una regla clinica lo impide
     */
    public final String registrarAtencion(LocalDate fecha, TipoAtencion tipo, double pesoKg, String observacion)
            throws AtencionRechazadaException {
        if (fecha == null || tipo == null) {
            throw new AtencionRechazadaException("indica la fecha y el tipo de atención.");
        }
        if (fecha.isAfter(LocalDate.now())) {
            throw new AtencionRechazadaException("la fecha no puede ser futura.");
        }
        if (fecha.isBefore(fechaNacimiento)) {
            throw new AtencionRechazadaException("la fecha es anterior al nacimiento de " + nombre + ".");
        }
        if (tipo == TipoAtencion.VACUNACION) {
            validarVacuna(fecha);
        }
        Atencion atencion = new Atencion(fecha, tipo, pesoKg, calcularCosto(tipo), observacion);
        atenciones.add(atencion);
        // El historial se mantiene ordenado por fecha aunque se registre una atencion atrasada.
        atenciones.sort(Comparator.comparing(Atencion::getFecha));
        return "Atención registrada: " + tipo + " para " + nombre + " | Costo: " + pesos(atencion.getCosto());
    }

    private void validarVacuna(LocalDate fecha) throws AtencionRechazadaException {
        // La regla depende de una CAPACIDAD (interfaz), no de la clase concreta.
        if (!(this instanceof Vacunable vacunable)) {
            throw new AtencionRechazadaException(nombre + " (" + obtenerEspecie() + ") no se vacuna en esta clínica.");
        }
        for (Atencion previa : atenciones) {
            if (previa.getTipo() == TipoAtencion.VACUNACION) {
                long dias = Math.abs(java.time.temporal.ChronoUnit.DAYS.between(previa.getFecha(), fecha));
                if (dias < vacunable.getDiasEntreVacunas()) {
                    throw new AtencionRechazadaException("ya tiene una vacuna el " + previa.getFecha().format(Atencion.FORMATO_FECHA)
                            + "; deben pasar " + vacunable.getDiasEntreVacunas() + " días entre vacunas.");
                }
            }
        }
    }

    public String obtenerDetalle() {
        int edad = getEdadAnios();
        return obtenerEspecie() + " · " + nombre + " (" + (edad == 1 ? "1 año" : edad + " años") + ")\n"
                + "Tutor: " + tutor + " · " + telefonoTutor + "\n"
                + "Nacimiento: " + fechaNacimiento.format(Atencion.FORMATO_FECHA)
                + (getPesoActual() > 0 ? " · Peso actual: " + getPesoActual() + " kg" : "");
    }

    public static String pesos(int monto) {
        return String.format(CHILE, "$%,d", monto);
    }

    /** Copia los datos comunes, con un historial NUEVO (las atenciones son inmutables). */
    protected void copiarEn(Paciente destino) {
        destino.nombre = nombre;
        destino.tutor = tutor;
        destino.telefonoTutor = telefonoTutor;
        destino.fechaNacimiento = fechaNacimiento;
        destino.atenciones = new ArrayList<>(atenciones);
    }

    // ---- valores calculados ----

    public int getEdadAnios() {
        return Period.between(fechaNacimiento, LocalDate.now()).getYears();
    }

    /** Ultima atencion, o null si no tiene. */
    public Atencion getUltimaAtencion() {
        return atenciones.isEmpty() ? null : atenciones.get(atenciones.size() - 1);
    }

    /** Peso de la ultima atencion, o 0 si no tiene. */
    public double getPesoActual() {
        Atencion ultima = getUltimaAtencion();
        return ultima == null ? 0 : ultima.getPesoKg();
    }

    public int getTotalAtenciones() {
        return atenciones.stream().mapToInt(Atencion::getCosto).sum();
    }

    // ---- getters y setters ----

    /** Vista de solo lectura: el historial solo crece con registrarAtencion(). */
    public List<Atencion> getAtenciones() {
        return Collections.unmodifiableList(atenciones);
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del paciente es obligatorio.");
        }
        this.nombre = nombre.trim();
    }

    public String getTutor() {
        return tutor;
    }

    public void setTutor(String tutor) {
        if (tutor == null || tutor.isBlank()) {
            throw new IllegalArgumentException("El nombre del tutor es obligatorio.");
        }
        this.tutor = tutor.trim();
    }

    public String getTelefonoTutor() {
        return telefonoTutor;
    }

    public void setTelefonoTutor(String telefonoTutor) {
        if (telefonoTutor == null || !telefonoTutor.trim().matches("\\+?\\d{8,12}")) {
            throw new IllegalArgumentException("El teléfono debe tener entre 8 y 12 dígitos (ej: +56912345678).");
        }
        this.telefonoTutor = telefonoTutor.trim();
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        if (fechaNacimiento == null || fechaNacimiento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento es obligatoria y no puede ser futura.");
        }
        if (fechaNacimiento.isBefore(LocalDate.now().minusYears(40))) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser de hace más de 40 años.");
        }
        this.fechaNacimiento = fechaNacimiento;
    }
}
