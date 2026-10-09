package cl.dsy1102.biblioteca;

import cl.dsy1102.biblioteca.model.Libro;
import cl.dsy1102.biblioteca.model.Material;
import cl.dsy1102.biblioteca.model.PrestamoRechazadoException;
import cl.dsy1102.biblioteca.model.Revista;

import java.util.List;

/**
 * Demostracion del modelo por consola (ya resuelto).
 * Ejecuta con: mvn compile exec:java
 */
public class Main {

    public static void main(String[] args) {
        Libro desolacion = new Libro("LIB-001", "Desolación", 1922, 2, "Gabriela Mistral", 248);
        Libro subterra = new Libro("LIB-002", "Sub terra", 1904, 1, "Baldomero Lillo", 180);
        Revista musica = new Revista("REV-001", "Revista Musical Chilena", 2024, 3, 241);
        Revista anales = new Revista("REV-002", "Anales de la Universidad de Chile", 2023, 1, 23);
        anales.restringirASala();

        prestar(desolacion, "Ana Rojas", 14);
        prestar(desolacion, "Bruno Diaz", 15);
        prestar(subterra, "Carla Soto", 7);
        prestar(subterra, "Diego Pinto", 7);
        prestar(musica, "Elena Mora", 3);
        prestar(anales, "Felipe Vera", 1);
        prestar(musica, " ", 2);

        devolver(subterra, 3);
        devolver(anales, 0);

        System.out.println();
        for (Material material : List.of(desolacion, subterra, musica, anales)) {
            System.out.println(material.getCodigo() + " | Disponibles: " + material.getDisponibles() + " de " + material.getEjemplares());
        }
    }

    private static void prestar(Material material, String lector, int dias) {
        try {
            System.out.println(material.prestar(lector, dias));
        } catch (PrestamoRechazadoException e) {
            System.out.println("Préstamo rechazado: " + e.getMessage());
        }
    }

    private static void devolver(Material material, int diasAtraso) {
        try {
            int multa = material.devolver(diasAtraso);
            System.out.println("Devolución de " + material.getCodigo() + " registrada | Multa: " + Material.pesos(multa));
        } catch (PrestamoRechazadoException e) {
            System.out.println("Devolución rechazada: " + e.getMessage());
        }
    }
}
