package cl.dsy1102.veterinaria;

import cl.dsy1102.veterinaria.model.Atencion;
import cl.dsy1102.veterinaria.model.AtencionRechazadaException;
import cl.dsy1102.veterinaria.model.Exotico;
import cl.dsy1102.veterinaria.model.Gato;
import cl.dsy1102.veterinaria.model.Paciente;
import cl.dsy1102.veterinaria.model.Perro;
import cl.dsy1102.veterinaria.model.Tamano;
import cl.dsy1102.veterinaria.model.TipoAtencion;

import java.time.LocalDate;
import java.util.List;

/**
 * Demostracion del modelo por consola (ya resuelto).
 * Ejecuta con: mvn compile exec:java
 *
 * Las fechas son relativas a hoy para que el resultado no dependa del dia en que se ejecuta.
 */
public class Main {

    public static void main(String[] args) {
        LocalDate hoy = LocalDate.now();
        Perro rocky = new Perro("Rocky", "Ana Rojas", "+56911112222", hoy.minusYears(4), "Labrador", Tamano.GRANDE);
        Gato misha = new Gato("Misha", "Bruno Diaz", "+56933334444", hoy.minusYears(2), true);
        Exotico copito = new Exotico("Copito", "Carla Soto", "225551234", hoy.minusYears(1), "Conejo");

        registrar(rocky, hoy.minusDays(10), TipoAtencion.VACUNACION, 31.5, "Antirrábica");
        registrar(rocky, hoy, TipoAtencion.VACUNACION, 31.8, null);
        registrar(rocky, hoy, TipoAtencion.CONSULTA, 31.8, "Control de rutina");
        registrar(misha, hoy, TipoAtencion.VACUNACION, 4.2, "Triple felina");
        registrar(copito, hoy, TipoAtencion.VACUNACION, 1.9, null);
        registrar(copito, hoy, TipoAtencion.DESPARASITACION, 1.9, null);
        registrar(misha, hoy.plusDays(1), TipoAtencion.CONSULTA, 4.2, null);
        registrar(copito, hoy, TipoAtencion.CONTROL_PESO, 300, null);

        System.out.println();
        for (Paciente paciente : List.of(rocky, misha, copito)) {
            System.out.println(paciente.obtenerEspecie() + " " + paciente.getNombre() + " | Atenciones: "
                    + paciente.getAtenciones().size() + " | Total: " + Paciente.pesos(paciente.getTotalAtenciones()));
            for (Atencion atencion : paciente.getAtenciones()) {
                System.out.println("   " + atencion);
            }
        }
    }

    private static void registrar(Paciente paciente, LocalDate fecha, TipoAtencion tipo, double peso, String observacion) {
        try {
            System.out.println(paciente.registrarAtencion(fecha, tipo, peso, observacion));
        } catch (AtencionRechazadaException e) {
            System.out.println("Atención rechazada: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Dato inválido: " + e.getMessage());
        }
    }
}
