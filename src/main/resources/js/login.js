// Configuración base
const API_URL = 'http://localhost:8080/api';

// Elementos del DOM
const loginForm = document.getElementById('loginForm');
const usernameInput = document.getElementById('username');
const passwordInput = document.getElementById('password');
const errorMessage = document.getElementById('errorMessage');
const successMessage = document.getElementById('successMessage');
const loginBtn = loginForm.querySelector('.btn-login');

// Event listeners
loginForm.addEventListener('submit', handleLogin);

/**
 * Decodifica el token JWT y extrae el rol
 */
function decodeTokenRol(token) {
    try {
        const payload = token.split('.')[1];
        const decoded = JSON.parse(atob(payload));
        return decoded.rol || null;
    } catch (error) {
        console.error('Error decodificando token:', error);
        return null;
    }
}

/**
 * Redirige al dashboard según el rol
 */
function redirectByRole(rol) {
    switch(rol) {
        case 'ADMIN':
            window.location.href = '/html/dashboard_admin.html';
            break;
        case 'ENCARGADO':
            window.location.href = '/html/dashboard_encargado.html';
            break;
        case 'OPERADOR':
            window.location.href = '/html/dashboard_operador.html';
            break;
        default:
            window.location.href = '/html/login.html';
    }
}

/**
 * Maneja el envío del formulario de login
 */
async function handleLogin(e) {
    e.preventDefault();

    // Validar campos
    if (!usernameInput.value.trim() || !passwordInput.value.trim()) {
        showError('Por favor completa todos los campos');
        return;
    }

    // Limpiar mensajes previos
    clearMessages();

    // Deshabilitar botón durante la petición
    loginBtn.disabled = true;
    loginBtn.textContent = 'Iniciando sesión...';

    try {
        // Realizar petición al backend
        const response = await fetch(`${API_URL}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                username: usernameInput.value.trim(),
                password: passwordInput.value.trim()
            })
        });

        // Parsear respuesta
        const data = await response.json();

        if (response.ok) {
            // Login exitoso
            showSuccess('¡Login exitoso! Redirigiendo...');
            
            // Guardar token en localStorage
            localStorage.setItem('token', data.token);
            
            // Guardar datos del usuario
            localStorage.setItem('username', usernameInput.value.trim());
            
            // Guardar rol del usuario (decodificar del token JWT)
            const rol = decodeTokenRol(data.token);
            if (rol) {
                localStorage.setItem('rol', rol);
                console.log('✅ Rol del usuario:', rol);
            }

            // Redirigir después de 1 segundo según el rol
            setTimeout(() => {
                redirectByRole(rol);
            }, 1000);
        } else {
            // Error en login
            showError(data.message || 'Usuario o contraseña inválidos');
        }
    } catch (error) {
        console.error('Error en login:', error);
        showError('Error al conectar con el servidor. Intenta nuevamente.');
    } finally {
        // Restaurar botón
        loginBtn.disabled = false;
        loginBtn.textContent = 'Iniciar Sesión';
    }
}

/**
 * Muestra mensaje de error
 */
function showError(message) {
    errorMessage.textContent = message;
    errorMessage.classList.add('show');
}

/**
 * Muestra mensaje de éxito
 */
function showSuccess(message) {
    successMessage.textContent = message;
    successMessage.classList.add('show');
}

/**
 * Limpia los mensajes
 */
function clearMessages() {
    errorMessage.classList.remove('show');
    successMessage.classList.remove('show');
    errorMessage.textContent = '';
    successMessage.textContent = '';
}

/**
 * Obtiene el token del localStorage
 */
function getToken() {
    return localStorage.getItem('token');
}

/**
 * Verifica si el usuario está autenticado
 */
function isAuthenticated() {
    return !!getToken();
}

/**
 * Cierra la sesión
 */
function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('rol');
    window.location.href = '/html/login.html';
}

// Verificar si el usuario está autenticado
document.addEventListener('DOMContentLoaded', () => {
    // Si ya está autenticado, redirigir al dashboard correspondiente
    if (isAuthenticated()) {
        const rol = localStorage.getItem('rol');
        redirectByRole(rol);
    }
});