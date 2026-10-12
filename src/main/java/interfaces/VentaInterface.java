package interfaces;

import modelo.Venta;
import dto.VentaDTO;
import java.util.ArrayList;

/**
 * Interfaz de Venta.
 * Ademas del CRUD<Venta> normal, agrega:
 *  - guardarVentaCompleta(...): guarda venta + detalles + descuenta
 *    stock TODO en una transaccion (todo o nada).
 *  - listarVentasConDetalles(): lista ventas con sus detalles y
 *    el cajero (VentaDTO).
 *  - listarConFiltros(...): lo mismo pero filtrado por fechas,
 *    cajero y/o producto (reportes y cierre).
 *  - anular(idVenta): marca la venta como ANULADA (sostiene %ERR).
 *  - listarDniRegistrados(): DNIs ya usados, para autocompletar
 *    en la venta sin necesidad de tabla de clientes.
 *  - obtenerSiguienteID(): devuelve el proximo numero de venta.
 * VentaDAO la implementa.
 */
public interface VentaInterface extends CRUD<Venta> {
    public int guardarVentaCompleta(VentaDTO ventaDTO);
    public ArrayList<VentaDTO> listarVentasConDetalles();
    public ArrayList<VentaDTO> listarConFiltros(String desde, String hasta, int idUsuario, int idProducto);
    public void anular(int idVenta);
    public ArrayList<String[]> listarDniRegistrados();
}
