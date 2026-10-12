package web;

import controller.UsuarioController;
import controller.ValidacionException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import modelo.Usuario;

/**
 * Controlador web del CRUD de Usuarios (/usuarios).
 *
 * Vista SOLO PARA ADMIN: si el usuario logueado no tiene rol ADMIN se
 * redirige al menu con un mensaje.
 */
@WebServlet(name = "UsuarioServlet", urlPatterns = {"/usuarios"})
public class UsuarioServlet extends HttpServlet {

    private final UsuarioController usuarioController = new UsuarioController();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Usuario actual = WebUtil.obtenerUsuario(req);
        if (!WebUtil.esAdmin(actual)) {
            WebUtil.mensajeFlash(req, "error", "No tiene permisos para gestionar usuarios");
            resp.sendRedirect(req.getContextPath() + "/menu");
            return;
        }
        req.setAttribute("usuarios", usuarioController.listar());
        req.setAttribute("usuarioActual", actual);
        req.getRequestDispatcher("/WEB-INF/jsp/usuarios.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Usuario actual = WebUtil.obtenerUsuario(req);
        if (!WebUtil.esAdmin(actual)) {
            resp.sendRedirect(req.getContextPath() + "/menu");
            return;
        }

        String accion = req.getParameter("accion");
        try {
            switch (accion == null ? "" : accion) {
                case "agregar" -> {
                    usuarioController.agregar(
                            req.getParameter("usuario"),
                            req.getParameter("clave"),
                            req.getParameter("nombre"),
                            req.getParameter("rol"));
                    WebUtil.mensajeFlash(req, "exito", "Usuario agregado correctamente");
                }
                case "modificar" -> {
                    usuarioController.modificar(
                            entero(req, "id"),
                            req.getParameter("usuario"),
                            req.getParameter("clave"),
                            req.getParameter("nombre"),
                            req.getParameter("rol"));
                    WebUtil.mensajeFlash(req, "exito", "Usuario modificado correctamente");
                }
                case "eliminar" -> {
                    usuarioController.eliminar(entero(req, "id"), actual.getIdusuario());
                    WebUtil.mensajeFlash(req, "exito", "Usuario eliminado");
                }
                default -> WebUtil.mensajeFlash(req, "error", "Accion no valida");
            }
        } catch (ValidacionException ex) {
            WebUtil.mensajeFlash(req, "error", ex.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + "/usuarios");
    }

    /** Convierte un parametro a int (0 si falta o no es numero). */
    private int entero(HttpServletRequest req, String nombre) {
        try {
            return Integer.parseInt(req.getParameter(nombre));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}