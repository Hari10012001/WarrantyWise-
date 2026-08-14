/**
 * WarrantyWise Authentication Pages Logic (Login & Registration)
 */
document.addEventListener('DOMContentLoaded', function () {
    // Initialize Theme
    ThemeUtil.init();

    // Setup Theme Toggle Button
    const themeBtn = document.getElementById('theme-toggle-btn');
    if (themeBtn) {
        themeBtn.addEventListener('click', function () {
            ThemeUtil.toggle();
        });
    }

    // 1. Redirect if user is already authenticated
    if (AuthUtil.isAuthenticated()) {
        const currentPath = window.location.pathname;
        if (currentPath.includes('/login.html') || currentPath.includes('/register.html')) {
            window.location.href = '/index.html';
            return;
        }
    }

    // 2. Helper: Display Global Alert
    function showAlert(message, type = 'danger') {
        const alertContainer = document.getElementById('auth-alert-container');
        if (alertContainer) {
            alertContainer.className = `alert alert-${type} alert-dismissible fade show`;
            alertContainer.innerHTML = `
                <i class="bi bi-exclamation-triangle-fill me-2"></i> ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            `;
            alertContainer.classList.remove('d-none');
        }
    }

    function hideAlert() {
        const alertContainer = document.getElementById('auth-alert-container');
        if (alertContainer) {
            alertContainer.classList.add('d-none');
        }
    }

    // 3. Password Visibility Toggles
    const togglePasswordBtn = document.getElementById('toggle-password-btn');
    const passwordInput = document.getElementById('password');
    if (togglePasswordBtn && passwordInput) {
        togglePasswordBtn.addEventListener('click', function () {
            const isPassword = passwordInput.getAttribute('type') === 'password';
            passwordInput.setAttribute('type', isPassword ? 'text' : 'password');
            const icon = togglePasswordBtn.querySelector('i');
            if (icon) {
                icon.className = isPassword ? 'bi bi-eye-slash' : 'bi bi-eye';
            }
        });
    }

    const toggleConfirmPasswordBtn = document.getElementById('toggle-confirm-password-btn');
    const confirmPasswordInput = document.getElementById('confirmPassword');
    if (toggleConfirmPasswordBtn && confirmPasswordInput) {
        toggleConfirmPasswordBtn.addEventListener('click', function () {
            const isPassword = confirmPasswordInput.getAttribute('type') === 'password';
            confirmPasswordInput.setAttribute('type', isPassword ? 'text' : 'password');
            const icon = toggleConfirmPasswordBtn.querySelector('i');
            if (icon) {
                icon.className = isPassword ? 'bi bi-eye-slash' : 'bi bi-eye';
            }
        });
    }

    // 4. Password Strength Indicator (Register Page)
    if (passwordInput && document.getElementById('password-strength-bar')) {
        passwordInput.addEventListener('input', function () {
            const val = passwordInput.value;
            const bar = document.getElementById('password-strength-bar');
            const text = document.getElementById('password-strength-text');

            let score = 0;
            if (val.length >= 8) score += 25;
            if (/[A-Z]/.test(val)) score += 25;
            if (/[0-9]/.test(val)) score += 25;
            if (/[^A-Za-z0-9]/.test(val)) score += 25;

            bar.style.width = score + '%';

            if (score === 0) {
                bar.className = 'progress-bar bg-danger';
                text.innerText = 'Password strength';
            } else if (score <= 50) {
                bar.className = 'progress-bar bg-danger';
                text.innerText = 'Weak password';
            } else if (score <= 75) {
                bar.className = 'progress-bar bg-warning';
                text.innerText = 'Medium password';
            } else {
                bar.className = 'progress-bar bg-success';
                text.innerText = 'Strong password';
            }
        });
    }

    // 5. LOGIN FORM SUBMISSION
    const loginForm = document.getElementById('login-form');
    if (loginForm) {
        loginForm.addEventListener('submit', async function (e) {
            e.preventDefault();
            hideAlert();

            const email = document.getElementById('email').value.trim();
            const password = document.getElementById('password').value;

            // Form validation
            let isValid = true;
            if (!email || !email.includes('@')) {
                document.getElementById('email').classList.add('is-invalid');
                isValid = false;
            } else {
                document.getElementById('email').classList.remove('is-invalid');
            }

            if (!password) {
                document.getElementById('password').classList.add('is-invalid');
                isValid = false;
            } else {
                document.getElementById('password').classList.remove('is-invalid');
            }

            if (!isValid) return;

            // Prevent double submission & show loading
            const submitBtn = document.getElementById('login-submit-btn');
            const btnText = document.getElementById('login-btn-text');
            const btnSpinner = document.getElementById('login-btn-spinner');

            submitBtn.disabled = true;
            btnText.innerText = 'Signing In...';
            btnSpinner.classList.remove('d-none');

            try {
                const response = await ApiClient.post('/auth/login', {
                    email: email,
                    password: password
                });

                if (response && response.accessToken) {
                    AuthUtil.setAuth(response);
                    UIComponents.showToast('Login successful! Redirecting...', 'success');
                    setTimeout(() => {
                        window.location.href = '/index.html';
                    }, 500);
                } else {
                    showAlert('Failed to sign in. Please check your credentials.', 'danger');
                }
            } catch (err) {
                showAlert(err.message || 'Invalid email or password', 'danger');
            } finally {
                submitBtn.disabled = false;
                btnText.innerText = 'Sign In';
                btnSpinner.classList.add('d-none');
            }
        });
    }

    // 6. REGISTER FORM SUBMISSION
    const registerForm = document.getElementById('register-form');
    if (registerForm) {
        registerForm.addEventListener('submit', async function (e) {
            e.preventDefault();
            hideAlert();

            const fullName = document.getElementById('fullName').value.trim();
            const email = document.getElementById('email').value.trim();
            const password = document.getElementById('password').value;
            const confirmPassword = document.getElementById('confirmPassword').value;

            let isValid = true;

            if (!fullName) {
                document.getElementById('fullName').classList.add('is-invalid');
                isValid = false;
            } else {
                document.getElementById('fullName').classList.remove('is-invalid');
            }

            if (!email || !email.includes('@')) {
                document.getElementById('email').classList.add('is-invalid');
                isValid = false;
            } else {
                document.getElementById('email').classList.remove('is-invalid');
            }

            if (!password || password.length < 8) {
                document.getElementById('password').classList.add('is-invalid');
                isValid = false;
            } else {
                document.getElementById('password').classList.remove('is-invalid');
            }

            if (password !== confirmPassword) {
                document.getElementById('confirmPassword').classList.add('is-invalid');
                isValid = false;
            } else {
                document.getElementById('confirmPassword').classList.remove('is-invalid');
            }

            if (!isValid) return;

            // Prevent double submission & show loading
            const submitBtn = document.getElementById('register-submit-btn');
            const btnText = document.getElementById('register-btn-text');
            const btnSpinner = document.getElementById('register-btn-spinner');

            submitBtn.disabled = true;
            btnText.innerText = 'Creating Account...';
            btnSpinner.classList.remove('d-none');

            try {
                const response = await ApiClient.post('/auth/register', {
                    fullName: fullName,
                    email: email,
                    password: password
                });

                if (response && response.accessToken) {
                    AuthUtil.setAuth(response);
                    UIComponents.showToast('Registration successful! Redirecting...', 'success');
                    setTimeout(() => {
                        window.location.href = '/index.html';
                    }, 500);
                } else {
                    showAlert('Account creation failed. Please try again.', 'danger');
                }
            } catch (err) {
                showAlert(err.message || 'Registration failed. Email may already be in use.', 'danger');
            } finally {
                submitBtn.disabled = false;
                btnText.innerText = 'Create Account';
                btnSpinner.classList.add('d-none');
            }
        });
    }
});
