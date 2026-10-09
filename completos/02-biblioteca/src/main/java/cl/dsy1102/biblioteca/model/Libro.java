package cl.dsy1102.biblioteca.model;

/**
 * Libro: se presta hasta 14 dias y la multa es de $200 por dia de atraso.
 */
public class Libro extends Material {

    public static final int DIAS_MAXIMOS = 14;
    public static final int MULTA_DIARIA = 200;

    private String autor;
    private int paginas;

    public Libro(String codigo, String titulo, int anio, int ejemplares, String autor, int paginas) {
        super(codigo, titulo, anio, ejemplares);
        setAutor(autor);
        setPaginas(paginas);
    }

    @Override
    public int getDiasMaximos() {
        return DIAS_MAXIMOS;
    }

    @Override
    public int calcularMulta(int diasAtraso) {
        return diasAtraso * MULTA_DIARIA;
    }

    @Override
    public Material copiar() {
        Libro copia = new Libro(getCodigo(), getTitulo(), getAnio(), getEjemplares(), autor, paginas);
        copiarEn(copia);
        return copia;
    }

    @Override
    public String obtenerDetalle() {
        return super.obtenerDetalle() + "\nAutor: " + autor + " · " + paginas + " páginas"
                + "\nPréstamo hasta " + DIAS_MAXIMOS + " días · Multa " + pesos(MULTA_DIARIA) + " por día";
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        if (autor == null || autor.isBlank()) {
            throw new IllegalArgumentException("El autor es obligatorio.");
        }
        this.autor = autor.trim();
    }

    public int getPaginas() {
        return paginas;
    }

    public void setPaginas(int paginas) {
        if (paginas < 1 || paginas > 5000) {
            throw new IllegalArgumentException("Las páginas deben estar entre 1 y 5000.");
        }
        this.paginas = paginas;
    }
}
