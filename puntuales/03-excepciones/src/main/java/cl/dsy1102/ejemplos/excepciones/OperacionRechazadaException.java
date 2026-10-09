package cl.dsy1102.ejemplos.excepciones;

/**
 * Una regla del negocio impidio la operacion (no es un error de programacion).
 *
 * Extiende Exception, por lo que es una excepcion COMPROBADA (checked):
 * quien llama a un metodo que la declara con throws esta obligado por el
 * compilador a capturarla o a declararla tambien.
 *
 * Ya esta resuelta. Es la superclase de las excepciones que debes crear.
 */
public class OperacionRechazadaException extends Exception {

    public OperacionRechazadaException(String mensaje) {
        super(mensaje);
    }
}
