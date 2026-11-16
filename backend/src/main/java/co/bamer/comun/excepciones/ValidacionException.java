package co.bamer.comun.excepciones;

/**
 * Se lanza cuando los datos de un producto no cumplen las reglas de negocio.
 */
public class ValidacionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ValidacionException(String mensaje) {
        super(mensaje);
    }
}
