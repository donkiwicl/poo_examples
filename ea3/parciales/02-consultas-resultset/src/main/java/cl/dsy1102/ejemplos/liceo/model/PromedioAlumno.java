package cl.dsy1102.ejemplos.liceo.model;

/**
 * Fila de un reporte: promedio de notas de un alumno.
 *
 * @param promedio null si el alumno todavía no tiene notas
 */
public record PromedioAlumno(String nombre, String curso, Double promedio, int cantidadNotas) {

    public boolean tieneNotas() {
        return promedio != null;
    }

    @Override
    public String toString() {
        String valor = tieneNotas() ? String.format("%.1f", promedio) : "sin notas";
        return String.format("%-4s %-16s %-9s (%d notas)", curso, nombre, valor, cantidadNotas);
    }
}
