package controller;

import java.util.ArrayList;
import modelo.Usuario;
import modeloDAO.UsuarioDAO;
import util.HashUtil;

/**
 * Controlador de la entidad Usuario.
 *
 * CAPA INTERMEDIA (logica): aca vive la logica del LOGIN.
 *
 * FLUJO: Vista (Logueo) -> UsuarioController.iniciarSesion ->
 *        UsuarioDAO.buscarPorNombre -> Base de datos
 *        y de vuelta: Usuario -> controlador (compara contraseñas) -> vista.
 *
 * SEGURIDAD: la clave que el usuario escribe se hashea y se compara contra
 * el hash guardado en la BD. Nunca se compara texto plano.
 */
public class UsuarioController {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    /**
     * Valida las credenciales de un usuario para iniciar sesion.
     *
     * @param nombre nombre de usuario ingresado
     * @param clave contrasenia ingresada
     * @return el Usuario autenticado (se guarda como "sesion" para pasarlo
     *         entre ventanas)
     * @throws ValidacionException con el mensaje de error correspondiente
     *         (campos vacios, usuario inexistente o contrasenia incorrecta)
     */
    public Usuario iniciarSesion(String nombre, String clave) throws ValidacionException {
        // 1) Validacion de campos vacios.
        if (nombre.isEmpty() || clave.isEmpty()) {
            throw new ValidacionException("Ingresar usuario y contraseña");
        }

        // 2) Buscar al usuario por su nombre en la BD.
        Usuario u = usuarioDAO.buscarPorNombre(nombre);
        if (u == null) {
            throw new ValidacionException("Usuario no existe");
        }

        // 3) Comparar el hash de lo ingresado con el hash guardado.
        if (!u.getClave().equals(HashUtil.sha256(clave))) {
            throw new ValidacionException("Usuario o contraseña invalido");
        }

        // Credenciales correctas: devolvemos el usuario logueado.
        return u;
    }

    /**
     * Devuelve todos los usuarios (pagina de administracion).
     */
    public ArrayList<Usuario> listar() {
        return usuarioDAO.listar();
    }

    /**
     * Agrega un usuario nuevo. El nombre de usuario no puede repetirse.
     */
    public void agregar(String usuario, String clave, String nombre, String rol) throws ValidacionException {
        validarDatos(usuario, clave, nombre, rol, true);

        if (usuarioDAO.buscarPorNombre(usuario) != null) {
            throw new ValidacionException("El nombre de usuario ya existe");
        }

        Usuario u = new Usuario();
        u.setUsuario(usuario);
        u.setClave(clave); // el DAO guardara el HASH
        u.setNombre(nombre);
        u.setRol(rol);
        usuarioDAO.agregar(u);
    }

    /**
     * Modifica un usuario (usuario, nombre, rol y opcionalmente la clave).
     * Si la clave llega vacia, se conserva la actual (no se re-hashea).
     */
    public void modificar(int id, String usuario, String clave, String nombre, String rol) throws ValidacionException {
        if (clave == null || clave.isEmpty()) {
            // Sin clave nueva: usamos el hash que ya estaba guardado.
            Usuario actual = usuarioDAO.buscar(id);
            if (actual == null) {
                throw new ValidacionException("El usuario no existe");
            }
            validarDatos(usuario, "x", nombre, rol, true);
            Usuario u = new Usuario();
            u.setIdusuario(id);
            u.setUsuario(usuario);
            u.setClave(actual.getClave());
            u.setNombre(nombre);
            u.setRol(rol);
            usuarioDAO.modificar(u);
        } else {
            validarDatos(usuario, clave, nombre, rol, true);
            Usuario u = new Usuario();
            u.setIdusuario(id);
            u.setUsuario(usuario);
            u.setClave(clave);
            u.setNombre(nombre);
            u.setRol(rol);
            usuarioDAO.modificar(u);
        }
    }

    /**
     * Elimina un usuario con dos reglas de seguridad:
     * 1) No puede eliminarse a si mismo.
     * 2) No puede eliminarse al unico ADMIN que queda en el sistema.
     *
     * @param id usuario a eliminar
     * @param idUsuarioActual el usuario que esta haciendo la operacion
     */
    public void eliminar(int id, int idUsuarioActual) throws ValidacionException {
        Usuario objetivo = usuarioDAO.buscar(id);
        if (objetivo == null) {
            throw new ValidacionException("El usuario no existe");
        }
        if (objetivo.getIdusuario() == idUsuarioActual) {
            throw new ValidacionException("No puede eliminarse a si mismo");
        }
        if ("ADMIN".equalsIgnoreCase(objetivo.getRol())) {
            int admins = 0;
            for (Usuario u : usuarioDAO.listar()) {
                if ("ADMIN".equalsIgnoreCase(u.getRol())) {
                    admins++;
                }
            }
            if (admins <= 1) {
                throw new ValidacionException("No puede eliminarse al unico administrador");
            }
        }
        usuarioDAO.eliminar(id);
    }

    /**
     * Cambio de contrasena de un usuario logueado.
     *
     * @param idUsuario el usuario que cambia su clave
     * @param claveActual clave que tiene hoy (se verifica contra el hash)
     * @param claveNueva nueva clave
     * @param confirmacion repeticion de la nueva clave
     */
    public void cambiarContrasena(int idUsuario, String claveActual, String claveNueva, String confirmacion)
            throws ValidacionException {
        if (claveActual == null || claveActual.isEmpty()) {
            throw new ValidacionException("Ingrese su contrasena actual");
        }
        if (claveNueva == null || claveNueva.isEmpty()) {
            throw new ValidacionException("Ingrese la nueva contrasena");
        }
        if (claveNueva.length() < 4) {
            throw new ValidacionException("La nueva contrasena debe tener al menos 4 caracteres");
        }
        if (!claveNueva.equals(confirmacion)) {
            throw new ValidacionException("La confirmacion no coincide con la nueva contrasena");
        }

        Usuario u = usuarioDAO.buscar(idUsuario);
        if (u == null) {
            throw new ValidacionException("El usuario no existe");
        }
        if (!u.getClave().equals(HashUtil.sha256(claveActual))) {
            throw new ValidacionException("La contrasena actual es incorrecta");
        }

        u.setClave(claveNueva); // el DAO guardara el HASH
        usuarioDAO.modificar(u);
    }

    /**
     * Validacion centralizada de un usuario. Cuando exigirClave es true,
     * la clave es obligatoria (alta del usuario).
     */
    private void validarDatos(String usuario, String clave, String nombre, String rol, boolean exigirClave)
            throws ValidacionException {
        if (usuario == null || usuario.trim().isEmpty()) {
            throw new ValidacionException("Ingrese el nombre de usuario");
        }
        if (exigirClave && (clave == null || clave.isEmpty())) {
            throw new ValidacionException("Ingrese la contrasena");
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ValidacionException("Ingrese el nombre completo");
        }
        if (rol == null || rol.trim().isEmpty()) {
            throw new ValidacionException("Seleccione el rol");
        }
    }
}