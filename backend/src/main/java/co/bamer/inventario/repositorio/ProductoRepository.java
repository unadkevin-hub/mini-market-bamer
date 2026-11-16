package co.bamer.inventario.repositorio;

import co.bamer.inventario.modelo.Producto;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de acceso a datos para los productos (operaciones CRUD).
 */
public interface ProductoRepository {

    /** Inserta un producto y devuelve el mismo con su id generado. */
    Producto guardar(Producto producto);

    Optional<Producto> buscarPorId(int id);

    Optional<Producto> buscarPorCodigo(String codigoBarras);

    List<Producto> listarTodos();

    List<Producto> buscarPorNombre(String texto);

    /** Productos cuyo stock es menor o igual al stock minimo. */
    List<Producto> listarPorAgotarse();

    /** Actualiza un producto existente. Devuelve true si se modifico una fila. */
    boolean actualizar(Producto producto);

    /** Elimina un producto por id. Devuelve true si se elimino una fila. */
    boolean eliminar(int id);

    /** Indica si ya existe un codigo de barras en otro producto distinto a idExcluido. */
    boolean existeCodigo(String codigoBarras, Integer idExcluido);
}
