// submit then navigate per https://reactrouter.com/start/declarative/navigating
import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { login, register, saveToken } from '../api/client'

type AuthMode = 'login' | 'register'

function AuthPage({ mode }: { mode: AuthMode }) {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [email, setEmail] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const [registered, setRegistered] = useState(false)
  const navigate = useNavigate()

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)
    setSubmitting(true)

    try {
      if (mode === 'register') {
        await register({ username, email, password })
        setRegistered(true)
      } else {
        const result = await login({ username, password })
        saveToken(result.token)
        navigate('/dashboard')
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Something went wrong')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main className="auth-page">
      <h1>{mode === 'login' ? 'Log in' : 'Register'}</h1>

      {registered ? (
        <>
          <p>Account created.</p>
          <p>
            <Link to="/login">Log in</Link>
          </p>
        </>
      ) : (
        <>
          {error && <p className="form-error">{error}</p>}
          <form className="auth-form" onSubmit={handleSubmit}>
            <label>
              Username
              <input
                name="username"
                value={username}
                onChange={(event) => setUsername(event.target.value)}
              />
            </label>
            <label>
              Password
              <input
                name="password"
                type="password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
              />
            </label>
            {mode === 'register' && (
              <label>
                Email
                <input
                  name="email"
                  type="email"
                  value={email}
                  onChange={(event) => setEmail(event.target.value)}
                />
              </label>
            )}
            <button type="submit" disabled={submitting}>
              {mode === 'login' ? 'Log in' : 'Create account'}
            </button>
          </form>
          <p>
            {mode === 'login' ? (
              <>
                No account? <Link to="/register">Register</Link>
              </>
            ) : (
              <>
                Have an account? <Link to="/login">Log in</Link>
              </>
            )}
          </p>
        </>
      )}
    </main>
  )
}

export default AuthPage
