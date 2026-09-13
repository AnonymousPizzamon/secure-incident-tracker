// owner scoped event list, rendered behind ProtectedRoute
// effect fetch per https://react.dev/reference/react/useEffect#fetching-data-with-effects
import type { FormEvent } from 'react'
import { useEffect, useState } from 'react'
import type {
  EventCreateRequest,
  EventResponse,
  EventUpdateRequest,
  IncidentStatus,
  Severity,
} from '../api/client'
import { createEvent, deleteEvent, listEvents, updateEvent } from '../api/client'

// backend sends LocalDateTime with no zone, so this reads as local time
function formatTime(value: string): string {
  return new Date(value).toLocaleString()
}

// typed against the unions, so a typo here fails the build instead of the request
const SEVERITIES: Severity[] = ['INFO', 'LOW', 'MEDIUM', 'HIGH', 'CRITICAL']
const STATUSES: IncidentStatus[] = ['UNRESOLVED', 'IN_PROGRESS', 'RESOLVED']

// inputs only ever hand back strings, so the form holds strings and converts on submit
interface EventForm {
  title: string
  severity: Severity
  incidentStatus: IncidentStatus
  sourceName: string
  description: string
  originIp: string
  eventTimestamp: string
}

type TextField = 'title' | 'sourceName' | 'description' | 'originIp' | 'eventTimestamp'

const emptyForm: EventForm = {
  title: '',
  severity: 'MEDIUM',
  incidentStatus: 'UNRESOLVED',
  sourceName: '',
  description: '',
  originIp: '',
  eventTimestamp: '',
}

// blank optional fields are dropped from the body rather than sent as ""
// empty string is not a valid ISO datetime and eventTimestamp has no server side default
function blankToUndefined(value: string): string | undefined {
  return value.trim() === '' ? undefined : value
}

