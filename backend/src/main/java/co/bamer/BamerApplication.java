package co.bamer;

import co.bamer.comun.conexion.ConexionBD;
import co.bamer.inventario.repositorio.ProductoJdbcRepository;
import co.bamer.inventario.servicio.ProductoService;
import co.bamer.inventario.vista.MenuInventario;

import java.util.Scanner;

/**
 * Punto de entrada del modulo de inventario del Mini Market Bamer.
 */
public final class BamerApplication {

    private BamerApplication() {
    }

    public static void main(String[] args) {
        ConexionBD conexionBD = new ConexionBD();
        ProductoService servicio = new ProductoService(new ProductoJdbcRepository(conexionBD));
        try (Scanner entrada = new Scanner(System.in)) {
            new MenuInventario(servicio, entrada, System.out).iniciar();
        }
    }
}
