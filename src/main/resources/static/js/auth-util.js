/**
 * WarrantyWise Authentication & Token Storage Utility
 */
const AuthUtil = (function () {
    const TOKEN_KEY = 'warrantywise_access_token';
    const REFRESH_KEY = 'warrantywise_refresh_token';
    const USER_KEY = 'warrantywise_user';

    return {
        getToken: function () {
            return localStorage.getItem(TOKEN_KEY);
        },

        getRefreshToken: function () {
            return localStorage.getItem(REFRESH_KEY);
        },

        getUser: function () {
            const userStr = localStorage.getItem(USER_KEY);
            try {
                return userStr ? JSON.parse(userStr) : null;
            } catch (e) {
                return null;
            }
        },

        setAuth: function (authResponse) {
            if (authResponse && authResponse.accessToken) {
                localStorage.setItem(TOKEN_KEY, authResponse.accessToken);
            }
            if (authResponse && authResponse.refreshToken) {
                localStorage.setItem(REFRESH_KEY, authResponse.refreshToken);
            }
            if (authResponse && authResponse.user) {
                localStorage.setItem(USER_KEY, JSON.stringify(authResponse.user));
            }
        },

        setUser: function (user) {
            if (user) {
                localStorage.setItem(USER_KEY, JSON.stringify(user));
            }
        },

        isAuthenticated: function () {
            const token = this.getToken();
            return !!token;
        },

        hasRole: function (roleName) {
            const user = this.getUser();
            return user && user.role === roleName;
        },

        logout: function () {
            localStorage.removeItem(TOKEN_KEY);
            localStorage.removeItem(REFRESH_KEY);
            localStorage.removeItem(USER_KEY);
            window.location.href = '/pages/login.html';
        }
    };
})();
