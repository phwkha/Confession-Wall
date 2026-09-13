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
});

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

export default api;
