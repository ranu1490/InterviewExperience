export const environment = {
  production: true,
  // In dev the Angular proxy (proxy.conf.json) forwards /api to the gateway on :8080.
  // In production the SPA is served behind the same origin as the gateway, so a relative path works.
  apiUrl: '/api',
  // Set to your Google OAuth Web Client ID to enable "Continue with Google".
  googleClientId: '',
};
