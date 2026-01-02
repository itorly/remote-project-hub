import { Link, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';

export const AppLayout = () => {
  const { user, logout, token, persistence, setPersistence } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const isAuthRoute = location.pathname === '/login' || location.pathname === '/register';

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="app-shell">
      <header className="app-header">
        <Link to="/" className="brand">
          Remote Project Hub · Board
        </Link>
        <div className="header-actions">
          {token ? (
            <>
              <div className="inline helper-text">
                <span>Session:</span>
                <select
                  className="select"
                  value={persistence}
                  onChange={(e) => setPersistence(e.target.value as typeof persistence)}
                >
                  <option value="memory">Memory (safer)</option>
                  <option value="local">Local storage (stay signed in)</option>
                </select>
              </div>
              <span className="helper-text">{user?.displayName}</span>
              <button className="btn btn-secondary" onClick={handleLogout}>
                Logout
              </button>
            </>
          ) : (
            !isAuthRoute && (
              <>
                <Link className="btn btn-secondary" to="/login">
                  Login
                </Link>
                <Link className="btn btn-primary" to="/register">
                  Register
                </Link>
              </>
            )
          )}
        </div>
      </header>
      <main className="main-content">
        <Outlet />
      </main>
    </div>
  );
};
