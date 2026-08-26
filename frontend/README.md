# Frontend

Vite + React + TypeScript UI for the Secure Event Dashboard. Two pages only: an auth page
(login/register toggle) and a dashboard page behind a `ProtectedRoute` wrapper that checks
for the JWT in localStorage and redirects to `/login` when absent.

Stack choices:

- react-router-dom v7 declarative routes (`BrowserRouter`, `Routes`, `Route`)
- native fetch through a small typed API client, no axios
- JWT kept in localStorage under `token`, sent as `Authorization: Bearer <token>`
- plain CSS, no UI kit

## Dev proxy

`vite.config.ts` forwards `/auth`, `/users`, `/events`, and `/actuator` to the Spring Boot
backend on `http://localhost:8080`. The browser only ever talks to Vite's origin, so API
calls stay same-origin during development and no CORS handling is needed anywhere. This is
dev-only config; in deployment Spring Boot serves the built `dist/` output from the same
origin as the API.

## Commands

Backend must be running on :8080 first.

```bash
npm install
npm run dev      # dev server with HMR on :5173
npm run build    # tsc -b && vite build, outputs dist/
npm run lint     # oxlint
```

## Notes

- Route table lives in `src/App.tsx`: `/login`, `/register`, guarded `/dashboard`, catch-all
  redirect to `/login`.
- Register mode shows an email field because the backend `RegisterRequest` DTO requires one;
  login mode sends username and password only.
