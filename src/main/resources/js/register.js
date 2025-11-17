// Configuración base
const API_URL = 'http://localhost:8080/api';

// Elementos del DOM
const registerForm = document.getElementById('registerForm');
const usernameInput = document.getElementById('username');
const nombreCompletoInput = document.getElementById('nombreCompleto');
const passwordInput = document.getElementById('password');
const confirmPasswordInput = document.getElementById('confirmPassword');
const rolSelect = document.getElementById('rol');
const errorMessage = document.getElementById('errorMessage');
const successMessage = document.getElementById('successMessage');
const registerBtn = registerForm.querySelector('.btn-login');

// Event listeners
registerForm.addEventListener('submit', handleRegister);

// Validación en tiempo real
confirmPasswordInput.addEventListener('input', () => {
    if (passwordInput.value && confirmPasswordInput.value) {
        if (passwordInput.value !== confirmPasswordInput.value) {
            confirmPasswordInput.setCustomValidity('Las contraseñas no coinciden');
        } else {
            confirmPasswordInput.setCustomValidity('');
        }
    }
});

/**
 * Maneja el envío del formulario de registro
 */
async function handleRegister(e) {
    e.preventDefault();

    // Limpiar mensajes previos
    clearMessages();

    // Validaciones adicionales
    const username = usernameInput.value.trim();
    const nombreCompleto = nombreCompletoInput.value.trim();
    const password = passwordInput.value;
    const confirmPassword = confirmPasswordInput.value;
    const rol = rolSelect.value;

    // Validar que todos los campos estén completos
    if (!username || !nombreCompleto || !password || !confirmPassword || !rol) {
        showError('Por favor completa todos los campos');
        return;
    }

    // Validar longitud mínima de username
    if (username.length < 3) {
        showError('El usuario debe tener al menos 3 caracteres');
        return;
    }

    // Validar longitud mínima de nombre completo
    if (nombreCompleto.length < 3) {
        showError('El nombre completo debe tener al menos 3 caracteres');
        return;
    }

    // Validar longitud mínima de contraseña
    if (password.length < 6) {
        showError('La contraseña debe tener al menos 6 caracteres');
        return;
    }

    // Validar que las contraseñas coincidan
    if (password !== confirmPassword) {
        showError('Las contraseñas no coinciden');
        confirmPasswordInput.focus();
        return;
    }

    // Validar formato de username (sin espacios, caracteres especiales limitados)
    const usernameRegex = /^[a-zA-Z0-9_-]+$/;
    if (!usernameRegex.test(username)) {
        showError('El usuario solo puede contener letras, números, guiones y guiones bajos');
        return;
    }

    // Deshabilitar botón durante la petición
    registerBtn.disabled = true;
    registerBtn.textContent = 'Registrando...';

    try {
        // Primero, verificar si el usuario ya existe
        const checkResponse = await fetch(`${API_URL}/usuarios/existe/${username}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (checkResponse.ok) {
            const existe = await checkResponse.json();
            if (existe) {
                showError('Este nombre de usuario ya está en uso. Por favor elige otro.');
                usernameInput.focus();
                return;
            }
        }

        // Realizar petición de registro al backend
        const response = await fetch(`${API_URL}/auth/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                username: username,
                password: password,
                nombreCompleto: nombreCompleto,
                rol: rol
            })
        });

        // Manejar respuesta
        if (response.ok) {
            // Registro exitoso
            const data = await response.text();
            showSuccess('✅ ' + data + ' Redirigiendo al login...');
            
            // Limpiar formulario
            registerForm.reset();

            // Redirigir al login después de 2 segundos
            setTimeout(() => {
                window.location.href = '/html/login.html';
            }, 2000);
        } else {
            // Error en registro
            const errorData = await response.text();
            showError(errorData || 'Error al registrar usuario');
        }
    } catch (error) {
        console.error('Error en registro:', error);
        showError('Error al conectar con el servidor. Intenta nuevamente.');
    } finally {
        // Restaurar botón
        registerBtn.disabled = false;
        registerBtn.textContent = 'Registrarse';
    }
}

/**
 * Muestra mensaje de error
 */
function showError(message) {
    errorMessage.textContent = message;
    errorMessage.classList.add('show');
    
    // Auto-ocultar después de 5 segundos
    setTimeout(() => {
        errorMessage.classList.remove('show');
    }, 5000);
}

/**
 * Muestra mensaje de éxito
 */
