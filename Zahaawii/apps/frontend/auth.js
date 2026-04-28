(function (global) {
    const LOG_PREFIX = "[auth]";
    const ENDPOINTS = {
        csrf: "/api/v1/users/auth/csrf",
        me: "/api/v1/users/auth/me",
        login: "/api/v1/users/auth/login",
        logout: "/api/v1/users/auth/logout",
        signup: "/api/v1/users/createuser"
    };

    let currentUserCache = null;
    let currentUserPromise = null;

    function log(message, details) {
        if (details === undefined) {
            console.log(`${LOG_PREFIX} ${message}`);
            return;
        }
        console.log(`${LOG_PREFIX} ${message}`, details);
    }

    function readCookie(name) {
        const match = document.cookie.match(new RegExp(`(?:^|; )${name.replace(/[.$?*|{}()[\]\\/+^]/g, "\\$&")}=([^;]*)`));
        return match ? decodeURIComponent(match[1]) : null;
    }

    function isMutationMethod(method) {
        const normalizedMethod = (method || "GET").toUpperCase();
        return normalizedMethod !== "GET" && normalizedMethod !== "HEAD" && normalizedMethod !== "OPTIONS";
    }

    function buildError(status, fallbackMessage) {
        const error = new Error(fallbackMessage);
        error.status = status;
        return error;
    }

    function dispatchAuthChanged() {
        global.dispatchEvent(new CustomEvent("auth:changed", {
            detail: { user: currentUserCache }
        }));
    }

    async function ensureCsrfToken(forceRefresh = false) {
        const existingToken = readCookie("XSRF-TOKEN");
        if (existingToken && !forceRefresh) {
            return existingToken;
        }

        log("Fetching CSRF token");
        const response = await fetch(ENDPOINTS.csrf, {
            credentials: "same-origin"
        });

        if (!response.ok) {
            throw buildError(response.status, `Failed to initialize CSRF protection (${response.status})`);
        }

        const payload = await response.json().catch(() => ({}));
        return payload.token || readCookie("XSRF-TOKEN");
    }

    async function fetchWithAuth(url, options = {}) {
        const method = (options.method || "GET").toUpperCase();
        const headers = new Headers(options.headers || {});

        if (isMutationMethod(method) && !headers.has("X-XSRF-TOKEN")) {
            const csrfToken = await ensureCsrfToken();
            if (csrfToken) {
                headers.set("X-XSRF-TOKEN", csrfToken);
            }
        }

        return fetch(url, {
            ...options,
            method,
            headers,
            credentials: options.credentials || "same-origin"
        });
    }

    async function getCurrentUser(forceRefresh = false) {
        if (currentUserCache && !forceRefresh) {
            return currentUserCache;
        }

        if (currentUserPromise && !forceRefresh) {
            return currentUserPromise;
        }

        currentUserPromise = (async () => {
            const response = await fetch(ENDPOINTS.me, {
                credentials: "same-origin",
                headers: {
                    "Accept": "application/json"
                }
            });

            if (response.status === 401 || response.status === 403) {
                currentUserCache = null;
                return null;
            }

            if (!response.ok) {
                throw buildError(response.status, `Failed to resolve current user (${response.status})`);
            }

            currentUserCache = await response.json();
            return currentUserCache;
        })().catch((error) => {
            log("Current user lookup failed", error);
            currentUserCache = null;
            return null;
        }).finally(() => {
            currentUserPromise = null;
        });

        return currentUserPromise;
    }

    async function login(credentials) {
        log("Submitting login request");
        const response = await fetchWithAuth(ENDPOINTS.login, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(credentials)
        });

        if (!response.ok) {
            throw buildError(response.status, `Login failed (${response.status})`);
        }

        const payload = await response.json();
        currentUserCache = null;
        await getCurrentUser(true);
        dispatchAuthChanged();
        return payload;
    }

    async function signup(payload) {
        log("Submitting signup request");
        const response = await fetchWithAuth(ENDPOINTS.signup, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const message = await response.text();
            const error = buildError(response.status, message || `Signup failed (${response.status})`);
            error.body = message;
            throw error;
        }

        const result = await response.json();
        currentUserCache = null;
        await getCurrentUser(true);
        dispatchAuthChanged();
        return result;
    }

    async function logout() {
        log("Submitting logout request");
        const response = await fetchWithAuth(ENDPOINTS.logout, {
            method: "POST"
        });

        if (!response.ok && response.status !== 204) {
            throw buildError(response.status, `Logout failed (${response.status})`);
        }

        currentUserCache = null;
        dispatchAuthChanged();
    }

    async function initializeSession() {
        try {
            await ensureCsrfToken();
        } catch (error) {
            log("Initial CSRF token request failed", error);
        }

        const user = await getCurrentUser(true);
        dispatchAuthChanged();
        return user;
    }

    global.authClient = {
        ensureCsrfToken,
        fetchWithAuth,
        getCurrentUser,
        login,
        signup,
        logout,
        initializeSession,
        getCachedUser() {
            return currentUserCache;
        }
    };
})(window);
