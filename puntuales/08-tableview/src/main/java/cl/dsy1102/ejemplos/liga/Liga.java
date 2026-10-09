package cl.dsy1102.ejemplos.liga;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

/**
 * Reglas de la liga sobre una lista observable de equipos. Ya esta resuelta.
 */
public class Liga {

    private final ObservableList<Equipo> equipos = FXCollections.observableArrayList();

    public ObservableList<Equipo> getEquipos() {
        return equipos;
    }

    public void agregar(Equipo equipo) {
        boolean repetido = equipos.stream().anyMatch(e -> e.getNombre().equalsIgnoreCase(equipo.getNombre()));
        if (repetido) {
            throw new IllegalArgumentException("Ya existe un equipo llamado " + equipo.getNombre() + ".");
        }
        equipos.add(equipo);
    }

    public void eliminar(Equipo equipo) {
        equipos.remove(equipo);
    }

    public void registrarResultado(Equipo local, Equipo visita, int golesLocal, int golesVisita) {
        if (local == null || visita == null) {
            throw new IllegalArgumentException("Selecciona ambos equipos.");
        }
        if (local == visita) {
            throw new IllegalArgumentException("Un equipo no puede jugar contra sí mismo.");
        }
        if (golesLocal < 0 || golesVisita < 0) {
            throw new IllegalArgumentException("Los goles no pueden ser negativos.");
        }
        local.registrarPartido(golesLocal, golesVisita);
        visita.registrarPartido(golesVisita, golesLocal);
    }

    /** Liga con datos de ejemplo para probar la tabla. */
    public static Liga conDatosDeEjemplo() {
        Liga liga = new Liga();
        for (String[] datos : List.of(
                new String[]{"Unión Bellavista", "Puente Alto"},
                new String[]{"Deportivo Nonato", "Pirque"},
                new String[]{"Juventud Las Vizcachas", "Puente Alto"},
                new String[]{"Estrella del Maipo", "San José de Maipo"},
                new String[]{"Club Los Quillayes", "La Florida"},
                new String[]{"Real Cordillera", "Pirque"})) {
            liga.agregar(new Equipo(datos[0], datos[1]));
        }
        ObservableList<Equipo> e = liga.getEquipos();
        liga.registrarResultado(e.get(0), e.get(1), 3, 1);
        liga.registrarResultado(e.get(2), e.get(3), 2, 2);
        liga.registrarResultado(e.get(4), e.get(5), 0, 1);
        liga.registrarResultado(e.get(1), e.get(2), 4, 0);
        liga.registrarResultado(e.get(3), e.get(4), 1, 1);
        liga.registrarResultado(e.get(5), e.get(0), 2, 2);
        return liga;
    }
}
