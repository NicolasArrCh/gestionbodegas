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
    
    // Verificar si el rol es OPERADOR
    const rol = localStorage.getItem('rol');
    if (rol !== 'OPERADOR') {
        showError('❌ Acceso Denegado: Esta página es solo para operadores');
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
    } else if (rol === 'ENCARGADO') {
        window.location.href = '/html/dashboard_encargado.html';
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

        // Obtener movimientos del usuario actual
        const username = localStorage.getItem('username');
        const usuario = await apiCall('GET', `/usuarios/username/${encodeURIComponent(username)}`);
        const movimientos = await apiCall('GET', `/movimientos/usuario/${usuario.id}`);
        document.getElementById('misMovimientos').textContent = movimientos.length || 0;

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
            case 'productos':
                await loadProductos();
                break;
            case 'movimientos':
                await loadMisMovimientos();
                break;
        }
    } catch (error) {
        console.error('Error cargando datos:', error);
        showError('Error al cargar los datos');
    }
}

/**
 * Carga los productos (solo lectura)
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
                <td>${producto.categoria || 'N/A'}</td>
                <td>${producto.stock || 0}</td>
                <td>$${(producto.precio || 0).toFixed(2)}</td>
                <td>${producto.bodega?.nombre || 'N/A'}</td>
            </tr>
        `).join('');
    } catch (error) {
        console.error('❌ Error cargando productos:', error);
        tbody.innerHTML = '<tr><td colspan="6" class="text-center">Error al cargar productos</td></tr>';
    }
}

/**
 * Carga solo los movimientos del usuario actual
 */
async function loadMisMovimientos() {
    const tbody = document.getElementById('movimientosTableBody');
    
    try {
        const username = localStorage.getItem('username');
        const usuario = await apiCall('GET', `/usuarios/username/${encodeURIComponent(username)}`);
        const movimientos = await apiCall('GET', `/movimientos/usuario/${usuario.id}`);
        
        if (!movimientos || movimientos.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" class="text-center">No tienes movimientos registrados</td></tr>';
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
                <td>${new Date(movimiento.fecha).toLocaleDateString('es-ES')}</td>
            </tr>
        `}).join('');
    } catch (error) {
        console.error('❌ Error cargando movimientos:', error);
        tbody.innerHTML = '<tr><td colspan="4" class="text-center">Error al cargar movimientos</td></tr>';
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