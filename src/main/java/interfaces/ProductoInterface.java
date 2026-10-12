package interfaces;

import dto.ProductoDTO;
import java.util.ArrayList;
import modelo.Producto;

/**
 * Interfaz de Producto.
 * Ademas del CRUD<Producto> normal, agrega un metodo propio:
 * listarProductoDTO() devuelve productos "enriquecidos" con el nombre
 * de su categoria (ProductoDTO = Producto + Categoria).
 * ProductoDAO la implementa.
 */
public interface ProductoInterface extends CRUD<Producto> {
    public ArrayList<ProductoDTO> listarDTO();
}
