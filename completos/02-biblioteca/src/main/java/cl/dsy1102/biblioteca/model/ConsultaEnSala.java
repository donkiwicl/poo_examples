package cl.dsy1102.biblioteca.model;

/**
 * Material que puede quedar restringido a consulta en sala (no se presta a domicilio).
 */
public interface ConsultaEnSala {

    boolean isSoloSala();

    /** Restringe el material a consulta en sala. */
    void restringirASala();
}
