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

// Verificar autenticación y rol de admin
document.addEventListener('DOMContentLoaded', () => {
    if (!isAuthenticated()) {
        window.location.href = '/html/login.html';
        return;
    }
    
    // Verificar si el rol es admin
    const rol = localStorage.getItem('rol');
    if (rol !== 'ADMIN') {
        showError('❌ Acceso Denegado: Solo los administradores pueden acceder a esta página');
        setTimeout(() => {
            window.location.href = '/html/login.html';
        }, 2000);
        return;
    }
    
    // Cargar datos iniciales
    loadDashboardData();
    usuarioNombre.textContent = localStorage.getItem('username') || 'Usuario';
});

/**
 * Maneja el click en los menús
 */
function handleMenuClick(e) {
    e.preventDefault();
    
    // Remover clase active de todos los links
    menuLinks.forEach(link => link.classList.remove('active'));
    e.target.classList.add('active');
    
    // Obtener sección
    const section = e.target.getAttribute('data-section');
    showSection(section);
}

/**
 * Muestra una sección y oculta las demás
 */
function showSection(sectionId) {
    contentSections.forEach(section => section.classList.remove('active'));
    document.getElementById(sectionId).classList.add('active');
    
    // Cargar datos según la sección
    loadSectionData(sectionId);
}

/**
 * Carga los datos iniciales del dashboard
 */
async function loadDashboardData() {
    try {
        console.log('📊 Cargando datos del dashboard...');
        
        // Cargar totales
        try {
            const productosRes = await apiCall('GET', '/productos');
            document.getElementById('totalProductos').textContent = productosRes.length || 0;
        } catch (e) {
            console.warn('⚠️ Error cargando productos:', e);
            document.getElementById('totalProductos').textContent = '0';
        }

        try {
            const bodegasRes = await apiCall('GET', '/bodegas');
            document.getElementById('totalBodegas').textContent = bodegasRes.length || 0;
        } catch (e) {
            console.warn('⚠️ Error cargando bodegas:', e);
            document.getElementById('totalBodegas').textContent = '0';
        }

        try {
            const usuariosRes = await apiCall('GET', '/usuarios');
            document.getElementById('usuariosActivos').textContent = usuariosRes.length || 0;
        } catch (e) {
            console.warn('⚠️ Error cargando usuarios:', e);
            document.getElementById('usuariosActivos').textContent = '0';
        }

        try {
            const movimientosRes = await apiCall('GET', '/movimientos');
            document.getElementById('movimientosHoy').textContent = movimientosRes.length || 0;
        } catch (e) {
            console.warn('⚠️ Error cargando movimientos:', e);
            document.getElementById('movimientosHoy').textContent = '0';
        }

        console.log('✅ Dashboard cargado exitosamente');
    } catch (error) {
        console.error('❌ Error cargando datos del dashboard:', error);
    }
}

/**
 * Carga datos según la sección seleccionada
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
            case 'usuarios':
                await loadUsuarios();
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
 * Carga y muestra las bodegas
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
                <td>
                    <div class="table-actions">
                        <button class="btn-edit" onclick="editBodega(${bodega.id})">Editar</button>
                        <button class="btn-delete" onclick="deleteBodega(${bodega.id})">Eliminar</button>
                    </div>
                </td>
            </tr>
        `).join('');
    } catch (error) {
        console.error('❌ Error cargando bodegas:', error);
        tbody.innerHTML = '<tr><td colspan="5" class="text-center">Error al cargar bodegas</td></tr>';
    }
}

/**
 * Carga y muestra los productos
 */
