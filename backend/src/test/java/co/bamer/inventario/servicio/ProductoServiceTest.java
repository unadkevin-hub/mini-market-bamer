package co.bamer.inventario.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.bamer.comun.excepciones.ProductoNoEncontradoException;
import co.bamer.comun.excepciones.ValidacionException;
import co.bamer.inventario.BaseDatosPrueba;
import co.bamer.inventario.modelo.Producto;
import co.bamer.inventario.repositorio.ProductoJdbcRepository;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProductoServiceTest {

    private ProductoService servicio;

    @BeforeEach
    void prepararBaseDatos() throws Exception {
        servicio = new ProductoService(new ProductoJdbcRepository(BaseDatosPrueba.crear("servicio")));
    }

    private Producto productoNuevo(String codigo) {
        return new Producto(codigo, "Aceite Premier 1 L", 5, "Aceite vegetal",
                new BigDecimal("8000"), new BigDecimal("9500"), 20, 5);
    }

    @Test
    void insertarGeneraIdYSePuedeConsultar() {
        Producto guardado = servicio.registrarProducto(productoNuevo("7709990001"));
        assertNotNull(guardado.getId());
        Producto leido = servicio.consultarPorCodigo("7709990001");
        assertEquals("Aceite Premier 1 L", leido.getNombre());
        assertEquals("Abarrotes", leido.getNombreCategoria());
        assertEquals(0, new BigDecimal("9500").compareTo(leido.getPrecioVenta()));
    }

    @Test
    void consultarTodosTraeLosDatosIniciales() {
        assertEquals(6, servicio.consultarTodos().size());
    }

    @Test
    void buscarPorNombreIgnoraMayusculas() {
        assertEquals(1, servicio.buscarPorNombre("ARROZ").size());
    }

    @Test
    void porAgotarseDevuelveHuevosYAzucar() {
        assertEquals(2, servicio.consultarPorAgotarse().size());
    }

    @Test
    void actualizarCambiaLosValores() {
        Producto producto = servicio.consultarPorCodigo("7701234500011");
        producto.setNombre("Arroz Diana 1 kg");
        producto.setPrecioVenta(new BigDecimal("4900"));
        producto.setStock(55);
        servicio.actualizarProducto(producto);
        Producto leido = servicio.consultarPorId(producto.getId());
        assertEquals("Arroz Diana 1 kg", leido.getNombre());
        assertEquals(55, leido.getStock());
    }

    @Test
    void eliminarQuitaElProducto() {
        Producto producto = servicio.consultarPorCodigo("7701234500011");
        servicio.eliminarProducto(producto.getId());
        assertThrows(ProductoNoEncontradoException.class, () -> servicio.consultarPorId(producto.getId()));
        assertEquals(5, servicio.consultarTodos().size());
    }

    @Test
    void eliminarInexistenteLanzaExcepcion() {
        assertThrows(ProductoNoEncontradoException.class, () -> servicio.eliminarProducto(9999));
    }

    @Test
    void rechazaCodigoDuplicado() {
        assertThrows(ValidacionException.class, () -> servicio.registrarProducto(productoNuevo("7701234500011")));
    }

    @Test
    void rechazaCodigoInvalido() {
        assertThrows(ValidacionException.class, () -> servicio.registrarProducto(productoNuevo("12")));
    }

    @Test
    void rechazaPrecioVentaMenorAlDeCompra() {
        Producto producto = productoNuevo("7709990002");
        producto.setPrecioVenta(new BigDecimal("100"));
        assertThrows(ValidacionException.class, () -> servicio.registrarProducto(producto));
    }

    @Test
    void rechazaStockNegativo() {
        Producto producto = productoNuevo("7709990003");
        producto.setStock(-1);
        assertThrows(ValidacionException.class, () -> servicio.registrarProducto(producto));
    }

    @Test
    void estaPorAgotarseSegunStockMinimo() {
        Producto producto = productoNuevo("7709990004");
        producto.setStock(5);
        assertTrue(producto.estaPorAgotarse());
        producto.setStock(6);
        assertFalse(producto.estaPorAgotarse());
    }
}
