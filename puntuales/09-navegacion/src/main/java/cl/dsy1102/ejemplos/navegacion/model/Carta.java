package cl.dsy1102.ejemplos.navegacion.model;

import java.util.List;

/**
 * Platos del dia. Ya esta resuelto.
 */
public final class Carta {

    private static final List<Plato> PLATOS = List.of(
            new Plato("Cazuela de vacuno", "Con zapallo, choclo, papa y arroz. Caldo de la casa.", 7_500),
            new Plato("Pastel de choclo", "Pino de carne, pollo, huevo y aceituna, gratinado con azúcar.", 8_900),
            new Plato("Porotos granados", "Con mazamorra y albahaca fresca. Ensalada chilena aparte.", 6_900),
            new Plato("Empanada de pino", "Horneada en el día. Pino de carne picada a cuchillo.", 3_200),
            new Plato("Sopaipillas pasadas", "Cuatro unidades en chancaca con cáscara de naranja.", 3_000),
            new Plato("Mote con huesillo", "Vaso de 500 ml bien helado.", 2_500));

    private Carta() {
    }

    public static List<Plato> platos() {
        return PLATOS;
    }
}
