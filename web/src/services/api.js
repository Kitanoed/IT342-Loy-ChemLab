import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor — attach JWT token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor — handle 401
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export { authAPI } from '../features/auth/api/authApi';

export const inventoryAPI = {
  list: (params) => api.get('/inventory', { params }),
  getById: (itemId) => api.get(`/inventory/${itemId}`),
  getItemAuditLogs: (itemId, params) => api.get(`/inventory/${itemId}/audit-logs`, { params }),
  create: (data) => api.post('/inventory', data),
  update: (itemId, data) => api.put(`/inventory/${itemId}`, data),
};

export const pubchemAPI = {
  lookup: (name) => api.get('/pubchem/lookup', { params: { name } }),
};

export const requestsAPI = {
  list: (params) => api.get('/requests', { params }),
  getById: (requestId) => api.get(`/requests/${requestId}`),
  create: (data) => api.post('/requests', data),
  approve: (requestId, data) => api.put(`/requests/${requestId}/approve`, data || {}),
  reject: (requestId, data) => api.put(`/requests/${requestId}/reject`, data || {}),
};

export const filesAPI = {
  list: (inventoryItemId) => api.get('/files', { params: { inventoryItemId } }),
  getById: (fileId) => api.get(`/files/${fileId}`),
  upload: (file, inventoryItemId) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('inventoryItemId', inventoryItemId);
    return api.post('/files/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
  downloadUrl: (fileId) => `${API_BASE_URL}/files/${fileId}/download`,
};

export default api;
