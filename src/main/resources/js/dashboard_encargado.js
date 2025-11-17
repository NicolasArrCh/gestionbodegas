// Configuración
const API_URL = 'http://localhost:8080/api';

// Elementos del DOM
const menuLinks = document.querySelectorAll('.menu-link');
const contentSections = document.querySelectorAll('.content-section');
const logoutBtn = document.getElementById('logoutBtn');
const usuarioNombre = document.getElementById('usuarioNombre');
const errorMessage = document.getElementById('errorMessage');

// Event listeners
menuLinks.forEach(link => {
    link.addEventListener('click', handleMenuClick);
});

logoutBtn.addEventListener('click', handleLogout);

// Verificar autenticación y rol
document.addEventListener('DOMContentLoaded', () => {
    if (!isAuthenticated()) {
        window.location.href = '/html/login.html';
        return;
    }
    
    // Verificar si el rol es ENCARGADO
    const rol = localStorage.getItem('rol');
    if (rol !== 'ENCARGADO') {
        showError('❌ Acceso Denegado: Esta página es solo para encargados');
        setTimeout(() => {
            redirectByRole(rol);
        }, 2000);
        return;
    }
    
    // Cargar datos iniciales
    loadDashboardData();
    usuarioNombre.textContent = localStorage.getItem('username') || 'Usuario';
});

/**
 * Redirige según el rol
 */
function redirectByRole(rol) {
    if (rol === 'ADMIN') {
        window.location.href = '/html/dashboard_admin.html';
    } else if (rol === 'OPERADOR') {
        window.location.href = '/html/dashboard_operador.html';
    } else {
        window.location.href = '/html/login.html';
    }
}

/**
 * Maneja el click en los menús
 */
function handleMenuClick(e) {
    e.preventDefault();
    
    menuLinks.forEach(link => link.classList.remove('active'));
    e.target.classList.add('active');
    
    const section = e.target.getAttribute('data-section');
    showSection(section);
}

/**
 * Muestra una sección
 */
function showSection(sectionId) {
    contentSections.forEach(section => section.classList.remove('active'));
    document.getElementById(sectionId).classList.add('active');
    
    loadSectionData(sectionId);
}

/**
 * Carga los datos iniciales del dashboard
 */
async function loadDashboardData() {
    try {
        const productos = await apiCall('GET', '/productos');
        document.getElementById('totalProductos').textContent = productos.length || 0;

        const bodegas = await apiCall('GET', '/bodegas');
        document.getElementById('totalBodegas').textContent = bodegas.length || 0;

        const movimientos = await apiCall('GET', '/movimientos');
        document.getElementById('movimientosHoy').textContent = movimientos.length || 0;

        // Productos con stock bajo
        const stockBajo = await apiCall('GET', '/productos/stock-bajo/10');
        document.getElementById('stockBajo').textContent = stockBajo.length || 0;

    } catch (error) {
        console.error('❌ Error cargando datos del dashboard:', error);
    }
}

/**
 * Carga datos según la sección
 */
async function loadSectionData(sectionId) {
    try {
        switch(sectionId) {
            case 'bodegas':
                await loadBodegas();
                break;
            case 'productos':
                await loadProductos();
                break;
            case 'movimientos':
                await loadMovimientos();
                break;
            case 'auditoria':
                await loadAuditoria();
                break;
        }
    } catch (error) {
        console.error('Error cargando datos:', error);
        showError('Error al cargar los datos');
    }
}

/**
 * Carga las bodegas (solo lectura)
 */
async function loadBodegas() {
    const tbody = document.getElementById('bodegasTableBody');
    
    try {
        const bodegas = await apiCall('GET', '/bodegas');
        
        if (!bodegas || bodegas.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" class="text-center">No hay bodegas registradas</td></tr>';
            return;
        }

        tbody.innerHTML = bodegas.map(bodega => `
            <tr>
                <td>${bodega.id || 'N/A'}</td>
                <td>${bodega.nombre || 'N/A'}</td>
                <td>${bodega.ubicacion || 'N/A'}</td>
                <td>${bodega.capacidad || 'N/A'}</td>
                <td>${bodega.encargado?.username || 'N/A'}</td>
            </tr>
        `).join('');
    } catch (error) {
        console.error('❌ Error cargando bodegas:', error);
        tbody.innerHTML = '<tr><td colspan="5" class="text-center">Error al cargar bodegas</td></tr>';
    }
}

/**
 * Carga los productos
 */
