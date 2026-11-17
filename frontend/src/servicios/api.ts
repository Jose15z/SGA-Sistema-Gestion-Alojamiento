import axios from 'axios';

// Cliente HTTP único de la aplicación: todos los servicios lo usan.
const apiUrl = import.meta.env.VITE_API_URL;
export const api = axios.create({
  baseURL: apiUrl || 'http://localhost:8080/api',
  timeout: apiUrl ? 210_000 : 10_000,
});
