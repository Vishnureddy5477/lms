// Development environment.
// Overridden to 8182 locally because port 8181 is occupied by another
// service (GlassFish) on this machine; run the backend with
// $env:SERVER_PORT = "8182" to match.
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8182/api',
};