async function loadProductos() {
    const tbody = document.getElementById('productosTableBody');
    
    try {
        const productos = await apiCall('GET', '/productos');
        
        if (!productos || productos.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="text-center">No hay productos registrados</td></tr>';
            return;
        }

        tbody.innerHTML = productos.map(producto => `
            <tr>
                <td>${producto.id || 'N/A'}</td>
                <td>${producto.nombre || 'N/A'}</td>
                <td>${producto.sku || 'N/A'}</td>
                <td>${producto.cantidad || 0}</td>
                <td>$${(producto.precio || 0).toFixed(2)}</td>
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
        tbody.innerHTML = '<tr><td colspan="6" class="text-center">Error al cargar productos</td></tr>';
    }
}

/**
 * Carga y muestra los movimientos
 */
async function loadMovimientos() {
    const tbody = document.getElementById('movimientosTableBody');
    
    try {
        const movimientos = await apiCall('GET', '/movimientos');
        
        if (!movimientos || movimientos.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="text-center">No hay movimientos registrados</td></tr>';
            return;
        }

        tbody.innerHTML = movimientos.map(movimiento => `
            <tr>
                <td>${movimiento.id || 'N/A'}</td>
                <td>${movimiento.tipo || 'N/A'}</td>
                <td>${movimiento.bodegaId || 'N/A'}</td>
                <td>${movimiento.cantidad || 0}</td>
                <td>${new Date(movimiento.fecha).toLocaleDateString('es-ES')}</td>
                <td>
                    <div class="table-actions">
                        <button class="btn-edit" onclick="editMovimiento(${movimiento.id})">Ver</button>
                    </div>
                </td>
            </tr>
        `).join('');
    } catch (error) {
        console.error('❌ Error cargando movimientos:', error);
        tbody.innerHTML = '<tr><td colspan="6" class="text-center">Error al cargar movimientos</td></tr>';
    }
}

/**
 * Carga y muestra los usuarios
 */
async function loadUsuarios() {
    const tbody = document.getElementById('usuariosTableBody');
    
    try {
        const usuarios = await apiCall('GET', '/usuarios');
        
        if (!usuarios || usuarios.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" class="text-center">No hay usuarios registrados</td></tr>';
            return;
        }

        tbody.innerHTML = usuarios.map(usuario => `
            <tr>
                <td>${usuario.id || 'N/A'}</td>
                <td>${usuario.username || 'N/A'}</td>
                <td>${usuario.nombreCompleto || 'N/A'}</td>
                <td>${usuario.rol || 'N/A'}</td>
                <td>
                    <div class="table-actions">
                        <button class="btn-edit" onclick="editUsuario(${usuario.id})">Editar</button>
                        <button class="btn-delete" onclick="deleteUsuario(${usuario.id})">Eliminar</button>
                    </div>
                </td>
            </tr>
        `).join('');
    } catch (error) {
        console.error('❌ Error cargando usuarios:', error);
        tbody.innerHTML = '<tr><td colspan="5" class="text-center">Error al cargar usuarios</td></tr>';
    }
}

/**
 * Carga y muestra la auditoría
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
                <td>${audit.usuario?.username || audit.usuario?.nombreCompleto || 'N/A'}</td>
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
 * Realiza una llamada a la API con token
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

    console.log(`📡 ${method} ${API_URL}${endpoint}`);
    console.log('🔐 Headers:', options.headers);

    const response = await fetch(`${API_URL}${endpoint}`, options);

    console.log(`✅ Respuesta: ${response.status}`);

    // Si es 401, token expiró
    if (response.status === 401) {
        console.error('❌ Token expirado');
        logout();
        throw new Error('Token expirado');
    }

    if (!response.ok) {
        const errorText = await response.text();
        console.error('❌ Error:', errorText);
        throw new Error(`Error ${response.status}: ${errorText}`);
    }

    const data = await response.json();
    console.log('📦 Datos recibidos:', data);
    return data;
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

/**
 * Obtiene el token del localStorage
 */
function getToken() {
    return localStorage.getItem('token');
}

/**
 * Verifica si está autenticado
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

/**
 * Funciones para acciones (eliminar, editar, etc)
 */
function editBodega(id) { 
    alert('Editar bodega: ' + id); 
}
async function deleteBodega(id) { 
    if (confirm('¿Estás seguro de que quieres eliminar esta bodega?')) {
        try {
            await apiCall('DELETE', `/bodegas/${id}`);
            showSuccess('✅ Bodega eliminada exitosamente');
            loadBodegas();
        } catch (error) {
            showError('❌ Error eliminando bodega: ' + error.message);
        }
    }
}

function editProducto(id) { 
    alert('Editar producto: ' + id); 
}

async function deleteProducto(id) { 
    if (confirm('¿Estás seguro de que quieres eliminar este producto?')) {
        try {
            await apiCall('DELETE', `/productos/${id}`);
            showSuccess('✅ Producto eliminado exitosamente');
            loadProductos();
        } catch (error) {
            showError('❌ Error eliminando producto: ' + error.message);
        }
    }
}

function editMovimiento(id) { 
    alert('Ver movimiento: ' + id); 
}

function editUsuario(id) { 
    alert('Editar usuario: ' + id); 
}

async function deleteUsuario(id) { 
    if (confirm('¿Estás seguro de que quieres eliminar este usuario?')) {
        try {
            await apiCall('DELETE', `/usuarios/${id}`);
            showSuccess('✅ Usuario eliminado exitosamente');
            loadUsuarios();
        } catch (error) {
            showError('❌ Error eliminando usuario: ' + error.message);
        }
    }
}

/* ------------------ Modales y CRUD handlers ------------------ */

function showSuccess(message) {
    const successDiv = document.createElement('div');
    successDiv.className = 'success-message';
    successDiv.textContent = message;
    successDiv.style.cssText = 'position: fixed; top: 20px; right: 20px; background: #4CAF50; color: white; padding: 12px 16px; border-radius: 6px; z-index: 1100; box-shadow: 0 6px 18px rgba(0,0,0,0.12);';
    document.body.appendChild(successDiv);
    setTimeout(() => successDiv.remove(), 3000);
}

// Open/close utility
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

// Wire buttons and form handlers after DOM ready
document.addEventListener('DOMContentLoaded', () => {
    // Nueva buttons
    const btnNuevaBodega = document.getElementById('agregarBodegaBtn');
    if (btnNuevaBodega) btnNuevaBodega.addEventListener('click', () => {
        // reset form
        const form = document.getElementById('formBodega');
        form.reset();
        document.getElementById('bodegaId').value = '';
        document.querySelector('#modalBodega h2').textContent = 'Nueva Bodega';
        openModal('modalBodega');
    });

    const btnNuevoProducto = document.getElementById('agregarProductoBtn');
    if (btnNuevoProducto) btnNuevoProducto.addEventListener('click', () => {
        const form = document.getElementById('formProducto'); form.reset(); document.getElementById('productoId').value = ''; document.querySelector('#modalProducto h2').textContent = 'Nuevo Producto'; openModal('modalProducto');
    });

    const btnNuevoUsuario = document.getElementById('agregarUsuarioBtn');
    if (btnNuevoUsuario) btnNuevoUsuario.addEventListener('click', () => {
        const form = document.getElementById('formUsuario'); form.reset(); document.getElementById('usuarioId').value = ''; document.querySelector('#modalUsuario h2').textContent = 'Nuevo Usuario'; openModal('modalUsuario');
    });

    // Close icons
    document.querySelectorAll('.close').forEach(el => {
        el.addEventListener('click', (e) => {
            const mid = e.target.getAttribute('data-close');
            if (mid) closeModal(mid);
            else e.target.closest('.modal').classList.remove('show');
        });
    });

    // Close when clicking overlay
    document.querySelectorAll('.modal').forEach(modal => {
        modal.addEventListener('click', (e) => {
            if (e.target === modal) modal.classList.remove('show');
        });
    });

    // Form submissions
    const formBodega = document.getElementById('formBodega');
    if (formBodega) formBodega.addEventListener('submit', async (e) => {
        e.preventDefault();
        const id = document.getElementById('bodegaId').value;
        const nombre = document.getElementById('bodegaNombre').value;
        const ubicacion = document.getElementById('bodegaUbicacion').value;
        const capacidad = parseInt(document.getElementById('bodegaCapacidad').value,10);
        try {
            const payload = { nombre, ubicacion, capacidad };
            if (id) await apiCall('PUT', `/bodegas/${id}`, payload);
            else await apiCall('POST', '/bodegas', payload);
            closeModal('modalBodega');
            showSuccess('✅ Bodega guardada');
            loadBodegas();
        } catch (err) { showError('❌ ' + err.message); }
    });

    const formProducto = document.getElementById('formProducto');
    if (formProducto) formProducto.addEventListener('submit', async (e) => {
        e.preventDefault();
        const id = document.getElementById('productoId').value;
        const nombre = document.getElementById('productoNombre').value;
        const sku = document.getElementById('productoSku').value;
        const cantidad = parseInt(document.getElementById('productoCantidad').value,10);
        const precio = parseFloat(document.getElementById('productoPrecio').value);
        try {
            const payload = { nombre, sku, cantidad, precio };
            if (id) await apiCall('PUT', `/productos/${id}`, payload);
            else await apiCall('POST', '/productos', payload);
            closeModal('modalProducto');
            showSuccess('✅ Producto guardado');
            loadProductos();
        } catch (err) { showError('❌ ' + err.message); }
    });

    const formUsuario = document.getElementById('formUsuario');
    if (formUsuario) formUsuario.addEventListener('submit', async (e) => {
        e.preventDefault();
        const id = document.getElementById('usuarioId').value;
        const username = document.getElementById('usuarioUsername').value;
        const nombreCompleto = document.getElementById('usuarioNombreCompleto').value;
        const rol = document.getElementById('usuarioRol').value;
        const password = document.getElementById('usuarioPassword').value;
        try {
            const payload = { username, nombreCompleto, rol };
            if (!id) {
                if (!password) { showError('❌ La contraseña es requerida'); return; }
                payload.password = password;
                await apiCall('POST', '/usuarios', payload);
            } else {
                if (password) payload.password = password;
                await apiCall('PUT', `/usuarios/${id}`, payload);
            }
            closeModal('modalUsuario');
            showSuccess('✅ Usuario guardado');
            loadUsuarios();
        } catch (err) { showError('❌ ' + err.message); }
    });
});

// Helper to open modals for editing (used by table buttons)
async function editBodega(id) {
    try {
        const b = await apiCall('GET', `/bodegas/${id}`);
        document.getElementById('bodegaId').value = b.id || '';
        document.getElementById('bodegaNombre').value = b.nombre || '';
        document.getElementById('bodegaUbicacion').value = b.ubicacion || '';
        document.getElementById('bodegaCapacidad').value = b.capacidad || '';
        document.querySelector('#modalBodega h2').textContent = 'Editar Bodega';
        openModal('modalBodega');
    } catch (err) { showError('❌ ' + err.message); }
}

async function editProducto(id) {
    try {
        const p = await apiCall('GET', `/productos/${id}`);
        document.getElementById('productoId').value = p.id || '';
        document.getElementById('productoNombre').value = p.nombre || '';
        document.getElementById('productoSku').value = p.sku || '';
        document.getElementById('productoCantidad').value = p.cantidad || '';
        document.getElementById('productoPrecio').value = p.precio || '';
        document.querySelector('#modalProducto h2').textContent = 'Editar Producto';
        openModal('modalProducto');
    } catch (err) { showError('❌ ' + err.message); }
}

async function editUsuario(id) {
    try {
        const u = await apiCall('GET', `/usuarios/${id}`);
        document.getElementById('usuarioId').value = u.id || '';
        document.getElementById('usuarioUsername').value = u.username || '';
        document.getElementById('usuarioNombreCompleto').value = u.nombreCompleto || '';
        document.getElementById('usuarioRol').value = u.rol || '';
        document.getElementById('usuarioPassword').value = '';
        document.querySelector('#modalUsuario h2').textContent = 'Editar Usuario';
        openModal('modalUsuario');
    } catch (err) { showError('❌ ' + err.message); }
}

async function deleteBodega(id) { 
    if (confirm('¿Estás seguro de que quieres eliminar esta bodega?')) {
        try {
            await apiCall('DELETE', `/bodegas/${id}`);
            showSuccess('✅ Bodega eliminada exitosamente');
            loadBodegas();
        } catch (error) { showError('❌ Error eliminando bodega: ' + error.message); }
    }
}

async function deleteProducto(id) { 
    if (confirm('¿Estás seguro de que quieres eliminar este producto?')) {
        try {
            await apiCall('DELETE', `/productos/${id}`);
            showSuccess('✅ Producto eliminado exitosamente');
            loadProductos();
        } catch (error) { showError('❌ Error eliminando producto: ' + error.message); }
    }
}

async function deleteUsuario(id) { 
    if (confirm('¿Estás seguro de que quieres eliminar este usuario?')) {
        try {
            await apiCall('DELETE', `/usuarios/${id}`);
            showSuccess('✅ Usuario eliminado exitosamente');
            loadUsuarios();
        } catch (error) { showError('❌ Error eliminando usuario: ' + error.message); }
    }
}
