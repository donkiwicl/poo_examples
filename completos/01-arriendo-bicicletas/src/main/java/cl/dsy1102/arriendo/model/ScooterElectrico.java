package cl.dsy1102.arriendo.model;

/**
 * Scooter electrico: cobra un seguro fijo por arriendo, necesita bateria
 * suficiente y gasta 10 % de bateria por hora de uso.
 */
public class ScooterElectrico extends Vehiculo implements Recargable {

    public static final int SEGURO = 1_000;
    public static final int CONSUMO_POR_HORA = 10;

    private int bateria;
    private int autonomiaKm;

    public ScooterElectrico(String codigo, String modelo, int tarifaHora, int bateria, int autonomiaKm) {
        super(codigo, modelo, tarifaHora);
        setBateria(bateria);
        setAutonomiaKm(autonomiaKm);
    }

    @Override
    public int calcularCosto(int horas) {
        return getTarifaHora() * horas + SEGURO;
    }

    @Override
    public Vehiculo copiar() {
        ScooterElectrico copia = new ScooterElectrico(getCodigo(), getModelo(), getTarifaHora(), bateria, autonomiaKm);
        copiarEn(copia);
        return copia;
    }

    @Override
    protected void validarCondiciones(int horas) throws ArriendoRechazadoException {
        if (!tieneCargaSuficiente()) {
            throw new ArriendoRechazadoException(getCodigo() + " tiene " + bateria
                    + "% de batería (mínimo " + BATERIA_MINIMA + "%).");
        }
    }

    @Override
    protected void registrarUso(int horas) {
        bateria = Math.max(0, bateria - CONSUMO_POR_HORA * horas);
    }

    @Override
    public void recargar() {
        bateria = 100;
    }

    @Override
    public String obtenerDetalle() {
        return super.obtenerDetalle() + "\nBatería: " + bateria + "% · Autonomía: " + autonomiaKm + " km"
                + "\nSeguro por arriendo: " + pesos(SEGURO);
    }

    @Override
    public int getBateria() {
        return bateria;
    }

    public void setBateria(int bateria) {
        if (bateria < 0 || bateria > 100) {
            throw new IllegalArgumentException("La batería debe estar entre 0 y 100.");
        }
        this.bateria = bateria;
    }

    public int getAutonomiaKm() {
        return autonomiaKm;
    }

    public void setAutonomiaKm(int autonomiaKm) {
        if (autonomiaKm < 5 || autonomiaKm > 120) {
            throw new IllegalArgumentException("La autonomía debe estar entre 5 y 120 km.");
        }
        this.autonomiaKm = autonomiaKm;
    }
}
