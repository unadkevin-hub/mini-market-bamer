package co.bamer.inventario;

import co.bamer.comun.conexion.ConexionBD;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utilidad de pruebas: crea una base H2 en memoria (modo MySQL) con el mismo script del proyecto.
 */
public final class BaseDatosPrueba {

    private static final Path SCRIPT = Path.of("..", "basedatos", "script_bamer.sql");

    private BaseDatosPrueba() {
    }

    public static ConexionBD crear(String nombre) throws IOException, SQLException {
        ConexionBD conexionBD = new ConexionBD(
                "jdbc:h2:mem:" + nombre + ";MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        StringBuilder sql = new StringBuilder();
        for (String linea : Files.readAllLines(SCRIPT)) {
            String limpia = linea.strip();
            boolean ignorar = limpia.startsWith("--") || limpia.startsWith("CREATE DATABASE")
                    || limpia.startsWith("USE ") || limpia.startsWith("CHARACTER SET")
                    || limpia.startsWith("COLLATE");
            if (!ignorar) {
                sql.append(linea).append('\n');
            }
        }
        try (Connection conexion = conexionBD.obtenerConexion(); Statement sentencia = conexion.createStatement()) {
            for (String instruccion : sql.toString().split(";")) {
                if (!instruccion.isBlank()) {
                    sentencia.execute(instruccion);
                }
            }
        }
        return conexionBD;
    }
}
