<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="sol" uri="http://elsol/funciones" %>
<jsp:include page="cabecera.jsp">
    <jsp:param name="titulo" value="Productos"/>
</jsp:include>
<%@ include file="flash.jsp" %>

<h1 class="titulo-pagina">Productos</h1>

<!-- VISTA PRODUCTOS: catalogo farmaceutico de la Botica. -->
<section class="panel-lateral">
    <div class="tarjeta">
        <h2>Lista de productos</h2>
        <div class="envoltorio-tabla">
            <table class="tabla">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Codigo</th>
                        <th>Nombre comercial</th>
                        <th>Principio activo</th>
                        <th class="texto-derecha">Precio</th>
                        <th class="texto-derecha">Stock</th>
                        <th class="texto-derecha">Stock min.</th>
                        <th>Categoria</th>
                        <th>Acciones</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="p" items="${productos}">
                        <tr>
                            <td>${p.idProducto}</td>
                            <td><strong><c:out value="${p.codigo}"/></strong></td>
                            <td><c:out value="${p.nombre}"/></td>
                            <td><c:out value="${p.principioActivo}"/></td>
                            <td class="texto-derecha monto">${sol:moneda(p.precio)}</td>
                            <td class="texto-derecha">
                                ${p.stock}
                                <c:if test="${p.stock <= p.stockMinimo}">
                                    <span class="etiqueta-alerta">Stock bajo</span>
                                </c:if>
                            </td>
                            <td class="texto-derecha">${p.stockMinimo}</td>
                            <td><c:out value="${p.categoria.descripcion}"/></td>
                            <td>
                                <div class="grupo-botones">
                                    <button type="button" class="btn btn-secundario btn-pequeno"
                                            onclick="editarProducto(${p.idProducto}, '${p.codigo}', '${p.nombre}', '${p.principioActivo}', ${p.precio}, ${p.stock}, ${p.stockMinimo}, ${p.categoria.idCategoria})">
                                        Editar
                                    </button>
                                    <form action="${pageContext.request.contextPath}/productos" method="post"
                                          onsubmit="return confirmarEliminacion(this, 'Esta seguro de eliminar el producto ${p.nombre}?')">
                                        <input type="hidden" name="accion" value="eliminar">
                                        <input type="hidden" name="id" value="${p.idProducto}">
                                        <button type="submit" class="btn btn-peligro btn-pequeno">Eliminar</button>
                                    </form>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty productos}">
                        <tr>
                            <td colspan="9">No hay productos registrados.</td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>

    <div class="tarjeta">
        <h2 id="titulo-formulario">Nuevo producto</h2>
        <form action="${pageContext.request.contextPath}/productos" method="post" class="formulario" id="formulario-producto">
            <input type="hidden" name="accion" id="accion" value="agregar">
            <input type="hidden" name="id" id="id">

            <div class="campo">
                <label for="codigo">Codigo del producto</label>
                <input type="text" id="codigo" name="codigo" required
                       placeholder="Ej.: F001" maxlength="30">
            </div>
            <div class="campo">
                <label for="nombre">Nombre comercial</label>
                <input type="text" id="nombre" name="nombre" required
                       placeholder="Ej.: Panadol 500mg x20 tab" maxlength="100">
            </div>
            <div class="campo">
                <label for="principioActivo">Principio activo</label>
                <input type="text" id="principioActivo" name="principioActivo" required
                       placeholder="Ej.: Paracetamol" maxlength="100">
            </div>
            <div class="campo">
                <label for="precio">Precio (S/.)</label>
                <input type="number" id="precio" name="precio" step="0.01" min="0.01" required
                        placeholder="Ej.: 12.50">
            </div>
            <div class="campo">
                <label for="stock">Stock</label>
                <input type="number" id="stock" name="stock" step="1" min="0" required
                       placeholder="Ej.: 10">
            </div>
            <div class="campo">
                <label for="stockMinimo">Stock minimo (alerta de reposicion)</label>
                <input type="number" id="stockMinimo" name="stockMinimo" step="1" min="0" required
                       placeholder="Ej.: 5">
            </div>
            <div class="campo">
                <label for="idcategoria">Categoria</label>
                <select id="idcategoria" name="idcategoria" required>
                    <option value="">-- Seleccionar --</option>
                    <c:forEach var="cat" items="${categorias}">
                        <option value="${cat.idCategoria}"><c:out value="${cat.descripcion}"/></option>
                    </c:forEach>
                </select>
            </div>

            <div class="grupo-botones">
                <button type="submit" class="btn btn-primario" id="boton-guardar">Agregar producto</button>
                <button type="button" class="btn btn-secundario" onclick="nuevoProducto()">Cancelar edicion</button>
            </div>
        </form>
    </div>
</section>

<script>
    // Carga los datos del producto seleccionado hacia el formulario.
    function editarProducto(id, codigo, nombre, principioActivo, precio, stock, stockMinimo, idCategoria) {
        document.getElementById("accion").value = "modificar";
        document.getElementById("id").value = id;
        document.getElementById("codigo").value = codigo;
        document.getElementById("nombre").value = nombre;
        document.getElementById("principioActivo").value = principioActivo;
        document.getElementById("precio").value = precio;
        document.getElementById("stock").value = stock;
        document.getElementById("stockMinimo").value = stockMinimo;
        document.getElementById("idcategoria").value = idCategoria;
        document.getElementById("titulo-formulario").textContent = "Editando producto #" + id;
        document.getElementById("boton-guardar").textContent = "Guardar cambios";
        document.getElementById("nombre").focus();
    }

    function nuevoProducto() {
        document.getElementById("formulario-producto").reset();
        document.getElementById("accion").value = "agregar";
        document.getElementById("id").value = "";
        document.getElementById("titulo-formulario").textContent = "Nuevo producto";
        document.getElementById("boton-guardar").textContent = "Agregar producto";
    }
</script>

<jsp:include page="pie.jsp"/>