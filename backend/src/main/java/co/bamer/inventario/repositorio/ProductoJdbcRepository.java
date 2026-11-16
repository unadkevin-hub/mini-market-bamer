package co.bamer.inventario.repositorio;

import co.bamer.comun.conexion.ConexionBD;
import co.bamer.comun.excepciones.AccesoDatosException;
import co.bamer.inventario.modelo.Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementacion JDBC del repositorio de productos. Usa PreparedStatement en todas
 * las consultas para evitar inyeccion SQL.
 */
public class ProductoJdbcRepository implements ProductoRepository {

    private static final String SELECT_BASE =
            "SELECT p.id, p.codigo_barras, p.nombre, p.id_categoria, c.nombre AS nombre_categoria, "
                    + "p.descripcion, p.precio_compra, p.precio_venta, p.stock, p.stock_minimo "
                    + "FROM productos p JOIN categorias c ON c.id = p.id_categoria ";

    private static final String SQL_INSERTAR =
            "INSERT INTO productos (codigo_barras, nombre, id_categoria, descripcion, "
                    + "precio_compra, precio_venta, stock, stock_minimo) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_ACTUALIZAR =
            "UPDATE productos SET codigo_barras = ?, nombre = ?, id_categoria = ?, descripcion = ?, "
                    + "precio_compra = ?, precio_venta = ?, stock = ?, stock_minimo = ? WHERE id = ?";

    private static final String SQL_ELIMINAR = "DELETE FROM productos WHERE id = ?";

    private final ConexionBD conexionBD;

    public ProductoJdbcRepository(ConexionBD conexionBD) {
        this.conexionBD = conexionBD;
    }

    @Override
    public Producto guardar(Producto producto) {
        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement sentencia =
                     conexion.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            asignarParametros(sentencia, producto);
            sentencia.executeUpdate();
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    producto.setId(claves.getInt(1));
                }
            }
            return producto;
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al guardar el producto: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Producto> buscarPorId(int id) {
        List<Producto> resultado = consultar(SELECT_BASE + "WHERE p.id = ?", id);
        return resultado.stream().findFirst();
    }

    @Override
    public Optional<Producto> buscarPorCodigo(String codigoBarras) {
        List<Producto> resultado = consultar(SELECT_BASE + "WHERE p.codigo_barras = ?", codigoBarras);
        return resultado.stream().findFirst();
    }

    @Override
    public List<Producto> listarTodos() {
        return consultar(SELECT_BASE + "ORDER BY p.nombre");
    }

    @Override
    public List<Producto> buscarPorNombre(String texto) {
        return consultar(SELECT_BASE + "WHERE LOWER(p.nombre) LIKE ? ORDER BY p.nombre",
                "%" + texto.toLowerCase() + "%");
    }

    @Override
    public List<Producto> listarPorAgotarse() {
        return consultar(SELECT_BASE + "WHERE p.stock <= p.stock_minimo ORDER BY p.stock");
    }

    @Override
    public boolean actualizar(Producto producto) {
        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_ACTUALIZAR)) {
            asignarParametros(sentencia, producto);
            sentencia.setInt(9, producto.getId());
            return sentencia.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al actualizar el producto: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean eliminar(int id) {
        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_ELIMINAR)) {
            sentencia.setInt(1, id);
            return sentencia.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al eliminar el producto: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existeCodigo(String codigoBarras, Integer idExcluido) {
        String sql = "SELECT COUNT(*) FROM productos WHERE codigo_barras = ? AND id <> ?";
        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, codigoBarras);
            sentencia.setInt(2, idExcluido == null ? -1 : idExcluido);
            try (ResultSet filas = sentencia.executeQuery()) {
                filas.next();
                return filas.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al verificar el codigo: " + e.getMessage(), e);
        }
    }

    private List<Producto> consultar(String sql, Object... parametros) {
        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                sentencia.setObject(i + 1, parametros[i]);
            }
            try (ResultSet filas = sentencia.executeQuery()) {
                List<Producto> productos = new ArrayList<>();
                while (filas.next()) {
                    productos.add(convertirFila(filas));
                }
                return productos;
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al consultar productos: " + e.getMessage(), e);
        }
    }

    private void asignarParametros(PreparedStatement sentencia, Producto producto) throws SQLException {
        sentencia.setString(1, producto.getCodigoBarras());
        sentencia.setString(2, producto.getNombre());
        sentencia.setInt(3, producto.getIdCategoria());
        sentencia.setString(4, producto.getDescripcion());
        sentencia.setBigDecimal(5, producto.getPrecioCompra());
        sentencia.setBigDecimal(6, producto.getPrecioVenta());
        sentencia.setInt(7, producto.getStock());
        sentencia.setInt(8, producto.getStockMinimo());
    }

    private Producto convertirFila(ResultSet fila) throws SQLException {
        Producto producto = new Producto();
        producto.setId(fila.getInt("id"));
        producto.setCodigoBarras(fila.getString("codigo_barras"));
        producto.setNombre(fila.getString("nombre"));
        producto.setIdCategoria(fila.getInt("id_categoria"));
        producto.setNombreCategoria(fila.getString("nombre_categoria"));
        producto.setDescripcion(fila.getString("descripcion"));
        producto.setPrecioCompra(fila.getBigDecimal("precio_compra"));
        producto.setPrecioVenta(fila.getBigDecimal("precio_venta"));
        producto.setStock(fila.getInt("stock"));
        producto.setStockMinimo(fila.getInt("stock_minimo"));
        return producto;
    }
}
