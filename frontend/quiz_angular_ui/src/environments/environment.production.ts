export const environment = {
  production: true,
  // In Docker: Nginx on port 80 proxies /api/* → api-gateway:8080
  // So frontend calls relative /api paths — Nginx routes them internally
  apiBaseUrl: ''   // empty = relative URLs like /api/auth/login
};