package co.bamer.inventario.vista;

import static org.junit.jupiter.api.Assertions.assertTrue;

import co.bamer.inventario.BaseDatosPrueba;
import co.bamer.inventario.repositorio.ProductoJdbcRepository;
import co.bamer.inventario.servicio.ProductoService;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.junit.jupiter.api.Test;

class MenuInventarioTest {

    private String ejecutar(String entradaSimulada) throws Exception {
        ProductoService servicio = new ProductoService(
                new ProductoJdbcRepository(BaseDatosPrueba.crear("menu")));
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream salida = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        new MenuInventario(servicio, new Scanner(entradaSimulada), salida).iniciar();
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void sesionCompletaInsertarConsultarActualizarEliminar() throws Exception {
        String entrada = String.join("\n",
                "1", "7709991111", "Pan tajado", "5", "Pan blanco", "3000", "3800", "12", "4",
                "3", "7709991111",
                "5", "7", "Pan tajado grande", "4200", "10",
                "6", "7", "s",
                "2",
                "0") + "\n";
        String salida = ejecutar(entrada);
        System.out.println(salida);
        assertTrue(salida.contains("Producto guardado con id 7"));
        assertTrue(salida.contains("Pan tajado"));
        assertTrue(salida.contains("Producto actualizado."));
        assertTrue(salida.contains("Producto eliminado."));
        assertTrue(salida.contains("Hasta luego."));
    }

    @Test
    void mensajesDeErrorAmigables() throws Exception {
        String salida = ejecutar("1\n12\nXX\n1\nd\n1\n2\n1\n1\n6\n9999\n0\n");
        assertTrue(salida.contains("Aviso:"));
    }
}