async function loadProductos() {
    const tbody = document.getElementById('productosTableBody');
    
    try {
        const productos = await apiCall('GET', '/productos');
        
        if (!productos || productos.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" class="text-center">No hay productos registrados</td></tr>';
            return;
        }

        tbody.innerHTML = productos.map(producto => `
            <tr>
                <td>${producto.id || 'N/A'}</td>
                <td>${producto.nombre || 'N/A'}</td>
                <td>${producto.categoria || 'N/A'}</td>
                <td>${producto.stock || 0}</td>
                <td>$${(producto.precio || 0).toFixed(2)}</td>
                <td>${producto.bodega?.nombre || 'N/A'}</td>
                <td>
                    <div class="table-actions">
                        <button class="btn-edit" onclick="editProducto(${producto.id})">Editar</button>
                        <button class="btn-delete" onclick="deleteProducto(${producto.id})">Eliminar</button>
                    </div>
                </td>
            </tr>
        `).join('');
    } catch (error) {
        console.error('❌ Error cargando productos:', error);
        tbody.innerHTML = '<tr><td colspan="7" class="text-center">Error al cargar productos</td></tr>';
    }
}

/**
 * Carga los movimientos
 */
async function loadMovimientos() {
    const tbody = document.getElementById('movimientosTableBody');
    
    try {
        const movimientos = await apiCall('GET', '/movimientos');
        
        if (!movimientos || movimientos.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="text-center">No hay movimientos registrados</td></tr>';
            return;
        }

        tbody.innerHTML = movimientos.map(movimiento => {
            let bodegas = '';
            if (movimiento.tipo === 'ENTRADA') {
                bodegas = `Destino: ${movimiento.bodegaDestino?.nombre || 'N/A'}`;
            } else if (movimiento.tipo === 'SALIDA') {
                bodegas = `Origen: ${movimiento.bodegaOrigen?.nombre || 'N/A'}`;
            } else if (movimiento.tipo === 'TRANSFERENCIA') {
                bodegas = `${movimiento.bodegaOrigen?.nombre || 'N/A'} → ${movimiento.bodegaDestino?.nombre || 'N/A'}`;
            }
            
            return `
            <tr>
                <td>${movimiento.id || 'N/A'}</td>
                <td>${movimiento.tipo || 'N/A'}</td>
                <td>${bodegas}</td>
                <td>${movimiento.usuario?.username || 'N/A'}</td>
                <td>${new Date(movimiento.fecha).toLocaleDateString('es-ES')}</td>
                <td>
                    <div class="table-actions">
                        <button class="btn-edit" onclick="editMovimiento(${movimiento.id})">Ver</button>
                    </div>
                </td>
            </tr>
        `}).join('');
    } catch (error) {
        console.error('❌ Error cargando movimientos:', error);
        tbody.innerHTML = '<tr><td colspan="6" class="text-center">Error al cargar movimientos</td></tr>';
    }
}

/**
 * Carga la auditoría
 */
async function loadAuditoria() {
    const tbody = document.getElementById('auditoriaTableBody');
    
    try {
        const auditoria = await apiCall('GET', '/auditorias');
        
        if (!auditoria || auditoria.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" class="text-center">No hay registros de auditoría</td></tr>';
            return;
        }

        tbody.innerHTML = auditoria.map(audit => `
            <tr>
                <td>${audit.id || 'N/A'}</td>
                <td>${audit.usuario?.username || 'N/A'}</td>
                <td>${audit.tipoOperacion || 'N/A'}</td>
                <td>${audit.entidadAfectada || 'N/A'}</td>
                <td>${audit.fechaHora ? new Date(audit.fechaHora).toLocaleDateString('es-ES') : 'N/A'}</td>
            </tr>
        `).join('');
    } catch (error) {
        console.error('❌ Error cargando auditoría:', error);
        tbody.innerHTML = '<tr><td colspan="5" class="text-center">Error al cargar auditoría</td></tr>';
    }
}

/**
 * Realiza una llamada a la API
 */
async function apiCall(method, endpoint, body = null) {
    const token = getToken();
    
    if (!token) {
        throw new Error('No hay token disponible');
    }

    const options = {
        method: method,
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        }
    };

    if (body) {
        options.body = JSON.stringify(body);
    }

    const response = await fetch(`${API_URL}${endpoint}`, options);

    if (response.status === 401) {
        logout();
        throw new Error('Token expirado');
    }

    if (response.status === 204) {
        return null;
    }

    if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`Error ${response.status}: ${errorText}`);
    }

    return await response.json();
}

/**
 * Muestra un mensaje de error
 */
function showError(message) {
    errorMessage.textContent = message;
    errorMessage.classList.add('show');
    
    setTimeout(() => {
        errorMessage.classList.remove('show');
    }, 5000);
}

