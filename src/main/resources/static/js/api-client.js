/**
 * WarrantyWise Reusable API Client Utility with Token Refresh Flow
 * Integrates with Spring Boot Backend REST APIs (/api/v1)
 */
const ApiClient = (function () {
    const API_BASE = '/api/v1';
    let isRefreshing = false;
    let refreshSubscribers = [];

    function subscribeTokenRefresh(cb) {
        refreshSubscribers.push(cb);
    }

    function onRefreshed(newToken) {
        refreshSubscribers.map(cb => cb(newToken));
        refreshSubscribers = [];
    }

    async function request(endpoint, options = {}, isRetry = false) {
        const url = endpoint.startsWith('http') ? endpoint : `${API_BASE}${endpoint}`;
        const headers = options.headers || {};
        
        // Attach JWT authorization header if available
        const token = AuthUtil.getToken();
        if (token && !headers['Authorization']) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        // Default Content-Type to JSON unless payload is FormData
        if (options.body && !(options.body instanceof FormData) && !headers['Content-Type']) {
            headers['Content-Type'] = 'application/json';
        }

        const config = {
            method: options.method || 'GET',
            headers: headers,
            ...options
        };

        if (config.body && typeof config.body === 'object' && !(config.body instanceof FormData)) {
            config.body = JSON.stringify(config.body);
        }

        try {
            const response = await fetch(url, config);

            // Handle HTTP 401 Unauthorized
            if (response.status === 401) {
                // If it's auth login or auth refresh request, throw directly
                if (endpoint.includes('/auth/login') || endpoint.includes('/auth/refresh')) {
                    const errorData = await response.json().catch(() => null);
                    const msg = (errorData && (errorData.message || errorData.error)) || 'Invalid credentials';
                    throw new Error(msg);
                }

                // If token refresh is possible and hasn't retried yet
                const refreshToken = AuthUtil.getRefreshToken();
                if (refreshToken && !isRetry) {
                    if (!isRefreshing) {
                        isRefreshing = true;
                        try {
                            const refreshResponse = await fetch(`${API_BASE}/auth/refresh`, {
                                method: 'POST',
                                headers: { 'Content-Type': 'application/json' },
                                body: JSON.stringify({ refreshToken: refreshToken })
                            });

                            if (refreshResponse.ok) {
                                const newAuth = await refreshResponse.json();
                                AuthUtil.setAuth(newAuth);
                                isRefreshing = false;
                                onRefreshed(newAuth.accessToken);
                                // Retry current request
                                options.headers = options.headers || {};
                                options.headers['Authorization'] = `Bearer ${newAuth.accessToken}`;
                                return await request(endpoint, options, true);
                            } else {
                                isRefreshing = false;
                                AuthUtil.logout();
                                throw new Error('Session expired. Please log in again.');
                            }
                        } catch (err) {
                            isRefreshing = false;
                            AuthUtil.logout();
                            throw err;
                        }
                    } else {
                        // Wait for token refresh to complete
                        return new Promise((resolve) => {
                            subscribeTokenRefresh(newToken => {
                                options.headers = options.headers || {};
                                options.headers['Authorization'] = `Bearer ${newToken}`;
                                resolve(request(endpoint, options, true));
                            });
                        });
                    }
                } else {
                    AuthUtil.logout();
                    throw new Error('Session expired. Please log in again.');
                }
            }

            // Handle HTTP 403 Forbidden
            if (response.status === 403) {
                UIComponents.showToast('Access denied. You do not have permission for this action.', 'danger');
                throw new Error('Access denied');
            }

            // Handle CSV or text blob downloads
            const contentType = response.headers.get('content-type') || '';
            if (contentType.includes('text/csv') || contentType.includes('application/octet-stream')) {
                if (!response.ok) throw new Error('Failed to download file');
                return await response.blob();
            }

            // Handle 204 No Content
            if (response.status === 204) {
                return null;
            }

            const data = await response.json().catch(() => null);

            if (!response.ok) {
                const errorMessage = (data && data.message) || (data && data.error) || `HTTP Error ${response.status}`;
                throw new Error(errorMessage);
            }

            return data;
        } catch (error) {
            console.error(`API Error [${config.method} ${url}]:`, error);
            throw error;
        }
    }

    return {
        get: function (endpoint, params = {}) {
            let queryString = '';
            if (params && Object.keys(params).length > 0) {
                const searchParams = new URLSearchParams();
                Object.keys(params).forEach(key => {
                    if (params[key] !== null && params[key] !== undefined && params[key] !== '') {
                        searchParams.append(key, params[key]);
                    }
                });
                queryString = `?${searchParams.toString()}`;
            }
            return request(`${endpoint}${queryString}`, { method: 'GET' });
        },

        post: function (endpoint, data) {
            return request(endpoint, { method: 'POST', body: data });
        },

        put: function (endpoint, data) {
            return request(endpoint, { method: 'PUT', body: data });
        },

        delete: function (endpoint) {
            return request(endpoint, { method: 'DELETE' });
        },

        upload: function (endpoint, formData) {
            return request(endpoint, {
                method: 'POST',
                body: formData
            });
        },

        downloadCsv: async function (endpoint, params = {}, filename = 'export.csv') {
            const blob = await this.get(endpoint, params);
            if (blob && blob instanceof Blob) {
                const downloadUrl = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = downloadUrl;
                a.download = filename;
                document.body.appendChild(a);
                a.click();
                a.remove();
                window.URL.revokeObjectURL(downloadUrl);
            }
        }
    };
})();
