package web;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filtro de AUTENTICACION.
 *
 * Protege TODAS las rutas de la aplicacion: si no hay un Usuario en la
 * sesion, se redirige al login. Solo quedan publicos el login y los
 * recursos estaticos (css, js, imagenes).
 *
 * Es el equivalente web a "si la ventana no esta abierta, no se puede usar".
 */
@WebFilter(urlPatterns = {"/*"})
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String contexto = req.getContextPath();
        String ruta = req.getRequestURI();

        // Rutas publicas: el login y los recursos estaticos.
        if (esRutaPublica(contexto, ruta)) {
            chain.doFilter(request, response);
            return;
        }

        // Si no hay sesion (o no hay usuario), fuera.
        if (WebUtil.obtenerUsuario(req) == null) {
            resp.sendRedirect(contexto + "/login");
            return;
        }

        chain.doFilter(request, response);
    }

    /** Indica si la ruta puede verse sin iniciar sesion. */
    private boolean esRutaPublica(String contexto, String ruta) {
        if (ruta.equals(contexto) || ruta.equals(contexto + "/")) {
            return true; // raiz: redirige via index.jsp
        }
        if (ruta.equals(contexto + "/login") || ruta.equals(contexto + "/index.jsp")) {
            return true;
        }
        if (ruta.startsWith(contexto + "/css/")
                || ruta.startsWith(contexto + "/js/")
                || ruta.startsWith(contexto + "/img/")) {
            return true;
        }
        return false;
    }
}