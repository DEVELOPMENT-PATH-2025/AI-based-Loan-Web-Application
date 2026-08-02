// Dashboard JavaScript Utility Functions

// API Base URL
const API_BASE = '/api';

// Token Management
function getAuthToken() {
    return localStorage.getItem('token');
}

function setAuthToken(token) {
    localStorage.setItem('token', token);
}

function removeAuthToken() {
    localStorage.removeItem('token');
}

// Role Management
function getUserRole() {
    return localStorage.getItem('role');
}

function setUserRole(role) {
    localStorage.setItem('role', role);
}

// User Data Management
function setUserData(userData) {
    localStorage.setItem('userData', JSON.stringify(userData));
}

function getUserData() {
    const userData = localStorage.getItem('userData');
    return userData ? JSON.parse(userData) : null;
}

// Clear all auth data
function clearAuthData() {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('userData');
}

// Logout function
function logout() {
    clearAuthData();
    window.location.href = '/login';
}

// Generic API fetch function with error handling
async function fetchAPI(endpoint, options = {}) {
    const token = getAuthToken();
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers
    };

    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    try {
        const response = await fetch(`${API_BASE}${endpoint}`, {
            ...options,
            headers
        });

        if (!response.ok) {
            if (response.status === 401) {
                // Unauthorized - redirect to login
                logout();
                throw new Error('Session expired. Please login again.');
            }
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        return await response.json();
    } catch (error) {
        console.error('API Error:', error);
        throw error;
    }
}

// Format currency
function formatCurrency(amount) {
    return new Intl.NumberFormat('en-IN', {
        style: 'currency',
        currency: 'INR',
        maximumFractionDigits: 0
    }).format(amount);
}

// Format date
function formatDate(dateString) {
    const options = { year: 'numeric', month: 'short', day: 'numeric' };
    return new Date(dateString).toLocaleDateString('en-IN', options);
}

// Show alert message
function showAlert(message, type = 'info') {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type}`;
    alertDiv.textContent = message;
    
    // Insert at the top of the body
    document.body.insertBefore(alertDiv, document.body.firstChild);
    
    // Remove after 5 seconds
    setTimeout(() => {
        alertDiv.remove();
    }, 5000);
}

// Show loading spinner
function showLoading(elementId) {
    const element = document.getElementById(elementId);
    if (element) {
        element.innerHTML = '<div class="spinner"></div>';
    }
}

// Hide loading spinner
function hideLoading(elementId, content) {
    const element = document.getElementById(elementId);
    if (element) {
        element.innerHTML = content || '';
    }
}

// Validate email format
function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

// Validate phone number (Indian format)
function isValidPhone(phone) {
    const phoneRegex = /^[6-9]\d{9}$/;
    return phoneRegex.test(phone);
}

// Calculate EMI
function calculateEMI(principal, annualRate, tenureMonths) {
    const monthlyRate = annualRate / 12 / 100;
    const emi = principal * monthlyRate * Math.pow(1 + monthlyRate, tenureMonths) / 
                (Math.pow(1 + monthlyRate, tenureMonths) - 1);
    return Math.round(emi);
}

// Calculate total interest
function calculateTotalInterest(principal, annualRate, tenureMonths) {
    const emi = calculateEMI(principal, annualRate, tenureMonths);
    const totalPayment = emi * tenureMonths;
    return totalPayment - principal;
}

// Debounce function for search inputs
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Check authentication on page load
function checkAuthentication(requiredRole = null) {
    const token = getAuthToken();
    const role = getUserRole();

    if (!token) {
        window.location.href = '/login';
        return false;
    }

    if (requiredRole && role !== requiredRole) {
        showAlert('Unauthorized access!', 'danger');
        window.location.href = '/dashboard';
        return false;
    }

    return true;
}

// Initialize user avatar
function initializeUserAvatar() {
    const userData = getUserData();
    if (userData && userData.name) {
        const avatarElements = document.querySelectorAll('[id="userAvatar"]');
        avatarElements.forEach(element => {
            element.textContent = userData.name.charAt(0).toUpperCase();
        });
    }
}

// Auto-refresh data at intervals
function autoRefresh(callback, intervalMinutes = 5) {
    const intervalMs = intervalMinutes * 60 * 1000;
    setInterval(callback, intervalMs);
}

// Export functions for use in other scripts
window.DashboardUtils = {
    getAuthToken,
    setAuthToken,
    removeAuthToken,
    getUserRole,
    setUserRole,
    setUserData,
    getUserData,
    clearAuthData,
    logout,
    fetchAPI,
    formatCurrency,
    formatDate,
    showAlert,
    showLoading,
    hideLoading,
    isValidEmail,
    isValidPhone,
    calculateEMI,
    calculateTotalInterest,
    debounce,
    checkAuthentication,
    initializeUserAvatar,
    autoRefresh
};
