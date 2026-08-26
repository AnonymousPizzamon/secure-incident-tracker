// render-or-redirect gate per https://reactrouter.com/start/declarative/routing
import type { ReactNode } from 'react'
import { Navigate } from 'react-router-dom'
import { getToken } from '../api/client'

function ProtectedRoute({ children }: { children: ReactNode }) {
  const token = getToken()

  if (!token) {
    return <Navigate to="/login" replace />
  }

  return children
}

export default ProtectedRoute
