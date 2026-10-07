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

// mirrors ApiError on the backend. fieldErrors is absent, not null, when there are none
interface ApiErrorBody {
  message: string
  fieldErrors?: Record<string, string>
}

// fieldErrors first. message is the constant "Validation failed" on @Valid failures
async function toError(response: Response): Promise<Error> {
  const body = (await response.json().catch(() => null)) as ApiErrorBody | null

  if (body?.fieldErrors) {
    const details = Object.entries(body.fieldErrors)
      .map(([field, message]) => `${field}: ${message}`)
      .join(', ')
    if (details !== '') {
      return new Error(details)
    }
  }

  return new Error(body?.message ?? `Request failed (${response.status})`)
}

async function postJson<T>(path: string, body: unknown): Promise<T> {
  const response = await fetch(path, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })

  if (!response.ok) {
    throw await toError(response)
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

// mirrors the optional query params on EventController.list. dates are ISO date time strings
export interface EventFilters {
  severity?: Severity
  incidentStatus?: IncidentStatus
  sourceName?: string
  startDate?: string
  endDate?: string
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
    throw await toError(response)
  }

  // 204 carries no body, so parsing it as json throws on the empty stream
  if (response.status === 204) {
    return undefined as T
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

// hard delete, 204 on success. nothing to return so the caller drops the row itself
export function deleteEvent(id: number): Promise<void> {
  return authedRequest<void>(`/events/${id}`, { method: 'DELETE' })
}

// only set filters go on the wire. an empty string would be sent as a real value and 400 on the enums
// https://developer.mozilla.org/en-US/docs/Web/API/URLSearchParams
export function listEvents(filters: EventFilters = {}): Promise<Page<EventResponse>> {
  const params = new URLSearchParams()
  for (const [key, value] of Object.entries(filters)) {
    if (value) {
      params.set(key, value)
    }
  }
  const query = params.toString()
  return authedRequest<Page<EventResponse>>(query ? `/events?${query}` : '/events')
}

export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY)
}
