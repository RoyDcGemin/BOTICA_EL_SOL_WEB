package modelo;

/**
 * MODELO Producto.
 * Representa una fila de la tabla "producto" de la Botica "El Sol".
 * POJO: solo datos y getters/setters.
 * Observa que el producto tiene idCategoria: la relacion con Categoria
 * se representa guardando el id (relacion 1 a muchos).
 * El campo codigo (UNICO) es lo que el personal digita en la venta para
 * emular el scanner de codigo de barras (sin hardware).
 * principioActivo: nombre generico del medicamento (ej. "Paracetamol"),
 * distinto del nombre comercial (ej. "Panadol 500mg").
 * stockMinimo: umbral por tipo de producto; si stock <= stockMinimo
 * el sistema lo marca como "Stock bajo" para reposicion oportuna.
 */
public class Producto {
    private int idProducto;
    private String codigo;
    private String nombre;
    private String principioActivo;
    private double precio;
    private int stock;
    private int stockMinimo;
    private int idCategoria;

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPrincipioActivo() {
        return principioActivo;
    }

    public void setPrincipioActivo(String principioActivo) {
        this.principioActivo = principioActivo;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }


}

