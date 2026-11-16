package co.bamer.comun.conexion;

import co.bamer.comun.excepciones.AccesoDatosException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Fabrica de conexiones JDBC. Las credenciales se leen de variables de entorno
 * para no dejar claves escritas en el codigo fuente.
 *
 * <ul>
 *   <li>BAMER_DB_URL (por defecto jdbc:mysql://localhost:3306/bamer)</li>
 *   <li>BAMER_DB_USUARIO (por defecto root)</li>
 *   <li>BAMER_DB_CLAVE (por defecto vacia)</li>
 * </ul>
 */
public class ConexionBD {

    private static final String URL_POR_DEFECTO =
            "jdbc:mysql://localhost:3306/bamer?useSSL=false&allowPublicKeyRetrieval=true"
                    + "&serverTimezone=America/Bogota";
    private static final String USUARIO_POR_DEFECTO = "root";

    private final String url;
    private final String usuario;
    private final String clave;

    /** Crea la fabrica leyendo la configuracion desde variables de entorno. */
    public ConexionBD() {
        this(leerVariable("BAMER_DB_URL", URL_POR_DEFECTO),
                leerVariable("BAMER_DB_USUARIO", USUARIO_POR_DEFECTO),
                leerVariable("BAMER_DB_CLAVE", ""));
    }

    /** Crea la fabrica con valores explicitos (util para pruebas). */
    public ConexionBD(String url, String usuario, String clave) {
        this.url = url;
        this.usuario = usuario;
        this.clave = clave;
    }

    /**
     * Abre una nueva conexion. Quien la solicita debe cerrarla (try-with-resources).
     *
     * @return conexion abierta con la base de datos
     * @throws AccesoDatosException si no se puede conectar
     */
    public Connection obtenerConexion() {
        try {
            return DriverManager.getConnection(url, usuario, clave);
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo conectar a la base de datos: " + e.getMessage(), e);
        }
    }

    private static String leerVariable(String nombre, String valorPorDefecto) {
        String valor = System.getenv(nombre);
        return (valor == null || valor.isBlank()) ? valorPorDefecto : valor;
    }
}
