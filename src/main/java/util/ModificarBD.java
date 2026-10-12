package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

/**
 * Herramienta de DESARROLLO para crear (o verificar) el esquema de la base
 * de datos MySQL y los datos iniciales de la Botica "El Sol".
 *
 * NO forma parte del flujo de la aplicacion: se ejecuta manualmente
 * (boton "Run" sobre esta clase en IntelliJ, o java -cp ...) cuando se
 * quiere una BD limpia. Requiere que el servidor MySQL este arriba y que
 * el usuario tenga permiso de CREATE.
 *
 * 1) Crea la base "elsol_db" si no existe (conexion sin base).
 * 2) Crea las 5 tablas del sistema (CREATE TABLE IF NOT EXISTS).
 * 3) Inserta usuarios, categorias farmaceuticas y productos iniciales.
 *
 * Columnas segun el diccionario del informe APF2 (snake_case).
 * El producto lleva principio_activo (nombre generico) y stock_minimo
 * (umbral de alerta de reposicion por tipo de producto).
 */
public class ModificarBD {

    /** URL base del servidor (sin base de datos), para crear elsol_db. */
    private static final String SERVIDOR_URL = System.getProperty(
            "app.bd.servidor",
            "jdbc:mysql://localhost:3306"
                    + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Lima");

    private static final String USUARIO = System.getProperty("app.bd.usuario", "elsol");
    private static final String CLAVE = System.getProperty("app.bd.clave", "elsol123");

    public static void main(String[] args) {
        crearBase();
        crearTablas();
        crearDatosIniciales();
        System.out.println("Esquema y datos iniciales creados/verificados.");
    }

    /** Crea la base de datos elsol_db si es que no existe. */
    private static void crearBase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(SERVIDOR_URL, USUARIO, CLAVE);
                 Statement st = conn.createStatement()) {
                st.executeUpdate("CREATE DATABASE IF NOT EXISTS elsol_db "
                        + "CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci");
            }
        } catch (Exception ex) {
            System.out.println("Error al crear la base: " + ex.getMessage());
        }
    }

    /** Crea las 5 tablas del sistema si es que no existen. */
    private static void crearTablas() {
        String[] tablas = {
            "CREATE TABLE IF NOT EXISTS usuario("
            + "id_usuario INT AUTO_INCREMENT PRIMARY KEY,"
            + "usuario VARCHAR(50) NOT NULL UNIQUE,"
            + "clave VARCHAR(64) NOT NULL,"
            + "nombre VARCHAR(100),"
            + "rol VARCHAR(20)) ENGINE=InnoDB;",

            "CREATE TABLE IF NOT EXISTS categoria("
            + "id_categoria INT AUTO_INCREMENT PRIMARY KEY,"
            + "descripcion VARCHAR(100)) ENGINE=InnoDB;",

            "CREATE TABLE IF NOT EXISTS producto("
            + "id_producto INT AUTO_INCREMENT PRIMARY KEY,"
            + "codigo VARCHAR(30) NOT NULL UNIQUE,"
            + "nombre VARCHAR(100) NOT NULL,"
            + "principio_activo VARCHAR(100) NOT NULL DEFAULT '',"
            + "precio DECIMAL(10,2) NOT NULL,"
            + "stock INT NOT NULL,"
            + "stock_minimo INT NOT NULL DEFAULT 5,"
            + "id_categoria INT,"
            + "FOREIGN KEY (id_categoria) REFERENCES categoria(id_categoria)) ENGINE=InnoDB;",

            "CREATE TABLE IF NOT EXISTS venta("
            + "id_venta INT AUTO_INCREMENT PRIMARY KEY,"
            + "fecha DATETIME NOT NULL,"
            + "hora_inicio DATETIME NOT NULL,"
            + "hora_fin DATETIME NOT NULL,"
            + "total DECIMAL(10,2) NOT NULL,"
            + "descuento DECIMAL(10,2) NOT NULL DEFAULT 0,"
            + "estado VARCHAR(12) NOT NULL DEFAULT 'VIGENTE',"
            + "dni_cliente VARCHAR(12),"
            + "nombre_cliente VARCHAR(100),"
            + "metodo_pago VARCHAR(20),"
            + "id_usuario INT,"
            + "FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)) ENGINE=InnoDB;",

            "CREATE TABLE IF NOT EXISTS detalle_venta("
            + "id_detalle INT AUTO_INCREMENT PRIMARY KEY,"
            + "id_venta INT NOT NULL,"
            + "id_producto INT NOT NULL,"
            + "cantidad INT NOT NULL,"
            + "precio_unitario DECIMAL(10,2) NOT NULL,"
            + "subtotal DECIMAL(10,2) NOT NULL,"
            + "FOREIGN KEY (id_venta) REFERENCES venta(id_venta),"
            + "FOREIGN KEY (id_producto) REFERENCES producto(id_producto)) ENGINE=InnoDB;"
        };

        try (Connection conn = Conexion.getConexion();
             Statement st = conn.createStatement()) {
            for (String sql : tablas) {
                st.executeUpdate(sql);
            }
        } catch (Exception ex) {
            System.out.println("Error al crear las tablas: " + ex.getMessage());
        }
    }

    /**
     * Inserta usuarios, categorias farmaceuticas y productos iniciales.
     * Las claves se guardan con HASH SHA-256 (nunca en texto plano).
     */
    private static void crearDatosIniciales() {
        try (Connection conn = Conexion.getConexion()) {
            // Migracion suave: si la tabla ya existia sin las columnas nuevas,
            // las agregamos sin perder datos.
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("ALTER TABLE producto ADD COLUMN IF NOT EXISTS principio_activo VARCHAR(100) NOT NULL DEFAULT ''");
                st.executeUpdate("ALTER TABLE producto ADD COLUMN IF NOT EXISTS stock_minimo INT NOT NULL DEFAULT 5");
            } catch (Exception ex) {
                System.out.println("Aviso migracion columnas: " + ex.getMessage());
            }

            insertarUsuario(conn, "admin", "1234", "Administrador El Sol", "ADMIN");
            insertarUsuario(conn, "cajero", "1234", "Personal El Sol", "CAJERO");

            // Limpieza de semilla legacy de minimarket (codigos 1001-5001).
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("DELETE FROM detalle_venta WHERE id_producto IN (SELECT id_producto FROM producto WHERE codigo IN ('1001','1002','1003','2001','2002','2003','3001','3002','4001','5001'))");
                st.executeUpdate("DELETE FROM producto WHERE codigo IN ('1001','1002','1003','2001','2002','2003','3001','3002','4001','5001')");
                st.executeUpdate("DELETE FROM categoria WHERE descripcion IN ('Abarrotes','Bebidas','Snacks','Limpieza','Higiene personal') AND id_categoria NOT IN (SELECT id_categoria FROM producto WHERE id_categoria IS NOT NULL)");
            } catch (Exception ex) {
                System.out.println("Aviso limpieza legacy: " + ex.getMessage());
            }

            int analgesicos = insertarCategoria(conn, "Analgesicos");
            int antibioticos = insertarCategoria(conn, "Antibioticos");
            int antiinflamatorios = insertarCategoria(conn, "Antiinflamatorios");
            int antihistaminicos = insertarCategoria(conn, "Antihistaminicos");
            int vitaminas = insertarCategoria(conn, "Vitaminas y suplementos");
            int cuidado = insertarCategoria(conn, "Cuidado personal");

            insertarProducto(conn, "F001", "Panadol 500mg x20 tab", "Paracetamol", 12.50, 80, 10, analgesicos);
            insertarProducto(conn, "F002", "Apronax 550mg x10 tab", "Naproxeno", 18.90, 50, 8, antiinflamatorios);
            insertarProducto(conn, "F003", "Ibuprofeno 400mg x10 tab", "Ibuprofeno", 9.50, 60, 8, antiinflamatorios);
            insertarProducto(conn, "F004", "Amoxicilina 500mg x12 cap", "Amoxicilina", 22.00, 40, 6, antibioticos);
            insertarProducto(conn, "F005", "Azitromicina 500mg x3 tab", "Azitromicina", 25.50, 30, 5, antibioticos);
            insertarProducto(conn, "F006", "Cetirizina 10mg x10 tab", "Cetirizina", 8.00, 70, 10, antihistaminicos);
            insertarProducto(conn, "F007", "Loratadina 10mg x10 tab", "Loratadina", 7.50, 65, 10, antihistaminicos);
            insertarProducto(conn, "F008", "Omeprazol 20mg x14 cap", "Omeprazol", 11.00, 55, 8, analgesicos);
            insertarProducto(conn, "F009", "Vitamina C 1g x10 eferv", "Acido ascorbico", 15.00, 45, 6, vitaminas);
            insertarProducto(conn, "F010", "Alcohol 70% 1L", "Alcohol etilico", 9.90, 100, 12, cuidado);
            insertarProducto(conn, "F011", "Mascarilla quirurgica x50", "No aplica", 19.90, 120, 15, cuidado);
            insertarProducto(conn, "F012", "Paracetamol jarabe 120mg/5ml", "Paracetamol", 13.50, 35, 5, analgesicos);
        } catch (Exception ex) {
            System.out.println("Error al crear datos iniciales: " + ex.getMessage());
        }
    }

    private static void insertarUsuario(Connection conn, String usuario, String clave,
                                        String nombre, String rol) throws Exception {
        String sql = "INSERT INTO usuario (usuario, clave, nombre, rol) "
                + "SELECT ?, ?, ?, ? WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE usuario = ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setString(2, HashUtil.sha256(clave));
            ps.setString(3, nombre);
            ps.setString(4, rol);
            ps.setString(5, usuario);
            ps.executeUpdate();
        }
    }

    /** Inserta la categoria si falta y devuelve su id. */
    private static int insertarCategoria(Connection conn, String descripcion) throws Exception {
        String sql = "INSERT INTO categoria (descripcion) "
                + "SELECT ? WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE descripcion = ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, descripcion);
            ps.setString(2, descripcion);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id_categoria FROM categoria WHERE descripcion = ?")) {
            ps.setString(1, descripcion);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_categoria");
                }
            }
        }
        return 0;
    }

    private static void insertarProducto(Connection conn, String codigo, String nombre,
                                         String principioActivo, double precio, int stock, int stockMinimo, int idCategoria) throws Exception {
        // Si el producto ya existe por codigo, actualizamos sus datos farma.
        String update = "UPDATE producto SET nombre=?, principio_activo=?, precio=?, stock=?, stock_minimo=?, id_categoria=? WHERE codigo=?";
        try (PreparedStatement ps = conn.prepareStatement(update)) {
            ps.setString(1, nombre);
            ps.setString(2, principioActivo);
            ps.setDouble(3, precio);
            ps.setInt(4, stock);
            ps.setInt(5, stockMinimo);
            ps.setInt(6, idCategoria);
            ps.setString(7, codigo);
            int filas = ps.executeUpdate();
            if (filas > 0) {
                return;
            }
        }
        String sql = "INSERT INTO producto (codigo, nombre, principio_activo, precio, stock, stock_minimo, id_categoria) "
                + "SELECT ?, ?, ?, ?, ?, ?, ? WHERE NOT EXISTS (SELECT 1 FROM producto WHERE codigo = ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigo);
            ps.setString(2, nombre);
            ps.setString(3, principioActivo);
            ps.setDouble(4, precio);
            ps.setInt(5, stock);
            ps.setInt(6, stockMinimo);
            ps.setInt(7, idCategoria);
            ps.setString(8, codigo);
            ps.executeUpdate();
        }
    }
}
