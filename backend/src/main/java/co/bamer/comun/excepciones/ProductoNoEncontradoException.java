package co.bamer.comun.excepciones;

/**
 * Se lanza cuando un producto buscado no existe en la base de datos.
 */
public class ProductoNoEncontradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ProductoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
