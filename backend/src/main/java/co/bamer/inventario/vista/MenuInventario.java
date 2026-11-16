package co.bamer.inventario.vista;

import co.bamer.comun.excepciones.AccesoDatosException;
import co.bamer.comun.excepciones.ProductoNoEncontradoException;
import co.bamer.comun.excepciones.ValidacionException;
import co.bamer.inventario.modelo.Producto;
import co.bamer.inventario.servicio.ProductoService;

import java.io.PrintStream;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * Menu de consola del modulo de inventario (insertar, consultar, actualizar y eliminar).
 */
public class MenuInventario {

    private static final Locale LOCALE_CO = Locale.forLanguageTag("es-CO");

    private final ProductoService servicio;
    private final Scanner entrada;
    private final PrintStream salida;

    public MenuInventario(ProductoService servicio, Scanner entrada, PrintStream salida) {
        this.servicio = servicio;
        this.entrada = entrada;
        this.salida = salida;
    }

    /** Ejecuta el ciclo del menu hasta que el usuario elija salir. */
    public void iniciar() {
        boolean continuar = true;
        while (continuar) {
            mostrarMenu();
            String opcion = leerTexto("Elige una opcion: ");
            try {
                continuar = ejecutarOpcion(opcion);
            } catch (ValidacionException | ProductoNoEncontradoException e) {
                salida.println("Aviso: " + e.getMessage());
            } catch (AccesoDatosException e) {
                salida.println("Error de base de datos: " + e.getMessage());
            }
        }
        salida.println("Hasta luego.");
    }

    private boolean ejecutarOpcion(String opcion) {
        switch (opcion) {
            case "1" -> insertarProducto();
            case "2" -> mostrarLista(servicio.consultarTodos());
            case "3" -> consultarPorCodigo();
            case "4" -> buscarPorNombre();
            case "5" -> actualizarProducto();
            case "6" -> eliminarProducto();
            case "7" -> mostrarLista(servicio.consultarPorAgotarse());
            case "0" -> {
                return false;
            }
            default -> salida.println("Opcion no valida.");
        }
        return true;
    }

    private void mostrarMenu() {
        salida.println();
        salida.println("=== Mini Market Bamer - Inventario ===");
        salida.println("1. Insertar producto");
        salida.println("2. Consultar todos");
        salida.println("3. Consultar por codigo");
        salida.println("4. Buscar por nombre");
        salida.println("5. Actualizar producto");
        salida.println("6. Eliminar producto");
        salida.println("7. Productos por agotarse");
        salida.println("0. Salir");
    }

    private void insertarProducto() {
        Producto producto = new Producto();
        producto.setCodigoBarras(leerTexto("Codigo de barras: "));
        producto.setNombre(leerTexto("Nombre: "));
        producto.setIdCategoria(leerEntero("Id de categoria (1 Granos, 2 Lacteos, 3 Bebidas, 4 Aseo, 5 Abarrotes): "));
        producto.setDescripcion(leerTexto("Descripcion: "));
        producto.setPrecioCompra(ProductoService.convertirPrecio(leerTexto("Precio de compra: "), "precio de compra"));
        producto.setPrecioVenta(ProductoService.convertirPrecio(leerTexto("Precio de venta: "), "precio de venta"));
        producto.setStock(leerEntero("Stock: "));
        producto.setStockMinimo(leerEntero("Stock minimo: "));
        Producto guardado = servicio.registrarProducto(producto);
        salida.println("Producto guardado con id " + guardado.getId());
    }

    private void consultarPorCodigo() {
        Producto producto = servicio.consultarPorCodigo(leerTexto("Codigo de barras: "));
        mostrarLista(List.of(producto));
    }

    private void buscarPorNombre() {
        mostrarLista(servicio.buscarPorNombre(leerTexto("Texto a buscar: ")));
    }

    private void actualizarProducto() {
        Producto producto = servicio.consultarPorId(leerEntero("Id del producto a actualizar: "));
        salida.println("Deja vacio un campo para conservar su valor actual.");
        String nombre = leerTexto("Nombre [" + producto.getNombre() + "]: ");
        if (!nombre.isBlank()) {
            producto.setNombre(nombre);
        }
        String precioVenta = leerTexto("Precio de venta [" + producto.getPrecioVenta() + "]: ");
        if (!precioVenta.isBlank()) {
            producto.setPrecioVenta(ProductoService.convertirPrecio(precioVenta, "precio de venta"));
        }
        String stock = leerTexto("Stock [" + producto.getStock() + "]: ");
        if (!stock.isBlank()) {
            producto.setStock(convertirEntero(stock));
        }
        servicio.actualizarProducto(producto);
        salida.println("Producto actualizado.");
    }

    private void eliminarProducto() {
        int id = leerEntero("Id del producto a eliminar: ");
        Producto producto = servicio.consultarPorId(id);
        String confirmacion = leerTexto("Seguro de eliminar '" + producto.getNombre() + "'? (s/n): ");
        if (confirmacion.equalsIgnoreCase("s")) {
            servicio.eliminarProducto(id);
            salida.println("Producto eliminado.");
        } else {
            salida.println("Operacion cancelada.");
        }
    }

    private void mostrarLista(List<Producto> productos) {
        if (productos.isEmpty()) {
            salida.println("No hay productos para mostrar.");
            return;
        }
        salida.printf("%-4s %-15s %-26s %-10s %12s %6s%n", "ID", "CODIGO", "NOMBRE", "CATEGORIA", "PRECIO", "STOCK");
        for (Producto p : productos) {
            salida.printf("%-4d %-15s %-26s %-10s %12s %6d%s%n", p.getId(), p.getCodigoBarras(), p.getNombre(),
                    p.getNombreCategoria(), formatearDinero(p), p.getStock(),
                    p.estaPorAgotarse() ? "  (por agotarse)" : "");
        }
    }

    private String formatearDinero(Producto producto) {
        NumberFormat formato = NumberFormat.getIntegerInstance(LOCALE_CO);
        return "$ " + formato.format(producto.getPrecioVenta());
    }

    private String leerTexto(String mensaje) {
        salida.print(mensaje);
        return entrada.hasNextLine() ? entrada.nextLine().trim() : "0";
    }

    private int leerEntero(String mensaje) {
        return convertirEntero(leerTexto(mensaje));
    }

    private int convertirEntero(String texto) {
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            throw new ValidacionException("Debes escribir un numero entero valido");
        }
    }
}
