// typed fetch calls per https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API/Using_Fetch

const TOKEN_KEY = 'token'

export interface RegisterRequest {
  username: string
  email: string
  password: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterResponse {
  id: number
  username: string
  email: string
}

export interface LoginResponse {
  token: string
}

interface ApiErrorBody {
  message: string
}

async function postJson<T>(path: string, body: unknown): Promise<T> {
  const response = await fetch(path, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })

  if (!response.ok) {
    const errorBody = await response.json().catch(() => null) as ApiErrorBody | null
    throw new Error(errorBody?.message ?? `Request failed (${response.status})`)
  }

  return response.json() as Promise<T>
}

export function register(request: RegisterRequest): Promise<RegisterResponse> {
  return postJson<RegisterResponse>('/auth/register', request)
}

export function login(request: LoginRequest): Promise<LoginResponse> {
  return postJson<LoginResponse>('/auth/login', request)
}

export function saveToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}