function showSuccess(message) {
    successMessage.textContent = messag// Configuración base
const API_URL = 'http://localhost:8080/api';

// Elementos del DOM
const registerForm = document.getElementById('registerForm');
const usernameInput = document.getElementById('username');
const nombreCompletoInput = document.getElementById('nombreCompleto');
const passwordInput = document.getElementById('password');
const confirmPasswordInput = document.getElementById('confirmPassword');
const rolSelect = document.getElementById('rol');
const errorMessage = document.getElementById('errorMessage');
const successMessage = document.getElementById('successMessage');
const registerBtn = registerForm.querySelector('.btn-login');

// Event listeners
registerForm.addEventListener('submit', handleRegister);

// Validación en tiempo real
confirmPasswordInput.addEventListener('input', () => {
    if (passwordInput.value && confirmPasswordInput.value) {
        if (passwordInput.value !== confirmPasswordInput.value) {
            confirmPasswordInput.setCustomValidity('Las contraseñas no coinciden');
        } else {
            confirmPasswordInput.setCustomValidity('');
        }
    }
});

/**
 * Maneja el envío del formulario de registro
 */
async function handleRegister(e) {
    e.preventDefault();

    // Limpiar mensajes previos
    clearMessages();

    // Validaciones adicionales
    const username = usernameInput.value.trim();
    const nombreCompleto = nombreCompletoInput.value.trim();
    const password = passwordInput.value;
    const confirmPassword = confirmPasswordInput.value;
    const rol = rolSelect.value;

    // Validar que todos los campos estén completos
    if (!username || !nombreCompleto || !password || !confirmPassword || !rol) {
        showError('Por favor completa todos los campos');
        return;
    }

    // Validar longitud mínima de username
    if (username.length < 3) {
        showError('El usuario debe tener al menos 3 caracteres');
        return;
    }

    // Validar longitud mínima de nombre completo
    if (nombreCompleto.length < 3) {
        showError('El nombre completo debe tener al menos 3 caracteres');
        return;
    }

    // Validar longitud mínima de contraseña
    if (password.length < 6) {
        showError('La contraseña debe tener al menos 6 caracteres');
        return;
    }

    // Validar que las contraseñas coincidan
    if (password !== confirmPassword) {
        showError('Las contraseñas no coinciden');
        confirmPasswordInput.focus();
        return;
    }

    // Validar formato de username (sin espacios, caracteres especiales limitados)
    const usernameRegex = /^[a-zA-Z0-9_-]+$/;
    if (!usernameRegex.test(username)) {
        showError('El usuario solo puede contener letras, números, guiones y guiones bajos');
        return;
    }

    // Deshabilitar botón durante la petición
    registerBtn.disabled = true;
    registerBtn.textContent = 'Registrando...';

    try {
        // Primero, verificar si el usuario ya existe
        const checkResponse = await fetch(`${API_URL}/usuarios/existe/${username}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (checkResponse.ok) {
            const existe = await checkResponse.json();
            if (existe) {
                showError('Este nombre de usuario ya está en uso. Por favor elige otro.');
                usernameInput.focus();
                return;
            }
        }

        // Realizar petición de registro al backend
        const response = await fetch(`${API_URL}/auth/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                username: username,
                password: password,
                nombreCompleto: nombreCompleto,
                rol: rol
            })
        });

        // Manejar respuesta
        if (response.ok) {
            // Registro exitoso
            const data = await response.text();
            showSuccess('✅ ' + data + ' Redirigiendo al login...');
            
            // Limpiar formulario
            registerForm.reset();

            // Redirigir al login después de 2 segundos
            setTimeout(() => {
                window.location.href = '/html/login.html';
            }, 2000);
        } else {
            // Error en registro
            const errorData = await response.text();
            showError(errorData || 'Error al registrar usuario');
        }
    } catch (error) {
        console.error('Error en registro:', error);
        showError('Error al conectar con el servidor. Intenta nuevamente.');
    } finally {
        // Restaurar botón
        registerBtn.disabled = false;
        registerBtn.textContent = 'Registrarse';
    }
}

/**
 * Muestra mensaje de error
 */
function showError(message) {
    errorMessage.textContent = message;
    errorMessage.classList.add('show');
    
    // Auto-ocultar después de 5 segundos
    setTimeout(() => {
        errorMessage.classList.remove('show');
    }, 5000);
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

// Verificar si ya está autenticado al cargar la página
document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('token');
    
    // Si ya está autenticado, redirigir al dashboard
    if (token) {
        const rol = localStorage.getItem('rol');
        if (rol === 'ADMIN') {
            window.location.href = '/html/dashboard_admin.html';
        } else {
            window.location.href = '/html/dashboard_admin.html';
        }
    }
});e;
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

// Verificar si ya está autenticado al cargar la página
document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('token');
    
    // Si ya está autenticado, redirigir al dashboard
    if (token) {
        const rol = localStorage.getItem('rol');
        if (rol === 'ADMIN') {
            window.location.href = '/html/dashboard_admin.html';
        } else {
            window.location.href = '/html/dashboard_admin.html';
        }
    }
});