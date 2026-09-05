// owner scoped event list, rendered behind ProtectedRoute
// effect fetch per https://react.dev/reference/react/useEffect#fetching-data-with-effects
import { useEffect, useState } from 'react'
import type { EventResponse } from '../api/client'
import { listEvents } from '../api/client'

// backend sends LocalDateTime with no zone, so this reads as local time
function formatTime(value: string): string {
  return new Date(value).toLocaleString()
}

function DashboardPage() {
  const [events, setEvents] = useState<EventResponse[]>([])
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  // empty dep array, so this runs once after the first render
  useEffect(() => {
    listEvents()
      .then((page) => setEvents(page.content))
      .catch((err) => setError(err instanceof Error ? err.message : 'Could not load events'))
      .finally(() => setLoading(false))
  }, [])

  return (
    <main className="dashboard">
      <h1>Events</h1>

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
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </main>
  )
}

export default DashboardPage
