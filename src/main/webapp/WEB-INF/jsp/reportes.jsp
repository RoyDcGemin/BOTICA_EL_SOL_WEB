<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="sol" uri="http://elsol/funciones" %>
<jsp:include page="cabecera.jsp">
    <jsp:param name="titulo" value="Reportes"/>
</jsp:include>
<%@ include file="flash.jsp" %>

<h1 class="titulo-pagina">Reportes de ventas</h1>

<!-- Tarjetas de indicadores (TPRV, %ERR, TPV, %OK) sobre el filtro actual. -->
<section class="parrilla-menu" aria-label="Indicadores">
    <div class="tarjeta">
        <h2>Tiempo promedio (TPRV)</h2>
        <p class="total-facturacion">${tprv} seg/venta</p>
    </div>
    <div class="tarjeta">
        <h2>Errores (%ERR)</h2>
        <p class="total-facturacion">${porcErr} %</p>
    </div>
    <div class="tarjeta">
        <h2>Ticket promedio (TPV)</h2>
        <p class="total-facturacion">${sol:moneda(tpv)}</p>
    </div>
    <div class="tarjeta">
        <h2>Ventas correctas (%OK)</h2>
        <p class="total-facturacion">${porcOk} %</p>
    </div>
</section>

<!-- VISTA REPORTES: filtros por fecha, cajero y producto. -->
<section class="panel-lateral">
    <div class="tarjeta">
        <h2>Filtros</h2>
        <form action="${pageContext.request.contextPath}/reportes" method="get" class="formulario">
            <div class="campo">
                <label for="desde">Desde</label>
                <input type="date" id="desde" name="desde" value="${desde}">
            </div>
            <div class="campo">
                <label for="hasta">Hasta</label>
                <input type="date" id="hasta" name="hasta" value="${hasta}">
            </div>
            <div class="campo">
                <label for="cajero">Cajero</label>
                <select id="cajero" name="cajero">
                    <option value="0">Todos</option>
                    <c:forEach var="u" items="${cajeros}">
                        <option value="${u.idusuario}" <c:if test="${u.idusuario == idCajero}">selected</c:if>>
                            <c:out value="${u.nombre}"/>
                        </option>
                    </c:forEach>
                </select>
            </div>
            <div class="campo">
                <label for="producto">Producto</label>
                <select id="producto" name="producto">
                    <option value="0">Todos</option>
                    <c:forEach var="p" items="${productos}">
                        <option value="${p.idProducto}" <c:if test="${p.idProducto == idProducto}">selected</c:if>>
                            <c:out value="${p.nombre}"/>
                        </option>
                    </c:forEach>
                </select>
            </div>
            <div class="grupo-botones">
                <button type="submit" class="btn btn-primario">Filtrar</button>
            </div>
        </form>

        <h2>Ventas (${ventas.size()} resultados)</h2>
        <div class="envoltorio-tabla">
            <table class="tabla">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Fecha</th>
                        <th>Cajero</th>
                        <th>Cliente</th>
                        <th>Estado</th>
                        <th class="texto-derecha">Total</th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="v" items="${ventas}">
                        <tr>
                            <td>${v.idVenta}</td>
                            <td>${v.fecha}</td>
                            <td><c:out value="${v.usuario.nombre}"/></td>
                            <td><c:out value="${v.nombreCliente}"/> (${v.dniCliente})</td>
                            <td><c:out value="${v.estado}"/></td>
                            <td class="texto-derecha monto">${sol:moneda(v.total)}</td>
                            <td>
                                <div class="grupo-botones">
                                    <a class="btn btn-secundario btn-pequeno"
                                       href="${pageContext.request.contextPath}/reportes?ver=${v.idVenta}&desde=${desde}&hasta=${hasta}&cajero=${idCajero}&producto=${idProducto}">Ver detalle</a>
                                    <c:if test="${v.estado == 'VIGENTE'}">
                                        <form action="${pageContext.request.contextPath}/reportes" method="post"
                                              onsubmit="return confirmarEliminacion(this, 'Anular la venta N° ${v.idVenta}?')">
                                            <input type="hidden" name="accion" value="anular">
                                            <input type="hidden" name="id" value="${v.idVenta}">
                                            <button type="submit" class="btn btn-peligro btn-pequeno">Anular</button>
                                        </form>
                                    </c:if>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty ventas}">
                        <tr>
                            <td colspan="7">No hay ventas para este filtro.</td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>

    <div class="tarjeta">
        <h2>Detalle de la venta</h2>
        <c:choose>
            <c:when test="${detalle != null}">
                <p><strong>VENTA N° ${detalle.idVenta}</strong> · ${detalle.fecha}</p>
                <p>Cliente: <c:out value="${detalle.nombreCliente}"/> (${detalle.dniCliente})</p>
                <p>Estado: <c:out value="${detalle.estado}"/> · Pago: <c:out value="${detalle.metodoPago}"/></p>

                <div class="envoltorio-tabla">
                    <table class="tabla">
                        <thead>
                            <tr>
                                <th>Producto</th>
                                <th class="texto-derecha">Cant.</th>
                                <th class="texto-derecha">Precio</th>
                                <th class="texto-derecha">Subtotal</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="d" items="${detalle.detalles}">
                                <tr>
                                    <td><c:out value="${d.producto.nombre}"/></td>
                                    <td class="texto-derecha">${d.cantidad}</td>
                                    <td class="texto-derecha monto">${sol:moneda(d.producto.precio)}</td>
                                    <td class="texto-derecha monto">${sol:moneda(d.subtotal)}</td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
                <p class="total-facturacion">
                    Descuento: ${sol:moneda(detalle.descuento)} ·
                    TOTAL: ${sol:moneda(detalle.total)}
                </p>
                <a href="${pageContext.request.contextPath}/comprobante?id=${detalle.idVenta}" class="btn btn-secundario">
                    Descargar PDF
                </a>
            </c:when>
            <c:otherwise>
                <p class="ayuda-campo">Seleccione una venta de la lista para ver su detalle.</p>
            </c:otherwise>
        </c:choose>
    </div>
</section>

<jsp:include page="pie.jsp"/>