function showSuccess(message) {
    const successDiv = document.createElement('div');
    successDiv.className = 'success-message';
    successDiv.textContent = message;
    successDiv.style.cssText = 'position: fixed; top: 20px; right: 20px; background: #4CAF50; color: white; padding: 12px 16px; border-radius: 6px; z-index: 1100; box-shadow: 0 6px 18px rgba(0,0,0,0.12);';
    document.body.appendChild(successDiv);
    setTimeout(() => successDiv.remove(), 3000);
}

/**
 * Obtiene el token
 */
function getToken() {
    return localStorage.getItem('token');
}

/**
 * Verifica autenticación
 */
function isAuthenticated() {
    return !!getToken();
}

/**
 * Maneja el logout
 */
function handleLogout() {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('rol');
    window.location.href = '/html/login.html';
}

function logout() {
    handleLogout();
}

// Funciones para modales y acciones
function openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (!modal) return;
    modal.classList.add('show');
    modal.setAttribute('aria-hidden','false');
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (!modal) return;
    modal.classList.remove('show');
    modal.setAttribute('aria-hidden','true');
}

async function deleteProducto(id) { 
    if (confirm('¿Estás seguro de que quieres eliminar este producto?')) {
        try {
            await apiCall('DELETE', `/productos/${id}`);
            showSuccess('✅ Producto eliminado exitosamente');
            await new Promise(resolve => setTimeout(resolve, 500));
            loadProductos();
        } catch (error) {
            showError('❌ Error eliminando producto: ' + error.message);
        }
    }
}

async function editProducto(id) {
    try {
        const p = await apiCall('GET', `/productos/${id}`);
        
        const bodegas = await apiCall('GET', '/bodegas');
        const select = document.getElementById('productoBodegaId');
        select.innerHTML = '<option value="">Selecciona una bodega</option>';
        for (const bodega of bodegas) {
            const option = document.createElement('option');
            option.value = bodega.id;
            option.textContent = bodega.nombre;
            if (p.bodega?.id === bodega.id) {
                option.selected = true;
            }
            select.appendChild(option);
        }
        
        document.getElementById('productoId').value = p.id || '';
        document.getElementById('productoNombre').value = p.nombre || '';
        document.getElementById('productoCategoria').value = p.categoria || '';
        document.getElementById('productoStock').value = p.stock || '';
        document.getElementById('productoPrecio').value = p.precio || '';
        document.querySelector('#modalProducto h2').textContent = 'Editar Producto';
        openModal('modalProducto');
    } catch (err) { showError('❌ ' + err.message); }
}

async function editMovimiento(id) {
    try {
        const m = await apiCall('GET', `/movimientos/${id}`);
        alert(`📋 Movimiento ID: ${m.id}\n\nTipo: ${m.tipo}\nFecha: ${new Date(m.fecha).toLocaleString('es-ES')}\nUsuario: ${m.usuario?.username || 'N/A'}\n\nBodega Origen: ${m.bodegaOrigen?.nombre || 'N/A'}\nBodega Destino: ${m.bodegaDestino?.nombre || 'N/A'}`);
    } catch (err) { showError('❌ ' + err.message); }
}

