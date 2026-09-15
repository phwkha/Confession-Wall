import axios from 'axios';

// Resolve API base URL with fallback to '/api' for Vite dev proxy or Nginx reverse proxy
const envBaseUrl = import.meta.env.VITE_API_BASE_URL || '/api';
const baseURL = envBaseUrl.endsWith('/') ? envBaseUrl.slice(0, -1) : envBaseUrl;

const api = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  },
  timeout: 10000,
  withCredentials: true,
  xsrfCookieName: 'XSRF-TOKEN',
  xsrfHeaderName: 'X-XSRF-TOKEN',
});

/**
 * Extract XSRF-TOKEN value directly from browser document.cookie
 */
export const getCsrfTokenFromCookie = () => {
  if (typeof document === 'undefined') return null;
  const match = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]*)/);
  return match ? decodeURIComponent(match[1]) : null;
};

let csrfPromise = null;

/**
 * Fetch a CSRF token from the backend /csrf endpoint if not present in cookies
 */
export const fetchCsrfToken = async () => {
  const existing = getCsrfTokenFromCookie();
  if (existing) return existing;
  if (csrfPromise) return csrfPromise;

  csrfPromise = api.get('/csrf')
    .then((res) => getCsrfTokenFromCookie() || res.data?.token || null)
    .catch((err) => {
      console.warn('Không thể tải CSRF token từ máy chủ:', err);
      return getCsrfTokenFromCookie();
    })
    .finally(() => {
      csrfPromise = null;
    });

  return csrfPromise;
};

// Request interceptor to attach X-XSRF-TOKEN header to mutating requests
api.interceptors.request.use(async (config) => {
  const method = config.method ? config.method.toUpperCase() : 'GET';
  if (['POST', 'PUT', 'DELETE', 'PATCH'].includes(method)) {
    let token = getCsrfTokenFromCookie();
    if (!token) {
      token = await fetchCsrfToken();
    }
    if (token) {
      if (config.headers && typeof config.headers.set === 'function') {
        config.headers.set('X-XSRF-TOKEN', token);
      } else if (config.headers) {
        config.headers['X-XSRF-TOKEN'] = token;
      }
    }
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 429) {
      const customMessage =
        error.response.data?.message ||
        'Bạn đang thao tác quá nhanh, vui lòng thử lại sau.';
      error.friendlyMessage = customMessage;
    }
    return Promise.reject(error);
  }
);

/**
 * Fetch all confessions ordered newest first
 * @returns {Promise<Array>} List of confession entities
 */
export const getConfessions = async () => {
  const response = await api.get('/confessions');
  return response.data;
};

/**
 * Create a new confession message
 * @param {Object} data { content: string, author?: string }
 * @returns {Promise<Object>} Created confession entity
 */
export const createConfession = async (data) => {
  const response = await api.post('/confessions', {
    content: data.content,
    author: data.author?.trim() || undefined,
  });
  return response.data;
};

/**
 * Increment like count for a confession by ID
 * @param {number|string} id Confession ID
 * @returns {Promise<Object>} Updated confession entity
 */
export const likeConfession = async (id) => {
  const response = await api.put(`/confessions/${id}/like`);
  return response.data;
};

/**
 * Get the Server-Sent Events realtime stream endpoint URL
 * @returns {string} Realtime endpoint URL
 */
export const getRealtimeStreamUrl = () => `${baseURL}/realtime`;

export default api;
