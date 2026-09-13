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
    const errorBody = (await response.json().catch(() => null)) as ApiErrorBody | null
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

export type Severity = 'INFO' | 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'

export type IncidentStatus = 'UNRESOLVED' | 'IN_PROGRESS' | 'RESOLVED'

// mirrors EventCreateRequest on the backend. optional fields are omitted, not sent empty
export interface EventCreateRequest {
  title: string
  severity: Severity
  incidentStatus?: IncidentStatus
  sourceName?: string
  description?: string
  originIp?: string
  eventTimestamp?: string
}

// PUT replaces the whole record, so every editable field is required here
// no eventTimestamp, occurrence time is fixed at creation
export interface EventUpdateRequest {
  title: string
  severity: Severity
  incidentStatus: IncidentStatus
  sourceName?: string
  description?: string
  originIp?: string
}

export interface EventResponse {
  id: number
  userId: number
  title: string
  severity: Severity
  incidentStatus: IncidentStatus
  sourceName: string | null
  description: string | null
  originIp: string | null
  eventTimestamp: string | null
  timeCreated: string
}

// PagedModel json, set by the VIA_DTO serialization mode on LogMonitoringAppApplication
export interface Page<T> {
  content: T[]
  page: {
    size: number
    number: number
    totalElements: number
    totalPages: number
  }
}

// every authenticated call goes through here, so an expired token is handled once
async function authedRequest<T>(path: string, init?: RequestInit): Promise<T> {
  // Headers accepts any HeadersInit form, so callers can pass their own and keep them
  const headers = new Headers(init?.headers)
  headers.set('Authorization', `Bearer ${getToken() ?? ''}`)

  const response = await fetch(path, { ...init, headers })

  // token is expired or forged. localStorage still holds it, so drop it before leaving
  if (response.status === 401) {
    clearToken()
    window.location.replace('/login')
    throw new Error('Session expired')
  }

  if (!response.ok) {
    const errorBody = (await response.json().catch(() => null)) as ApiErrorBody | null
    throw new Error(errorBody?.message ?? `Request failed (${response.status})`)
  }

  return response.json() as Promise<T>
}

// 201 comes back with the created event, so callers get the server assigned id and timeCreated
export function createEvent(request: EventCreateRequest): Promise<EventResponse> {
  return authedRequest<EventResponse>('/events', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  })
}

// returns the updated event, so the caller can swap it into state without a reload
export function updateEvent(id: number, request: EventUpdateRequest): Promise<EventResponse> {
  return authedRequest<EventResponse>(`/events/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  })
}

export function listEvents(): Promise<Page<EventResponse>> {
  return authedRequest<Page<EventResponse>>('/events')
}

export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY)
}
