package co.bamer.inventario.servicio;

import co.bamer.comun.excepciones.ProductoNoEncontradoException;
import co.bamer.comun.excepciones.ValidacionException;
import co.bamer.inventario.modelo.Producto;
import co.bamer.inventario.repositorio.ProductoRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Reglas de negocio del inventario. Replica las validaciones del prototipo front-end.
 */
public class ProductoService {

    private static final Pattern PATRON_CODIGO = Pattern.compile("^\\d{4,14}$");
    private static final int LONGITUD_MINIMA_NOMBRE = 3;

    private final ProductoRepository repositorio;

    public ProductoService(ProductoRepository repositorio) {
        this.repositorio = repositorio;
    }

    public Producto registrarProducto(Producto producto) {
        validar(producto);
        if (repositorio.existeCodigo(producto.getCodigoBarras(), null)) {
            throw new ValidacionException("Ya existe un producto con el codigo " + producto.getCodigoBarras());
        }
        return repositorio.guardar(producto);
    }

    public List<Producto> consultarTodos() {
        return repositorio.listarTodos();
    }

    public Producto consultarPorCodigo(String codigoBarras) {
        return repositorio.buscarPorCodigo(codigoBarras)
                .orElseThrow(() -> new ProductoNoEncontradoException(
                        "No existe un producto con el codigo " + codigoBarras));
    }

    public Producto consultarPorId(int id) {
        return repositorio.buscarPorId(id)
                .orElseThrow(() -> new ProductoNoEncontradoException("No existe un producto con id " + id));
    }

    public List<Producto> buscarPorNombre(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new ValidacionException("Escribe un texto para buscar");
        }
        return repositorio.buscarPorNombre(texto.trim());
    }

    public List<Producto> consultarPorAgotarse() {
        return repositorio.listarPorAgotarse();
    }

    public void actualizarProducto(Producto producto) {
        if (producto.getId() == null) {
            throw new ValidacionException("El producto a actualizar debe tener id");
        }
        validar(producto);
        if (repositorio.existeCodigo(producto.getCodigoBarras(), producto.getId())) {
            throw new ValidacionException("Otro producto ya usa el codigo " + producto.getCodigoBarras());
        }
        if (!repositorio.actualizar(producto)) {
            throw new ProductoNoEncontradoException("No existe un producto con id " + producto.getId());
        }
    }

    public void eliminarProducto(int id) {
        if (!repositorio.eliminar(id)) {
            throw new ProductoNoEncontradoException("No existe un producto con id " + id);
        }
    }

    private void validar(Producto producto) {
        if (producto.getCodigoBarras() == null || !PATRON_CODIGO.matcher(producto.getCodigoBarras()).matches()) {
            throw new ValidacionException("El codigo debe tener entre 4 y 14 digitos");
        }
        if (producto.getNombre() == null || producto.getNombre().trim().length() < LONGITUD_MINIMA_NOMBRE) {
            throw new ValidacionException("El nombre debe tener al menos 3 caracteres");
        }
        if (producto.getIdCategoria() <= 0) {
            throw new ValidacionException("Debes elegir una categoria");
        }
        if (producto.getPrecioCompra() == null || producto.getPrecioCompra().signum() < 0) {
            throw new ValidacionException("El precio de compra no puede ser negativo");
        }
        if (producto.getPrecioVenta() == null
                || producto.getPrecioVenta().compareTo(producto.getPrecioCompra()) < 0) {
            throw new ValidacionException("El precio de venta no puede ser menor al de compra");
        }
        if (producto.getStock() < 0 || producto.getStockMinimo() < 0) {
            throw new ValidacionException("El stock y el stock minimo deben ser 0 o mayores");
        }
        producto.setNombre(producto.getNombre().trim());
    }

    /** Utilidad para convertir texto a BigDecimal lanzando una excepcion de validacion clara. */
    public static BigDecimal convertirPrecio(String texto, String campo) {
        try {
            return new BigDecimal(texto.trim().replace(",", "."));
        } catch (NumberFormatException | NullPointerException e) {
            throw new ValidacionException("El campo " + campo + " debe ser un numero valido");
        }
    }
}
