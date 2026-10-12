<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="cabecera.jsp">
    <jsp:param name="titulo" value="Usuarios"/>
</jsp:include>
<%@ include file="flash.jsp" %>

<h1 class="titulo-pagina">Usuarios</h1>

<!-- VISTA SOLO ADMIN: gestion de usuarios del sistema. -->
<section class="panel-lateral">
    <div class="tarjeta">
        <h2>Lista de usuarios</h2>
        <div class="envoltorio-tabla">
            <table class="tabla">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Usuario</th>
                        <th>Nombre</th>
                        <th>Rol</th>
                        <th>Acciones</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="u" items="${usuarios}">
                        <tr>
                            <td>${u.idusuario}</td>
                            <td><c:out value="${u.usuario}"/></td>
                            <td><c:out value="${u.nombre}"/></td>
                            <td><c:out value="${u.rol}"/></td>
                            <td>
                                <div class="grupo-botones">
                                    <button type="button" class="btn btn-secundario btn-pequeno"
                                            onclick="editarUsuario(${u.idusuario}, '${u.usuario}', '${u.nombre}', '${u.rol}')">
                                        Editar
                                    </button>
                                    <form action="${pageContext.request.contextPath}/usuarios" method="post"
                                          onsubmit="return confirmarEliminacion(this, 'Esta seguro de eliminar al usuario ${u.usuario}?')">
                                        <input type="hidden" name="accion" value="eliminar">
                                        <input type="hidden" name="id" value="${u.idusuario}">
                                        <button type="submit" class="btn btn-peligro btn-pequeno">Eliminar</button>
                                    </form>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </div>

    <div class="tarjeta">
        <h2 id="titulo-formulario">Nuevo usuario</h2>
        <form action="${pageContext.request.contextPath}/usuarios" method="post" class="formulario" id="formulario-usuario">
            <input type="hidden" name="accion" id="accion" value="agregar">
            <input type="hidden" name="id" id="id">

            <div class="campo">
                <label for="usuario">Nombre de usuario</label>
                <input type="text" id="usuario" name="usuario" required
                       placeholder="Con el que iniciara sesion" maxlength="30">
            </div>
            <div class="campo">
                <label for="nombre">Nombre completo</label>
                <input type="text" id="nombre" name="nombre" required maxlength="100">
            </div>
            <div class="campo">
                <label for="rol">Rol</label>
                <select id="rol" name="rol" required>
                    <option value="">-- Seleccionar --</option>
                    <option value="ADMIN">ADMIN</option>
                    <option value="VENDEDOR">VENDEDOR</option>
                </select>
            </div>
            <div class="campo">
                <label for="clave">Contrasena</label>
                <input type="password" id="clave" name="clave" placeholder="Minimo 4 caracteres">
                <span class="ayuda-campo" id="ayuda-clave">En edicion, dejar vacio conserva la contrasena actual.</span>
            </div>

            <div class="grupo-botones">
                <button type="submit" class="btn btn-primario" id="boton-guardar">Agregar usuario</button>
                <button type="button" class="btn btn-secundario" onclick="nuevoUsuario()">Cancelar edicion</button>
            </div>
        </form>
    </div>
</section>

<script>
    function editarUsuario(id, usuario, nombre, rol) {
        document.getElementById("accion").value = "modificar";
        document.getElementById("id").value = id;
        document.getElementById("usuario").value = usuario;
        document.getElementById("nombre").value = nombre;
        document.getElementById("rol").value = rol;
        document.getElementById("clave").value = "";
        document.getElementById("clave").required = false;
        document.getElementById("titulo-formulario").textContent = "Editando usuario #" + id;
        document.getElementById("boton-guardar").textContent = "Guardar cambios";
        document.getElementById("usuario").focus();
    }

    function nuevoUsuario() {
        document.getElementById("formulario-usuario").reset();
        document.getElementById("accion").value = "agregar";
        document.getElementById("id").value = "";
        document.getElementById("clave").required = true;
        document.getElementById("titulo-formulario").textContent = "Nuevo usuario";
        document.getElementById("boton-guardar").textContent = "Agregar usuario";
    }
</script>

<jsp:include page="pie.jsp"/>