function DashboardPage() {
  const [events, setEvents] = useState<EventResponse[]>([])
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)
  const [form, setForm] = useState<EventForm>(emptyForm)
  const [formError, setFormError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)
  // null means the form is creating, an id means it is editing that event
  const [editingId, setEditingId] = useState<number | null>(null)

  const isEditing = editingId !== null

  // empty dep array, so this runs once after the first render
  useEffect(() => {
    listEvents()
      .then((page) => setEvents(page.content))
      .catch((err) => setError(err instanceof Error ? err.message : 'Could not load events'))
      .finally(() => setLoading(false))
  }, [])

  function updateField(field: TextField, value: string) {
    setForm((current) => ({ ...current, [field]: value }))
  }

  // seeds every editable field from the row already in state, so no extra GET is needed
  // an unseeded field would submit blank and PUT would overwrite the stored value
  function startEdit(event: EventResponse) {
    setFormError(null)
    setEditingId(event.id)
    setForm({
      title: event.title,
      severity: event.severity,
      incidentStatus: event.incidentStatus,
      sourceName: event.sourceName ?? '',
      description: event.description ?? '',
      originIp: event.originIp ?? '',
      eventTimestamp: '',
    })
  }

  function cancelEdit() {
    setEditingId(null)
    setFormError(null)
    setForm(emptyForm)
  }

  // confirm blocks until dismissed. cancelling leaves state untouched and sends no request
  async function handleDelete(event: EventResponse) {
    if (!window.confirm(`Delete "${event.title}"?`)) {
      return
    }

    try {
      await deleteEvent(event.id)
      setEvents((current) => current.filter((row) => row.id !== event.id))
      // form was seeded from a row that is now gone, so saving it would 404
      if (editingId === event.id) {
        cancelEdit()
      }
    } catch (err) {
      // list level failure, not a form one, so it renders above the table
      setError(err instanceof Error ? err.message : 'Could not delete event')
    }
  }

  async function handleSubmit(submitEvent: FormEvent<HTMLFormElement>) {
    submitEvent.preventDefault()
    setFormError(null)
    setSubmitting(true)

    try {
      if (editingId === null) {
        // no user id in the body. owner is resolved from the bearer token on the backend
        const request: EventCreateRequest = {
          title: form.title,
          severity: form.severity,
          incidentStatus: form.incidentStatus,
          sourceName: blankToUndefined(form.sourceName),
          description: blankToUndefined(form.description),
          originIp: blankToUndefined(form.originIp),
          eventTimestamp: blankToUndefined(form.eventTimestamp),
        }
        const created = await createEvent(request)
        // 201 body already carries id, userId and timeCreated, so there is nothing to refetch
        // prepend matches the server sort, newest first
        setEvents((current) => [created, ...current])
      } else {
        // all six fields go out every time. PUT replaces the record, it does not patch it
        const request: EventUpdateRequest = {
          title: form.title,
          severity: form.severity,
          incidentStatus: form.incidentStatus,
          sourceName: blankToUndefined(form.sourceName),
          description: blankToUndefined(form.description),
          originIp: blankToUndefined(form.originIp),
        }
        const updated = await updateEvent(editingId, request)
        // timeCreated is unchanged by an update, so sort order holds and the row swaps in place
        setEvents((current) => current.map((row) => (row.id === updated.id ? updated : row)))
        setEditingId(null)
      }
      setForm(emptyForm)
    } catch (err) {
      // form keeps its values so a rejected save can be corrected and retried
      setFormError(err instanceof Error ? err.message : 'Could not save event')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main className="dashboard">
      <h1>Events</h1>

      <section className="event-create">
        <h2>{isEditing ? 'Edit event' : 'New event'}</h2>

        {formError && <p className="form-error">{formError}</p>}

        <form className="event-form" onSubmit={handleSubmit}>
          <label>
            Title
            <input
              name="title"
              value={form.title}
              onChange={(changeEvent) => updateField('title', changeEvent.target.value)}
            />
          </label>
          <label>
            Severity
            <select
              name="severity"
              value={form.severity}
              onChange={(changeEvent) =>
                setForm((current) => ({
                  ...current,
                  severity: changeEvent.target.value as Severity,
                }))
              }
            >
              {SEVERITIES.map((severity) => (
                <option key={severity} value={severity}>
                  {severity}
                </option>
              ))}
            </select>
          </label>
          <label>
            Status
            <select
              name="incidentStatus"
              value={form.incidentStatus}
              onChange={(changeEvent) =>
                setForm((current) => ({
                  ...current,
                  incidentStatus: changeEvent.target.value as IncidentStatus,
                }))
              }
            >
              {STATUSES.map((status) => (
                <option key={status} value={status}>
                  {status}
                </option>
              ))}
            </select>
          </label>
          <label>
            Source
            <input
              name="sourceName"
              value={form.sourceName}
              onChange={(changeEvent) => updateField('sourceName', changeEvent.target.value)}
            />
          </label>
          <label>
            Origin IP
            <input
              name="originIp"
              value={form.originIp}
              onChange={(changeEvent) => updateField('originIp', changeEvent.target.value)}
            />
          </label>
          {!isEditing && (
            <label>
              Occurred
              <input
                name="eventTimestamp"
                type="datetime-local"
                value={form.eventTimestamp}
                onChange={(changeEvent) => updateField('eventTimestamp', changeEvent.target.value)}
              />
            </label>
          )}
          <label className="event-form-wide">
            Description
            <textarea
              name="description"
              rows={2}
              value={form.description}
              onChange={(changeEvent) => updateField('description', changeEvent.target.value)}
            />
          </label>
          <div className="event-form-wide event-form-actions">
            <button type="submit" disabled={submitting}>
              {submitting ? 'Saving...' : isEditing ? 'Save changes' : 'Create event'}
            </button>
            {isEditing && (
              <button type="button" onClick={cancelEdit} disabled={submitting}>
                Cancel
              </button>
            )}
          </div>
        </form>
      </section>

      {error && <p className="form-error">{error}</p>}

      {loading && <p>Loading events...</p>}

      {!loading && !error && events.length === 0 && <p>No events yet.</p>}

      {events.length > 0 && (
        <table className="event-table">
          <thead>
            <tr>
              <th>Title</th>
              <th>Severity</th>
              <th>Status</th>
              <th>Source</th>
              <th>Occurred</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {events.map((event) => (
              <tr key={event.id}>
                <td>{event.title}</td>
                <td>{event.severity}</td>
                <td>{event.incidentStatus}</td>
                <td>{event.sourceName ?? ''}</td>
                <td>{formatTime(event.eventTimestamp ?? event.timeCreated)}</td>
                <td>
                  <button type="button" onClick={() => startEdit(event)}>
                    Edit
                  </button>
                  <button type="button" onClick={() => handleDelete(event)}>
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </main>
  )
}

export default DashboardPage
