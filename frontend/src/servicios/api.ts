import axios from 'axios';

// Cliente HTTP único de la aplicación: todos los servicios lo usan.
// Cambiar la URL del backend o el tiempo de espera se hace aquí, una sola vez.
export const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10_000,   // milisegundos: pasado este tiempo, la petición falla
});
