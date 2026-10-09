package cl.dsy1102.ejemplos.liceo.model;

/**
 * Fila de un reporte: estadísticas de una asignatura.
 */
public record ResumenAsignatura(String asignatura, int cantidadNotas, double promedio, double minima, double maxima) {

    @Override
    public String toString() {
        return String.format("%-11s %d notas · promedio %.1f · mínima %.1f · máxima %.1f",
                asignatura, cantidadNotas, promedio, minima, maxima);
    }
}