// Event listeners para modales
document.addEventListener('DOMContentLoaded', () => {
    const btnNuevoProducto = document.getElementById('agregarProductoBtn');
    if (btnNuevoProducto) btnNuevoProducto.addEventListener('click', async () => {
        const form = document.getElementById('formProducto'); 
        form.reset(); 
        document.getElementById('productoId').value = ''; 
        document.querySelector('#modalProducto h2').textContent = 'Nuevo Producto'; 
        
        try {
            const bodegas = await apiCall('GET', '/bodegas');
            const select = document.getElementById('productoBodegaId');
            select.innerHTML = '<option value="">Selecciona una bodega</option>';
            bodegas.forEach(bodega => {
                const option = document.createElement('option');
                option.value = bodega.id;
                option.textContent = bodega.nombre;
                select.appendChild(option);
            });
        } catch (err) {
            console.error('Error cargando bodegas:', err);
        }
        
        openModal('modalProducto');
    });

    const btnNuevoMovimiento = document.getElementById('agregarMovimientoBtn');
    if (btnNuevoMovimiento) btnNuevoMovimiento.addEventListener('click', async () => {
        const form = document.getElementById('formMovimiento');
        form.reset();
        document.getElementById('movimientoId').value = '';
        document.querySelector('#modalMovimiento h2').textContent = 'Nuevo Movimiento';

        try {
            const bodegas = await apiCall('GET', '/bodegas');
            const productos = await apiCall('GET', '/productos');

            const origen = document.getElementById('movimientoBodegaOrigen');
            const destino = document.getElementById('movimientoBodegaDestino');
            origen.innerHTML = '<option value="">Ninguna</option>';
            destino.innerHTML = '<option value="">Ninguna</option>';
            
            for (const b of bodegas) {
                const o = document.createElement('option'); 
                o.value = b.id; 
                o.textContent = b.nombre; 
                origen.appendChild(o);
                
                const d = document.createElement('option'); 
                d.value = b.id; 
                d.textContent = b.nombre; 
                destino.appendChild(d);
            }

            const prodSelect = document.getElementById('movimientoProducto');
            prodSelect.innerHTML = '<option value="">Selecciona un producto</option>';
            for (const p of productos) {
                const opt = document.createElement('option'); 
                opt.value = p.id; 
                opt.textContent = p.nombre; 
                prodSelect.appendChild(opt);
            }
        } catch (err) {
            console.error('Error cargando datos:', err);
        }

        openModal('modalMovimiento');
    });

    document.querySelectorAll('.close').forEach(el => {
        el.addEventListener('click', (e) => {
            const mid = e.target.getAttribute('data-close');
            if (mid) closeModal(mid);
        });
    });

    document.querySelectorAll('.modal').forEach(modal => {
        modal.addEventListener('click', (e) => {
            if (e.target === modal) modal.classList.remove('show');
        });
    });

    const formProducto = document.getElementById('formProducto');
    if (formProducto) formProducto.addEventListener('submit', async (e) => {
        e.preventDefault();
        const id = document.getElementById('productoId').value;
        const nombre = document.getElementById('productoNombre').value;
        const categoria = document.getElementById('productoCategoria').value;
        const stock = Number.parseInt(document.getElementById('productoStock').value, 10);
        const precio = Number.parseFloat(document.getElementById('productoPrecio').value);
        const bodegaId = Number.parseInt(document.getElementById('productoBodegaId').value, 10);
        try {
            const bodega = { id: bodegaId };
            const payload = { nombre, categoria, stock, precio, bodega };
            if (id) await apiCall('PUT', `/productos/${id}`, payload);
            else await apiCall('POST', '/productos', payload);
            closeModal('modalProducto');
            showSuccess('✅ Producto guardado');
            loadProductos();
        } catch (err) { showError('❌ ' + err.message); }
    });

    const formMovimiento = document.getElementById('formMovimiento');
    if (formMovimiento) {
        formMovimiento.removeEventListener && formMovimiento.removeEventListener('submit', window.__movimientoSubmitHandler);

        const movimientoSubmitHandler = async (e) => {
            e.preventDefault();
            const submitBtn = formMovimiento.querySelector('button[type="submit"]');
            if (submitBtn) submitBtn.disabled = true;

            try {
                const tipo = document.getElementById('movimientoTipo').value;
                const origenVal = document.getElementById('movimientoBodegaOrigen').value;
                const destinoVal = document.getElementById('movimientoBodegaDestino').value;
                const productoVal = document.getElementById('movimientoProducto').value;
                const cantidadVal = document.getElementById('movimientoCantidad').value;

                if (!tipo) { showError('❌ Selecciona tipo'); return; }
                if (!productoVal) { showError('❌ Selecciona producto'); return; }
                if (!cantidadVal || Number.parseInt(cantidadVal, 10) <= 0) { showError('❌ Cantidad inválida'); return; }

                if (tipo === 'ENTRADA' && !destinoVal) { showError('❌ ENTRADA necesita destino'); return; }
                if (tipo === 'SALIDA' && !origenVal) { showError('❌ SALIDA necesita origen'); return; }
                if (tipo === 'TRANSFERENCIA' && (!origenVal || !destinoVal)) { showError('❌ TRANSFERENCIA necesita ambos'); return; }

                const username = localStorage.getItem('username');
                const usuario = await apiCall('GET', `/usuarios/username/${encodeURIComponent(username)}`);

                const movimientoPayload = {
                    tipo: tipo,
                    usuario: { id: usuario.id },
                    bodegaOrigen: origenVal ? { id: Number.parseInt(origenVal, 10) } : null,
                    bodegaDestino: destinoVal ? { id: Number.parseInt(destinoVal, 10) } : null
                };

                const movimientoCreado = await apiCall('POST', '/movimientos', movimientoPayload);

                const detallePayload = {
                    movimiento: { id: movimientoCreado.id },
                    producto: { id: Number.parseInt(productoVal, 10) },
                    cantidad: Number.parseInt(cantidadVal, 10)
                };

                await apiCall('POST', '/detalle-movimientos', detallePayload);

                closeModal('modalMovimiento');
                showSuccess('✅ Movimiento guardado exitosamente');
                await loadMovimientos();

            } catch (err) {
                showError('❌ ' + (err.message || 'Error desconocido'));
            } finally {
                if (submitBtn) submitBtn.disabled = false;
            }
        };

        window.__movimientoSubmitHandler = movimientoSubmitHandler;
        formMovimiento.addEventListener('submit', movimientoSubmitHandler);
    }
});