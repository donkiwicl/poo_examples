package cl.dsy1102.ejemplos.liga;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Equipo de la liga con propiedades JavaFX: cuando cambia un valor, las
 * celdas de la tabla que lo muestran se actualizan solas.
 *
 * Ya esta resuelto.
 */
public class Equipo {

    private final StringProperty nombre = new SimpleStringProperty();
    private final StringProperty comuna = new SimpleStringProperty();
    private final IntegerProperty ganados = new SimpleIntegerProperty();
    private final IntegerProperty empatados = new SimpleIntegerProperty();
    private final IntegerProperty perdidos = new SimpleIntegerProperty();
    private final IntegerProperty golesFavor = new SimpleIntegerProperty();
    private final IntegerProperty golesContra = new SimpleIntegerProperty();

    // Valores calculados: se recalculan solos cuando cambian sus dependencias.
    private final IntegerBinding jugados = Bindings.createIntegerBinding(
            () -> ganados.get() + empatados.get() + perdidos.get(), ganados, empatados, perdidos);
    private final IntegerBinding puntos = Bindings.createIntegerBinding(
            () -> ganados.get() * 3 + empatados.get(), ganados, empatados);
    private final IntegerBinding diferencia = Bindings.createIntegerBinding(
            () -> golesFavor.get() - golesContra.get(), golesFavor, golesContra);

    public Equipo(String nombre, String comuna) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del equipo es obligatorio.");
        }
        if (comuna == null || comuna.isBlank()) {
            throw new IllegalArgumentException("La comuna es obligatoria.");
        }
        this.nombre.set(nombre.trim());
        this.comuna.set(comuna.trim());
    }

    /** Suma un partido jugado con los goles a favor y en contra. */
    public void registrarPartido(int favor, int contra) {
        if (favor < 0 || contra < 0) {
            throw new IllegalArgumentException("Los goles no pueden ser negativos.");
        }
        golesFavor.set(golesFavor.get() + favor);
        golesContra.set(golesContra.get() + contra);
        if (favor > contra) {
            ganados.set(ganados.get() + 1);
        } else if (favor == contra) {
            empatados.set(empatados.get() + 1);
        } else {
            perdidos.set(perdidos.get() + 1);
        }
    }

    // Convencion JavaFX: getX() para el valor y xProperty() para la propiedad observable.

    public String getNombre() { return nombre.get(); }
    public StringProperty nombreProperty() { return nombre; }

    public String getComuna() { return comuna.get(); }
    public StringProperty comunaProperty() { return comuna; }

    public int getGanados() { return ganados.get(); }
    public ReadOnlyIntegerProperty ganadosProperty() { return ganados; }

    public int getEmpatados() { return empatados.get(); }
    public ReadOnlyIntegerProperty empatadosProperty() { return empatados; }

    public int getPerdidos() { return perdidos.get(); }
    public ReadOnlyIntegerProperty perdidosProperty() { return perdidos; }

    public int getGolesFavor() { return golesFavor.get(); }
    public ReadOnlyIntegerProperty golesFavorProperty() { return golesFavor; }

    public int getGolesContra() { return golesContra.get(); }
    public ReadOnlyIntegerProperty golesContraProperty() { return golesContra; }

    public int getJugados() { return jugados.get(); }
    public IntegerBinding jugadosBinding() { return jugados; }

    public int getPuntos() { return puntos.get(); }
    public IntegerBinding puntosBinding() { return puntos; }

    public int getDiferencia() { return diferencia.get(); }
    public IntegerBinding diferenciaBinding() { return diferencia; }

    @Override
    public String toString() {
        return getNombre();
    }
}
