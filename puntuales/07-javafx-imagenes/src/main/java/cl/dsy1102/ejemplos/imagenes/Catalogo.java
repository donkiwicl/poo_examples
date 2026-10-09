package cl.dsy1102.ejemplos.imagenes;

import java.util.List;

/**
 * Datos del catalogo. Ya esta resuelto.
 *
 * Pan de Azucar apunta a una imagen que NO existe en los recursos, a proposito:
 * la aplicacion debe mostrar la imagen de reemplazo en vez de fallar.
 */
public final class Catalogo {

    private Catalogo() {
    }

    public static List<Parque> parques() {
        return List.of(
                new Parque("Torres del Paine", "Magallanes",
                        "Macizo de granito con lagos de color turquesa y glaciares.", "torres-del-paine.png"),
                new Parque("Lauca", "Arica y Parinacota",
                        "Altiplano a más de 4.000 metros, con el volcán Parinacota y el lago Chungará.", "lauca.png"),
                new Parque("Conguillío", "La Araucanía",
                        "Bosques de araucarias a los pies del volcán Llaima.", "conguillio.png"),
                new Parque("Rapa Nui", "Valparaíso",
                        "Patrimonio de la Humanidad, hogar de los moai.", "rapa-nui.png"),
                new Parque("Chiloé", "Los Lagos",
                        "Bosque siempreverde frente al Pacífico, cerca de los palafitos de Castro.", "chiloe.png"),
                new Parque("Pan de Azúcar", "Atacama",
                        "Desierto costero con colonias de pingüinos de Humboldt.", "pan-de-azucar.png"));
    }
}
